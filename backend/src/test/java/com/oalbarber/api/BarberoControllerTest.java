package com.oalbarber.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.oalbarber.api.common.exception.GlobalExceptionHandler;
import com.oalbarber.api.common.exception.RecursoDuplicadoException;
import com.oalbarber.api.common.exception.RecursoNoEncontradoException;
import com.oalbarber.api.common.exception.ReglaNegocioException;
import com.oalbarber.api.config.SecurityConfig;
import com.oalbarber.api.modules.auth.service.TokenServicio;
import com.oalbarber.api.modules.barbero.controller.BarberoController;
import com.oalbarber.api.modules.barbero.dto.BarberoAdminResponse;
import com.oalbarber.api.modules.barbero.dto.BarberoPublicoResponse;
import com.oalbarber.api.modules.barbero.service.BarberoService;
import com.oalbarber.api.modules.servicio.dto.ServicioResponse;

@Import({SecurityConfig.class, GlobalExceptionHandler.class,
        BarberoControllerTest.SeguridadTestConfig.class})
@WebMvcTest(BarberoController.class)
class BarberoControllerTest {

    private static final UUID PERFIL_ID =
            UUID.fromString("aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa");
    private static final UUID USUARIO_ID =
            UUID.fromString("11111111-1111-4111-8111-111111111111");
    private static final UUID SERVICIO_ID =
            UUID.fromString("cccccccc-cccc-4ccc-8ccc-ccccccccccc1");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BarberoService barberoService;

    /** Conserva la cadena JWT real; los tests de controlador no validan firmas criptográficas. */
    @TestConfiguration(proxyBeanMethods = false)
    static class SeguridadTestConfig {

        @Bean
        TokenServicio tokenServicio() {
            TokenServicio servicio = mock(TokenServicio.class);
            when(servicio.decodificador()).thenReturn(mock(JwtDecoder.class));
            return servicio;
        }
    }

