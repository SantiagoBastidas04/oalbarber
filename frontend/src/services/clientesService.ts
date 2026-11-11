import { apiClient } from './apiClient'
import type { UsuarioResponse } from '../types/auth'

/**
 * Consultas de administración de usuarios (solo el rol ADMIN llega aquí:
 * el backend lo exige con @PreAuthorize y el candado de RutaProtegida).
 */

/** Lista de clientes para el panel, ordenada por nombre. */
export async function listarClientes(): Promise<UsuarioResponse[]> {
  const { data } = await apiClient.get<UsuarioResponse[]>('/usuarios/clientes')
  return data
}
