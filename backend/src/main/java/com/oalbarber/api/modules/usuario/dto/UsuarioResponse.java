package com.oalbarber.api.modules.usuario.dto;

import java.time.Instant;
import java.util.UUID;

import com.oalbarber.api.modules.usuario.domain.RolUsuario;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Usuario devuelto por la API. Nunca incluye la contraseña ni el hash.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "UsuarioResponse", description = "Usuario devuelto por la API")
public class UsuarioResponse {

    @Schema(description = "Identificador del usuario", example = "3f1b6c2e-9a4d-4c8b-8f7e-1a2b3c4d5e6f")
    private UUID id;

    @Schema(description = "Nombre completo", example = "Santiago Bastidas")
    private String nombre;

    @Schema(description = "Celular en formato internacional", example = "+573001234567")
    private String celular;

    @Schema(description = "Rol del usuario", example = "CLIENTE")
    private RolUsuario rol;

    @Schema(description = "Si la cuenta puede iniciar sesión", example = "true")
    private boolean activo;

    @Schema(description = "Fecha de creación", example = "2026-11-07T10:15:30-05:00")
    private Instant fechaCreacion;
}
