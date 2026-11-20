package com.oalbarber.api.modules.barbero;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
class BarberoMigracionTest {

    private static final String SQL_STATE_UNIQUE = "23505";
    private static final String SQL_STATE_FOREIGN_KEY = "23503";
    private static final String SQL_STATE_RESTRICT = "r";

    @Container
    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(
            DockerImageName.parse("postgres:17-alpine"));

    @BeforeAll
    static void aplicarMigracionesEnBaseVacia() {
        Flyway flyway = Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .locations("classpath:db/migration")
                .load();

        assertEquals(6, flyway.migrate().migrationsExecuted,
                "La base vacía debe aplicar V1 a V6");
        assertEquals(List.of("1", "2", "3", "4", "5", "6"),
                Arrays.stream(flyway.info().applied())
                        .map(migracion -> migracion.getVersion().getVersion())
                        .toList(),
                "Flyway debe aplicar la secuencia V1 a V6");
    }

    @Test
    void exigeUsuarioUnicoExistenteYRestringeSuEliminacion() throws Exception {
        UUID usuarioId = UUID.fromString("11111111-1111-4111-8111-111111111111");
        UUID segundoUsuarioId = UUID.fromString("22222222-2222-4222-8222-222222222222");
        UUID barberoId = UUID.fromString("aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa");

        insertarUsuario(usuarioId, "+573000000001");
        insertarUsuario(segundoUsuarioId, "+573000000002");
        insertarBarbero(barberoId, usuarioId);

        assertSqlState(SQL_STATE_UNIQUE, () -> insertarBarbero(barberoId, segundoUsuarioId));
        assertSqlState(SQL_STATE_UNIQUE, () -> insertarBarbero(
                UUID.fromString("bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb"), usuarioId));

        assertSqlState(SQL_STATE_FOREIGN_KEY, () -> insertarBarbero(
                UUID.fromString("cccccccc-cccc-4ccc-8ccc-cccccccccccc"),
                UUID.fromString("33333333-3333-4333-8333-333333333333")));

        assertEquals(SQL_STATE_RESTRICT, tipoDeEliminacionDeClaveForanea("fk_barbero_usuario"),
                "La FK debe declarar ON DELETE RESTRICT");
        assertSqlState(SQL_STATE_FOREIGN_KEY, () -> eliminarUsuario(usuarioId));
        assertTrue(estadoActivoDelBarbero(barberoId),
                "El perfil debe quedar activo por defecto");

        // El segundo usuario permanece sin perfil, confirmando que la restricción
        // no impide tener usuarios que todavía no son barberos.
        assertEquals(0, cantidadDeBarberosPara(segundoUsuarioId));
    }

    @Test
    void agregaFechaDeCreacionAlPerfilDeBarbero() throws Exception {
        try (Connection conexion = conexion();
             PreparedStatement consulta = conexion.prepareStatement("""
                     SELECT is_nullable, column_default
                     FROM information_schema.columns
                     WHERE table_schema = 'public' AND table_name = 'barbero'
                       AND column_name = 'fecha_creacion'
                     """)) {
            try (ResultSet resultado = consulta.executeQuery()) {
                assertTrue(resultado.next(), "V6 debe agregar barbero.fecha_creacion");
                assertEquals("NO", resultado.getString("is_nullable"));
                assertTrue(resultado.getString("column_default").contains("now()"));
            }
        }
    }

    @Test
    void relacionBarberoServicioExigeParUnicoYRestringeAmbasEliminaciones() throws Exception {
        UUID usuarioId = UUID.fromString("44444444-4444-4444-8444-444444444444");
        UUID barberoId = UUID.fromString("dddddddd-dddd-4ddd-8ddd-dddddddddddd");
        UUID servicioId = UUID.fromString("eeeeeeee-eeee-4eee-8eee-eeeeeeeeeeee");
        UUID servicioInexistenteId = UUID.fromString("ffffffff-ffff-4fff-8fff-ffffffffffff");
        UUID barberoInexistenteId = UUID.fromString("99999999-9999-4999-8999-999999999999");

        insertarUsuario(usuarioId, "+573000000004");
        insertarBarbero(barberoId, usuarioId);
        insertarServicio(servicioId, "Servicio de prueba migracion");
        insertarBarberoServicio(barberoId, servicioId);

        assertTrue(tieneFechaCreacion(barberoId, servicioId),
                "creado_en debe completarse con el valor por defecto");
        assertSqlState(SQL_STATE_UNIQUE, () -> insertarBarberoServicio(barberoId, servicioId));
        assertSqlState(SQL_STATE_FOREIGN_KEY,
                () -> insertarBarberoServicio(barberoInexistenteId, servicioId));
        assertSqlState(SQL_STATE_FOREIGN_KEY,
                () -> insertarBarberoServicio(barberoId, servicioInexistenteId));

        assertEquals(SQL_STATE_RESTRICT, tipoDeEliminacionDeClaveForanea(
                "fk_barbero_servicio_barbero"));
        assertEquals(SQL_STATE_RESTRICT, tipoDeEliminacionDeClaveForanea(
                "fk_barbero_servicio_servicio"));
        assertSqlState(SQL_STATE_FOREIGN_KEY, () -> eliminarBarbero(barberoId));
        assertSqlState(SQL_STATE_FOREIGN_KEY, () -> eliminarServicio(servicioId));
    }

