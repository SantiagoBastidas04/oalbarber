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

/** Cuerpo de `POST /api/servicios` y `PUT /api/servicios/{id}` (RF-08). */
export interface ServicioRequest {
  nombre: string
  descripcion: string | null
  /** Solo se aceptan 40, 90 o 180 minutos. */
  duracionMin: number
  /** Mayor que cero; el backend exige máximo dos decimales. */
  precio: number
  /** Nombre del archivo estático de la foto, o `null` si no tiene. */
  imagen: string | null
}
