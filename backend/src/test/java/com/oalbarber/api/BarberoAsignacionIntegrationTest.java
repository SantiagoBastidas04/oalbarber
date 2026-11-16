package com.oalbarber.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import com.oalbarber.api.common.exception.RecursoNoEncontradoException;
import com.oalbarber.api.common.exception.ReglaNegocioException;
import com.oalbarber.api.modules.barbero.domain.Barbero;
import com.oalbarber.api.modules.barbero.service.BarberoService;
import com.oalbarber.api.modules.servicio.dto.ServicioResponse;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = {
        "JWT_SECRETO=clave-falsa-solo-pruebas-no-usar-produccion-123456",
        "app.admin.celular=+573000000099",
        "app.admin.nombre=Administrador ficticio",
        "app.admin.contrasena=ClaveAdminFicticia123"
})
@Transactional
class BarberoAsignacionIntegrationTest {

    private static final UUID USUARIO_UNO_ID =
            UUID.fromString("11111111-1111-4111-8111-111111111131");
    private static final UUID USUARIO_DOS_ID =
            UUID.fromString("22222222-2222-4222-8222-222222222132");
    private static final UUID BARBERO_UNO_ID =
            UUID.fromString("aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa3");
    private static final UUID BARBERO_DOS_ID =
            UUID.fromString("bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbb4");
    private static final UUID BARBERO_INEXISTENTE_ID =
            UUID.fromString("99999999-9999-4999-8999-999999999991");
    private static final UUID SERVICIO_UNO_ID =
            UUID.fromString("cccccccc-cccc-4ccc-8ccc-cccccccccc31");
    private static final UUID SERVICIO_DOS_ID =
            UUID.fromString("dddddddd-dddd-4ddd-8ddd-dddddddddd32");
    private static final UUID SERVICIO_TRES_ID =
            UUID.fromString("eeeeeeee-eeee-4eee-8eee-eeeeeeeeee33");
    private static final UUID SERVICIO_INACTIVO_ID =
            UUID.fromString("ffffffff-ffff-4fff-8fff-fffffffffff4");
    private static final UUID SERVICIO_INEXISTENTE_ID =
            UUID.fromString("99999999-9999-4999-8999-999999999995");

    @Autowired
    private BarberoService barberoService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PersistenceContext
    private EntityManager entityManager;

    @BeforeEach
    void prepararDatosFicticios() {
        insertarUsuario(USUARIO_UNO_ID, "+573000000031");
        insertarUsuario(USUARIO_DOS_ID, "+573000000032");
        jdbcTemplate.update("INSERT INTO barbero (id, usuario_id) VALUES (?, ?)",
                BARBERO_UNO_ID, USUARIO_UNO_ID);
        jdbcTemplate.update("INSERT INTO barbero (id, usuario_id) VALUES (?, ?)",
                BARBERO_DOS_ID, USUARIO_DOS_ID);
        insertarServicio(SERVICIO_UNO_ID, "Corte ficticio uno", true);
        insertarServicio(SERVICIO_DOS_ID, "Corte ficticio dos", true);
        insertarServicio(SERVICIO_TRES_ID, "Corte ficticio tres", true);
        insertarServicio(SERVICIO_INACTIVO_ID, "Corte ficticio inactivo", false);
    }

    @Test
    void reemplazaServiciosAgregandoYQuitandoEnLaMismaOperacion() {
        insertarAsignacion(BARBERO_UNO_ID, SERVICIO_UNO_ID);
        insertarAsignacion(BARBERO_UNO_ID, SERVICIO_DOS_ID);

        List<ServicioResponse> resultado = barberoService.reemplazarServicios(
                BARBERO_UNO_ID, List.of(SERVICIO_DOS_ID, SERVICIO_TRES_ID));

        entityManager.flush();
        assertThat(ids(resultado)).containsExactlyInAnyOrder(SERVICIO_DOS_ID, SERVICIO_TRES_ID);
        assertEquals(0, cantidadAsignaciones(BARBERO_UNO_ID, SERVICIO_UNO_ID));
        assertEquals(1, cantidadAsignaciones(BARBERO_UNO_ID, SERVICIO_DOS_ID));
        assertEquals(1, cantidadAsignaciones(BARBERO_UNO_ID, SERVICIO_TRES_ID));
        assertThat(ids(barberoService.listarServiciosDe(BARBERO_UNO_ID)))
                .containsExactlyInAnyOrder(SERVICIO_DOS_ID, SERVICIO_TRES_ID);
    }

