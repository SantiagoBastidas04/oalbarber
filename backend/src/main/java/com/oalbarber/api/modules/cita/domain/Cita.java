package com.oalbarber.api.modules.cita.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Cita persistida con duracion y precio vigentes al reservar (RN-16). */
@Entity
@Table(name = "cita")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "cliente_id", nullable = false)
    private UUID clienteId;

    @Column(name = "barbero_id", nullable = false)
    private UUID barberoId;

    @Column(name = "servicio_id", nullable = false)
    private UUID servicioId;

    @Column(nullable = false)
    private Instant inicio;

    @Column(nullable = false)
    private Instant fin;

    @Column(name = "duracion_min", nullable = false)
    private int duracionMin;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCita estado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CanalCita canal;

    @Column(name = "creada_en", nullable = false, updatable = false)
    private Instant creadaEn;
}
