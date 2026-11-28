package com.oalbarber.api.modules.horario.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oalbarber.api.common.exception.RecursoNoEncontradoException;
import com.oalbarber.api.common.exception.ValidacionException;
import com.oalbarber.api.modules.barbero.repository.BarberoRepository;
import com.oalbarber.api.modules.horario.domain.HorarioAtencion;
import com.oalbarber.api.modules.horario.domain.HorarioExcepcion;
import com.oalbarber.api.modules.horario.domain.Bloqueo;
import com.oalbarber.api.modules.horario.domain.Configuracion;
import com.oalbarber.api.modules.horario.dto.BloqueoResponse;
import com.oalbarber.api.modules.horario.dto.HorarioDiaRequest;
import com.oalbarber.api.modules.horario.dto.HorarioDiaResponse;
import com.oalbarber.api.modules.horario.dto.HorarioExcepcionRequest;
import com.oalbarber.api.modules.horario.dto.HorarioExcepcionResponse;
import com.oalbarber.api.modules.horario.dto.HorarioSemanaRequest;
import com.oalbarber.api.modules.horario.dto.ParametrosDisponibilidadResponse;
import com.oalbarber.api.modules.horario.repository.HorarioAtencionRepository;
import com.oalbarber.api.modules.horario.repository.HorarioExcepcionRepository;
import com.oalbarber.api.modules.horario.repository.BloqueoRepository;
import com.oalbarber.api.modules.horario.repository.ConfiguracionRepository;

import lombok.RequiredArgsConstructor;

/**
 * Reglas de aplicación del horario de atención (RF-11, RN-01 y RN-02).
 *
 * <p>Se guarda una jornada continua por día con un almuerzo opcional. Los días
 * que no se envían quedan cerrados. Además, una fecha concreta puede tener su
 * propia jornada o quedar cerrada, sin tocar la semana habitual. El intervalo
 * de turnos no vive aquí: es una regla global de {@code configuracion}
 * (H-18, RN-07).</p>
 */
@Service
@RequiredArgsConstructor
public class HorarioService {

    private static final LocalTime APERTURA = LocalTime.of(10, 0);
    private static final LocalTime CIERRE = LocalTime.of(19, 0);
    private static final int DOMINGO = 7;

    private final HorarioAtencionRepository horarioRepository;
    private final HorarioExcepcionRepository excepcionRepository;
    private final BarberoRepository barberoRepository;
    private final BloqueoRepository bloqueoRepository;
    private final ConfiguracionRepository configuracionRepository;

    /**
     * Devuelve el horario de un barbero o, si no se indica, el de todos
     * (solo para administradores). Un barbero solo puede consultar el suyo.
     */
    @Transactional(readOnly = true)
    public List<HorarioDiaResponse> listar(UUID barberoId, boolean administrador, UUID usuarioId) {
        if (administrador) {
            if (barberoId == null) {
                return horarioRepository.findAllByOrderByBarberoIdAscDiaSemanaAsc().stream()
                        .map(this::aResponse)
                        .toList();
            }
            exigirBarberoExistente(barberoId);
            return diasDe(barberoId);
        }

        UUID propio = barberoPropio(usuarioId);
        if (barberoId != null && !barberoId.equals(propio)) {
            throw new AccessDeniedException("Solo puedes consultar tu propio horario.");
        }
        return diasDe(propio);
    }

    /** Datos internos por lote para el cálculo público de disponibilidad. */
    @Transactional(readOnly = true)
    public List<HorarioDiaResponse> listarParaDisponibilidad(Collection<UUID> barberoIds,
                                                              int diaSemana) {
        if (barberoIds == null || barberoIds.isEmpty()) {
            return List.of();
        }
        return horarioRepository.findByBarberoIdInAndDiaSemana(barberoIds, (short) diaSemana)
                .stream().map(this::aResponse).toList();
    }

