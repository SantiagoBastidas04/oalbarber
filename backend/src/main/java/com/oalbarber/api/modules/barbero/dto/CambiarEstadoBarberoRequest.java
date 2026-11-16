package com.oalbarber.api.modules.barbero.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/** Estado solicitado para el perfil del barbero; no cambia la cuenta de usuario. */
@Schema(name = "CambiarEstadoBarberoRequest", description = "Estado del perfil de barbero")
public record CambiarEstadoBarberoRequest(

        @Schema(description = "Estado activo del perfil", example = "false")
        @NotNull(message = "El estado es obligatorio")
        Boolean activo) {
}
