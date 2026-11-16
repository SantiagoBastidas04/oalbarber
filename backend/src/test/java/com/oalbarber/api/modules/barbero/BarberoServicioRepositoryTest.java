package com.oalbarber.api.modules.barbero;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import com.oalbarber.api.modules.barbero.domain.BarberoServicio;
import com.oalbarber.api.modules.barbero.repository.BarberoServicioRepository;

@DataJpaTest
@Testcontainers
class BarberoServicioRepositoryTest {

    @Container
    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(
            DockerImageName.parse("postgres:17-alpine"));

    private static final UUID USUARIO_UNO_ID =
            UUID.fromString("11111111-1111-4111-8111-111111111121");
    private static final UUID USUARIO_DOS_ID =
            UUID.fromString("22222222-2222-4222-8222-222222222122");
    private static final UUID BARBERO_UNO_ID =
            UUID.fromString("aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa1");
    private static final UUID BARBERO_DOS_ID =
            UUID.fromString("bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbb2");
    private static final UUID SERVICIO_UNO_ID =
            UUID.fromString("cccccccc-cccc-4ccc-8ccc-ccccccccccc1");
    private static final UUID SERVICIO_DOS_ID =
            UUID.fromString("dddddddd-dddd-4ddd-8ddd-ddddddddddd2");

    @Autowired
    private BarberoServicioRepository barberoServicioRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void configurarPostgres(DynamicPropertyRegistry propiedades) {
        propiedades.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        propiedades.add("spring.datasource.username", POSTGRES::getUsername);
        propiedades.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @BeforeEach
    void crearDatosDePrueba() {
        insertarUsuario(USUARIO_UNO_ID, "+573000000021");
        insertarUsuario(USUARIO_DOS_ID, "+573000000022");
        jdbcTemplate.update("INSERT INTO barbero (id, usuario_id) VALUES (?, ?)",
                BARBERO_UNO_ID, USUARIO_UNO_ID);
        jdbcTemplate.update("INSERT INTO barbero (id, usuario_id) VALUES (?, ?)",
                BARBERO_DOS_ID, USUARIO_DOS_ID);
        insertarServicio(SERVICIO_UNO_ID, "Servicio ficticio uno");
        insertarServicio(SERVICIO_DOS_ID, "Servicio ficticio dos");
    }

    @Test
    void buscaAsignacionesPorBarberoYPorServicio() {
        barberoServicioRepository.saveAllAndFlush(List.of(
                BarberoServicio.crear(BARBERO_UNO_ID, SERVICIO_UNO_ID),
                BarberoServicio.crear(BARBERO_UNO_ID, SERVICIO_DOS_ID),
                BarberoServicio.crear(BARBERO_DOS_ID, SERVICIO_UNO_ID)));

        assertThat(barberoServicioRepository.findById_BarberoId(BARBERO_UNO_ID))
                .extracting(asignacion -> asignacion.getId().getServicioId())
                .containsExactlyInAnyOrder(SERVICIO_UNO_ID, SERVICIO_DOS_ID);
        assertThat(barberoServicioRepository.findById_ServicioId(SERVICIO_UNO_ID))
                .extracting(asignacion -> asignacion.getId().getBarberoId())
                .containsExactlyInAnyOrder(BARBERO_UNO_ID, BARBERO_DOS_ID);
    }

    @Test
    void compruebaYEliminaUnaAsignacionPorClaveCompuesta() {
        barberoServicioRepository.saveAndFlush(
                BarberoServicio.crear(BARBERO_UNO_ID, SERVICIO_UNO_ID));

        assertThat(barberoServicioRepository.existsById_BarberoIdAndId_ServicioId(
                BARBERO_UNO_ID, SERVICIO_UNO_ID)).isTrue();
        assertThat(barberoServicioRepository.existsById_BarberoIdAndId_ServicioId(
                BARBERO_DOS_ID, SERVICIO_DOS_ID)).isFalse();

        assertThat(barberoServicioRepository.deleteById_BarberoIdAndId_ServicioId(
                BARBERO_UNO_ID, SERVICIO_UNO_ID)).isEqualTo(1);
        assertThat(barberoServicioRepository.existsById_BarberoIdAndId_ServicioId(
                BARBERO_UNO_ID, SERVICIO_UNO_ID)).isFalse();
    }

    private void insertarUsuario(UUID id, String celular) {
        jdbcTemplate.update("""
                INSERT INTO usuario (id, nombre, celular, contrasena_hash, rol)
                VALUES (?, ?, ?, ?, 'BARBERO')
                """, id, "Usuario ficticio", celular, "hash-ficticio");
    }

    private void insertarServicio(UUID id, String nombre) {
        jdbcTemplate.update("""
                INSERT INTO servicio (id, nombre, duracion_min, precio)
                VALUES (?, ?, 40, 15000.00)
                """, id, nombre);
    }
}
