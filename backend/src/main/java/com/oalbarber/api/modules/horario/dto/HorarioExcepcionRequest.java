package com.oalbarber.api.modules.horario.dto;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Datos de una fecha especial. Si {@code cerrado} es verdadero, las horas
 * deben venir nulas; si es falso, la jornada y el almuerzo siguen las mismas
 * reglas que la semana (RN-01 y RN-02).
 */
@Schema(name = "HorarioExcepcionRequest",
        description = "Horario especial de una fecha concreta")
public record HorarioExcepcionRequest(

        @Schema(description = "Verdadero si la fecha no atiende", example = "false")
        boolean cerrado,

        @Schema(description = "Hora de inicio de la jornada", example = "11:00")
        @JsonFormat(pattern = "HH:mm")
        LocalTime horaInicio,

        @Schema(description = "Hora de fin de la jornada", example = "17:00")
        @JsonFormat(pattern = "HH:mm")
        LocalTime horaFin,

        @Schema(description = "Inicio del almuerzo; nulo si no hay", example = "13:00")
        @JsonFormat(pattern = "HH:mm")
        LocalTime almuerzoInicio,

        @Schema(description = "Fin del almuerzo; nulo si no hay", example = "14:00")
        @JsonFormat(pattern = "HH:mm")
        LocalTime almuerzoFin) {
}
