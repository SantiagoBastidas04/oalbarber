package com.oalbarber.api.modules.horario.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Reemplazo completo de la semana de un barbero.
 *
 * <p>Los días que no vengan en la lista quedan cerrados; una lista vacía
 * deja la semana cerrada.</p>
 */
@Schema(name = "HorarioSemanaRequest", description = "Semana completa de horarios de un barbero")
public record HorarioSemanaRequest(

        @Schema(description = "Días configurados; los ausentes quedan cerrados")
        List<HorarioDiaRequest> dias) {
}
