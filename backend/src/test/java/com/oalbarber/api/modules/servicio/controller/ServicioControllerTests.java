package com.oalbarber.api.modules.servicio.controller;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.oalbarber.api.TestcontainersConfiguration;
import com.oalbarber.api.modules.auth.service.TokenServicio;
import com.oalbarber.api.modules.servicio.domain.Servicio;
import com.oalbarber.api.modules.servicio.repository.ServicioRepository;
import com.oalbarber.api.modules.usuario.domain.RolUsuario;
import com.oalbarber.api.modules.usuario.domain.Usuario;

/**
 * Contrato del catálogo público (RF-07): el GET es anónimo, solo muestra los
 * activos y {@code incluirInactivos=true} exige el rol ADMINISTRADOR.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "app.auth.jwt-secreto=clave-de-pruebas-solo-para-tests-con-mas-de-32-caracteres")
@AutoConfigureMockMvc
class ServicioControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private TokenServicio tokenServicio;

    @BeforeEach
    void prepararCatalogo() {
        servicioRepository.deleteAll();
        servicioRepository.save(servicio("Corte normal", "Corte clásico", 40,
                "25000", "corte-normal.jpg", true));
        servicioRepository.save(servicio("Mechas", "Trabajo de color", 180,
                "120000", "mechas.jpg", false));
    }

    @Test
    @DisplayName("el visitante ve solo los servicios activos con sus datos y su foto")
    void visitanteVeSoloServiciosActivos() throws Exception {
        mockMvc.perform(get("/api/servicios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombre").value("Corte normal"))
                .andExpect(jsonPath("$[0].descripcion").value("Corte clásico"))
                .andExpect(jsonPath("$[0].duracionMin").value(40))
                .andExpect(jsonPath("$[0].precio", closeTo(25000.0, 0.01)))
                .andExpect(jsonPath("$[0].imagen").value("corte-normal.jpg"))
                .andExpect(jsonPath("$[0].activo").value(true));
    }

    @Test
    @DisplayName("un visitante anónimo no puede pedir los inactivos")
    void anonimoNoPuedePedirInactivos() throws Exception {
        mockMvc.perform(get("/api/servicios").param("incluirInactivos", "true"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACCESO_DENEGADO"));
    }

    @Test
    @DisplayName("un cliente autenticado tampoco puede pedir los inactivos")
    void clienteNoPuedePedirInactivos() throws Exception {
        mockMvc.perform(get("/api/servicios")
                        .param("incluirInactivos", "true")
                        .header("Authorization", pase(RolUsuario.CLIENTE)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACCESO_DENEGADO"));
    }

    @Test
    @DisplayName("el administrador ve todo el catálogo ordenado por nombre")
    void administradorVeTodoElCatalogo() throws Exception {
        mockMvc.perform(get("/api/servicios")
                        .param("incluirInactivos", "true")
                        .header("Authorization", pase(RolUsuario.ADMINISTRADOR)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nombre").value("Corte normal"))
                .andExpect(jsonPath("$[1].nombre").value("Mechas"))
                .andExpect(jsonPath("$[1].activo").value(false));
    }

    @Test
    @DisplayName("el administrador sin el parámetro sigue viendo solo los activos")
    void administradorSinParametroVeSoloActivos() throws Exception {
        mockMvc.perform(get("/api/servicios")
                        .header("Authorization", pase(RolUsuario.ADMINISTRADOR)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombre").value("Corte normal"));
    }

    private String pase(RolUsuario rol) {
        Usuario usuario = Usuario.builder()
                .id(UUID.randomUUID())
                .nombre("Usuario de prueba")
                .rol(rol)
                .build();
        return "Bearer " + tokenServicio.firmarAcceso(usuario);
    }

    private static Servicio servicio(String nombre, String descripcion, int duracionMin,
                                     String precio, String imagen, boolean activo) {
        return Servicio.builder()
                .nombre(nombre)
                .descripcion(descripcion)
                .duracionMin(duracionMin)
                .precio(new BigDecimal(precio))
                .imagen(imagen)
                .activo(activo)
                .build();
    }
}
