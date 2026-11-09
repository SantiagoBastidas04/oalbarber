package com.oalbarber.api.modules.usuario.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "UsuarioRequest", description = "Datos para crear un usuario")
public class UsuarioRequest {

    @Schema(description = "Nombre completo", example = "Edwin Ordoñez", maxLength = 100)
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
    private String nombre;

    @Schema(description = "Celular en formato internacional", example = "+573001234567", maxLength = 20)
    @NotBlank(message = "El celular es obligatorio")
    @Size(max = 20, message = "El celular no puede superar 20 caracteres")
    @Pattern(regexp = "^\\+?[1-9]\\d{7,14}$",
            message = "Usa el formato internacional, por ejemplo +573001234567")
    private String celular;

    @Schema(description = "Contraseña en texto plano; el servicio la cifra",
            example = "ClaveSegura123", minLength = 8, maxLength = 72,
            accessMode = Schema.AccessMode.WRITE_ONLY)
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
    private String contrasena;
}
