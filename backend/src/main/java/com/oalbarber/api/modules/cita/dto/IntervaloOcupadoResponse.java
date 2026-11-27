package com.oalbarber.api.modules.cita.dto;

import java.time.Instant;
import java.util.UUID;

/** Intervalo de una cita vigente para restarlo del horario (RN-06 y RN-10). */
public record IntervaloOcupadoResponse(UUID barberoId, Instant inicio, Instant fin) {
}
