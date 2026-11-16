package com.oalbarber.api.modules.barbero.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Asignación persistente de un servicio a un barbero. */
@Entity
@Table(name = "barbero_servicio")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BarberoServicio {

    @EmbeddedId
    private BarberoServicioId id;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    private BarberoServicio(BarberoServicioId id) {
        this.id = Objects.requireNonNull(id, "La clave de la asignación es obligatoria.");
    }

    /** Crea la asignación usando los identificadores de ambos módulos. */
    public static BarberoServicio crear(UUID barberoId, UUID servicioId) {
        return new BarberoServicio(new BarberoServicioId(barberoId, servicioId));
    }

    @PrePersist
    void alCrear() {
        if (creadoEn == null) {
            creadoEn = Instant.now();
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
        BarberoServicio otra = (BarberoServicio) objeto;
        return id != null && id.equals(otra.id);
    }

    @Override
    public int hashCode() {
        return id == null ? getClass().hashCode() : id.hashCode();
    }
}
