import type { UsuarioRegistroRequest, UsuarioRegistroResponse } from '../types/usuario'
import { apiClient } from './apiClient'

/**
 * RF-01: registra un cliente con rol CLIENTE.
 *
 * El backend devuelve el usuario creado, sin sesión: el inicio de sesión se
 * hará con el módulo `auth` cuando esté disponible.
 */
export async function registrarUsuario(
  datos: UsuarioRegistroRequest,
): Promise<UsuarioRegistroResponse> {
  const { data } = await apiClient.post<UsuarioRegistroResponse>('/usuarios/registro', datos)
  return data
}
