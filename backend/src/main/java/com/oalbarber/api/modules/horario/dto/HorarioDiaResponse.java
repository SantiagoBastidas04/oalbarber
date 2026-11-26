package com.oalbarber.api.modules.horario.dto;

import java.time.LocalTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;

/** Un día configurado del horario de un barbero. */
@Schema(name = "HorarioDiaResponse", description = "Horario de atención configurado para un día")
public record HorarioDiaResponse(

        @Schema(description = "Identificador de la fila")
        UUID id,

        @Schema(description = "Identificador del perfil de barbero")
        UUID barberoId,

        @Schema(description = "Día ISO de la semana (1 = lunes ... 7 = domingo)")
        int diaSemana,

        @Schema(description = "Hora de inicio de la jornada", example = "10:00")
        @JsonFormat(pattern = "HH:mm")
        LocalTime horaInicio,

        @Schema(description = "Hora de fin de la jornada", example = "19:00")
        @JsonFormat(pattern = "HH:mm")
        LocalTime horaFin,

        @Schema(description = "Inicio del almuerzo; nulo si no hay almuerzo", example = "13:00")
        @JsonFormat(pattern = "HH:mm")
        LocalTime almuerzoInicio,

        @Schema(description = "Fin del almuerzo; nulo si no hay almuerzo", example = "14:00")
        @JsonFormat(pattern = "HH:mm")
        LocalTime almuerzoFin) {
}
