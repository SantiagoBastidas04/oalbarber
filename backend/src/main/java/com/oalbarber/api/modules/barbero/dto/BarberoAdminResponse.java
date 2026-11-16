package com.oalbarber.api.modules.barbero.dto;

import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

/** Perfil ampliado para el panel administrativo; no se expone en la API pública. */
@Schema(name = "BarberoAdminResponse", description = "Perfil y datos de cuenta para administradores")
public record BarberoAdminResponse(

        @Schema(description = "Identificador del perfil")
        UUID id,

        @Schema(description = "Identificador de la cuenta asociada")
        UUID usuarioId,

        @Schema(description = "Nombre de la cuenta")
        String nombre,

        @Schema(description = "Celular en formato internacional")
        String celular,

        @Schema(description = "Nombre visible público")
        String nombrePublico,

        @Schema(description = "Descripción pública del perfil")
        String descripcion,

        @Schema(description = "Nombre de archivo estático de la foto")
        String foto,

        @Schema(description = "Estado del perfil")
        boolean activo,

        @Schema(description = "Identificadores de los servicios asignados al perfil")
        List<UUID> servicioIds) {
}