    /** Devuelve solo las excepciones de la fecha consultada, sin exponerlas por el endpoint público. */
    @Transactional(readOnly = true)
    public List<HorarioExcepcionResponse> listarExcepcionesParaDisponibilidad(
            Collection<UUID> barberoIds, LocalDate fecha) {
        if (barberoIds == null || barberoIds.isEmpty()) {
            return List.of();
        }
        return excepcionRepository.findByBarberoIdInAndFecha(barberoIds, fecha)
                .stream().map(this::aResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<BloqueoResponse> listarBloqueosParaDisponibilidad(
            Collection<UUID> barberoIds, Instant inicio, Instant fin) {
        if (barberoIds == null || barberoIds.isEmpty()) {
            return List.of();
        }
        return bloqueoRepository.buscarCruces(barberoIds, inicio, fin).stream()
                .map(bloqueo -> new BloqueoResponse(bloqueo.getId(), bloqueo.getBarberoId(),
                        bloqueo.getInicio(), bloqueo.getFin()))
                .toList();
    }

    @Transactional(readOnly = true)
    public ParametrosDisponibilidadResponse obtenerParametrosDisponibilidad() {
        Map<String, String> valores = configuracionRepository.findAll().stream()
                .collect(Collectors.toMap(Configuracion::getClave, Configuracion::getValor));
        return new ParametrosDisponibilidadResponse(
                parametro(valores, "anticipacion_minima_horas"),
                parametro(valores, "maximo_dias_adelante"),
                parametro(valores, "intervalo_turnos_minutos"),
                parametro(valores, "margen_entre_citas_minutos"));
    }

    private static int parametro(Map<String, String> valores, String clave) {
        String valor = valores.get(clave);
        if (valor == null) {
            throw new IllegalStateException("Falta el parámetro RN-18 " + clave + " en configuracion.");
        }
        return Integer.parseInt(valor);
    }

    /**
     * Reemplaza la semana completa del barbero en una sola transacción.
     *
     * @param barberoId perfil cuyo horario se reemplaza
     * @param solicitud días configurados; los ausentes quedan cerrados
     * @return días configurados después del reemplazo, ordenados por día
     * @throws RecursoNoEncontradoException si el perfil no existe
     * @throws ValidacionException con detalle por día si alguna fila no cumple las reglas
     */
    @Transactional
    public List<HorarioDiaResponse> reemplazar(UUID barberoId, HorarioSemanaRequest solicitud) {
        exigirBarberoExistente(barberoId);

        List<HorarioDiaRequest> dias = solicitud == null ? null : solicitud.dias();
        if (dias == null) {
            throw new ValidacionException("HORARIO_INVALIDO", "Revisa los horarios marcados.",
                    Map.of("dias",
                            "Envía la lista de días; para cerrar la semana envía una lista vacía."));
        }

        Map<Integer, Long> repeticiones = new LinkedHashMap<>();
        for (HorarioDiaRequest dia : dias) {
            if (dia != null && dia.diaSemana() != null) {
                repeticiones.merge(dia.diaSemana(), 1L, Long::sum);
            }
        }

        Map<String, String> errores = new LinkedHashMap<>();
        List<HorarioAtencion> filas = new ArrayList<>();

        for (int i = 0; i < dias.size(); i++) {
            HorarioDiaRequest dia = dias.get(i);
            String clave = dia != null && dia.diaSemana() != null
                    ? String.valueOf(dia.diaSemana())
                    : "fila " + (i + 1);

            if (dia == null) {
                errores.put(clave, "El día viene vacío.");
                continue;
            }
            if (dia.diaSemana() != null && repeticiones.get(dia.diaSemana()) > 1) {
                errores.putIfAbsent(clave, "El día está repetido en la lista.");
                continue;
            }

            String error = validarDia(dia);
            if (error != null) {
                errores.putIfAbsent(clave, error);
                continue;
            }
            filas.add(aEntidad(barberoId, dia));
        }

        if (!errores.isEmpty()) {
            throw new ValidacionException("HORARIO_INVALIDO", "Revisa los horarios marcados.", errores);
        }

        horarioRepository.deleteByBarberoId(barberoId);
        horarioRepository.flush();
        List<HorarioAtencion> guardados = horarioRepository.saveAll(filas);
        return guardados.stream()
                .sorted(Comparator.comparingInt(HorarioAtencion::getDiaSemana))
                .map(this::aResponse)
                .toList();
    }

    // ------------------------------------------------------------------
    // Fechas especiales
    // ------------------------------------------------------------------

    /** Lista las fechas especiales de un perfil; un barbero solo ve las suyas. */
    @Transactional(readOnly = true)
    public List<HorarioExcepcionResponse> listarExcepciones(UUID barberoId, boolean administrador,
                                                            UUID usuarioId) {
        exigirBarberoExistente(barberoId);
        if (!administrador) {
            UUID propio = barberoPropio(usuarioId);
            if (!barberoId.equals(propio)) {
                throw new AccessDeniedException(
                        "Solo puedes consultar tus propias fechas especiales.");
            }
        }
        return excepcionRepository.findByBarberoIdOrderByFechaAsc(barberoId).stream()
                .map(this::aResponse)
                .toList();
    }

    /**
     * Crea o actualiza la fecha especial indicada.
     *
     * @param barberoId perfil al que pertenece la fecha
     * @param fecha fecha concreta; si ya existe se actualiza
     * @param solicitud jornada especial o marca de cerrado
     * @return la fila guardada
     */
    @Transactional
    public HorarioExcepcionResponse guardarExcepcion(UUID barberoId, LocalDate fecha,
                                                     HorarioExcepcionRequest solicitud) {
        exigirBarberoExistente(barberoId);

        Map<String, String> errores = new LinkedHashMap<>();
        if (fecha == null) {
            errores.put("fecha", "Indica la fecha de la excepción.");
        }
        if (solicitud == null) {
            errores.put("horario", "Envía los datos de la fecha especial.");
        } else if (solicitud.cerrado()) {
            if (solicitud.horaInicio() != null || solicitud.horaFin() != null
                    || solicitud.almuerzoInicio() != null || solicitud.almuerzoFin() != null) {
                errores.put("horario", "Una fecha cerrada no lleva horas.");
            }
        } else {
            String error = validarHoras(solicitud.horaInicio(), solicitud.horaFin(),
                    solicitud.almuerzoInicio(), solicitud.almuerzoFin());
            if (error != null) {
                errores.put("horario", error);
            }
        }

        if (!errores.isEmpty()) {
            throw new ValidacionException("EXCEPCION_INVALIDA", "Revisa la fecha especial.", errores);
        }

        HorarioExcepcion fila = excepcionRepository.findByBarberoIdAndFecha(barberoId, fecha)
                .orElseGet(() -> HorarioExcepcion.builder()
                        .barberoId(barberoId)
                        .fecha(fecha)
                        .build());

        boolean cerrado = solicitud.cerrado();
        fila.actualizar(cerrado,
                cerrado ? null : solicitud.horaInicio(),
                cerrado ? null : solicitud.horaFin(),
                cerrado ? null : solicitud.almuerzoInicio(),
                cerrado ? null : solicitud.almuerzoFin());
        return aResponse(excepcionRepository.save(fila));
    }

    /** Elimina la fecha especial si existe; no falla cuando ya no está. */
    @Transactional
    public void eliminarExcepcion(UUID barberoId, LocalDate fecha) {
        exigirBarberoExistente(barberoId);
        excepcionRepository.deleteByBarberoIdAndFecha(barberoId, fecha);
    }

    // ------------------------------------------------------------------
    // Validación
    // ------------------------------------------------------------------

    private String validarDia(HorarioDiaRequest dia) {
        Integer numero = dia.diaSemana();
        if (numero == null) {
            return "Indica el día de la semana (1 = lunes ... 7 = domingo).";
        }
        if (numero < 1 || numero > 7) {
            return "El día debe estar entre 1 (lunes) y 7 (domingo).";
        }
        if (numero == DOMINGO) {
            return "El domingo está cerrado (RN-01).";
        }
        return validarHoras(dia.horaInicio(), dia.horaFin(),
                dia.almuerzoInicio(), dia.almuerzoFin());
    }

    /** Reglas comunes a la semana y a las fechas especiales. */
    private String validarHoras(LocalTime inicio, LocalTime fin,
                                LocalTime almuerzoInicio, LocalTime almuerzoFin) {
        if (inicio == null || fin == null) {
            return "Indica la hora de inicio y la hora de fin.";
        }
        if (!inicio.isBefore(fin)) {
            return "La hora de inicio debe ser menor que la de fin.";
        }
        if (inicio.isBefore(APERTURA) || fin.isAfter(CIERRE)) {
            return "La jornada debe estar entre las 10:00 y las 19:00 (RN-01).";
        }
        if ((almuerzoInicio == null) != (almuerzoFin == null)) {
            return "El almuerzo debe tener hora de inicio y de fin, o quedar vacío.";
        }
        if (almuerzoInicio == null) {
            return null;
        }
        if (!almuerzoInicio.isBefore(almuerzoFin)) {
            return "El almuerzo debe empezar antes de terminar.";
        }
        if (!almuerzoInicio.isAfter(inicio) || !almuerzoFin.isBefore(fin)) {
            return "El almuerzo debe quedar dentro de la jornada.";
        }
        return null;
    }

    // ------------------------------------------------------------------
    // Utilidades
    // ------------------------------------------------------------------

    private List<HorarioDiaResponse> diasDe(UUID barberoId) {
        return horarioRepository.findByBarberoIdOrderByDiaSemanaAsc(barberoId).stream()
                .map(this::aResponse)
                .toList();
    }

    private HorarioAtencion aEntidad(UUID barberoId, HorarioDiaRequest dia) {
        return HorarioAtencion.builder()
                .barberoId(barberoId)
                .diaSemana(dia.diaSemana().shortValue())
                .horaInicio(dia.horaInicio())
                .horaFin(dia.horaFin())
                .almuerzoInicio(dia.almuerzoInicio())
                .almuerzoFin(dia.almuerzoFin())
                .build();
    }

    private HorarioDiaResponse aResponse(HorarioAtencion horario) {
        return new HorarioDiaResponse(horario.getId(), horario.getBarberoId(),
                horario.getDiaSemana(), horario.getHoraInicio(), horario.getHoraFin(),
                horario.getAlmuerzoInicio(), horario.getAlmuerzoFin());
    }

    private HorarioExcepcionResponse aResponse(HorarioExcepcion excepcion) {
        return new HorarioExcepcionResponse(excepcion.getId(), excepcion.getBarberoId(),
                excepcion.getFecha(), excepcion.isCerrado(), excepcion.getHoraInicio(),
                excepcion.getHoraFin(), excepcion.getAlmuerzoInicio(),
                excepcion.getAlmuerzoFin());
    }

    private UUID barberoPropio(UUID usuarioId) {
        return barberoRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new AccessDeniedException(
                        "Tu cuenta no tiene un perfil de barbero asociado."))
                .getId();
    }

    private void exigirBarberoExistente(UUID barberoId) {
        if (!barberoRepository.existsById(barberoId)) {
            throw new RecursoNoEncontradoException("No existe el perfil de barbero indicado.");
        }
    }
}
