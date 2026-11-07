/** Refleja el DTO `BarberoResponse` del backend (propuesta). */
export interface BarberoResponse {
  id: number
  /** Referencia al usuario con rol BARBERO o ADMIN. */
  usuarioId: number
  nombre: string
  nombrePublico?: string
  descripcion?: string
  /** Solo el nombre del archivo estático de la foto. */
  foto?: string | null
  activo: boolean
}
