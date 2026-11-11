package com.oalbarber.api.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Datos para iniciar sesión. Usamos celular + contraseña, igual que el registro.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "LoginRequest", description = "Datos para iniciar sesión")
public class LoginRequest {

    @Schema(description = "Celular en formato internacional", example = "+573172392355")
    @NotBlank(message = "El celular es obligatorio")
    private String celular;

    @Schema(description = "Contraseña en texto plano",
            accessMode = Schema.AccessMode.WRITE_ONLY)
    @NotBlank(message = "La contraseña es obligatoria")
    private String contrasena;
}
