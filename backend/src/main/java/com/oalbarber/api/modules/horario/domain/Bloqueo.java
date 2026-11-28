package com.oalbarber.api.modules.horario.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Franja semiabierta de tiempo que no se ofrece en la agenda de un barbero. */
@Entity
@Table(name = "bloqueo")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Bloqueo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "barbero_id", nullable = false)
    private UUID barberoId;

    @Column(nullable = false)
    private Instant inicio;

    @Column(nullable = false)
    private Instant fin;

    @Column(length = 255)
    private String motivo;
}
