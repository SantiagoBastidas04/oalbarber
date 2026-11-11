package com.oalbarber.api.modules.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.oalbarber.api.common.exception.SesionInvalidaException;
import com.oalbarber.api.modules.auth.domain.SesionRefresco;
import com.oalbarber.api.modules.auth.repository.SesionRefrescoRepository;
import com.oalbarber.api.modules.usuario.domain.Usuario;

/**
 * Administrador de las sesiones de refresco (el comprobante de largo plazo).
 *
 * <p>Abre sesiones por dispositivo, las rota en cada refresco, las cierra al
 * cerrar sesión y detecta reutilización: si llega un comprobante que ya fue
 * reemplazado o revocado, se asume robo y se cierran todas las sesiones del
 * usuario. El comprobante en claro nunca se guarda; solo su huella SHA-256.</p>
 */
@Service
public class SesionRefrescoServicio {

    /** 32 bytes de azar: 256 bits de entropía para el comprobante. */
    private static final int BYTES_TOKEN = 32;

    private final SesionRefrescoRepository sesiones;
    private final Duration vigencia;
    private final TransactionTemplate transaccionIndependiente;

    public SesionRefrescoServicio(SesionRefrescoRepository sesiones,
                                  @Value("${app.auth.refresh-dias}") long diasVigencia,
                                  PlatformTransactionManager gestorTransacciones) {
        this.sesiones = sesiones;
        this.vigencia = Duration.ofDays(diasVigencia);
        // El cierre de emergencia corre en SU propia transacción: debe quedar
        // guardado aunque la petición termine en excepción y haga rollback.
        this.transaccionIndependiente = new TransactionTemplate(gestorTransacciones);
        this.transaccionIndependiente.setPropagationBehavior(
                TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    /** El comprobante en claro (va a la cookie) y la fila que quedó en la base. */
    public record SesionNueva(String tokenEnClaro, SesionRefresco sesion) {}

    /** Abre una sesión nueva para el dispositivo que inicia sesión. */
    @Transactional
    public SesionNueva abrir(Usuario usuario, String agente, String ip) {
        return guardar(usuario, agente, ip);
    }

    /**
     * El corazón del módulo: valida el comprobante que llegó y lo rota.
     *
     * <ul>
     *   <li>Revocada → reutilización (robo): se cierran TODAS las sesiones del
     *       usuario y se rechaza la petición.</li>
     *   <li>Vencida → toca iniciar sesión otra vez.</li>
     *   <li>Viva → se crea un comprobante nuevo; la fila vieja queda marcada
     *       como revocada apuntando a la nueva (cadena de rotación).</li>
     * </ul>
     */
    @Transactional
    public SesionNueva refrescar(String tokenEnClaro, String agente, String ip) {
        SesionRefresco sesion = sesiones.findByHashToken(hash(tokenEnClaro))
                .orElseThrow(() -> new SesionInvalidaException("SESION_DESCONOCIDA",
                        "La sesión no existe. Inicia sesión de nuevo."));

        if (sesion.isRevocado()) {
            transaccionIndependiente.execute(sinResultado -> {
                sesiones.revocarTodas(sesion.getUsuario().getId(), Instant.now());
                return null;
            });
            throw new SesionInvalidaException("SESION_REUTILIZADA",
                    "Detectamos un uso anómalo de la sesión y la cerramos por seguridad. "
                            + "Inicia sesión de nuevo.");
        }

        Instant ahora = Instant.now();
        if (!ahora.isBefore(sesion.getFechaExpiracion())) {
            throw new SesionInvalidaException("SESION_EXPIRADA",
                    "La sesión venció. Inicia sesión de nuevo.");
        }

        SesionNueva nueva = guardar(sesion.getUsuario(), agente, ip);

        sesion.setRevocado(true);
        sesion.setFechaRevocacion(ahora);
        sesion.setReemplazadoPorId(nueva.sesion().getId());
        sesion.setUltimaActividad(ahora);

        return nueva;
    }

    /** Cierra la sesión del comprobante. Idempotente: cerrar dos veces no rompe nada. */
    @Transactional
    public void cerrar(String tokenEnClaro) {
        sesiones.findByHashToken(hash(tokenEnClaro)).ifPresent(sesion -> {
            if (!sesion.isRevocado()) {
                sesion.setRevocado(true);
                sesion.setFechaRevocacion(Instant.now());
            }
        });
    }

    /** Sesiones vivas del usuario, para "dispositivos conectados" (después). */
    @Transactional(readOnly = true)
    public List<SesionRefresco> activas(UUID usuarioId) {
        return sesiones.findByUsuarioIdAndRevocadoFalseAndFechaExpiracionAfter(
                usuarioId, Instant.now());
    }

    private SesionNueva guardar(Usuario usuario, String agente, String ip) {
        String tokenEnClaro = nuevoToken();
        SesionRefresco sesion = SesionRefresco.builder()
                .usuario(usuario)
                .hashToken(hash(tokenEnClaro))
                .agenteUsuario(acortar(agente, 255))
                .ip(ip)
                .fechaExpiracion(Instant.now().plus(vigencia))
                .build();
        return new SesionNueva(tokenEnClaro, sesiones.save(sesion));
    }

    private static String nuevoToken() {
        byte[] bytes = new byte[BYTES_TOKEN];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String hash(String token) {
        try {
            MessageDigest resumen = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(resumen.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no está disponible en esta JVM", e);
        }
    }

    private static String acortar(String valor, int maximo) {
        if (valor == null) {
            return null;
        }
        return valor.length() <= maximo ? valor : valor.substring(0, maximo);
    }
}
