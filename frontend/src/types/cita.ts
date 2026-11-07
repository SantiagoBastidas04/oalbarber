/** Estados de una cita según el SRS (RN-17). */
export type EstadoCita = 'CONFIRMADA' | 'CANCELADA' | 'COMPLETADA' | 'NO_ASISTIO'

/** Canal de origen de la cita (RNF-15). */
export type Canal = 'WEB' | 'WHATSAPP' | 'ADMIN'

/**
 * Refleja el DTO `CitaResponse` del backend (propuesta).
 * Las fechas son ISO-8601 con offset de Bogotá: 2026-11-10T10:00:00-05:00.
 */
export interface CitaResponse {
  id: number
  clienteId: number
  barberoId: number
  servicioId: number
  inicio: string
  fin: string
  duracionMin: number
  precio: number
  estado: EstadoCita
  canal: Canal
  creadaEn?: string
}

/** Datos mínimos para reservar. El canal lo decide el backend (WEB). */
export interface CrearCitaRequest {
  servicioId: number
  barberoId: number
  /** ISO-8601 con offset, por ejemplo 2026-11-10T10:00:00-05:00. */
  inicio: string
}
