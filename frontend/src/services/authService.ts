import { apiClient } from './apiClient'
import { guardarToken, limpiarToken } from './tokenStore'
import type { AuthResponse, LoginRequest, RegistroRequest, Rol } from '../types/auth'

/** Rol tal como lo nombra el backend (`RolUsuario`). */
type RolApi = 'ADMINISTRADOR' | 'BARBERO' | 'CLIENTE'

/** Sesión tal como la entrega el backend, antes de normalizar el rol. */
interface SesionApi {
  tokenAcceso: string
  usuario: {
    id: string
    nombre: string
    celular: string
    rol: RolApi
    activo: boolean
    fechaCreacion: string
  }
}

/** ADMINISTRADOR → ADMIN: una sola palabra en toda la interfaz. */
function normalizarRol(rol: RolApi): Rol {
  return rol === 'ADMINISTRADOR' ? 'ADMIN' : rol
}

function mapear(sesion: SesionApi): AuthResponse {
  return {
    tokenAcceso: sesion.tokenAcceso,
    usuario: { ...sesion.usuario, rol: normalizarRol(sesion.usuario.rol) },
  }
}

/** RF-01: registra el cliente y devuelve la sesión ya iniciada. */
export async function registrar(datos: RegistroRequest): Promise<AuthResponse> {
  const { data } = await apiClient.post<SesionApi>('/auth/registro', datos)
  const sesion = mapear(data)
  guardarToken(sesion.tokenAcceso)
  return sesion
}

/** RF-02: inicio de sesión con celular (internacional) y contraseña. */
export async function iniciarSesion(datos: LoginRequest): Promise<AuthResponse> {
  const { data } = await apiClient.post<SesionApi>('/auth/login', datos)
  const sesion = mapear(data)
  guardarToken(sesion.tokenAcceso)
  return sesion
}

let renovacionEnCurso: Promise<AuthResponse> | null = null

/**
 * RF-03: renueva el pase corto con el comprobante de la cookie.
 *
 * Si varias peticiones (o la restauración de la sesión) piden renovación a la
 * vez, todas esperan la MISMA petición: no disparamos dos refresh con el mismo
 * comprobante, porque el segundo dispararía la detección de reutilización.
 */
export function renovarSesion(): Promise<AuthResponse> {
  renovacionEnCurso ??= apiClient
    .post<SesionApi>('/auth/refresh')
    .then((respuesta) => {
      const sesion = mapear(respuesta.data)
      guardarToken(sesion.tokenAcceso)
      return sesion
    })
    .finally(() => {
      renovacionEnCurso = null
    })

  return renovacionEnCurso
}

/** RF-04: cierra la sesión y revoca el comprobante en el backend. */
export async function cerrarSesion(): Promise<void> {
  try {
    await apiClient.post('/auth/logout')
  } finally {
    limpiarToken()
  }
}
