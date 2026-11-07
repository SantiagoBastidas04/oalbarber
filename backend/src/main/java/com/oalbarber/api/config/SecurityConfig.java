package com.oalbarber.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración mínima de seguridad.
 *
 * <p>Deja público únicamente el health check de Actuator para que el {@code HEALTHCHECK} del
 * contenedor pueda consultarlo; cualquier otra ruta exige autenticación. Spring Security bloquea
 * todo por defecto, así que sin esta clase el contenedor nunca quedaría saludable.</p>
 */
@Configuration
public class SecurityConfig {

    // TODO: reemplazar al implementar el módulo auth (JWT)
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(autorizacion -> autorizacion
                        .requestMatchers("/actuator/health/**").permitAll()
                        .anyRequest().authenticated())
                .httpBasic(org.springframework.security.config.Customizer.withDefaults());
        return http.build();
    }
}
