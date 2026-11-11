/**
 * Roles del SRS tal como los usa la interfaz.
 * El backend nombra `ADMINISTRADOR`; el servicio de autenticación lo
 * normaliza a `ADMIN` al llegar la respuesta, para usar una sola palabra.
 */
export type Rol = 'ADMIN' | 'BARBERO' | 'CLIENTE'

/** Usuario autenticado, con el rol ya normalizado. */
export interface UsuarioResponse {
  id: string
  nombre: string
  /** Celular en formato internacional: +573001234567 */
  celular: string
  rol: Rol
  activo: boolean
  fechaCreacion: string
}

export interface LoginRequest {
  celular: string
  contrasena: string
}

export interface RegistroRequest {
  nombre: string
  celular: string
  contrasena: string
}

/**
 * Respuesta de registro, login y refresh.
 * El token de acceso se guarda solo en memoria (RNF-02); el comprobante de
 * refresco viaja en una cookie HttpOnly que el navegador gestiona sin
 * exponerla al código.
 */
export interface AuthResponse {
  tokenAcceso: string
  usuario: UsuarioResponse
}
