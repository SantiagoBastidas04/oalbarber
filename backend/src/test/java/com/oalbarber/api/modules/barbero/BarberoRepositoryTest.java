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

import com.oalbarber.api.modules.barbero.domain.Barbero;
import com.oalbarber.api.modules.barbero.repository.BarberoRepository;

@DataJpaTest
@Testcontainers
class BarberoRepositoryTest {

    @Container
    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(
            DockerImageName.parse("postgres:17-alpine"));

    private static final UUID USUARIO_ACTIVO_ID =
            UUID.fromString("11111111-1111-4111-8111-111111111111");
    private static final UUID USUARIO_INACTIVO_ID =
            UUID.fromString("22222222-2222-4222-8222-222222222222");
    private static final UUID USUARIO_SIN_PERFIL_ID =
            UUID.fromString("33333333-3333-4333-8333-333333333333");

    @Autowired
    private BarberoRepository barberoRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void configurarPostgres(DynamicPropertyRegistry propiedades) {
        propiedades.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        propiedades.add("spring.datasource.username", POSTGRES::getUsername);
        propiedades.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @BeforeEach
    void crearUsuariosDePrueba() {
        insertarUsuario(USUARIO_ACTIVO_ID, "+573000000001");
        insertarUsuario(USUARIO_INACTIVO_ID, "+573000000002");
        insertarUsuario(USUARIO_SIN_PERFIL_ID, "+573000000003");
    }

    @Test
    void encuentraSoloLosPerfilesActivos() {
        barberoRepository.saveAndFlush(Barbero.crear(USUARIO_ACTIVO_ID));
        Barbero inactivo = Barbero.crear(USUARIO_INACTIVO_ID);
        inactivo.cambiarEstado(false);
        barberoRepository.saveAndFlush(inactivo);

        List<Barbero> activos = barberoRepository.findByActivoTrue();

        assertThat(activos)
                .hasSize(1)
                .extracting(Barbero::getUsuarioId)
                .containsExactly(USUARIO_ACTIVO_ID);
    }

    @Test
    void buscaUnPerfilPorUsuario() {
        Barbero guardado = barberoRepository.saveAndFlush(
                Barbero.crear(USUARIO_ACTIVO_ID, "Barbero de prueba", null, null));

        assertThat(barberoRepository.findByUsuarioId(USUARIO_ACTIVO_ID))
                .contains(guardado);
        assertThat(barberoRepository.findByUsuarioId(USUARIO_SIN_PERFIL_ID))
                .isEmpty();
    }

    @Test
    void compruebaSiUnUsuarioYaTienePerfil() {
        barberoRepository.saveAndFlush(Barbero.crear(USUARIO_ACTIVO_ID));

        assertThat(barberoRepository.existsByUsuarioId(USUARIO_ACTIVO_ID)).isTrue();
        assertThat(barberoRepository.existsByUsuarioId(USUARIO_SIN_PERFIL_ID)).isFalse();
    }

    private void insertarUsuario(UUID id, String celular) {
        jdbcTemplate.update("""
                INSERT INTO usuario (id, nombre, celular, contrasena_hash, rol)
                VALUES (?, ?, ?, ?, 'BARBERO')
                """, id, "Usuario de prueba", celular, "hash-ficticio");
    }
}
