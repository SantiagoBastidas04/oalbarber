package com.oalbarber.api.modules.horario.domain;

import java.time.LocalTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Jornada continua de atencion de un barbero para un dia de la semana.
 *
 * <p>Una fila por dia; los dias sin fila se consideran cerrados. {@code diaSemana}
 * usa ISO 8601: 1 = lunes ... 7 = domingo. Las horas son de reloj local
 * (America/Bogota) y el motor de disponibilidad las combina con la fecha.</p>
 */
@Builder
@Entity
@Table(name = "horario_atencion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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

    @Column(name = "almuerzo_inicio")
    private LocalTime almuerzoInicio;

    @Column(name = "almuerzo_fin")
    private LocalTime almuerzoFin;
}
