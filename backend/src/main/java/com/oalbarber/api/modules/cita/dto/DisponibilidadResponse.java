package com.oalbarber.api.modules.cita.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

/** Turno ofrecido para un servicio y barbero disponibles (RN-05 a RN-10). */
@Schema(name = "DisponibilidadResponse", description = "Horario disponible para reservar")
public record DisponibilidadResponse(
        @Schema(description = "Inicio del turno con offset de America/Bogota",
                example = "2026-11-26T10:00:00-05:00")
        OffsetDateTime inicio,

        @Schema(description = "Fin del turno, exactamente según la duración del servicio",
                example = "2026-11-26T10:40:00-05:00")
        OffsetDateTime fin,

        @Schema(description = "Identificador UUID del barbero asignado")
        UUID barberoId) {
}
