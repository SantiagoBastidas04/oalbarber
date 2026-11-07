/** Roles definidos en el SRS (RN-15 y sección de roles). */
export type Rol = 'ADMIN' | 'BARBERO' | 'CLIENTE'

export interface UsuarioResponse {
  id: number
  nombre: string
  /** Celular en formato internacional: +573001234567 */
  telefono: string
  rol: Rol
  activo: boolean
}

export interface LoginRequest {
  telefono: string
  password: string
}

export interface RegistroRequest {
  nombre: string
  telefono: string
  password: string
  /** RF-01 y RNF-10: consentimiento de la política de tratamiento de datos. */
  aceptaTratamientoDatos: boolean
}

/**
 * Respuesta de registro, login y refresh.
 * El token de acceso se guarda solo en memoria (RNF-02); el refresh token viaja
 * en una cookie HttpOnly que el navegador gestiona sin exponerla al código.
 */
export interface AuthResponse {
  tokenAcceso: string
  usuario: UsuarioResponse
}