    @Test
    void listadoPublicoDevuelveSoloRespuestaPublicaSinAutenticacion() throws Exception {
        when(barberoService.listarActivos()).thenReturn(List.of(publico()));

        mockMvc.perform(get("/api/barberos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(PERFIL_ID.toString()))
                .andExpect(jsonPath("$[0].nombrePublico").value("Barbero visible"))
                .andExpect(jsonPath("$[0].celular").doesNotExist());

        verify(barberoService).listarActivos();
    }

    @Test
    void listadoAdminRequiereAutenticacionYRolAdministrador() throws Exception {
        when(barberoService.listarTodos()).thenReturn(List.of(admin()));

        mockMvc.perform(get("/api/barberos/admin"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(conAutenticacion(get("/api/barberos/admin"), "CLIENTE"))
                .andExpect(status().isForbidden());
        mockMvc.perform(conAutenticacion(get("/api/barberos/admin"), "BARBERO"))
                .andExpect(status().isForbidden());
        mockMvc.perform(conAutenticacion(get("/api/barberos/admin"), "ADMINISTRADOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].celular").value("+573000000001"))
                .andExpect(jsonPath("$[0].servicioIds").isArray());
    }

    @Test
    void crearBarberoDevuelve201YEsSoloParaAdministrador() throws Exception {
        when(barberoService.crear(any())).thenReturn(admin());
        String cuerpo = """
                {
                  "nombre": "Barbero de prueba",
                  "celular": "+573000000001",
                  "contrasena": "ClaveSegura123",
                  "nombrePublico": "Barbero visible",
                  "descripcion": "Descripción ficticia",
                  "foto": "barbero-01.webp"
                }
                """;

        mockMvc.perform(post("/api/barberos").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(conAutenticacion(post("/api/barberos"), "CLIENTE")
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isForbidden());
        mockMvc.perform(conAutenticacion(post("/api/barberos"), "BARBERO")
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isForbidden());
        mockMvc.perform(conAutenticacion(post("/api/barberos"), "ADMINISTRADOR")
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(PERFIL_ID.toString()));

        verify(barberoService).crear(any());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void altaInvalidaDevuelve400YSinInvocarElServicio() throws Exception {
        mockMvc.perform(post("/api/barberos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"\", \"celular\":\"mal\", \"contrasena\":\"corta\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACION"));

        verifyNoInteractions(barberoService);
    }

    @Test
    void celularDuplicadoSeConvierteEn409() throws Exception {
        when(barberoService.crear(any())).thenThrow(new RecursoDuplicadoException(
                "CELULAR_DUPLICADO", "EL CELULAR YA ESTA REGISTRADO EN NUESTRO SISTEMA."));
        String cuerpo = """
                {"nombre":"Barbero de prueba", "celular":"+573000000001",
                 "contrasena":"ClaveSegura123"}
                """;

        mockMvc.perform(conAutenticacion(post("/api/barberos"), "ADMINISTRADOR")
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CELULAR_DUPLICADO"));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void patchEstadoActualizaElPerfilYDevuelve404SiNoExiste() throws Exception {
        when(barberoService.cambiarEstado(PERFIL_ID, false)).thenReturn(adminInactivo());

        mockMvc.perform(patch("/api/barberos/{id}/estado", PERFIL_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"activo\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));

        when(barberoService.cambiarEstado(PERFIL_ID, true))
                .thenThrow(new RecursoNoEncontradoException("No existe el perfil de barbero indicado."));
        mockMvc.perform(patch("/api/barberos/{id}/estado", PERFIL_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"activo\":true}"))
                .andExpect(status().isNotFound());

        verify(barberoService).cambiarEstado(PERFIL_ID, false);
        verify(barberoService).cambiarEstado(PERFIL_ID, true);
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void patchEstadoRequiereActivoEnElCuerpo() throws Exception {
        mockMvc.perform(patch("/api/barberos/{id}/estado", PERFIL_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACION"));
    }

    @Test
    void listadoPublicoDeServiciosSoloAdmitePerfilesActivos() throws Exception {
        when(barberoService.listarServiciosPublicosDe(PERFIL_ID))
                .thenReturn(List.of(servicio()));

        mockMvc.perform(get("/api/barberos/{id}/servicios", PERFIL_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(SERVICIO_ID.toString()))
                .andExpect(jsonPath("$[0].nombre").value("Corte ficticio"));

        when(barberoService.listarServiciosPublicosDe(PERFIL_ID))
                .thenThrow(new RecursoNoEncontradoException(
                        "No existe el perfil de barbero indicado."));
        mockMvc.perform(get("/api/barberos/{id}/servicios", PERFIL_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    void reemplazoDeServiciosEsSoloAdministradorYDevuelveListaActualizada() throws Exception {
        when(barberoService.reemplazarServicios(PERFIL_ID, List.of(SERVICIO_ID)))
                .thenReturn(List.of(servicio()));
        String cuerpo = """
                {"servicioIds":["%s"]}
                """.formatted(SERVICIO_ID);

        mockMvc.perform(put("/api/barberos/{id}/servicios", PERFIL_ID)
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(conAutenticacion(put("/api/barberos/{id}/servicios", PERFIL_ID), "CLIENTE")
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isForbidden());
        mockMvc.perform(conAutenticacion(put("/api/barberos/{id}/servicios", PERFIL_ID), "BARBERO")
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isForbidden());
        mockMvc.perform(conAutenticacion(put("/api/barberos/{id}/servicios", PERFIL_ID),
                        "ADMINISTRADOR")
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(SERVICIO_ID.toString()));

        verify(barberoService).reemplazarServicios(PERFIL_ID, List.of(SERVICIO_ID));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void reemplazoConListaNulaODuplicadaDevuelve400SinInvocarServicio() throws Exception {
        mockMvc.perform(put("/api/barberos/{id}/servicios", PERFIL_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"servicioIds\":null}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACION"));

        mockMvc.perform(put("/api/barberos/{id}/servicios", PERFIL_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"servicioIds\":[\"" + SERVICIO_ID + "\",\"" + SERVICIO_ID + "\"]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACION"));

        verifyNoInteractions(barberoService);
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void reemplazoDevuelve404ParaBarberoAusenteY409ParaServicioNoValido() throws Exception {
        when(barberoService.reemplazarServicios(PERFIL_ID, List.of(SERVICIO_ID)))
                .thenThrow(new RecursoNoEncontradoException("No existe el perfil de barbero indicado."));
        String cuerpo = "{\"servicioIds\":[\"" + SERVICIO_ID + "\"]}";

        mockMvc.perform(put("/api/barberos/{id}/servicios", PERFIL_ID)
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isNotFound());

        doThrow(new ReglaNegocioException("SERVICIO_NO_VALIDO",
                "Todos los servicios deben existir y estar activos."))
                .when(barberoService).reemplazarServicios(PERFIL_ID, List.of(SERVICIO_ID));
        mockMvc.perform(put("/api/barberos/{id}/servicios", PERFIL_ID)
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("SERVICIO_NO_VALIDO"));
    }

    private static MockHttpServletRequestBuilder conAutenticacion(
            MockHttpServletRequestBuilder peticion, String rol) {
        return peticion.with(org.springframework.security.test.web.servlet.request
                .SecurityMockMvcRequestPostProcessors.user("usuario-prueba").roles(rol));
    }

    private static BarberoPublicoResponse publico() {
        return new BarberoPublicoResponse(PERFIL_ID, "Barbero visible",
                "Descripción ficticia", "barbero-01.webp", true);
    }

    private static BarberoAdminResponse admin() {
        return new BarberoAdminResponse(PERFIL_ID, USUARIO_ID, "Barbero de prueba",
                "+573000000001", "Barbero visible", "Descripción ficticia",
                "barbero-01.webp", true, List.of());
    }

    private static BarberoAdminResponse adminInactivo() {
        return new BarberoAdminResponse(PERFIL_ID, USUARIO_ID, "Barbero de prueba",
                "+573000000001", "Barbero visible", "Descripción ficticia",
                "barbero-01.webp", false, List.of());
    }

    private static ServicioResponse servicio() {
        ServicioResponse servicio = new ServicioResponse();
        servicio.setId(SERVICIO_ID);
        servicio.setNombre("Corte ficticio");
        servicio.setActivo(true);
        return servicio;
    }
}
