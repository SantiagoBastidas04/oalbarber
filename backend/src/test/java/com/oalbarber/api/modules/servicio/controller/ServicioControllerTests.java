package com.oalbarber.api.modules.servicio.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.UUID;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.oalbarber.api.TestcontainersConfiguration;
import com.oalbarber.api.modules.auth.service.TokenServicio;
import com.oalbarber.api.modules.servicio.domain.Servicio;
import com.oalbarber.api.modules.servicio.repository.ServicioRepository;
import com.oalbarber.api.modules.usuario.domain.RolUsuario;
import com.oalbarber.api.modules.usuario.domain.Usuario;

/**
 * Contrato del catálogo (RF-07 y RF-08) sobre PostgreSQL real:
 * <ul>
 *   <li>El GET es anónimo y solo muestra los activos; los inactivos exigen ADMINISTRADOR.</li>
 *   <li>POST, PUT y PATCH solo los puede usar el ADMINISTRADOR.</li>
 *   <li>Se validan duración, precio y nombre único (409 NOMBRE_DUPLICADO).</li>
 *   <li>Desactivar no borra: el servicio y sus datos siguen ahí (RN-16).</li>
 * </ul>
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "app.auth.jwt-secreto=clave-de-pruebas-solo-para-tests-con-mas-de-32-caracteres")
@AutoConfigureMockMvc
class ServicioControllerTests {

    private static final String SERVICIO_VALIDO = """
            {"nombre": "Corte premium", "descripcion": "Corte con lavado",
             "duracionMin": 90, "precio": 45000, "imagen": "corte-premium.jpg"}""";

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

    // ------------------------------------------------------------------
    // Catálogo público (RF-07)
    // ------------------------------------------------------------------

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

    // ------------------------------------------------------------------
    // Crear (RF-08)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("el administrador crea un servicio y queda activo en el catálogo")
    void administradorCreaServicio() throws Exception {
        mockMvc.perform(post("/api/servicios")
                        .header("Authorization", pase(RolUsuario.ADMINISTRADOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(SERVICIO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nombre").value("Corte premium"))
                .andExpect(jsonPath("$.duracionMin").value(90))
                .andExpect(jsonPath("$.precio").value(precio(45000)))
                .andExpect(jsonPath("$.imagen").value("corte-premium.jpg"))
                .andExpect(jsonPath("$.activo").value(true));

        assertThat(servicioRepository.existsByNombreIgnoreCase("Corte premium")).isTrue();
    }

    @Test
    @DisplayName("crear sin sesión responde 401")
    void crearSinSesionResponde401() throws Exception {
        mockMvc.perform(post("/api/servicios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(SERVICIO_VALIDO))
                .andExpect(status().isUnauthorized());

        assertThat(servicioRepository.existsByNombreIgnoreCase("Corte premium")).isFalse();
    }

    @Test
    @DisplayName("un cliente no puede crear servicios")
    void clienteNoPuedeCrearServicios() throws Exception {
        mockMvc.perform(post("/api/servicios")
                        .header("Authorization", pase(RolUsuario.CLIENTE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(SERVICIO_VALIDO))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACCESO_DENEGADO"));

        assertThat(servicioRepository.existsByNombreIgnoreCase("Corte premium")).isFalse();
    }

    @Test
    @DisplayName("una duración fuera de 40, 90 o 180 responde 400 en el campo duracionMin")
    void crearConDuracionInvalidaResponde400() throws Exception {
        mockMvc.perform(post("/api/servicios")
                        .header("Authorization", pase(RolUsuario.ADMINISTRADOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(SERVICIO_VALIDO.replace("\"duracionMin\": 90",
                                "\"duracionMin\": 50")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACION"))
                .andExpect(jsonPath("$.errors.duracionMin")
                        .value("La duración debe ser 40, 90 o 180 minutos"));
    }

    @Test
    @DisplayName("un precio de cero responde 400 en el campo precio")
    void crearConPrecioInvalidoResponde400() throws Exception {
        mockMvc.perform(post("/api/servicios")
                        .header("Authorization", pase(RolUsuario.ADMINISTRADOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(SERVICIO_VALIDO.replace("\"precio\": 45000", "\"precio\": 0")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACION"))
                .andExpect(jsonPath("$.errors.precio").value("El precio debe ser mayor que cero"));
    }

    @Test
    @DisplayName("un nombre repetido, aunque cambie mayúsculas, responde 409 NOMBRE_DUPLICADO")
    void crearConNombreRepetidoResponde409() throws Exception {
        mockMvc.perform(post("/api/servicios")
                        .header("Authorization", pase(RolUsuario.ADMINISTRADOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(SERVICIO_VALIDO.replace("\"Corte premium\"", "\"  corte NORMAL  \"")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("NOMBRE_DUPLICADO"));

        assertThat(servicioRepository.existsByNombreIgnoreCase("Corte premium")).isFalse();
    }

    // ------------------------------------------------------------------
    // Editar (RF-08)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("el administrador cambia precio y duración y el catálogo lo refleja")
    void administradorActualizaServicio() throws Exception {
        UUID id = idDe("Corte normal");

        mockMvc.perform(put("/api/servicios/{id}", id)
                        .header("Authorization", pase(RolUsuario.ADMINISTRADOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "Corte normal", "descripcion": "Corte clásico",
                                 "duracionMin": 90, "precio": 30000,
                                 "imagen": "corte-normal.jpg"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Corte normal"))
                .andExpect(jsonPath("$.duracionMin").value(90))
                .andExpect(jsonPath("$.precio").value(precio(30000)))
                .andExpect(jsonPath("$.activo").value(true));

        Servicio guardado = servicioRepository.findById(id).orElseThrow();
        assertThat(guardado.getNombre()).isEqualTo("Corte normal");
        assertThat(guardado.getDuracionMin()).isEqualTo(90);
        assertThat(guardado.getPrecio()).isEqualByComparingTo("30000");

        mockMvc.perform(get("/api/servicios"))
                .andExpect(jsonPath("$[0].precio", closeTo(30000.0, 0.01)));
    }

    @Test
    @DisplayName("renombrar un servicio con el nombre de otro responde 409 NOMBRE_DUPLICADO")
    void actualizarConNombreDeOtroResponde409() throws Exception {
        UUID id = idDe("Corte normal");

        mockMvc.perform(put("/api/servicios/{id}", id)
                        .header("Authorization", pase(RolUsuario.ADMINISTRADOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "Mechas", "descripcion": null,
                                 "duracionMin": 40, "precio": 25000, "imagen": null}"""))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("NOMBRE_DUPLICADO"));

        assertThat(servicioRepository.findById(id).orElseThrow().getNombre())
                .isEqualTo("Corte normal");
    }

    @Test
    @DisplayName("editar un servicio que no existe responde 404")
    void actualizarInexistenteResponde404() throws Exception {
        mockMvc.perform(put("/api/servicios/{id}", UUID.randomUUID())
                        .header("Authorization", pase(RolUsuario.ADMINISTRADOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(SERVICIO_VALIDO))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("NO_ENCONTRADO"));
    }

    // ------------------------------------------------------------------
    // Activar y desactivar (RN-16)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("desactivar lo saca del catálogo público sin borrarlo ni tocar sus datos (RN-16)")
    void desactivarNoBorraElServicio() throws Exception {
        UUID id = idDe("Corte normal");

        mockMvc.perform(patch("/api/servicios/{id}/estado", id)
                        .header("Authorization", pase(RolUsuario.ADMINISTRADOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"activo\": false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.activo").value(false))
                .andExpect(jsonPath("$.duracionMin").value(40))
                .andExpect(jsonPath("$.precio", closeTo(25000.0, 0.01)));

        Servicio desactivado = servicioRepository.findById(id).orElseThrow();
        assertThat(desactivado.isActivo()).isFalse();
        assertThat(desactivado.getDuracionMin()).isEqualTo(40);
        assertThat(desactivado.getPrecio()).isEqualByComparingTo("25000");

        mockMvc.perform(get("/api/servicios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        mockMvc.perform(get("/api/servicios")
                        .param("incluirInactivos", "true")
                        .header("Authorization", pase(RolUsuario.ADMINISTRADOR)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("un cliente no puede desactivar servicios")
    void clienteNoPuedeCambiarElEstado() throws Exception {
        UUID id = idDe("Corte normal");

        mockMvc.perform(patch("/api/servicios/{id}/estado", id)
                        .header("Authorization", pase(RolUsuario.CLIENTE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"activo\": false}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACCESO_DENEGADO"));

        assertThat(servicioRepository.findById(id).orElseThrow().isActivo()).isTrue();
    }

    @Test
    @DisplayName("el estado sin valor responde 400 en el campo activo")
    void cambiarEstadoSinValorResponde400() throws Exception {
        UUID id = idDe("Corte normal");

        mockMvc.perform(patch("/api/servicios/{id}/estado", id)
                        .header("Authorization", pase(RolUsuario.ADMINISTRADOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"activo\": null}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACION"))
                .andExpect(jsonPath("$.errors.activo")
                        .value("Indica si el servicio queda activo"));
    }

    // ------------------------------------------------------------------
    // Utilidades
    // ------------------------------------------------------------------

    /**
     * Compara un precio sin depender de cómo lo serialice Jackson: un
     * {@code BigDecimal} con escala 0 sale como entero y con escala 2 como
     * decimal. Al cliente le da igual; el valor es el mismo.
     */
    private static Matcher<Object> precio(double esperado) {
        return new TypeSafeMatcher<>() {
            @Override
            protected boolean matchesSafely(Object valor) {
                return valor != null
                        && Math.abs(new BigDecimal(valor.toString()).doubleValue() - esperado) < 0.01;
            }

            @Override
            public void describeTo(Description descripcion) {
                descripcion.appendText("un precio de " + esperado);
            }
        };
    }

    private UUID idDe(String nombre) {
        return servicioRepository.findAllByOrderByNombreAsc().stream()
                .filter(servicio -> servicio.getNombre().equals(nombre))
                .map(Servicio::getId)
                .findFirst()
                .orElseThrow();
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
