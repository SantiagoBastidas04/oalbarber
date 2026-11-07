/** Horario que el backend ofrece para un servicio y un día (RF-16). */
export interface HorarioDisponibleResponse {
  /** Inicio del turno en ISO-8601 con offset. */
  inicio: string
  /** Fin del turno en ISO-8601 con offset. */
  fin: string
  /** Barbero que atiende el turno; la API lo resuelve también para "cualquiera". */
  barberoId: number
}
