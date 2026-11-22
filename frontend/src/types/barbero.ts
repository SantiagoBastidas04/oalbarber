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

/** Reemplazo completo de las asignaciones activas de un perfil. */
export interface BarberoServiciosRequest {
  servicioIds: string[]
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
  /** UUIDs de servicios asignados; incluye asignaciones a servicios inactivos. */
  servicioIds: string[]
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

/** Datos opcionales para agregar la cuenta administradora como barbero. */
export interface BarberoPerfilRequest {
  nombrePublico?: string | null
  descripcion?: string | null
  foto?: string | null
}

/** Perfil creado a partir de la cuenta del administrador autenticado. */
export interface BarberoPerfilResponse {
  id: string
  usuarioId: string
  nombre: string
  nombrePublico: string | null
  descripcion: string | null
  foto: string | null
  activo: boolean
}
