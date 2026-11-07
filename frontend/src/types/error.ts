/**
 * Error normalizado a partir del formato ProblemDetail (RFC 7807) que devuelve
 * la API, o de un fallo de red. Es lo que recibe el código de la interfaz.
 */
export interface ApiError {
  /** Código HTTP; 0 cuando la petición no llegó al servidor. */
  status: number
  title: string
  detail: string
  type?: string
  instance?: string
  /** Errores de validación por campo, si el backend los envía. */
  erroresCampo?: Record<string, string>
}
