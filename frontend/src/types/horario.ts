/** Un día configurado del horario de un barbero (RF-11). */
export interface HorarioDiaResponse {
  id: string
  barberoId: string
  /** Día ISO: 1 = lunes ... 7 = domingo. */
  diaSemana: number
  /** Hora en formato HH:mm. */
  horaInicio: string
  horaFin: string
  /** Nulo cuando el día no tiene almuerzo. */
  almuerzoInicio: string | null
  almuerzoFin: string | null
}

/** Un día dentro del reemplazo completo de la semana. */
export interface HorarioDiaRequest {
  diaSemana: number
  horaInicio: string
  horaFin: string
  almuerzoInicio?: string | null
  almuerzoFin?: string | null
}

/** Reemplaza la semana completa; los días ausentes quedan cerrados. */
export interface HorarioSemanaRequest {
  dias: HorarioDiaRequest[]
}

/** Fecha especial configurada: abre, cierra o cambia el horario de un día concreto. */
export interface HorarioExcepcionResponse {
  id: string
  barberoId: string
  /** Fecha ISO sin hora: YYYY-MM-DD. */
  fecha: string
  /** Verdadero cuando la fecha no atiende. */
  cerrado: boolean
  horaInicio: string | null
  horaFin: string | null
  almuerzoInicio: string | null
  almuerzoFin: string | null
}

/** Datos para guardar una fecha especial; las horas van nulas si está cerrada. */
export interface HorarioExcepcionRequest {
  cerrado: boolean
  horaInicio?: string | null
  horaFin?: string | null
  almuerzoInicio?: string | null
  almuerzoFin?: string | null
}
