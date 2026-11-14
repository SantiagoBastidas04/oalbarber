/** Perfil público del backend; solo representa barberos activos. */
export interface BarberoPublicoResponse {
  /** UUID serializado como cadena por la API. */
  id: string
  nombrePublico: string | null
  descripcion: string | null
  /** Nombre de archivo estático; no es una URL ni una ruta local. */
  foto: string | null
  activo: boolean
}

/** Perfil y datos de cuenta que solo devuelve el endpoint administrativo. */
export interface BarberoAdminResponse {
  id: string
  usuarioId: string
  nombre: string
  celular: string
  nombrePublico: string | null
  descripcion: string | null
  foto: string | null
  activo: boolean
}

/** Datos de alta; el rol BARBERO lo asigna el backend. */
export interface BarberoRequest {
  nombre: string
  celular: string
  contrasena: string
  nombrePublico?: string | null
  descripcion?: string | null
  foto?: string | null
}

/** Estado del perfil; no cambia el estado de la cuenta de usuario. */
export interface CambiarEstadoBarberoRequest {
  activo: boolean
}
