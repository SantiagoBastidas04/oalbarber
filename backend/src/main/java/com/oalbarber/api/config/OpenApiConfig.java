package com.oalbarber.api.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

/**
 * Documentación OpenAPI de la API.
 *
 * <p>Swagger UI queda en {@code /swagger-ui.html} y el JSON en {@code /v3/api-docs}. El esquema
 * {@code bearerAuth} es el que usa el botón "Authorize" para probar endpoints protegidos.</p>
 */
@Configuration
@OpenAPIDefinition(info = @Info(
        title = "OalBarber API",
        version = "v1",
        description = "Reservas de citas de la barbería. Fechas en ISO-8601 con offset -05:00. "
                + "Los errores usan ProblemDetail (RFC 7807) con un campo extra 'codigo'."))
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT")
public class OpenApiConfig {
}
