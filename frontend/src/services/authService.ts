import type { AuthResponse, LoginRequest, RegistroRequest } from '../types/auth'
import { apiClient } from './apiClient'
import { guardarToken, limpiarToken } from './tokenStore'

/** RF-01: registro de un cliente. La respuesta ya trae la sesión iniciada. */
export async function registrar(datos: RegistroRequest): Promise<AuthResponse> {
  const { data } = await apiClient.post<AuthResponse>('/auth/registro', datos)
  guardarToken(data.tokenAcceso)
  return data
}

/** RF-02: inicio de sesión con celular y contraseña. */
export async function iniciarSesion(datos: LoginRequest): Promise<AuthResponse> {
  const { data } = await apiClient.post<AuthResponse>('/auth/login', datos)
  guardarToken(data.tokenAcceso)
  return data
}

/** RF-03: renueva el token de acceso con el refresh token de la cookie. */
export async function renovarSesion(): Promise<AuthResponse> {
  const { data } = await apiClient.post<AuthResponse>('/auth/refresh')
  guardarToken(data.tokenAcceso)
  return data
}

/** RF-04: cierra la sesión y revoca el refresh token en el backend. */
export async function cerrarSesion(): Promise<void> {
  try {
    await apiClient.post('/auth/logout')
  } finally {
    limpiarToken()
  }
}
