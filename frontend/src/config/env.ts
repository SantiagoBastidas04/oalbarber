/**
 * Variables de entorno del frontend. Solo valores públicos con prefijo VITE_.
 * Nunca se guardan aquí tokens ni credenciales.
 */
const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL ?? '/api').replace(/\/+$/, '')

export const env = {
  /** Base de la API; en desarrollo y en Docker es `/api` (mismo origen). */
  apiBaseUrl,
} as const
