import { apiClient } from './apiClient'
import type {
  BarberoAdminResponse,
  BarberoPublicoResponse,
  BarberoRequest,
  CambiarEstadoBarberoRequest,
} from '../types/barbero'

/** RF-10: consulta pública de perfiles activos, sin datos personales de cuenta. */
export async function listarBarberosActivos(): Promise<BarberoPublicoResponse[]> {
  const { data } = await apiClient.get<BarberoPublicoResponse[]>('/barberos')
  return data
}

/** RF-10: lista perfiles activos e inactivos y datos de cuenta para ADMINISTRADOR. */
export async function listarBarberosAdmin(): Promise<BarberoAdminResponse[]> {
  const { data } = await apiClient.get<BarberoAdminResponse[]>('/barberos/admin')
  return data
}

/** RF-10: crea cuenta BARBERO y perfil; el backend asigna el rol y cifra la clave. */
export async function crearBarbero(
  solicitud: BarberoRequest,
): Promise<BarberoAdminResponse> {
  const { data } = await apiClient.post<BarberoAdminResponse>('/barberos', solicitud)
  return data
}

/** RF-10: activa o desactiva solo el perfil, conservando la cuenta y las sesiones. */
export async function cambiarEstadoBarbero(
  id: string,
  solicitud: CambiarEstadoBarberoRequest,
): Promise<BarberoAdminResponse> {
  const { data } = await apiClient.patch<BarberoAdminResponse>(
    `/barberos/${id}/estado`,
    solicitud,
  )
  return data
}
