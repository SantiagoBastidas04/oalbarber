package com.oalbarber.api.modules.cita.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.oalbarber.api.common.exception.ReglaNegocioException;
import com.oalbarber.api.modules.barbero.dto.BarberoPublicoResponse;
import com.oalbarber.api.modules.barbero.service.BarberoService;
import com.oalbarber.api.modules.cita.dto.DisponibilidadResponse;
import com.oalbarber.api.modules.cita.dto.IntervaloOcupadoResponse;
import com.oalbarber.api.modules.horario.dto.BloqueoResponse;
import com.oalbarber.api.modules.horario.dto.HorarioDiaResponse;
import com.oalbarber.api.modules.horario.dto.HorarioExcepcionResponse;
import com.oalbarber.api.modules.horario.dto.ParametrosDisponibilidadResponse;
import com.oalbarber.api.modules.horario.service.HorarioService;
import com.oalbarber.api.modules.servicio.dto.ServicioResponse;
import com.oalbarber.api.modules.servicio.service.ServicioService;

@ExtendWith(MockitoExtension.class)
class DisponibilidadServiceTest {

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");
    private static final LocalDate FECHA = LocalDate.of(2030, 1, 7);
    private static final UUID SERVICIO_ID = UUID.fromString("cccccccc-cccc-4ccc-8ccc-ccccccccccc1");
    private static final UUID BARBERO_MENOR_ID = UUID.fromString("aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa");
    private static final UUID BARBERO_MAYOR_ID = UUID.fromString("bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb");
    private static final UUID BARBERO_AJENO_ID = UUID.fromString("dddddddd-dddd-4ddd-8ddd-dddddddddddd");
    private static final Clock RELOJ = Clock.fixed(
            FECHA.minusDays(1).atTime(10, 0).atZone(ZONA).toInstant(), ZONA);
    private static final ParametrosDisponibilidadResponse PARAMETROS =
            new ParametrosDisponibilidadResponse(24, 7, 40, 0);

    @Mock
    private ServicioService servicioService;
    @Mock
    private BarberoService barberoService;
    @Mock
    private HorarioService horarioService;
    @Mock
    private CitaService citaService;

    private DisponibilidadService disponibilidadService;

    @BeforeEach
    void preparar() {
        disponibilidadService = new DisponibilidadService(
                servicioService, barberoService, horarioService, citaService, RELOJ);
        when(servicioService.obtenerActivo(SERVICIO_ID)).thenReturn(new ServicioResponse(
                SERVICIO_ID, "Corte normal", "Servicio de prueba", 40,
                new BigDecimal("25000"), "corte.jpg", true));
    }