    @Test
    void rechazaIdsDuplicados() {
        ReglaNegocioException excepcion = assertThrows(ReglaNegocioException.class,
                () -> barberoService.reemplazarServicios(
                        BARBERO_UNO_ID, List.of(SERVICIO_UNO_ID, SERVICIO_UNO_ID)));

        assertEquals("SERVICIO_NO_VALIDO", excepcion.getCodigo());
    }

    @Test
    void rechazaUnServicioInexistente() {
        ReglaNegocioException excepcion = assertThrows(ReglaNegocioException.class,
                () -> barberoService.reemplazarServicios(
                        BARBERO_UNO_ID, List.of(SERVICIO_INEXISTENTE_ID)));

        assertEquals("SERVICIO_NO_VALIDO", excepcion.getCodigo());
    }

    @Test
    void rechazaUnServicioInactivo() {
        ReglaNegocioException excepcion = assertThrows(ReglaNegocioException.class,
                () -> barberoService.reemplazarServicios(
                        BARBERO_UNO_ID, List.of(SERVICIO_INACTIVO_ID)));

        assertEquals("SERVICIO_NO_VALIDO", excepcion.getCodigo());
    }

    @Test
    void devuelveNoEncontradoSiElBarberoNoExiste() {
        assertThrows(RecursoNoEncontradoException.class,
                () -> barberoService.reemplazarServicios(BARBERO_INEXISTENTE_ID, List.of()));
    }

    @Test
    void permiteDesasignarTodosLosServicios() {
        insertarAsignacion(BARBERO_UNO_ID, SERVICIO_UNO_ID);

        assertThat(barberoService.reemplazarServicios(BARBERO_UNO_ID, List.of())).isEmpty();
        entityManager.flush();
        assertEquals(0, cantidadAsignaciones(BARBERO_UNO_ID, SERVICIO_UNO_ID));
    }

    @Test
    void soloListaBarberosActivosQueOfrecenElServicioActivo() {
        insertarAsignacion(BARBERO_UNO_ID, SERVICIO_UNO_ID);
        insertarAsignacion(BARBERO_DOS_ID, SERVICIO_UNO_ID);
        jdbcTemplate.update("UPDATE barbero SET activo = false WHERE id = ?", BARBERO_DOS_ID);

        assertThat(barberoService.listarActivosQueOfrecenServicio(SERVICIO_UNO_ID))
                .extracting(Barbero::getId)
                .containsExactly(BARBERO_UNO_ID);
        assertThat(barberoService.listarActivosQueOfrecenServicio(SERVICIO_DOS_ID)).isEmpty();
        assertThat(barberoService.listarActivosQueOfrecenServicio(SERVICIO_INACTIVO_ID)).isEmpty();
        assertThat(barberoService.ofreceServicio(BARBERO_UNO_ID, SERVICIO_UNO_ID)).isTrue();
        assertThat(barberoService.ofreceServicio(BARBERO_UNO_ID, SERVICIO_INACTIVO_ID)).isFalse();
    }

    private void insertarUsuario(UUID id, String celular) {
        jdbcTemplate.update("""
                INSERT INTO usuario (id, nombre, celular, contrasena_hash, rol)
                VALUES (?, ?, ?, ?, 'BARBERO')
                """, id, "Usuario ficticio", celular, "hash-ficticio");
    }

    private void insertarServicio(UUID id, String nombre, boolean activo) {
        jdbcTemplate.update("""
                INSERT INTO servicio (id, nombre, duracion_min, precio, activo)
                VALUES (?, ?, 40, 15000.00, ?)
                """, id, nombre, activo);
    }

    private void insertarAsignacion(UUID barberoId, UUID servicioId) {
        jdbcTemplate.update("""
                INSERT INTO barbero_servicio (barbero_id, servicio_id)
                VALUES (?, ?)
                """, barberoId, servicioId);
    }

    private int cantidadAsignaciones(UUID barberoId, UUID servicioId) {
        return jdbcTemplate.queryForObject("""
                SELECT count(*) FROM barbero_servicio
                WHERE barbero_id = ? AND servicio_id = ?
                """, Integer.class, barberoId, servicioId);
    }

    private static List<UUID> ids(List<ServicioResponse> servicios) {
        return servicios.stream().map(ServicioResponse::getId).toList();
    }
}
