package com.oalbarber.api.modules.barbero.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

/** Datos mínimos y no personales para elegir un perfil activo. */
@Schema(name = "BarberoPublicoResponse", description = "Perfil público de un barbero activo")
public record BarberoPublicoResponse(

        @Schema(description = "Identificador del perfil")
        UUID id,

        @Schema(description = "Nombre público, si fue configurado")
        String nombrePublico,

        @Schema(description = "Descripción pública del perfil")
        String descripcion,

        @Schema(description = "Nombre de archivo estático de la foto")
        String foto,

        @Schema(description = "Estado del perfil")
        boolean activo) {
}
