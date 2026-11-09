/** Roles que devuelve la API (coinciden con el enum `RolUsuario` del backend). */
export type RolUsuario = 'ADMINISTRADOR' | 'CLIENTE' | 'BARBERO'

/**
 * Cuerpo de `POST /api/usuarios/registro`.
 * El rol, el estado y el hash de la contraseña los asigna el backend.
 */
export interface UsuarioRegistroRequest {
  nombre: string
  /** Celular en formato internacional: +573001234567 */
  celular: string
  contrasena: string
}

/** Respuesta de `POST /api/usuarios/registro` (`UsuarioResponse` del backend). */
export interface UsuarioRegistroResponse {
  id: string
  nombre: string
  celular: string
  rol: RolUsuario
  activo: boolean
  fechaCreacion: string
}
