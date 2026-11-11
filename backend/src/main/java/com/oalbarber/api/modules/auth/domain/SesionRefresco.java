package com.oalbarber.api.modules.auth.domain;

import java.time.Instant;
import java.util.UUID;

import com.oalbarber.api.modules.usuario.domain.Usuario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Una fila por dispositivo con sesión iniciada (V2).
 *
 * <p>Guardamos la huella SHA-256 del comprobante, nunca el comprobante en
 * claro. La rotación marca la fila vieja con {@code revocado} y apunta con
 * {@code reemplazadoPorId} a la fila nueva: si el comprobante viejo reaparece,
 * es señal de robo y se cierran todas las sesiones del usuario.</p>
 */
@Entity
@Table(name = "sesion_refresco")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SesionRefresco {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "hash_token", nullable = false, length = 64)
    private String hashToken;

    @Column(name = "agente_usuario", length = 255)
    private String agenteUsuario;

    @Column(length = 45)
    private String ip;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private Instant fechaCreacion;

    @Column(name = "ultima_actividad", nullable = false)
    private Instant ultimaActividad;

    @Column(name = "fecha_expiracion", nullable = false)
    private Instant fechaExpiracion;

    @Column(nullable = false)
    private boolean revocado;

    @Column(name = "fecha_revocacion")
    private Instant fechaRevocacion;

    @Column(name = "reemplazado_por_id")
    private UUID reemplazadoPorId;

    @PrePersist
    void alCrear() {
        Instant ahora = Instant.now();
        if (fechaCreacion == null) {
            fechaCreacion = ahora;
        }
        if (ultimaActividad == null) {
            ultimaActividad = ahora;
        }
    }
}
