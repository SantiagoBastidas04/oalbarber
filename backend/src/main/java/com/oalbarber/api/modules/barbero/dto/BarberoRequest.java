package com.oalbarber.api.modules.barbero.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Datos de alta para un barbero; el rol siempre lo asigna el backend. */
@Schema(name = "BarberoRequest", description = "Datos de cuenta y perfil para crear un barbero")
public record BarberoRequest(

        @Schema(description = "Nombre de la cuenta", example = "Santiago Bastidas", maxLength = 100)
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        String nombre,

        @Schema(description = "Celular en formato internacional", example = "+573001234567", maxLength = 20)
        @NotBlank(message = "El celular es obligatorio")
        @Size(max = 20, message = "El celular no puede superar 20 caracteres")
        @Pattern(regexp = "^\\+?[1-9]\\d{7,14}$",
                message = "Usa el formato internacional, por ejemplo +573001234567")
        String celular,

        @Schema(description = "Contraseña inicial; se almacena cifrada", example = "ClaveSegura123",
                minLength = 8, maxLength = 72, accessMode = Schema.AccessMode.WRITE_ONLY)
        @NotBlank(message = "La contraseña inicial es obligatoria")
        @Size(min = 8, max = 72,
                message = "La contraseña debe tener entre 8 y 72 caracteres")
        String contrasena,

        @Schema(description = "Nombre visible público; no se usa el nombre de la cuenta", maxLength = 100)
        @Size(max = 100, message = "El nombre público no puede superar 100 caracteres")
        String nombrePublico,

        @Schema(description = "Descripción pública del perfil", maxLength = 500)
        @Size(max = 500, message = "La descripción no puede superar 500 caracteres")
        String descripcion,

        @Schema(description = "Nombre de archivo estático, sin rutas", example = "barbero-01.webp",
                maxLength = 255)
        @Size(max = 255, message = "El nombre del archivo no puede superar 255 caracteres")
        @Pattern(regexp = "^(?:[a-z0-9-]+\\.(?:jpg|jpeg|png|webp))?$",
                message = "La foto debe ser un nombre de archivo .jpg, .jpeg, .png o .webp")
        String foto) {
}
