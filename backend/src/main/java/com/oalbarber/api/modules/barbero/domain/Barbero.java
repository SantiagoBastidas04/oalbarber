package com.oalbarber.api.modules.barbero.domain;

import java.time.Instant;
import java.util.UUID;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Perfil profesional enlazado a una cuenta de usuario mediante su UUID. */
@Entity
@Table(name = "barbero", uniqueConstraints = {
        @UniqueConstraint(name = "uk_barbero_usuario_id", columnNames = "usuario_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Barbero {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** UUID del usuario asociado; se guarda como valor para no cruzar entidades entre módulos. */
    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "nombre_publico", length = 100)
    private String nombrePublico;

    @Column(columnDefinition = "text")
    private String descripcion;

    @Column(length = 255)
    private String foto;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private Instant fechaCreacion;

    /** Crea un perfil sin datos de presentación, activo por defecto. */
    public static Barbero crear(UUID usuarioId) {
        return crear(usuarioId, null, null, null);
    }

    /** Crea un perfil con sus datos opcionales de presentación. */
    public static Barbero crear(UUID usuarioId, String nombrePublico, String descripcion,
                                String foto) {
        Barbero barbero = new Barbero();
        barbero.usuarioId = Objects.requireNonNull(usuarioId,
                "El usuario asociado es obligatorio.");
        barbero.nombrePublico = nombrePublico;
        barbero.descripcion = descripcion;
        barbero.foto = foto;
        return barbero;
    }

    /** Cambia únicamente el estado del perfil; no modifica la cuenta de usuario. */
    public void cambiarEstado(boolean activo) {
        this.activo = activo;
    }

    @PrePersist
    void alCrear() {
        if (fechaCreacion == null) {
            fechaCreacion = Instant.now();
        }
    }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }
        if (objeto == null || getClass() != objeto.getClass()) {
            return false;
        }
        Barbero otro = (Barbero) objeto;
        return id != null && id.equals(otro.id);
    }

    @Override
    public int hashCode() {
        return id == null ? getClass().hashCode() : id.hashCode();
    }
}