    private static void insertarUsuario(UUID id, String celular) throws SQLException {
        ejecutar("""
                INSERT INTO usuario (id, nombre, celular, contrasena_hash, rol)
                VALUES (?, ?, ?, ?, 'BARBERO')
                """, id, "Usuario de prueba", celular, "hash-ficticio");
    }

    private static void insertarBarbero(UUID id, UUID usuarioId) throws SQLException {
        ejecutar("INSERT INTO barbero (id, usuario_id) VALUES (?, ?)", id, usuarioId);
    }

    private static void insertarServicio(UUID id, String nombre) throws SQLException {
        ejecutar("""
                INSERT INTO servicio (id, nombre, duracion_min, precio)
                VALUES (?, ?, 40, 15000.00)
                """, id, nombre);
    }

    private static void insertarBarberoServicio(UUID barberoId, UUID servicioId) throws SQLException {
        ejecutar("INSERT INTO barbero_servicio (barbero_id, servicio_id) VALUES (?, ?)",
                barberoId, servicioId);
    }

    private static void eliminarUsuario(UUID id) throws SQLException {
        ejecutar("DELETE FROM usuario WHERE id = ?", id);
    }

    private static void eliminarBarbero(UUID id) throws SQLException {
        ejecutar("DELETE FROM barbero WHERE id = ?", id);
    }

    private static void eliminarServicio(UUID id) throws SQLException {
        ejecutar("DELETE FROM servicio WHERE id = ?", id);
    }

    private static int cantidadDeBarberosPara(UUID usuarioId) throws SQLException {
        try (Connection conexion = conexion();
             PreparedStatement consulta = conexion.prepareStatement(
                     "SELECT count(*) FROM barbero WHERE usuario_id = ?")) {
            consulta.setObject(1, usuarioId);
            try (ResultSet resultado = consulta.executeQuery()) {
                resultado.next();
                return resultado.getInt(1);
            }
        }
    }

    private static boolean estadoActivoDelBarbero(UUID id) throws SQLException {
        try (Connection conexion = conexion();
             PreparedStatement consulta = conexion.prepareStatement(
                     "SELECT activo FROM barbero WHERE id = ?")) {
            consulta.setObject(1, id);
            try (ResultSet resultado = consulta.executeQuery()) {
                if (!resultado.next()) {
                    throw new AssertionError("No se encontró el perfil de barbero esperado");
                }
                return resultado.getBoolean(1);
            }
        }
    }

    private static boolean tieneFechaCreacion(UUID barberoId, UUID servicioId) throws SQLException {
        try (Connection conexion = conexion();
             PreparedStatement consulta = conexion.prepareStatement("""
                     SELECT creado_en IS NOT NULL
                     FROM barbero_servicio
                     WHERE barbero_id = ? AND servicio_id = ?
                     """)) {
            consulta.setObject(1, barberoId);
            consulta.setObject(2, servicioId);
            try (ResultSet resultado = consulta.executeQuery()) {
                if (!resultado.next()) {
                    throw new AssertionError("No se encontró la asignación barbero-servicio esperada");
                }
                return resultado.getBoolean(1);
            }
        }
    }

    private static String tipoDeEliminacionDeClaveForanea(String nombre) throws SQLException {
        try (Connection conexion = conexion();
             PreparedStatement consulta = conexion.prepareStatement("""
                     SELECT confdeltype
                     FROM pg_constraint
                     WHERE conname = ?
                     """)) {
            consulta.setString(1, nombre);
            try (ResultSet resultado = consulta.executeQuery()) {
                if (!resultado.next()) {
                    throw new AssertionError("No se encontró la FK " + nombre);
                }
                return resultado.getString(1);
            }
        }
    }

    private static void assertSqlState(String estadoEsperado, SqlAction accion) {
        SQLException excepcion = assertThrows(SQLException.class, accion::run);
        assertEquals(estadoEsperado, excepcion.getSQLState());
    }

    private static void ejecutar(String sql, Object... parametros) throws SQLException {
        try (Connection conexion = conexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            for (int indice = 0; indice < parametros.length; indice++) {
                sentencia.setObject(indice + 1, parametros[indice]);
            }
            sentencia.executeUpdate();
        }
    }

    private static Connection conexion() throws SQLException {
        return DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
    }

    @FunctionalInterface
    private interface SqlAction {
        void run() throws SQLException;
    }
}
