/** Refleja el DTO `ServicioResponse` del backend. */
export interface ServicioResponse {
  /** UUID del servicio, igual que en el resto de la API. */
  id: string
  nombre: string
  descripcion: string | null
  /** Duración en minutos (RN-03). */
  duracionMin: number
  /** Precio en pesos colombianos, igual para ambos barberos (RN-03). */
  precio: number
  /** Solo el nombre del archivo estático, por ejemplo `corte-normal.jpg`. */
  imagen: string | null
  activo: boolean
}
