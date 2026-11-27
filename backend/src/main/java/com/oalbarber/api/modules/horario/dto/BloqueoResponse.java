package com.oalbarber.api.modules.horario.dto;

import java.time.Instant;
import java.util.UUID;

/** Bloqueo de agenda transformado a DTO para otros modulos. */
public record BloqueoResponse(UUID id, UUID barberoId, Instant inicio, Instant fin) {
}