    @Test
    @DisplayName("cualquiera devuelve inicios únicos asignados al barbero con menos citas")
    void cualquieraAsignaAlBarberoConMenosCitas() {
        configurarAgenda(List.of(new IntervaloOcupadoResponse(BARBERO_MAYOR_ID,
                        instant("2030-01-07T11:00:00-05:00"),
                        instant("2030-01-07T11:40:00-05:00"))));

        List<DisponibilidadResponse> turnos = disponibilidadService
                .calcularDisponibilidad(SERVICIO_ID, FECHA, null);

        assertThat(turnos).isNotEmpty();
        assertThat(turnos.getFirst().barberoId()).isEqualTo(BARBERO_MENOR_ID);
        assertThat(turnos.getFirst().inicio().toString()).isEqualTo("2030-01-07T10:00-05:00");
        assertThat(turnos.getFirst().fin().toString()).isEqualTo("2030-01-07T10:40-05:00");
        assertThat(turnos).extracting(DisponibilidadResponse::inicio).doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("en empate asigna de forma determinista el UUID menor aunque llegue segundo")
    void desempataPorUuidMenor() {
        configurarAgenda(List.of());
        List<DisponibilidadResponse> turnos = disponibilidadService
                .calcularDisponibilidad(SERVICIO_ID, FECHA, null);

        assertThat(turnos.getFirst().barberoId()).isEqualTo(BARBERO_MENOR_ID);
    }

    @Test
    @DisplayName("un barbero explícito limita las opciones a su propio perfil")
    void barberoExplicitoLimitaOpciones() {
        configurarAgenda(List.of());
        List<DisponibilidadResponse> turnos = disponibilidadService
                .calcularDisponibilidad(SERVICIO_ID, FECHA, BARBERO_MAYOR_ID);

        assertThat(turnos).isNotEmpty().allMatch(turno -> turno.barberoId().equals(BARBERO_MAYOR_ID));
    }

    @Test
    @DisplayName("rechaza un barbero que no existe, está inactivo o no ofrece el servicio")
    void barberoExplicitoNoElegibleLanzaErrorDeNegocio() {
        when(barberoService.listarActivosQueOfrecenServicio(SERVICIO_ID))
                .thenReturn(List.of(barbero(BARBERO_MAYOR_ID), barbero(BARBERO_MENOR_ID)));
        assertThatThrownBy(() -> disponibilidadService.calcularDisponibilidad(
                SERVICIO_ID, FECHA, BARBERO_AJENO_ID))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("no existe, está inactivo o no ofrece el servicio");
        verify(horarioService, never()).listarParaDisponibilidad(anyCollection(), anyInt());
    }

    @Test
    @DisplayName("sin barberos activos que ofrezcan el servicio devuelve una lista vacía")
    void sinCandidatosDevuelveListaVacia() {
        when(barberoService.listarActivosQueOfrecenServicio(SERVICIO_ID)).thenReturn(List.of());

        assertThat(disponibilidadService.calcularDisponibilidad(SERVICIO_ID, FECHA, null)).isEmpty();
        verify(horarioService, never()).listarParaDisponibilidad(anyCollection(), anyInt());
    }

    @Test
    @DisplayName("una excepción abierta permite un domingo aunque la semana ordinaria no tenga fila")
    void excepcionAbreDomingo() {
        LocalDate domingo = FECHA.plusDays(6);
        when(barberoService.listarActivosQueOfrecenServicio(SERVICIO_ID))
                .thenReturn(List.of(barbero(BARBERO_MENOR_ID)));
        when(horarioService.obtenerParametrosDisponibilidad()).thenReturn(PARAMETROS);
        when(horarioService.listarParaDisponibilidad(anyCollection(), eq(7))).thenReturn(List.of());
        when(horarioService.listarExcepcionesParaDisponibilidad(anyCollection(), eq(domingo)))
                .thenReturn(List.of(excepcionAbierta(BARBERO_MENOR_ID, domingo,
                        LocalTime.of(11, 0), LocalTime.of(16, 0), null, null)));
        when(horarioService.listarBloqueosParaDisponibilidad(
                anyCollection(), any(Instant.class), any(Instant.class))).thenReturn(List.of());
        when(citaService.listarOcupadas(anyCollection(), any(Instant.class), any(Instant.class)))
                .thenReturn(List.of());
        Clock relojDomingo = Clock.fixed(domingo.minusDays(1).atTime(11, 0)
                .atZone(ZONA).toInstant(), ZONA);
        disponibilidadService = new DisponibilidadService(
                servicioService, barberoService, horarioService, citaService, relojDomingo);

        List<DisponibilidadResponse> turnos = disponibilidadService
                .calcularDisponibilidad(SERVICIO_ID, domingo, null);

        assertThat(turnos).isNotEmpty();
        assertThat(turnos.getFirst().inicio().toString()).isEqualTo("2030-01-13T11:00-05:00");
    }

    @Test
    @DisplayName("una excepción cerrada prevalece sobre el horario semanal")
    void excepcionCerradaPrevaleceSobreSemana() {
        configurarAgenda(List.of());
        when(horarioService.listarExcepcionesParaDisponibilidad(anyCollection(), eq(FECHA)))
                .thenReturn(List.of(excepcionCerrada(BARBERO_MAYOR_ID, FECHA)));

        List<DisponibilidadResponse> turnos = disponibilidadService
                .calcularDisponibilidad(SERVICIO_ID, FECHA, BARBERO_MAYOR_ID);

        assertThat(turnos).isEmpty();
    }

    @Test
    @DisplayName("la excepción reemplaza el horario y el almuerzo de la fecha")
    void excepcionReemplazaHorarioYAlmuerzo() {
        configurarAgenda(List.of());
        when(horarioService.listarExcepcionesParaDisponibilidad(anyCollection(), eq(FECHA)))
                .thenReturn(List.of(excepcionAbierta(BARBERO_MAYOR_ID, FECHA,
                        LocalTime.of(12, 0), LocalTime.of(17, 0),
                        LocalTime.of(13, 0), LocalTime.of(14, 0))));

        List<DisponibilidadResponse> turnos = disponibilidadService
                .calcularDisponibilidad(SERVICIO_ID, FECHA, BARBERO_MAYOR_ID);

        assertThat(turnos).extracting(turno -> turno.inicio().toLocalTime())
                .contains(LocalTime.of(12, 0), LocalTime.of(14, 0))
                .doesNotContain(LocalTime.of(10, 0), LocalTime.of(13, 0), LocalTime.of(13, 40));
    }

    private static BarberoPublicoResponse barbero(UUID id) {
        return new BarberoPublicoResponse(id, "Barbero", null, null, true);
    }

    private void configurarAgenda(List<IntervaloOcupadoResponse> citas) {
        when(barberoService.listarActivosQueOfrecenServicio(SERVICIO_ID))
                .thenReturn(List.of(barbero(BARBERO_MAYOR_ID), barbero(BARBERO_MENOR_ID)));
        when(horarioService.obtenerParametrosDisponibilidad()).thenReturn(PARAMETROS);
        when(horarioService.listarParaDisponibilidad(anyCollection(), anyInt()))
                .thenReturn(List.of(horario(BARBERO_MAYOR_ID), horario(BARBERO_MENOR_ID)));
        lenient().when(horarioService.listarExcepcionesParaDisponibilidad(
                anyCollection(), any(LocalDate.class))).thenReturn(List.of());
        when(horarioService.listarBloqueosParaDisponibilidad(
                anyCollection(), any(Instant.class), any(Instant.class)))
                .thenReturn(List.<BloqueoResponse>of());
        when(citaService.listarOcupadas(anyCollection(), any(Instant.class), any(Instant.class)))
                .thenReturn(citas);
    }

    private static HorarioDiaResponse horario(UUID id) {
        return new HorarioDiaResponse(UUID.randomUUID(), id, 1, LocalTime.of(10, 0),
                LocalTime.of(19, 0), null, null);
    }

    private static HorarioExcepcionResponse excepcionAbierta(UUID barberoId, LocalDate fecha,
                                                              LocalTime inicio, LocalTime fin,
                                                              LocalTime almuerzoInicio,
                                                              LocalTime almuerzoFin) {
        return new HorarioExcepcionResponse(UUID.randomUUID(), barberoId, fecha, false,
                inicio, fin, almuerzoInicio, almuerzoFin);
    }

    private static HorarioExcepcionResponse excepcionCerrada(UUID barberoId, LocalDate fecha) {
        return new HorarioExcepcionResponse(UUID.randomUUID(), barberoId, fecha, true,
                null, null, null, null);
    }

    private static Instant instant(String fechaHora) {
        return java.time.OffsetDateTime.parse(fechaHora).toInstant();
    }
}
