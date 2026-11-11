package com.oalbarber.api.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import com.oalbarber.api.modules.auth.service.TokenServicio;

/**
 * Seguridad con JWT: el servidor no guarda sesiones (STATELESS); cada petición
 * trae su pase Bearer, que se valida con la llave del .env. Solo el health
 * check, la documentación, el registro y los tres endpoints de entrada a la
 * sesión son públicos; todo lo demás exige un pase válido.
 *
 * <p>Con {@code @EnableMethodSecurity} los endpoints pueden pedir un rol en su
 * propia anotación ({@code @PreAuthorize}); el rol llega en el claim `rol` del
 * pase y se traduce a la autoridad `ROLE_...` que Spring entiende.</p>
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Traduce el claim `rol` del pase a una autoridad `ROLE_<rol>`. Sin esto,
     * Spring Security solo miraría el claim `scope` (que no usamos) y ningún
     * `hasRole` funcionaría.
     */
    @Bean
    Converter<Jwt, AbstractAuthenticationToken> conversorDeRoles() {
        JwtAuthenticationConverter conversor = new JwtAuthenticationConverter();
        conversor.setJwtGrantedAuthoritiesConverter(jwt -> {
            List<GrantedAuthority> autoridades = new ArrayList<>();
            String rol = jwt.getClaimAsString("rol");
            if (rol != null && !rol.isBlank()) {
                autoridades.add(new SimpleGrantedAuthority("ROLE_" + rol));
            }
            return autoridades;
        });
        return conversor;
    }

    /** El mismo decodificador que usa {@link TokenServicio} para firmar. */
    @Bean
    JwtDecoder jwtDecoder(TokenServicio tokenServicio) {
        return tokenServicio.decodificador();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                // Sin sesión de servidor: no hay cookie JSESSIONID ni redirecciones a formulario.
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(autorizacion -> autorizacion
                        .requestMatchers("/actuator/health/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        // Entradas a la sesión: públicas porque aún no hay con qué autenticarse.
                        .requestMatchers(HttpMethod.POST, "/api/auth/registro", "/api/auth/login",
                                "/api/auth/refresh", "/api/auth/logout").permitAll()
                        .anyRequest().authenticated())
                // Cada petición con Authorization: Bearer ... se valida contra JwtDecoder;
                // el claim `rol` se convierte en autoridad ROLE_... (ver conversorDeRoles).
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(
                        jwt -> jwt.jwtAuthenticationConverter(conversorDeRoles())))
                // Sin httpBasic ni formularios: solo JWT.
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable);
        return http.build();
    }
}
