package com.oalbarber.api.modules.auth.service;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Escribe la cookie del comprobante de refresco.
 *
 * <p>{@code HttpOnly}: el JavaScript del navegador no puede leerla, solo se
 * envía sola con cada petición. {@code path=/api/auth}: el navegador solo la
 * manda a los endpoints de sesión, no a todos. {@code SameSite=Lax}: no viaja
 * en peticiones hechas desde otro sitio web.</p>
 */
@Component
public class CookieSesion {

    public static final String NOMBRE = "oalbarber_refresco";

    private final long diasVigencia;

    public CookieSesion(@Value("${app.auth.refresh-dias}") long diasVigencia) {
        this.diasVigencia = diasVigencia;
    }

    /** Coloca la cookie con un comprobante válido. */
    public void colocar(HttpServletResponse respuesta, String valor) {
        respuesta.addHeader(HttpHeaders.SET_COOKIE, construir(valor, Duration.ofDays(diasVigencia)));
    }

    /** La cookie de borrado, para respuestas que la llevan en sus cabeceras. */
    public ResponseCookie borrada() {
        return ResponseCookie.from(NOMBRE, "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/api/auth")
                .maxAge(Duration.ZERO)
                .build();
    }

    private static String construir(String valor, Duration vida) {
        // secure=false porque en desarrollo corremos por http://localhost.
        // En producción, detrás de HTTPS, cambiarlo a true.
        return ResponseCookie.from(NOMBRE, valor)
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/api/auth")
                .maxAge(vida)
                .build()
                .toString();
    }
}
