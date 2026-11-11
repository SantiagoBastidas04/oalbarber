package com.oalbarber.api.modules.auth.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import com.oalbarber.api.common.exception.TokenInvalidoException;
import com.oalbarber.api.modules.usuario.domain.Usuario;

/**
 * Fabrica y valida el pase corto de sesión (JWT de acceso).
 *
 * <p>El pase lleva el id del usuario y su rol firmados con {@code JWT_SECRETO}.
 * Va vencido a los {@code JWT_MINUTOS_ACCESO} minutos: si se roba, poco puede
 * hacerse con él. La sesión de largo plazo vive en {@code sesion_refresco}.</p>
 */
@Component
public class TokenServicio {

    private final SecretKey claveFirma;
    private final int minutosAcceso;

    public TokenServicio(@Value("${app.auth.jwt-secreto}") String secreto,
                         @Value("${app.auth.jwt-minutos-acceso}") int minutosAcceso) {
        this.claveFirma = aClave(secreto);
        this.minutosAcceso = minutosAcceso;
    }

    
    public String firmarAcceso(Usuario usuario) {
        Instant ahora = Instant.now();

        JWSHeader encabezado = new JWSHeader.Builder(JWSAlgorithm.HS256)
                .type(JOSEObjectType.JWT)
                .build();

        JWTClaimsSet afirmaciones = new JWTClaimsSet.Builder()
                .subject(usuario.getId().toString())
                .issuer("oalbarber")
                // Identificador único del pase: dos pases emitidos en el mismo
                // segundo nunca quedan idénticos.
                .jwtID(UUID.randomUUID().toString())
                .issueTime(Date.from(ahora))
                .expirationTime(Date.from(ahora.plusSeconds(minutosAcceso * 60L)))
                .claim("rol", usuario.getRol().name())
                .claim("nombre", usuario.getNombre())
                .build();

        SignedJWT pase = new SignedJWT(encabezado, afirmaciones);
        try {
            pase.sign(new MACSigner(claveFirma));
        } catch (JOSEException e) {
            throw new IllegalStateException("No se pudo firmar el pase de acceso", e);
        }
        return pase.serialize();
    }

    /**
     * Lee un pase y confirma que la firma cuadra y que no está vencido.
     * Si algo falla, lanza {@link TokenInvalidoException}.
     */
    public PaseAcceso leer(String valor) {
        try {
            SignedJWT pase = SignedJWT.parse(valor);

            if (!pase.verify(new MACVerifier(claveFirma))) {
                throw new TokenInvalidoException("La firma del token no coincide.");
            }

            JWTClaimsSet afirmaciones = pase.getJWTClaimsSet();
            Date vencimiento = afirmaciones.getExpirationTime();
            if (vencimiento == null || vencimiento.toInstant().isBefore(Instant.now())) {
                throw new TokenInvalidoException("El pase de acceso ya está vencido.");
            }

            return new PaseAcceso(
                    UUID.fromString(afirmaciones.getSubject()),
                    afirmaciones.getStringClaim("rol"),
                    afirmaciones.getStringClaim("nombre"));
        } catch (TokenInvalidoException e) {
            throw e;
        } catch (Exception e) {
            throw new TokenInvalidoException("El token no se pudo leer o no sirve.");
        }
    }

    
    public record PaseAcceso(UUID usuarioId, String rol, String nombre) {}

    /**
     * El decodificador con el que Spring Security valida cada pase Bearer que
     * llega: verifica la firma con la misma llave y rechaza los vencidos.
     */
    public JwtDecoder decodificador() {
        return NimbusJwtDecoder.withSecretKey(claveFirma)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    
    private static SecretKey aClave(String secreto) {
        byte[] bytes;
        try {
            bytes = Base64.getUrlDecoder().decode(secreto);
        } catch (IllegalArgumentException e) {
            bytes = secreto.getBytes(StandardCharsets.UTF_8);
        }
        if (bytes.length < 32) {
            throw new IllegalStateException(
                    "JWT_SECRETO debe tener al menos 32 caracteres (256 bits) para HS256.");
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }
}
