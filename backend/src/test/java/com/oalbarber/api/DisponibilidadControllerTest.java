package com.oalbarber.api;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.oalbarber.api.common.exception.GlobalExceptionHandler;
import com.oalbarber.api.common.exception.RecursoNoEncontradoException;
import com.oalbarber.api.config.SecurityConfig;
import com.oalbarber.api.modules.auth.service.TokenServicio;
import com.oalbarber.api.modules.cita.controller.DisponibilidadController;
import com.oalbarber.api.modules.cita.dto.DisponibilidadResponse;
import com.oalbarber.api.modules.cita.service.DisponibilidadService;

@Import({SecurityConfig.class, GlobalExceptionHandler.class,
        DisponibilidadControllerTest.SeguridadTestConfig.class})
@WebMvcTest(DisponibilidadController.class)
class DisponibilidadControllerTest {

    private static final UUID SERVICIO_ID = UUID.fromString("cccccccc-cccc-4ccc-8ccc-ccccccccccc1");
    private static final UUID BARBERO_ID = UUID.fromString("aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DisponibilidadService disponibilidadService;

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
    void consultaPublicaDevuelveLosHorariosConOffsetBogota() throws Exception {
        when(disponibilidadService.calcularDisponibilidad(SERVICIO_ID,
                LocalDate.of(2026, 11, 26), null)).thenReturn(List.of(new DisponibilidadResponse(
                        OffsetDateTime.parse("2026-11-26T10:00:00-05:00"),
                        OffsetDateTime.parse("2026-11-26T10:40:00-05:00"), BARBERO_ID)));

        mockMvc.perform(get("/api/disponibilidad")
                        .param("servicioId", SERVICIO_ID.toString())
                        .param("fecha", "2026-11-26"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].inicio").value("2026-11-26T10:00:00-05:00"))
                .andExpect(jsonPath("$[0].fin").value("2026-11-26T10:40:00-05:00"))
                .andExpect(jsonPath("$[0].barberoId").value(BARBERO_ID.toString()));

        verify(disponibilidadService).calcularDisponibilidad(
                SERVICIO_ID, LocalDate.of(2026, 11, 26), null);
    }

    @Test
    void parametrosObligatoriosAusentesResponden400ConElCampo() throws Exception {
        mockMvc.perform(get("/api/disponibilidad").param("servicioId", SERVICIO_ID.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACION"))
                .andExpect(jsonPath("$.errors.fecha").value("El campo es obligatorio."));

        verifyNoInteractions(disponibilidadService);
    }

    @Test
    void fechaConFormatoInvalidoResponde400ConElCampo() throws Exception {
        mockMvc.perform(get("/api/disponibilidad")
                        .param("servicioId", SERVICIO_ID.toString())
                        .param("fecha", "26-11-2026"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.fecha").value("Debe tener formato AAAA-MM-DD."));

        verifyNoInteractions(disponibilidadService);
    }

    @Test
    void identificadorConFormatoInvalidoResponde400ConElCampo() throws Exception {
        mockMvc.perform(get("/api/disponibilidad")
                        .param("servicioId", "no-es-uuid")
                        .param("fecha", "2026-11-26"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.servicioId")
                        .value("Debe ser un identificador UUID válido."));

        verifyNoInteractions(disponibilidadService);
    }

    @Test
    void postNoQuedaPublicoAunqueGetSiLoEste() throws Exception {
        mockMvc.perform(post("/api/disponibilidad"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(disponibilidadService);
    }

    @Test
    void servicioInexistenteDevuelve404ProblemDetail() throws Exception {
        when(disponibilidadService.calcularDisponibilidad(eq(SERVICIO_ID),
                eq(LocalDate.of(2026, 11, 26)), eq(null)))
                .thenThrow(new RecursoNoEncontradoException("No existe un servicio activo."));

        mockMvc.perform(get("/api/disponibilidad")
                        .param("servicioId", SERVICIO_ID.toString())
                        .param("fecha", "2026-11-26"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("No existe un servicio activo."));
    }
}
