package com.oalbarber.api.modules.barbero.domain;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Identificador compuesto de una asignación entre barbero y servicio. */
@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BarberoServicioId implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "barbero_id", nullable = false)
    private UUID barberoId;

    @Column(name = "servicio_id", nullable = false)
    private UUID servicioId;

    public BarberoServicioId(UUID barberoId, UUID servicioId) {
        this.barberoId = Objects.requireNonNull(barberoId, "El barbero es obligatorio.");
        this.servicioId = Objects.requireNonNull(servicioId, "El servicio es obligatorio.");
    }
}
