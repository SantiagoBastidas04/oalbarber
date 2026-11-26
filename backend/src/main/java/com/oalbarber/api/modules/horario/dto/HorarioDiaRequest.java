package com.oalbarber.api.modules.horario.dto;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Un día de la semana dentro del reemplazo completo del horario.
 * Las horas viajan como texto {@code HH:mm}; el almuerzo es opcional.
 */
@Schema(name = "HorarioDiaRequest", description = "Horario de atención de un día (1 = lunes ... 7 = domingo)")
public record HorarioDiaRequest(

        @Schema(description = "Día ISO de la semana (1 = lunes ... 6 = sábado)", example = "1")
        Integer diaSemana,

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
