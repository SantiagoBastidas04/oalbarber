package com.oalbarber.api.modules.auth.dto;

import com.oalbarber.api.modules.usuario.dto.UsuarioResponse;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Respuesta de login y refresh: el pase corto para la memoria del navegador y
 * el usuario para pintar la interfaz. El comprobante de largo plazo viaja por
 * su cuenta en la cookie HttpOnly, nunca en este cuerpo.
 */
@Schema(name = "AuthResponse", description = "Sesión iniciada: pase corto y usuario")
public record AuthResponse(

        @Schema(description = "JWT de acceso (15 minutos); se guarda solo en memoria")
        String tokenAcceso,

        @Schema(description = "Datos del usuario autenticado")
        UsuarioResponse usuario) {
}
