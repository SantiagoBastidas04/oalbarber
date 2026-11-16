package com.oalbarber.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import com.oalbarber.api.config.AdminInicializador;
import com.oalbarber.api.modules.barbero.dto.BarberoRequest;
import com.oalbarber.api.modules.barbero.service.BarberoService;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = {
        "JWT_SECRETO=clave-falsa-solo-pruebas-no-usar-produccion-123456",
        "app.admin.celular=+573000000099",
        "app.admin.nombre=Administrador ficticio",
        "app.admin.contrasena=ClaveAdminFicticia123"
})
class BarberoServiceIntegrationTest {

    @Autowired
    private AdminInicializador adminInicializador;

    @Autowired
    private BarberoService barberoService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void inicializadorIdempotenteYAltaDePerfilAtomica() {
        assertEquals(1, cantidad("SELECT count(*) FROM barbero b JOIN usuario u "
                + "ON u.id = b.usuario_id WHERE u.celular = ?", "+573000000099"));

        adminInicializador.run(new DefaultApplicationArguments(new String[0]));

        assertEquals(1, cantidad("SELECT count(*) FROM barbero b JOIN usuario u "
                + "ON u.id = b.usuario_id WHERE u.celular = ?", "+573000000099"));

        BarberoRequest solicitudQueFalla = new BarberoRequest(
                "Barbero ficticio", "+573000000098", "ClaveInicialFicticia123",
                "N".repeat(101), null, null);
        assertThrows(RuntimeException.class, () -> barberoService.crear(solicitudQueFalla));

        assertEquals(0, cantidad("SELECT count(*) FROM usuario WHERE celular = ?",
                "+573000000098"));
    }

    private int cantidad(String sql, String celular) {
        return jdbcTemplate.queryForObject(sql, Integer.class, celular);
    }
}
