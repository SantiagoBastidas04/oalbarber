package com.oalbarber.api.modules.cita.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.oalbarber.api.modules.cita.dto.IntervaloOcupadoResponse;
import com.oalbarber.api.modules.cita.service.CalculadorHuecos.IntervaloDisponible;
import com.oalbarber.api.modules.horario.dto.BloqueoResponse;
import com.oalbarber.api.modules.horario.dto.HorarioDiaResponse;
import com.oalbarber.api.modules.horario.dto.ParametrosDisponibilidadResponse;

class CalculadorHuecosTest {

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");
    private static final LocalDate FECHA = LocalDate.of(2030, 1, 7); // lunes
    private static final UUID BARBERO = UUID.fromString("aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa");
    private static final UUID OTRO_BARBERO = UUID.fromString("bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb");
    private static final Clock RELOJ = Clock.fixed(
            FECHA.minusDays(1).atTime(10, 0).atZone(ZONA).toInstant(), ZONA);
    private static final ParametrosDisponibilidadResponse PARAMETROS =
            new ParametrosDisponibilidadResponse(24, 7, 40, 0);

    private final CalculadorHuecos calculador = new CalculadorHuecos();

    @Test
    @DisplayName("inicia los turnos desde cada hueco y permite terminar exactamente al cierre (RN-06 y RN-07)")
    void cuentaDesdeCadaHuecoYPermiteTerminarAlCierre() {
        List<IntervaloDisponible> turnos = calcular(horario(10, 19, null, null), List.of(),
                List.of(), 180, PARAMETROS, RELOJ);

        assertThat(turnos).extracting(IntervaloDisponible::inicio)
                .contains(instant("2030-01-07T16:00:00-05:00"));
        assertThat(turnos).filteredOn(turno -> turno.inicio()
                .equals(instant("2030-01-07T16:00:00-05:00")))
                .singleElement().extracting(IntervaloDisponible::fin)
                .isEqualTo(instant("2030-01-07T19:00:00-05:00"));
    }

    @Test
    @DisplayName("ofrece el siguiente turno justo al terminar la cita anterior (CA-02)")
    void elSiguienteTurnoPuedeEmpezarCuandoTerminaLaCita() {
        List<IntervaloDisponible> turnos = calcular(horario(10, 19, null, null), List.of(),
                List.of(cita("2030-01-07T10:00:00-05:00", "2030-01-07T13:00:00-05:00")),
                180, PARAMETROS, RELOJ);

        assertThat(turnos).extracting(IntervaloDisponible::inicio)
                .contains(instant("2030-01-07T13:00:00-05:00"));
    }

    @Test
    @DisplayName("aplica el límite mínimo de 24 horas y el máximo exacto de siete días (RN-08)")
    void filtraPorAnticipacionMinimaYMaxima() {
        Clock reloj = Clock.fixed(FECHA.atTime(10, 0).atZone(ZONA).toInstant(), ZONA);
        HorarioDiaResponse jornadaManana = horario(10, 19, null, null);
        List<IntervaloDisponible> desdeHoy = calcular(jornadaManana, List.of(), List.of(),
                40, PARAMETROS, reloj);
        assertThat(desdeHoy).noneMatch(turno -> turno.inicio()
                .equals(instant("2030-01-07T10:00:00-05:00")));

        LocalDate ultimoDia = FECHA.plusDays(7);
        HorarioDiaResponse jornadaUltimoDia = new HorarioDiaResponse(
                UUID.randomUUID(), BARBERO, ultimoDia.getDayOfWeek().getValue(),
                LocalTime.of(10, 0), LocalTime.of(19, 0), null, null);
        Clock relojLimite = Clock.fixed(FECHA.atTime(10, 0).atZone(ZONA).toInstant(), ZONA);
        List<IntervaloDisponible> enLimite = calculador.calcular(ultimoDia, BARBERO,
                jornadaUltimoDia, List.of(), List.of(), 40, PARAMETROS, relojLimite);
        assertThat(enLimite).extracting(IntervaloDisponible::inicio)
                .contains(ultimoDia.atTime(10, 0).atZone(ZONA).toInstant());
    }

