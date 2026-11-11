package com.oalbarber.api.modules.auth.controller;

import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.oalbarber.api.common.exception.SesionInvalidaException;
import com.oalbarber.api.modules.auth.dto.AuthResponse;
import com.oalbarber.api.modules.auth.dto.LoginRequest;
import com.oalbarber.api.modules.auth.service.AuthServicio;
import com.oalbarber.api.modules.auth.service.CookieSesion;
import com.oalbarber.api.modules.usuario.dto.UsuarioRequest;
import com.oalbarber.api.modules.usuario.dto.UsuarioResponse;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
/**
 * Endpoints de sesión. El comprobante de refresco viaja siempre en la cookie
 * {@code oalbarber_refresco}: el cuerpo de las respuestas nunca lo incluye.
 */
@Tag(name = "Autenticación", description = "Login, renovación de sesión y cierre")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthServicio authServicio;
    private final CookieSesion cookieSesion;

    @Operation(summary = "Registrar un cliente y abrir su sesión",
            description = "Crea la cuenta con rol CLIENTE y devuelve el pase corto; el "
                    + "comprobante viaja en la cookie. El usuario ya queda con sesión iniciada.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cuenta creada con sesión iniciada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "El celular ya está registrado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/registro")
    public AuthResponse registrar(@Valid @RequestBody UsuarioRequest solicitud,
                                  HttpServletRequest peticion,
                                  HttpServletResponse respuesta) {
        AuthServicio.SesionAbierta sesion = authServicio.registrar(
                solicitud, peticion.getHeader("User-Agent"), peticion.getRemoteAddr());
        cookieSesion.colocar(respuesta, sesion.comprobante());
        return sesion.respuesta();
    }

    @Operation(summary = "Iniciar sesión",
            description = "Devuelve el pase corto y coloca la cookie del comprobante.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sesión iniciada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Credenciales incorrectas",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/login")
    public AuthResponse ingresar(@Valid @RequestBody LoginRequest solicitud,
                                 HttpServletRequest peticion,
                                 HttpServletResponse respuesta) {
        AuthServicio.SesionAbierta sesion = authServicio.ingresar(
                solicitud.getCelular(), solicitud.getContrasena(),
                peticion.getHeader("User-Agent"), peticion.getRemoteAddr());
        cookieSesion.colocar(respuesta, sesion.comprobante());
        return sesion.respuesta();
    }

    @Operation(summary = "Renovar el pase corto",
            description = "Usa la cookie del comprobante. Rota el comprobante: la respuesta "
                    + "trae un pase nuevo y la cookie queda reemplazada.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pase renovado"),
            @ApiResponse(responseCode = "401", description = "Sesión vencida, revocada o reutilizada",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/refresh")
    public AuthResponse refrescar(
            @CookieValue(name = CookieSesion.NOMBRE, required = false) String comprobante,
            HttpServletRequest peticion,
            HttpServletResponse respuesta) {
        if (comprobante == null || comprobante.isBlank()) {
            throw new SesionInvalidaException("SESION_DESCONOCIDA",
                    "No hay sesión activa en este navegador. Inicia sesión de nuevo.");
        }
        AuthServicio.SesionAbierta sesion = authServicio.refrescar(
                comprobante, peticion.getHeader("User-Agent"), peticion.getRemoteAddr());
        cookieSesion.colocar(respuesta, sesion.comprobante());
        return sesion.respuesta();
    }

    @Operation(summary = "Cerrar sesión",
            description = "Revoca el comprobante en la base y borra la cookie. "
                    + "No requiere pase válido: cerrar una sesión vencida debe poder.")
    @ApiResponses(@ApiResponse(responseCode = "204", description = "Sesión cerrada"))
    @PostMapping("/logout")
    public ResponseEntity<Void> salir(
            @CookieValue(name = CookieSesion.NOMBRE, required = false) String comprobante) {
        if (comprobante != null && !comprobante.isBlank()) {
            authServicio.cerrarSesion(comprobante);
        }
        // La cookie de borrado va en la cabecera de la respuesta: con
        // @ResponseStatus(204) el servidor la descartaba.
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookieSesion.borrada().toString())
                .build();
    }

    @Operation(summary = "Quién soy",
            description = "Devuelve los datos del usuario del pase Bearer actual.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario autenticado"),
            @ApiResponse(responseCode = "401", description = "Pase ausente, inválido o vencido",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/yo")
    public UsuarioResponse yo(@AuthenticationPrincipal Jwt pase) {
        UUID usuarioId = UUID.fromString(pase.getSubject());
        return authServicio.yo(usuarioId);
    }
}
