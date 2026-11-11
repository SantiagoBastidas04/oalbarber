import axios, { type AxiosError, type InternalAxiosRequestConfig } from 'axios'
import { env } from '../config/env'
import { RUTAS } from '../config/rutas'
import type { ApiError } from '../types/error'
import { limpiarToken, obtenerToken } from './tokenStore'
import { renovarSesion } from './authService'

/** Marca interna para no reintentar dos veces la misma petición. */
interface ConfigReintentable extends InternalAxiosRequestConfig {
  _reintentado?: boolean
}

/** Subconjunto de ProblemDetail (RFC 7807) que nos interesa. */
interface ProblemDetail {
  type?: string
  title?: string
  detail?: string
  instance?: string
  errors?: Record<string, string>
}

const MENSAJES_POR_ESTADO: Record<number, string> = {
  400: 'Los datos enviados no son válidos.',
  401: 'Tu sesión expiró. Vuelve a ingresar.',
  403: 'No tienes permisos para realizar esta acción.',
  404: 'No encontramos lo que buscas.',
  409: 'La operación entra en conflicto con el estado actual.',
  422: 'Los datos enviados no son válidos.',
  500: 'Ocurrió un error en el servidor. Intenta de nuevo.',
}

/** Convierte cualquier fallo en el `ApiError` que consume la interfaz. */
export function normalizarError(error: unknown): ApiError {
  if (axios.isAxiosError(error)) {
    const respuesta = error.response
    if (!respuesta) {
      return {
        status: 0,
        title: 'Sin conexión',
        detail: 'No pudimos conectar con el servidor. Revisa tu conexión e intenta de nuevo.',
      }
    }

    const datos = respuesta.data as ProblemDetail | undefined
    return {
      status: respuesta.status,
      title: datos?.title ?? 'No se pudo completar la solicitud',
      detail:
        datos?.detail ??
        MENSAJES_POR_ESTADO[respuesta.status] ??
        'Ocurrió un error inesperado. Intenta de nuevo.',
      type: datos?.type,
      instance: datos?.instance,
      erroresCampo: datos?.errors,
    }
  }

  return {
    status: 0,
    title: 'Error inesperado',
    detail: 'Ocurrió un error inesperado. Intenta de nuevo.',
  }
}

export const apiClient = axios.create({
  baseURL: env.apiBaseUrl,
  withCredentials: true,
  headers: { 'Content-Type': 'application/json' },
})

apiClient.interceptors.request.use((config) => {
  const token = obtenerToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

function redirigirAIngresar(): void {
  if (window.location.pathname === RUTAS.ingresar) {
    return
  }
  const origen = `${window.location.pathname}${window.location.search}`
  window.location.assign(`${RUTAS.ingresar}?origen=${encodeURIComponent(origen)}`)
}

apiClient.interceptors.response.use(
  (respuesta) => respuesta,
  async (error: AxiosError) => {
    const config = error.config as ConfigReintentable | undefined
    const esRutaAuth = Boolean(config?.url?.includes('/auth/'))
    const es401 = error.response?.status === 401

    if (es401 && config && !config._reintentado && !esRutaAuth) {
      config._reintentado = true
      try {
        const sesion = await renovarSesion()
        config.headers.Authorization = `Bearer ${sesion.tokenAcceso}`
        return apiClient(config)
      } catch {
        limpiarToken()
        redirigirAIngresar()
      }
    }

    return Promise.reject(normalizarError(error))
  },
)