    @Test
    @DisplayName("descuenta almuerzo solo cuando está definido (RN-02)")
    void descuentaAlmuerzoDefinido() {
        List<IntervaloDisponible> conAlmuerzo = calcular(
                horario(10, 19, LocalTime.of(13, 0), LocalTime.of(14, 0)), List.of(), List.of(),
                40, PARAMETROS, RELOJ);
        List<IntervaloDisponible> sinAlmuerzo = calcular(
                horario(10, 19, null, null), List.of(), List.of(), 40, PARAMETROS, RELOJ);

        assertThat(conAlmuerzo).noneMatch(turno -> turno.inicio()
                .equals(instant("2030-01-07T13:00:00-05:00")));
        assertThat(sinAlmuerzo).anyMatch(turno -> turno.inicio()
                .equals(instant("2030-01-07T13:20:00-05:00")));
        assertThat(conAlmuerzo).anyMatch(turno -> turno.inicio()
                .equals(instant("2030-01-07T14:00:00-05:00")));
    }

    @Test
    @DisplayName("recorta bloqueos parciales y de jornada completa (CA-09)")
    void descuentaBloqueosParcialesYCompletos() {
        List<IntervaloDisponible> parcial = calcular(horario(10, 19, null, null),
                List.of(bloqueo("2030-01-07T12:00:00-05:00", "2030-01-07T13:00:00-05:00")),
                List.of(), 40, PARAMETROS, RELOJ);
        List<IntervaloDisponible> completo = calcular(horario(10, 19, null, null),
                List.of(bloqueo("2030-01-07T09:00:00-05:00", "2030-01-07T20:00:00-05:00")),
                List.of(), 40, PARAMETROS, RELOJ);

        assertThat(parcial).noneMatch(turno -> turno.inicio()
                .equals(instant("2030-01-07T12:00:00-05:00")));
        assertThat(parcial).anyMatch(turno -> turno.inicio()
                .equals(instant("2030-01-07T13:00:00-05:00")));
        assertThat(completo).isEmpty();
    }

    @Test
    @DisplayName("una cita parte el hueco en dos y las canceladas no se pasan como ocupación (CA-05)")
    void citaParteHuecoYAlCancelarlaSeLibera() {
        HorarioDiaResponse horario = horario(10, 19, null, null);
        List<IntervaloDisponible> ocupado = calcular(horario, List.of(),
                List.of(cita("2030-01-07T13:00:00-05:00", "2030-01-07T14:00:00-05:00")),
                40, PARAMETROS, RELOJ);
        List<IntervaloDisponible> liberado = calcular(horario, List.of(), List.of(),
                40, PARAMETROS, RELOJ);

        assertThat(ocupado).noneMatch(turno -> turno.inicio()
                .equals(instant("2030-01-07T13:00:00-05:00")));
        assertThat(liberado).anyMatch(turno -> turno.inicio()
                .equals(instant("2030-01-07T13:20:00-05:00")));
    }

    @Test
    @DisplayName("un domingo sin horario queda cerrado (RN-01)")
    void domingoSinHorarioNoOfreceTurnos() {
        LocalDate domingo = FECHA.plusDays(6);

        assertThat(calcular(null, List.of(), List.of(), 40, PARAMETROS, RELOJ)).isEmpty();
        assertThat(calculador.calcular(domingo, BARBERO, null, List.of(), List.of(),
                40, PARAMETROS, RELOJ)).isEmpty();
    }

    @Test
    @DisplayName("acepta un horario existente para domingo (día ISO 7)")
    void domingoConHorarioExistentePuedeOfrecerTurnos() {
        LocalDate domingo = FECHA.plusDays(6);
        HorarioDiaResponse horarioDomingo = new HorarioDiaResponse(UUID.randomUUID(), BARBERO, 7,
                LocalTime.of(10, 0), LocalTime.of(19, 0), null, null);

        List<IntervaloDisponible> turnos = calculador.calcular(domingo, BARBERO,
                horarioDomingo, List.of(), List.of(), 40, PARAMETROS, RELOJ);

        assertThat(turnos).isNotEmpty();
        assertThat(turnos.getFirst().inicio())
                .isEqualTo(instant("2030-01-13T10:00:00-05:00"));
    }

