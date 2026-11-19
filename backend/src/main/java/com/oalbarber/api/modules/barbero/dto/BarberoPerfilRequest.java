package com.oalbarber.api.modules.barbero.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Datos opcionales de perfil para agregar al administrador autenticado como barbero. */
@Schema(name = "BarberoPerfilRequest", description = "Datos propios del perfil profesional")
public record BarberoPerfilRequest(

        @Schema(description = "Nombre visible público", maxLength = 100)
        @Size(max = 100, message = "El nombre público no puede superar 100 caracteres")
        String nombrePublico,

        @Schema(description = "Descripción pública", maxLength = 500)
        @Size(max = 500, message = "La descripción no puede superar 500 caracteres")
        String descripcion,

        @Schema(description = "Nombre de archivo estático, sin rutas", example = "admin.webp",
                maxLength = 255)
        @Size(max = 255, message = "El nombre del archivo no puede superar 255 caracteres")
        @Pattern(regexp = "^(?:[a-z0-9-]+\\.(?:jpg|jpeg|png|webp))?$",
                message = "La foto debe ser un nombre de archivo .jpg, .jpeg, .png o .webp")
        String foto) {
}
