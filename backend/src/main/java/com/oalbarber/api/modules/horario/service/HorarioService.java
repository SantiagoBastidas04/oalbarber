package com.oalbarber.api.modules.horario.service;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oalbarber.api.modules.horario.domain.Bloqueo;
import com.oalbarber.api.modules.horario.domain.HorarioAtencion;
import com.oalbarber.api.modules.horario.dto.BloqueoResponse;
import com.oalbarber.api.modules.horario.dto.HorarioAtencionResponse;
import com.oalbarber.api.modules.horario.dto.ParametrosDisponibilidadResponse;
import com.oalbarber.api.modules.horario.repository.BloqueoRepository;
import com.oalbarber.api.modules.horario.repository.ConfiguracionRepository;
import com.oalbarber.api.modules.horario.repository.HorarioAtencionRepository;

/** Lectura de horarios, bloqueos y parametros requeridos por RN-05 a RN-08. */
@Service
public class HorarioService {

    private final HorarioAtencionRepository horarioRepository;
    private final BloqueoRepository bloqueoRepository;
    private final ConfiguracionRepository configuracionRepository;

    public HorarioService(HorarioAtencionRepository horarioRepository,
                          BloqueoRepository bloqueoRepository,
                          ConfiguracionRepository configuracionRepository) {
        this.horarioRepository = horarioRepository;
        this.bloqueoRepository = bloqueoRepository;
        this.configuracionRepository = configuracionRepository;
    }

    /** Busca horario del dia; la ausencia de fila representa dia cerrado (RN-01). */
    @Transactional(readOnly = true)
    public HorarioAtencionResponse obtenerHorario(UUID barberoId, short diaSemana) {
        return horarioRepository.findByBarberoIdAndDiaSemana(barberoId, diaSemana)
                .map(this::aRespuesta)
                .orElse(null);
    }

    /** Carga horarios de varios barberos en una consulta para evitar N+1 (RNF-07). */
    @Transactional(readOnly = true)
    public List<HorarioAtencionResponse> listarHorarios(Collection<UUID> barberoIds,
                                                       short diaSemana) {
        if (barberoIds.isEmpty()) {
            return List.of();
        }
        return horarioRepository.findByBarberoIdInAndDiaSemana(barberoIds, diaSemana).stream()
                .map(this::aRespuesta)
                .toList();
    }

    /** Lee por lote los bloqueos que se cruzan con la fecha solicitada (RN-12). */
    @Transactional(readOnly = true)
    public List<BloqueoResponse> listarBloqueos(Collection<UUID> barberoIds,
                                                Instant inicio, Instant fin) {
        if (barberoIds.isEmpty()) {
            return List.of();
        }
        return bloqueoRepository.buscarCruces(barberoIds, inicio, fin).stream()
                .map(this::aRespuesta)
                .toList();
    }

    /** Lee valores de agenda de RN-18 sin fijarlos en el codigo. */
    @Transactional(readOnly = true)
    public ParametrosDisponibilidadResponse obtenerParametrosDisponibilidad() {
        var valores = configuracionRepository.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(
                        configuracion -> configuracion.getClave(),
                        configuracion -> configuracion.getValor()));
        return new ParametrosDisponibilidadResponse(
                Integer.parseInt(valores.get("anticipacion_minima_horas")),
                Integer.parseInt(valores.get("maximo_dias_adelante")),
                Integer.parseInt(valores.get("intervalo_turnos_minutos")),
                Integer.parseInt(valores.get("margen_entre_citas_minutos")));
    }

    private HorarioAtencionResponse aRespuesta(HorarioAtencion horario) {
        return new HorarioAtencionResponse(horario.getBarberoId(), horario.getDiaSemana(),
                horario.getHoraInicio(), horario.getHoraFin(), horario.getInicioAlmuerzo(),
                horario.getFinAlmuerzo());
    }

    private BloqueoResponse aRespuesta(Bloqueo bloqueo) {
        return new BloqueoResponse(bloqueo.getId(), bloqueo.getBarberoId(),
                bloqueo.getInicio(), bloqueo.getFin());
    }
}