    @Test
    @DisplayName("no genera turnos cuando el servicio no cabe en ningún hueco")
    void servicioMasLargoQueJornadaNoOfreceTurnos() {
        assertThat(calcular(horario(10, 19, null, null), List.of(), List.of(),
                600, PARAMETROS, RELOJ)).isEmpty();
    }

    @Test
    @DisplayName("aplica el margen antes y después de las citas (RN-18)")
    void aplicaMargenEntreCitas() {
        ParametrosDisponibilidadResponse conMargen = new ParametrosDisponibilidadResponse(24, 7, 40, 20);
        List<IntervaloDisponible> turnos = calcular(horario(10, 19, null, null), List.of(),
                List.of(cita("2030-01-07T13:00:00-05:00", "2030-01-07T14:00:00-05:00")),
                40, conMargen, RELOJ);

        assertThat(turnos).noneMatch(turno -> turno.inicio()
                .equals(instant("2030-01-07T12:40:00-05:00")));
        assertThat(turnos).noneMatch(turno -> turno.inicio()
                .equals(instant("2030-01-07T14:00:00-05:00")));
        assertThat(turnos).anyMatch(turno -> turno.inicio()
                .equals(instant("2030-01-07T14:20:00-05:00")));
    }

    @Test
    @DisplayName("un turno de la mañana no mueve turnos ya posibles por la tarde (CA-08)")
    void calcularNoReubicaTurnosDeLaTarde() {
        HorarioDiaResponse horario = horario(10, 19, null, null);
        List<IntervaloDisponible> antes = calcular(horario, List.of(), List.of(),
                40, PARAMETROS, RELOJ);
        List<IntervaloDisponible> despuesDeAgendarManana = calcular(horario, List.of(),
                List.of(cita("2030-01-07T10:00:00-05:00", "2030-01-07T10:40:00-05:00")),
                40, PARAMETROS, RELOJ);

        assertThat(despuesDeAgendarManana.stream().filter(turno -> turno.inicio()
                .isAfter(instant("2030-01-07T14:00:00-05:00"))).toList())
                .containsExactlyElementsOf(antes.stream().filter(turno -> turno.inicio()
                        .isAfter(instant("2030-01-07T14:00:00-05:00"))).toList());
    }

    @Test
    @DisplayName("ignora bloqueos y citas de otros barberos al calcular la jornada")
    void ignoraIntervalosDeOtroBarbero() {
        List<IntervaloDisponible> turnos = calcular(horario(10, 19, null, null),
                List.of(new BloqueoResponse(UUID.randomUUID(), OTRO_BARBERO,
                        instant("2030-01-07T10:00:00-05:00"), instant("2030-01-07T19:00:00-05:00"))),
                List.of(new IntervaloOcupadoResponse(OTRO_BARBERO,
                        instant("2030-01-07T10:00:00-05:00"), instant("2030-01-07T19:00:00-05:00"))),
                40, PARAMETROS, RELOJ);

        assertThat(turnos).isNotEmpty();
    }

    private List<IntervaloDisponible> calcular(HorarioDiaResponse horario,
                                                List<BloqueoResponse> bloqueos,
                                                List<IntervaloOcupadoResponse> citas,
                                                int duracion,
                                                ParametrosDisponibilidadResponse parametros,
                                                Clock reloj) {
        return calculador.calcular(FECHA, BARBERO, horario, bloqueos, citas,
                duracion, parametros, reloj);
    }

    private static HorarioDiaResponse horario(int apertura, int cierre,
                                               LocalTime almuerzoInicio,
                                               LocalTime almuerzoFin) {
        return new HorarioDiaResponse(UUID.randomUUID(), BARBERO, 1,
                LocalTime.of(apertura, 0), LocalTime.of(cierre, 0), almuerzoInicio, almuerzoFin);
    }

    private static BloqueoResponse bloqueo(String inicio, String fin) {
        return new BloqueoResponse(UUID.randomUUID(), BARBERO, instant(inicio), instant(fin));
    }

    private static IntervaloOcupadoResponse cita(String inicio, String fin) {
        return new IntervaloOcupadoResponse(BARBERO, instant(inicio), instant(fin));
    }

    private static Instant instant(String fechaHora) {
        return java.time.OffsetDateTime.parse(fechaHora).toInstant();
    }
}
