package com.oalbarber.api.modules.horario.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Excepción del horario semanal para una fecha concreta.
 *
 * <p>Sirve para abrir un domingo, cerrar un festivo o cambiar el horario de un
 * día puntual. Si {@code cerrado} es verdadero, la fecha no atiende y las horas
 * quedan nulas; si es falso, se guarda una jornada completa con almuerzo opcional.</p>
 */
@Builder
@Entity
@Table(name = "horario_excepcion", uniqueConstraints = {
        @UniqueConstraint(name = "uk_horario_excepcion_barbero_fecha",
                columnNames = {"barbero_id", "fecha"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HorarioExcepcion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "barbero_id", nullable = false)
    private UUID barberoId;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private boolean cerrado;

    @Column(name = "hora_inicio")
    private LocalTime horaInicio;

    @Column(name = "hora_fin")
    private LocalTime horaFin;

    @Column(name = "almuerzo_inicio")
    private LocalTime almuerzoInicio;

    @Column(name = "almuerzo_fin")
    private LocalTime almuerzoFin;

    /** Actualiza la fila existente sin cambiar su identificador ni su barbero. */
    public void actualizar(boolean cerrado, LocalTime horaInicio, LocalTime horaFin,
                           LocalTime almuerzoInicio, LocalTime almuerzoFin) {
        this.cerrado = cerrado;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.almuerzoInicio = almuerzoInicio;
        this.almuerzoFin = almuerzoFin;
    }
}
