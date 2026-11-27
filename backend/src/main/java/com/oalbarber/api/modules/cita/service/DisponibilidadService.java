package com.oalbarber.api.modules.cita.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oalbarber.api.common.exception.ReglaNegocioException;
import com.oalbarber.api.modules.barbero.dto.BarberoPublicoResponse;
import com.oalbarber.api.modules.barbero.service.BarberoService;
import com.oalbarber.api.modules.cita.dto.DisponibilidadResponse;
import com.oalbarber.api.modules.cita.dto.IntervaloOcupadoResponse;
import com.oalbarber.api.modules.cita.service.CalculadorHuecos.IntervaloDisponible;
import com.oalbarber.api.modules.horario.dto.BloqueoResponse;
import com.oalbarber.api.modules.horario.dto.HorarioAtencionResponse;
import com.oalbarber.api.modules.horario.dto.ParametrosDisponibilidadResponse;
import com.oalbarber.api.modules.horario.service.HorarioService;
import com.oalbarber.api.modules.servicio.dto.ServicioResponse;
import com.oalbarber.api.modules.servicio.service.ServicioService;

/** Orquesta el cálculo de horarios de cita sin depender de HTTP (RN-05 a RN-10). */
@Service
public class DisponibilidadService {

    private static final ZoneId ZONA_NEGOCIO = ZoneId.of("America/Bogota");

    private final ServicioService servicioService;
    private final BarberoService barberoService;
    private final HorarioService horarioService;
    private final CitaService citaService;
    private final Clock reloj;
    private final CalculadorHuecos calculadorHuecos;

    public DisponibilidadService(ServicioService servicioService,
                                 BarberoService barberoService,
                                 HorarioService horarioService,
                                 CitaService citaService,
                                 Clock reloj) {
        this.servicioService = servicioService;
        this.barberoService = barberoService;
        this.horarioService = horarioService;
        this.citaService = citaService;
        this.reloj = reloj;
        this.calculadorHuecos = new CalculadorHuecos();
    }

    /**
     * Calcula horarios para un barbero o para cualquiera que ofrezca el servicio. Si dos perfiles
     * tienen el mismo inicio, asigna el turno al que tiene menos citas ese día y, en empate, al de
     * UUID menor (RN-05). El resultado es de solo lectura y no reubica citas existentes (RN-10).
     *
     * @param servicioId identificador del servicio activo
     * @param fecha fecha local consultada
     * @param barberoId perfil solicitado, o {@code null} para cualquiera disponible
     * @return horarios únicos ordenados por inicio, con offset America/Bogota
     * @throws com.oalbarber.api.common.exception.RecursoNoEncontradoException si el servicio no
     *         existe o está inactivo
     * @throws ReglaNegocioException si el barbero indicado no está activo o no ofrece el servicio
     */
    @Transactional(readOnly = true)
    public List<DisponibilidadResponse> calcularDisponibilidad(UUID servicioId,
                                                               LocalDate fecha,
                                                               UUID barberoId) {
        ServicioResponse servicio = servicioService.obtenerActivo(servicioId);
        List<BarberoPublicoResponse> candidatos = barberoService
                .listarActivosQueOfrecenServicio(servicioId);

        if (barberoId != null) {
            candidatos = candidatos.stream().filter(barbero -> barbero.id().equals(barberoId)).toList();
            if (candidatos.isEmpty()) {
                throw new ReglaNegocioException("BARBERO_NO_DISPONIBLE",
                        "El barbero indicado no existe, está inactivo o no ofrece el servicio seleccionado.");
            }
        }
        if (candidatos.isEmpty() || fecha.getDayOfWeek().getValue() == 7) {
            return List.of();
        }

        Collection<UUID> idsBarberos = candidatos.stream().map(BarberoPublicoResponse::id).toList();
        short diaSemana = (short) fecha.getDayOfWeek().getValue();
        Instant inicioDia = fecha.atStartOfDay(ZONA_NEGOCIO).toInstant();
        Instant finDia = fecha.plusDays(1).atStartOfDay(ZONA_NEGOCIO).toInstant();
        ParametrosDisponibilidadResponse parametros = horarioService.obtenerParametrosDisponibilidad();
        Duration margen = Duration.ofMinutes(parametros.margenEntreCitasMinutos());

        List<HorarioAtencionResponse> horarios = horarioService.listarHorarios(idsBarberos, diaSemana);
        List<BloqueoResponse> bloqueos = horarioService.listarBloqueos(idsBarberos, inicioDia, finDia);
        List<IntervaloOcupadoResponse> citas = citaService.listarOcupadas(
                idsBarberos, inicioDia.minus(margen), finDia.plus(margen));

        Map<UUID, HorarioAtencionResponse> horarioPorBarbero = new HashMap<>();
        horarios.forEach(horario -> horarioPorBarbero.put(horario.barberoId(), horario));
        Map<UUID, Long> citasPorBarbero = contarCitasDelDia(citas, inicioDia, finDia);
        List<BarberoPublicoResponse> priorizados = candidatos.stream()
                .sorted(Comparator.comparingLong((BarberoPublicoResponse barbero) ->
                                citasPorBarbero.getOrDefault(barbero.id(), 0L))
                        .thenComparing(BarberoPublicoResponse::id))
                .toList();

        Map<Instant, DisponibilidadResponse> porInicio = new TreeMap<>();
        for (BarberoPublicoResponse barbero : priorizados) {
            List<IntervaloDisponible> turnos = calculadorHuecos.calcular(fecha, barbero.id(),
                    horarioPorBarbero.get(barbero.id()), bloqueos, citas,
                    servicio.getDuracionMin(), parametros, reloj);
            for (IntervaloDisponible turno : turnos) {
                porInicio.putIfAbsent(turno.inicio(), aRespuesta(turno));
            }
        }
        return List.copyOf(porInicio.values());
    }

    private static Map<UUID, Long> contarCitasDelDia(List<IntervaloOcupadoResponse> citas,
                                                    Instant inicioDia, Instant finDia) {
        Map<UUID, Long> conteos = new HashMap<>();
        for (IntervaloOcupadoResponse cita : citas) {
            if (cita.inicio().isBefore(finDia) && cita.fin().isAfter(inicioDia)) {
                conteos.merge(cita.barberoId(), 1L, Long::sum);
            }
        }
        return conteos;
    }

    private static DisponibilidadResponse aRespuesta(IntervaloDisponible turno) {
        return new DisponibilidadResponse(
                turno.inicio().atZone(ZONA_NEGOCIO).toOffsetDateTime(),
                turno.fin().atZone(ZONA_NEGOCIO).toOffsetDateTime(),
                turno.barberoId());
    }
}
