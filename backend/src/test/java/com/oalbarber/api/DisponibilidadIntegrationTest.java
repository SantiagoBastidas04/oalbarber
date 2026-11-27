package com.oalbarber.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = {
        "JWT_SECRETO=clave-falsa-solo-pruebas-no-usar-produccion-123456",
        "app.admin.celular=+573000000099",
        "app.admin.nombre=Administrador ficticio",
        "app.admin.contrasena=ClaveAdminFicticia123"
})
@AutoConfigureMockMvc
@Transactional
class DisponibilidadIntegrationTest {

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");
    private static final UUID USUARIO_BARBERO_UNO = uuid("11111111-1111-4111-8111-111111111101");
    private static final UUID USUARIO_BARBERO_DOS = uuid("22222222-2222-4222-8222-222222222102");
    private static final UUID USUARIO_BARBERO_TRES = uuid("33333333-3333-4333-8333-333333333103");
    private static final UUID USUARIO_BARBERO_CUATRO = uuid("44444444-4444-4444-8444-444444444104");
    private static final UUID CLIENTE_ID = uuid("55555555-5555-4555-8555-555555555105");
    private static final UUID BARBERO_UNO = uuid("aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaa101");
    private static final UUID BARBERO_DOS = uuid("bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbb02");
    private static final UUID BARBERO_NO_OFRECE = uuid("cccccccc-cccc-4ccc-8ccc-ccccccccc103");
    private static final UUID BARBERO_INACTIVO = uuid("dddddddd-dddd-4ddd-8ddd-ddddddddd104");
    private static final UUID SERVICIO_ID = uuid("eeeeeeee-eeee-4eee-8eee-eeeeeeeeee01");
    private static final UUID SERVICIO_INACTIVO_ID = uuid("ffffffff-ffff-4fff-8fff-fffffffff102");

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private Clock clock;

    private LocalDate fecha;

    @BeforeEach
    void prepararDatos() {
        fecha = proximaFechaLaboral();
        insertarUsuario(USUARIO_BARBERO_UNO, "+573000000101", "BARBERO");
        insertarUsuario(USUARIO_BARBERO_DOS, "+573000000102", "BARBERO");
        insertarUsuario(USUARIO_BARBERO_TRES, "+573000000103", "BARBERO");
        insertarUsuario(USUARIO_BARBERO_CUATRO, "+573000000104", "BARBERO");
        insertarUsuario(CLIENTE_ID, "+573000000105", "CLIENTE");
        insertarBarbero(BARBERO_UNO, USUARIO_BARBERO_UNO, true);
        insertarBarbero(BARBERO_DOS, USUARIO_BARBERO_DOS, true);
        insertarBarbero(BARBERO_NO_OFRECE, USUARIO_BARBERO_TRES, true);
        insertarBarbero(BARBERO_INACTIVO, USUARIO_BARBERO_CUATRO, false);
        insertarServicio(SERVICIO_ID, "Servicio de disponibilidad", true);
        insertarServicio(SERVICIO_INACTIVO_ID, "Servicio inactivo de disponibilidad", false);
        insertarAsignacion(BARBERO_UNO, SERVICIO_ID);
        insertarAsignacion(BARBERO_DOS, SERVICIO_ID);
        insertarAsignacion(BARBERO_INACTIVO, SERVICIO_ID);
        insertarHorario(BARBERO_UNO, fecha);
        insertarHorario(BARBERO_DOS, fecha);
    }

