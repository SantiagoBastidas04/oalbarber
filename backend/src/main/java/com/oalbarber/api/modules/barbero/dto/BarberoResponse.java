package com.oalbarber.api.modules.barbero.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

/** Perfil de barbero con nombre de cuenta no sensible y datos de presentación. */
@Schema(name = "BarberoResponse", description = "Perfil profesional asociado a una cuenta existente")
public record BarberoResponse(

        @Schema(description = "Identificador del perfil")
        UUID id,

        @Schema(description = "Identificador de la cuenta existente")
        UUID usuarioId,

        @Schema(description = "Nombre de la cuenta asociada")
        String nombre,

        @Schema(description = "Nombre público del perfil")
        String nombrePublico,

        @Schema(description = "Descripción pública del perfil")
        String descripcion,

        @Schema(description = "Nombre del archivo estático de foto")
        String foto,

        @Schema(description = "Estado del perfil")
        boolean activo) {
}
