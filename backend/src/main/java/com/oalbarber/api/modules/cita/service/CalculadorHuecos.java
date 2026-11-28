package com.oalbarber.api.modules.cita.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import com.oalbarber.api.modules.cita.dto.IntervaloOcupadoResponse;
import com.oalbarber.api.modules.horario.dto.BloqueoResponse;
import com.oalbarber.api.modules.horario.dto.HorarioDiaResponse;
import com.oalbarber.api.modules.horario.dto.ParametrosDisponibilidadResponse;

/**
 * Calcula turnos a partir de una jornada y sus intervalos ocupados (RN-05 a RN-10).
 * Es una clase pura: no conoce Spring, HTTP ni el contexto de seguridad.
 */
public final class CalculadorHuecos {

    private static final ZoneId ZONA_NEGOCIO = ZoneId.of("America/Bogota");

    /**
     * Obtiene los inicios posibles dentro de la jornada, descontando almuerzo, bloqueos y citas.
     * Los turnos se cuentan desde el inicio de cada hueco, y su fin puede coincidir con el cierre
     * (RN-06 y RN-07). La anticipación se mide como duración exacta desde el reloj recibido (RN-08).
     *
     * @param fecha fecha local de la jornada
     * @param barberoId identificador del barbero al que pertenecen los intervalos
     * @param horario horario de atención del día; {@code null} representa día cerrado
     * @param bloqueos bloqueos del día, posiblemente de varios barberos
     * @param citas citas vigentes del día, posiblemente de varios barberos
     * @param duracionServicioMin duración actual del servicio
     * @param parametros valores configurables de RN-18
     * @param reloj reloj inyectado para aplicar las ventanas de anticipación
     * @return turnos válidos ordenados por inicio
     */
    public List<IntervaloDisponible> calcular(
            LocalDate fecha,
            UUID barberoId,
            HorarioDiaResponse horario,
            Collection<BloqueoResponse> bloqueos,
            Collection<IntervaloOcupadoResponse> citas,
            int duracionServicioMin,
            ParametrosDisponibilidadResponse parametros,
            Clock reloj) {
        if (horario == null || horario.diaSemana() != fecha.getDayOfWeek().getValue()) {
            return List.of();
        }
        if (duracionServicioMin <= 0 || parametros.intervaloTurnosMinutos() <= 0) {
            throw new IllegalArgumentException("La duración y el intervalo deben ser mayores que cero.");
        }

        Instant inicioJornada = fecha.atTime(horario.horaInicio()).atZone(ZONA_NEGOCIO).toInstant();
        Instant finJornada = fecha.atTime(horario.horaFin()).atZone(ZONA_NEGOCIO).toInstant();
        List<Intervalo> ocupados = new ArrayList<>();

        if (horario.almuerzoInicio() != null && horario.almuerzoFin() != null) {
            agregarSiCruza(ocupados,
                    fecha.atTime(horario.almuerzoInicio()).atZone(ZONA_NEGOCIO).toInstant(),
                    fecha.atTime(horario.almuerzoFin()).atZone(ZONA_NEGOCIO).toInstant(),
                    inicioJornada, finJornada);
        }
        if (bloqueos != null) {
            bloqueos.stream().filter(bloqueo -> barberoId.equals(bloqueo.barberoId()))
                    .forEach(bloqueo -> agregarSiCruza(ocupados, bloqueo.inicio(), bloqueo.fin(),
                            inicioJornada, finJornada));
        }

        Duration margen = Duration.ofMinutes(parametros.margenEntreCitasMinutos());
        if (citas != null) {
            citas.stream().filter(cita -> barberoId.equals(cita.barberoId()))
                    .forEach(cita -> agregarSiCruza(ocupados,
                            cita.inicio().minus(margen), cita.fin().plus(margen),
                            inicioJornada, finJornada));
        }

        List<Intervalo> huecos = restar(inicioJornada, finJornada, ocupados);
        Instant ahora = reloj.instant();
        Instant inicioMinimo = ahora.plus(Duration.ofHours(parametros.anticipacionMinimaHoras()));
        Instant inicioMaximo = ahora.plus(Duration.ofDays(parametros.maximoDiasAdelante()));
        Duration duracion = Duration.ofMinutes(duracionServicioMin);
        Duration paso = Duration.ofMinutes(parametros.intervaloTurnosMinutos());
        List<IntervaloDisponible> resultado = new ArrayList<>();

        for (Intervalo hueco : huecos) {
            for (Instant inicio = hueco.inicio(); !inicio.plus(duracion).isAfter(hueco.fin());
                 inicio = inicio.plus(paso)) {
                if (!inicio.isBefore(inicioMinimo) && !inicio.isAfter(inicioMaximo)) {
                    resultado.add(new IntervaloDisponible(inicio, inicio.plus(duracion), barberoId));
                }
            }
        }
        return List.copyOf(resultado);
    }

    private static void agregarSiCruza(List<Intervalo> ocupados, Instant inicio, Instant fin,
                                       Instant inicioJornada, Instant finJornada) {
        Instant inicioRecortado = inicio.isAfter(inicioJornada) ? inicio : inicioJornada;
        Instant finRecortado = fin.isBefore(finJornada) ? fin : finJornada;
        if (inicioRecortado.isBefore(finRecortado)) {
            ocupados.add(new Intervalo(inicioRecortado, finRecortado));
        }
    }

    private static List<Intervalo> restar(Instant inicio, Instant fin, List<Intervalo> ocupados) {
        List<Intervalo> ordenados = ocupados.stream()
                .sorted(Comparator.comparing(Intervalo::inicio).thenComparing(Intervalo::fin))
                .toList();
        List<Intervalo> huecos = new ArrayList<>();
        Instant cursor = inicio;
        for (Intervalo ocupado : ordenados) {
            if (ocupado.inicio().isAfter(cursor)) {
                huecos.add(new Intervalo(cursor, ocupado.inicio()));
            }
            if (ocupado.fin().isAfter(cursor)) {
                cursor = ocupado.fin();
            }
        }
        if (cursor.isBefore(fin)) {
            huecos.add(new Intervalo(cursor, fin));
        }
        return huecos;
    }

    private record Intervalo(Instant inicio, Instant fin) { }

    /** Turno calculado; el mapeo al contrato HTTP pertenece a DisponibilidadService. */
    public record IntervaloDisponible(Instant inicio, Instant fin, UUID barberoId) { }
}
