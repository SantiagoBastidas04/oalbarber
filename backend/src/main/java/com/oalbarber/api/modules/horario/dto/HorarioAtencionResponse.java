package com.oalbarber.api.modules.horario.dto;

import java.time.LocalTime;
import java.util.UUID;

/** Horario del dia disponible para que cita aplique las reglas RN-01 y RN-02. */
public record HorarioAtencionResponse(UUID barberoId, short diaSemana, LocalTime horaInicio,
                                      LocalTime horaFin, LocalTime inicioAlmuerzo,
                                      LocalTime finAlmuerzo) {
}
