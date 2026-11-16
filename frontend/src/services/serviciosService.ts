import { apiClient } from './apiClient'
import type { ServicioRequest, ServicioResponse } from '../types/servicio'

/**
 * Catálogo de servicios (RF-07 y RF-08). El backend devuelve solo los activos
 * y los ordena por nombre; el panel pide también los inactivos con
 * `incluirInactivos`. La foto viaja como nombre de archivo (RN-03).
 */
export async function listarServicios(
  incluirInactivos = false,
): Promise<ServicioResponse[]> {
  const { data } = await apiClient.get<ServicioResponse[]>('/servicios', {
    // Sin el parámetro la petición queda idéntica a la del catálogo público.
    params: incluirInactivos ? { incluirInactivos: true } : undefined,
  })
  return data
}

/** RF-08: crea un servicio. El backend responde 201 y el servicio nace activo. */
export async function crearServicio(datos: ServicioRequest): Promise<ServicioResponse> {
  const { data } = await apiClient.post<ServicioResponse>('/servicios', datos)
  return data
}

/** RF-08: reemplaza los datos editables de un servicio. */
export async function actualizarServicio(
  id: string,
  datos: ServicioRequest,
): Promise<ServicioResponse> {
  const { data } = await apiClient.put<ServicioResponse>(`/servicios/${id}`, datos)
  return data
}

/** RF-08 / RN-16: activa o desactiva; nunca borra, así que las citas no cambian. */
export async function cambiarEstadoServicio(
  id: string,
  activo: boolean,
): Promise<ServicioResponse> {
  const { data } = await apiClient.patch<ServicioResponse>(`/servicios/${id}/estado`, {
    activo,
  })
  return data
}
