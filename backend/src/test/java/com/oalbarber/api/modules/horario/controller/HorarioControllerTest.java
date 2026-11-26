package com.oalbarber.api.modules.horario.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
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

import com.oalbarber.api.common.exception.GlobalExceptionHandler;
import com.oalbarber.api.common.exception.ValidacionException;
import com.oalbarber.api.config.SecurityConfig;
import com.oalbarber.api.modules.auth.service.TokenServicio;
import com.oalbarber.api.modules.horario.dto.HorarioDiaResponse;
import com.oalbarber.api.modules.horario.dto.HorarioExcepcionResponse;
import com.oalbarber.api.modules.horario.service.HorarioService;

@Import({SecurityConfig.class, GlobalExceptionHandler.class,
        HorarioControllerTest.SeguridadTestConfig.class})
@WebMvcTest(HorarioController.class)
class HorarioControllerTest {

    private static final UUID BARBERO_ID =
            UUID.fromString("bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb");
    private static final UUID USUARIO_ID =
            UUID.fromString("11111111-1111-4111-8111-111111111111");
    private static final LocalDate FECHA = LocalDate.of(2026, 12, 6);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HorarioService horarioService;

    /** Conserva la cadena JWT real; los tests de controlador no validan firmas. */
    @TestConfiguration(proxyBeanMethods = false)
    static class SeguridadTestConfig {

        @Bean
        TokenServicio tokenServicio() {
            TokenServicio servicio = mock(TokenServicio.class);
            when(servicio.decodificador()).thenReturn(mock(JwtDecoder.class));
            return servicio;
        }
    }

    // ------------------------------------------------------------------
    // Consulta
    // ------------------------------------------------------------------

