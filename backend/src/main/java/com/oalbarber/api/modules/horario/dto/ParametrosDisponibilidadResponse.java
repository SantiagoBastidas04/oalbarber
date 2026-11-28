package com.oalbarber.api.modules.horario.dto;

/** Valores RN-18 que usa el calculo de disponibilidad. */
public record ParametrosDisponibilidadResponse(int anticipacionMinimaHoras,
                                               int maximoDiasAdelante,
                                               int intervaloTurnosMinutos,
                                               int margenEntreCitasMinutos) {
}
