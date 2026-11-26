package com.oalbarber.api.modules.horario.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;

/** Fecha especial configurada para un barbero. */
@Schema(name = "HorarioExcepcionResponse",
        description = "Horario especial de una fecha concreta")
public record HorarioExcepcionResponse(

        @Schema(description = "Identificador de la fila")
        UUID id,

        @Schema(description = "Identificador del perfil de barbero")
        UUID barberoId,

        @Schema(description = "Fecha especial en formato ISO", example = "2026-12-06")
        LocalDate fecha,

        @Schema(description = "Verdadero si la fecha no atiende", example = "false")
        boolean cerrado,

        @Schema(description = "Hora de inicio; nula si la fecha está cerrada", example = "11:00")
        @JsonFormat(pattern = "HH:mm")
        LocalTime horaInicio,

        @Schema(description = "Hora de fin; nula si la fecha está cerrada", example = "17:00")
        @JsonFormat(pattern = "HH:mm")
        LocalTime horaFin,

        @Schema(description = "Inicio del almuerzo; nulo si no hay", example = "13:00")
        @JsonFormat(pattern = "HH:mm")
        LocalTime almuerzoInicio,

        @Schema(description = "Fin del almuerzo; nulo si no hay", example = "14:00")
        @JsonFormat(pattern = "HH:mm")
        LocalTime almuerzoFin) {
}