    @Test
    @DisplayName("sin pase responde 401")
    void sinPaseResponde401() throws Exception {
        mockMvc.perform(get("/api/horarios").param("barberoId", BARBERO_ID.toString()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("un cliente autenticado no puede consultar horarios")
    void clienteNoConsultaHorarios() throws Exception {
        mockMvc.perform(get("/api/horarios")
                        .with(autenticacionComo("CLIENTE")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(horarioService);
    }

    @Test
    @WithMockUser(username = "11111111-1111-4111-8111-111111111111",
            roles = "ADMINISTRADOR")
    @DisplayName("el administrador consulta un barbero y ve las horas en HH:mm")
    void administradorConsultaHorario() throws Exception {
        when(horarioService.listar(eq(BARBERO_ID), eq(true), eq(USUARIO_ID)))
                .thenReturn(List.of(dia(1, true)));

        mockMvc.perform(get("/api/horarios").param("barberoId", BARBERO_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].barberoId").value(BARBERO_ID.toString()))
                .andExpect(jsonPath("$[0].diaSemana").value(1))
                .andExpect(jsonPath("$[0].horaInicio").value("10:00"))
                .andExpect(jsonPath("$[0].horaFin").value("19:00"))
                .andExpect(jsonPath("$[0].almuerzoInicio").value("13:00"))
                .andExpect(jsonPath("$[0].almuerzoFin").value("14:00"));
    }

    @Test
    @WithMockUser(username = "11111111-1111-4111-8111-111111111111", roles = "BARBERO")
    @DisplayName("un barbero puede consultar su propio horario")
    void barberoConsultaSuHorario() throws Exception {
        when(horarioService.listar(eq(BARBERO_ID), eq(false), eq(USUARIO_ID)))
                .thenReturn(List.of(dia(2, false)));

        mockMvc.perform(get("/api/horarios").param("barberoId", BARBERO_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].diaSemana").value(2))
                .andExpect(jsonPath("$[0].almuerzoInicio").doesNotExist());
    }

    // ------------------------------------------------------------------
    // Reemplazo
    // ------------------------------------------------------------------

    @Test
    @DisplayName("sin pase no se puede guardar el horario")
    void sinPaseNoGuarda() throws Exception {
        mockMvc.perform(put("/api/horarios/{id}", BARBERO_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dias\":[]}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(horarioService);
    }

    @Test
    @WithMockUser(username = "11111111-1111-4111-8111-111111111111", roles = "BARBERO")
    @DisplayName("un barbero no puede modificar horarios")
    void barberoNoGuarda() throws Exception {
        mockMvc.perform(put("/api/horarios/{id}", BARBERO_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dias\":[]}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACCESO_DENEGADO"));

        verifyNoInteractions(horarioService);
    }

    @Test
    @WithMockUser(username = "11111111-1111-4111-8111-111111111111",
            roles = "ADMINISTRADOR")
    @DisplayName("el administrador guarda la semana y recibe los días configurados")
    void administradorGuardaSemana() throws Exception {
        when(horarioService.reemplazar(eq(BARBERO_ID), any())).thenReturn(List.of(dia(1, true)));

        mockMvc.perform(put("/api/horarios/{id}", BARBERO_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"dias": [
                                  {"diaSemana": 1, "horaInicio": "10:00", "horaFin": "19:00",
                                   "almuerzoInicio": "13:00", "almuerzoFin": "14:00"}
                                ]}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].diaSemana").value(1))
                .andExpect(jsonPath("$[0].horaInicio").value("10:00"));

        verify(horarioService).reemplazar(eq(BARBERO_ID), any());
    }

    @Test
    @WithMockUser(username = "11111111-1111-4111-8111-111111111111",
            roles = "ADMINISTRADOR")
    @DisplayName("un horario inválido responde 400 con detalle por día")
    void horarioInvalidoResponde400ConDetalle() throws Exception {
        when(horarioService.reemplazar(eq(BARBERO_ID), any()))
                .thenThrow(new ValidacionException("HORARIO_INVALIDO",
                        "Revisa los horarios marcados.",
                        Map.of("7", "El domingo está cerrado (RN-01).")));

        mockMvc.perform(put("/api/horarios/{id}", BARBERO_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"dias": [
                                  {"diaSemana": 7, "horaInicio": "10:00", "horaFin": "19:00"}
                                ]}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("HORARIO_INVALIDO"))
                .andExpect(jsonPath("$.errors.7").value("El domingo está cerrado (RN-01)."));
    }

    @Test
    @WithMockUser(username = "11111111-1111-4111-8111-111111111111",
            roles = "ADMINISTRADOR")
    @DisplayName("una hora con formato inválido responde 400 y no llega al servicio")
    void horaMalFormadaResponde400() throws Exception {
        mockMvc.perform(put("/api/horarios/{id}", BARBERO_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"dias": [
                                  {"diaSemana": 1, "horaInicio": "25:99", "horaFin": "19:00"}
                                ]}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("CUERPO_INVALIDO"));

        verifyNoInteractions(horarioService);
    }

    // ------------------------------------------------------------------
    // Fechas especiales
    // ------------------------------------------------------------------

    @Test
    @WithMockUser(username = "11111111-1111-4111-8111-111111111111",
            roles = "ADMINISTRADOR")
    @DisplayName("el administrador lista las fechas especiales de un barbero")
    void administradorListaExcepciones() throws Exception {
        when(horarioService.listarExcepciones(eq(BARBERO_ID), eq(true), eq(USUARIO_ID)))
                .thenReturn(List.of(excepcion(false)));

        mockMvc.perform(get("/api/horarios/{id}/excepciones", BARBERO_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fecha").value("2026-12-06"))
                .andExpect(jsonPath("$[0].cerrado").value(false))
                .andExpect(jsonPath("$[0].horaInicio").value("11:00"));
    }

    @Test
    @WithMockUser(username = "11111111-1111-4111-8111-111111111111", roles = "BARBERO")
    @DisplayName("un barbero puede listar sus propias fechas especiales")
    void barberoListaSusExcepciones() throws Exception {
        when(horarioService.listarExcepciones(eq(BARBERO_ID), eq(false), eq(USUARIO_ID)))
                .thenReturn(List.of(excepcion(true)));

        mockMvc.perform(get("/api/horarios/{id}/excepciones", BARBERO_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cerrado").value(true));
    }

    @Test
    @DisplayName("un cliente no puede listar fechas especiales")
    void clienteNoListaExcepciones() throws Exception {
        mockMvc.perform(get("/api/horarios/{id}/excepciones", BARBERO_ID)
                        .with(autenticacionComo("CLIENTE")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(horarioService);
    }

    @Test
    @WithMockUser(username = "11111111-1111-4111-8111-111111111111",
            roles = "ADMINISTRADOR")
    @DisplayName("el administrador guarda una fecha especial")
    void administradorGuardaExcepcion() throws Exception {
        when(horarioService.guardarExcepcion(eq(BARBERO_ID), eq(FECHA), any()))
                .thenReturn(excepcion(false));

        mockMvc.perform(put("/api/horarios/{id}/excepciones/{fecha}", BARBERO_ID, "2026-12-06")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cerrado": false, "horaInicio": "11:00", "horaFin": "17:00"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fecha").value("2026-12-06"))
                .andExpect(jsonPath("$.horaFin").value("17:00"));

        verify(horarioService).guardarExcepcion(eq(BARBERO_ID), eq(FECHA), any());
    }

    @Test
    @WithMockUser(username = "11111111-1111-4111-8111-111111111111", roles = "BARBERO")
    @DisplayName("un barbero no puede guardar fechas especiales")
    void barberoNoGuardaExcepcion() throws Exception {
        mockMvc.perform(put("/api/horarios/{id}/excepciones/{fecha}", BARBERO_ID, "2026-12-06")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cerrado\": true}"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(horarioService);
    }

    @Test
    @WithMockUser(username = "11111111-1111-4111-8111-111111111111",
            roles = "ADMINISTRADOR")
    @DisplayName("una fecha especial inválida responde 400 con detalle")
    void excepcionInvalidaResponde400() throws Exception {
        when(horarioService.guardarExcepcion(eq(BARBERO_ID), eq(FECHA), any()))
                .thenThrow(new ValidacionException("EXCEPCION_INVALIDA",
                        "Revisa la fecha especial.",
                        Map.of("horario", "Una fecha cerrada no lleva horas.")));

        mockMvc.perform(put("/api/horarios/{id}/excepciones/{fecha}", BARBERO_ID, "2026-12-06")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cerrado": true, "horaInicio": "10:00", "horaFin": "19:00"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("EXCEPCION_INVALIDA"))
                .andExpect(jsonPath("$.errors.horario")
                        .value("Una fecha cerrada no lleva horas."));
    }

    @Test
    @WithMockUser(username = "11111111-1111-4111-8111-111111111111",
            roles = "ADMINISTRADOR")
    @DisplayName("una fecha con formato inválido responde 400")
    void fechaMalFormadaResponde400() throws Exception {
        mockMvc.perform(put("/api/horarios/{id}/excepciones/{fecha}", BARBERO_ID, "2026-13-40")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cerrado\": true}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("PARAMETRO_INVALIDO"));

        verifyNoInteractions(horarioService);
    }

    @Test
    @WithMockUser(username = "11111111-1111-4111-8111-111111111111",
            roles = "ADMINISTRADOR")
    @DisplayName("el administrador elimina una fecha especial")
    void administradorEliminaExcepcion() throws Exception {
        mockMvc.perform(delete("/api/horarios/{id}/excepciones/{fecha}",
                        BARBERO_ID, "2026-12-06"))
                .andExpect(status().isNoContent());

        verify(horarioService).eliminarExcepcion(BARBERO_ID, FECHA);
    }

    @Test
    @WithMockUser(username = "11111111-1111-4111-8111-111111111111", roles = "BARBERO")
    @DisplayName("un barbero no puede eliminar fechas especiales")
    void barberoNoEliminaExcepcion() throws Exception {
        mockMvc.perform(delete("/api/horarios/{id}/excepciones/{fecha}",
                        BARBERO_ID, "2026-12-06"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(horarioService);
    }

    // ------------------------------------------------------------------
    // Utilidades
    // ------------------------------------------------------------------

    private static HorarioDiaResponse dia(int numero, boolean conAlmuerzo) {
        return new HorarioDiaResponse(UUID.randomUUID(), BARBERO_ID, numero,
                LocalTime.of(10, 0), LocalTime.of(19, 0),
                conAlmuerzo ? LocalTime.of(13, 0) : null,
                conAlmuerzo ? LocalTime.of(14, 0) : null);
    }

    private static HorarioExcepcionResponse excepcion(boolean cerrado) {
        return new HorarioExcepcionResponse(UUID.randomUUID(), BARBERO_ID, FECHA,
                cerrado,
                cerrado ? null : LocalTime.of(11, 0),
                cerrado ? null : LocalTime.of(17, 0),
                null, null);
    }

    private static org.springframework.test.web.servlet.request.RequestPostProcessor
            autenticacionComo(String rol) {
        return org.springframework.security.test.web.servlet.request
                .SecurityMockMvcRequestPostProcessors.user("usuario")
                .roles(rol);
    }
}
