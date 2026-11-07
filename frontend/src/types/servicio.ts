/** Refleja el DTO `ServicioResponse` del backend (propuesta). */
export interface ServicioResponse {
  id: number
  nombre: string
  descripcion: string
  /** Duración en minutos (RN-03). */
  duracionMin: number
  /** Precio en pesos colombianos, igual para ambos barberos (RN-03). */
  precio: number
  /** Solo el nombre del archivo estático, por ejemplo `corte-normal.jpg`. */
  imagen: string | null
  activo: boolean
}
