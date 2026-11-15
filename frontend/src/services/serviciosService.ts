import { apiClient } from './apiClient'
import type { ServicioResponse } from '../types/servicio'

/**
 * Catálogo público de servicios (RF-07). El backend devuelve solo los activos
 * y los ordena por nombre; la foto viaja como nombre de archivo (RN-03).
 */
export async function listarServicios(): Promise<ServicioResponse[]> {
  const { data } = await apiClient.get<ServicioResponse[]>('/servicios')
  return data
}
