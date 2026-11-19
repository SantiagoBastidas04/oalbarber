package com.oalbarber.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.oalbarber.api.config.AdminInicializador;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = {
        "JWT_SECRETO=clave-falsa-solo-pruebas-no-usar-produccion-123456",
        "app.admin.celular=+573000000099",
        "app.admin.nombre=Administrador ficticio",
        "app.admin.contrasena=ClaveAdminFicticia123"
})
@AutoConfigureMockMvc
class BarberoServiceIntegrationTest {

    private static final String CELULAR_ADMIN = "+573000000099";

    @Autowired
    private AdminInicializador adminInicializador;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void adminNoSeSiembraYSeAgregaExplicitamenteSinCambiarSuCuenta() throws Exception {
        UUID adminId = usuarioAdminId();
        var usuarioAntes = usuarioAdmin();

        assertEquals(0, cantidad("SELECT count(*) FROM barbero WHERE usuario_id = ?", adminId));
        mockMvc.perform(get("/api/barberos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        adminInicializador.run(new DefaultApplicationArguments(new String[0]));
        assertEquals(1, cantidad("SELECT count(*) FROM usuario WHERE celular = ?", CELULAR_ADMIN));
        assertEquals(0, cantidad("SELECT count(*) FROM barbero WHERE usuario_id = ?", adminId));

        mockMvc.perform(post("/api/barberos/admin")
                        .with(jwtDe("ADMINISTRADOR", adminId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombrePublico\":\"Admin visible\",\"foto\":\"admin.webp\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuarioId").value(adminId.toString()))
                .andExpect(jsonPath("$.nombre").value("Administrador ficticio"))
                .andExpect(jsonPath("$.activo").value(true));

        assertEquals(usuarioAntes, usuarioAdmin(), "El POST no debe modificar la cuenta usuario");
        assertEquals(1, cantidad("SELECT count(*) FROM barbero WHERE usuario_id = ?", adminId));
        mockMvc.perform(get("/api/barberos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombrePublico").value("Admin visible"));

        mockMvc.perform(post("/api/barberos/admin")
                        .with(jwtDe("ADMINISTRADOR", adminId))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("BARBERO_DUPLICADO"));
    }

    @Test
    void registroExplicitoRequiereSesionYRolAdministrador() throws Exception {
        UUID idFicticio = UUID.fromString("22222222-2222-4222-8222-222222222222");
        mockMvc.perform(post("/api/barberos/admin").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/barberos/admin")
                        .with(jwtDe("CLIENTE", idFicticio))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/barberos/admin")
                        .with(jwtDe("BARBERO", idFicticio))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    private UUID usuarioAdminId() {
        return jdbcTemplate.queryForObject(
                "SELECT id FROM usuario WHERE celular = ?", UUID.class, CELULAR_ADMIN);
    }

    private Object usuarioAdmin() {
        return jdbcTemplate.queryForMap("""
                SELECT id, nombre, celular, contrasena_hash, rol, activo, fecha_creacion
                FROM usuario WHERE celular = ?
                """, CELULAR_ADMIN);
    }

    private int cantidad(String sql, Object parametro) {
        return jdbcTemplate.queryForObject(sql, Integer.class, parametro);
    }

    private static RequestPostProcessor jwtDe(String rol, UUID sujeto) {
        return jwt().jwt(jwt -> jwt.subject(sujeto.toString()).claim("rol", rol))
                .authorities(new SimpleGrantedAuthority("ROLE_" + rol));
    }
}