    @Test
    @DisplayName("consulta pública combina dos barberos y asigna el inicio repetido al menos ocupado")
    void cualquieraUsaDosBarberosYDeduplicaInicio() throws Exception {
        insertarCita(BARBERO_UNO, fecha.atTime(10, 0), 40, 1);

        MvcResult respuesta = mockMvc.perform(get("/api/disponibilidad")
                        .param("servicioId", SERVICIO_ID.toString())
                        .param("fecha", fecha.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].inicio").value(fecha + "T10:00:00-05:00"))
                .andExpect(jsonPath("$[0].barberoId").value(BARBERO_DOS.toString()))
                .andReturn();

        List<String> inicios = JsonPath.read(respuesta.getResponse().getContentAsString(), "$[*].inicio");
        assertThat(inicios).doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("barbero que no ofrece el servicio o está inactivo recibe 409")
    void barberoNoElegibleRespondeConflicto() throws Exception {
        mockMvc.perform(get("/api/disponibilidad")
                        .param("servicioId", SERVICIO_ID.toString())
                        .param("fecha", fecha.toString())
                        .param("barberoId", BARBERO_NO_OFRECE.toString()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(
                        "El barbero indicado no existe, está inactivo o no ofrece el servicio seleccionado."));

        mockMvc.perform(get("/api/disponibilidad")
                        .param("servicioId", SERVICIO_ID.toString())
                        .param("fecha", fecha.toString())
                        .param("barberoId", BARBERO_INACTIVO.toString()))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("servicio inexistente o inactivo devuelve 404 y fechas inválidas 400")
    void manejaServicioYFechaInvalidos() throws Exception {
        mockMvc.perform(get("/api/disponibilidad")
                        .param("servicioId", SERVICIO_INACTIVO_ID.toString())
                        .param("fecha", fecha.toString()))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/disponibilidad")
                        .param("servicioId", uuid("99999999-9999-4999-8999-999999999901").toString())
                        .param("fecha", fecha.toString()))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/disponibilidad")
                        .param("servicioId", SERVICIO_ID.toString())
                        .param("fecha", "fecha-invalida"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.fecha").exists());
    }

    @Test
    @DisplayName("veinte citas y dos barberos se consultan en menos de un segundo")
    void rendimientoDeHumoConVeinteCitas() throws Exception {
        for (int indice = 0; indice < 20; indice++) {
            UUID barberoId = indice % 2 == 0 ? BARBERO_UNO : BARBERO_DOS;
            insertarCita(barberoId, fecha.atTime(10, 0).plusMinutes((indice / 2) * 40L), 40, indice);
        }

        consultarPublico(); // Calienta Spring MVC, Hibernate y el pool antes de medir.
        long inicio = System.nanoTime();
        consultarPublico();
        long milisegundos = (System.nanoTime() - inicio) / 1_000_000;

        assertThat(milisegundos).isLessThan(1_000);
    }

    private void consultarPublico() throws Exception {
        mockMvc.perform(get("/api/disponibilidad")
                        .param("servicioId", SERVICIO_ID.toString())
                        .param("fecha", fecha.toString()))
                .andExpect(status().isOk());
    }

    private LocalDate proximaFechaLaboral() {
        LocalDate candidata = LocalDate.now(clock).plusDays(2);
        while (candidata.getDayOfWeek().getValue() == 7) {
            candidata = candidata.plusDays(1);
        }
        return candidata;
    }

    private void insertarUsuario(UUID id, String celular, String rol) {
        jdbcTemplate.update("""
                INSERT INTO usuario (id, nombre, celular, contrasena_hash, rol)
                VALUES (?, 'Usuario de integración', ?, 'hash-ficticio', ?)
                """, id, celular, rol);
    }

    private void insertarBarbero(UUID id, UUID usuarioId, boolean activo) {
        jdbcTemplate.update("INSERT INTO barbero (id, usuario_id, activo) VALUES (?, ?, ?)",
                id, usuarioId, activo);
    }

    private void insertarServicio(UUID id, String nombre, boolean activo) {
        jdbcTemplate.update("""
                INSERT INTO servicio (id, nombre, descripcion, duracion_min, precio, activo)
                VALUES (?, ?, 'Servicio de prueba', 40, 25000, ?)
                """, id, nombre, activo);
    }

    private void insertarAsignacion(UUID barberoId, UUID servicioId) {
        jdbcTemplate.update("INSERT INTO barbero_servicio (barbero_id, servicio_id) VALUES (?, ?)",
                barberoId, servicioId);
    }

    private void insertarHorario(UUID barberoId, LocalDate dia) {
        jdbcTemplate.update("""
                INSERT INTO horario_atencion (id, barbero_id, dia_semana, hora_inicio, hora_fin)
                VALUES (?, ?, ?, '10:00', '19:00')
                """, UUID.randomUUID(), barberoId, dia.getDayOfWeek().getValue());
    }

    private void insertarCita(UUID barberoId, java.time.LocalDateTime inicioLocal,
                              int duracionMin, int indice) {
        OffsetDateTime inicio = inicioLocal.atZone(ZONA).toOffsetDateTime();
        OffsetDateTime fin = inicio.plusMinutes(duracionMin);
        UUID id = UUID.nameUUIDFromBytes(("disponibilidad-cita-" + indice + "-" + barberoId)
                .getBytes(StandardCharsets.UTF_8));
        jdbcTemplate.update("""
                INSERT INTO cita (id, cliente_id, barbero_id, servicio_id, inicio, fin,
                                  duracion_min, precio, estado, canal)
                VALUES (?, ?, ?, ?, ?, ?, ?, 25000, 'CONFIRMADA', 'WEB')
                """, id, CLIENTE_ID, barberoId, SERVICIO_ID, inicio, fin, duracionMin);
    }

    private static UUID uuid(String valor) {
        return UUID.fromString(valor);
    }
}
