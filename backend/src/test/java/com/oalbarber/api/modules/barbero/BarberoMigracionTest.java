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

    @Container
    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(
            DockerImageName.parse("postgres:17-alpine"));

    @BeforeAll
    static void aplicarMigracionesEnBaseVacia() {
        Flyway flyway = Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .locations("classpath:db/migration")
                .load();

        assertEquals(4, flyway.migrate().migrationsExecuted,
                "La base vacía debe aplicar V1, V2, V3 y V4");
        assertEquals(List.of("1", "2", "3", "4"),
                Arrays.stream(flyway.info().applied())
                        .map(migracion -> migracion.getVersion().getVersion())
                        .toList(),
                "Flyway debe aplicar la secuencia V1 a V4");
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

        assertEquals('r', tipoDeEliminacionDeLaClaveForanea(),
                "La FK debe declarar ON DELETE RESTRICT");
        assertSqlState(SQL_STATE_FOREIGN_KEY, () -> eliminarUsuario(usuarioId));
        assertTrue(estadoActivoDelBarbero(barberoId),
                "El perfil debe quedar activo por defecto");

        // El segundo usuario permanece sin perfil, confirmando que la restricción
        // no impide tener usuarios que todavía no son barberos.
        assertEquals(0, cantidadDeBarberosPara(segundoUsuarioId));
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

    private static void eliminarUsuario(UUID id) throws SQLException {
        ejecutar("DELETE FROM usuario WHERE id = ?", id);
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

    private static char tipoDeEliminacionDeLaClaveForanea() throws SQLException {
        try (Connection conexion = conexion();
             PreparedStatement consulta = conexion.prepareStatement("""
                     SELECT confdeltype
                     FROM pg_constraint
                     WHERE conname = 'fk_barbero_usuario'
                     """);
             ResultSet resultado = consulta.executeQuery()) {
            if (!resultado.next()) {
                throw new AssertionError("No se encontró la FK fk_barbero_usuario");
            }
            return resultado.getString(1).charAt(0);
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
