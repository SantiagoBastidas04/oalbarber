package com.oalbarber.api.modules.horario.domain;

import java.time.LocalTime;
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

/** Horario semanal de un barbero, incluidos los tiempos de almuerzo confirmados. */
@Entity
@Table(name = "horario_atencion")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HorarioAtencion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "barbero_id", nullable = false)
    private UUID barberoId;

    @Column(name = "dia_semana", nullable = false)
    private short diaSemana;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Column(name = "inicio_almuerzo")
    private LocalTime inicioAlmuerzo;

    @Column(name = "fin_almuerzo")
    private LocalTime finAlmuerzo;
}
