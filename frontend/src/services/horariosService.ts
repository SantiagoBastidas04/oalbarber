import { apiClient } from './apiClient'
import type {
  HorarioDiaResponse,
  HorarioExcepcionRequest,
  HorarioExcepcionResponse,
  HorarioSemanaRequest,
} from '../types/horario'

/** RF-11: días configurados del barbero; los ausentes están cerrados. */
export async function listarHorarios(
  barberoId: string,
): Promise<HorarioDiaResponse[]> {
  const { data } = await apiClient.get<HorarioDiaResponse[]>('/horarios', {
    params: { barberoId },
  })
  return data
}

/** RF-11: reemplaza la semana completa; solo ADMINISTRADOR. */
export async function reemplazarHorarios(
  barberoId: string,
  solicitud: HorarioSemanaRequest,
): Promise<HorarioDiaResponse[]> {
  const { data } = await apiClient.put<HorarioDiaResponse[]>(
    `/horarios/${barberoId}`,
    solicitud,
  )
  return data
}

/** RF-11: fechas especiales que sobrescriben la semana (domingos, festivos...). */
export async function listarExcepciones(
  barberoId: string,
): Promise<HorarioExcepcionResponse[]> {
  const { data } = await apiClient.get<HorarioExcepcionResponse[]>(
    `/horarios/${barberoId}/excepciones`,
  )
  return data
}

/** RF-11: crea o actualiza la fecha especial; solo ADMINISTRADOR. */
export async function guardarExcepcion(
  barberoId: string,
  fecha: string,
  solicitud: HorarioExcepcionRequest,
): Promise<HorarioExcepcionResponse> {
  const { data } = await apiClient.put<HorarioExcepcionResponse>(
    `/horarios/${barberoId}/excepciones/${fecha}`,
    solicitud,
  )
  return data
}

/** RF-11: quita la fecha especial; la fecha vuelve a la semana habitual. */
export async function eliminarExcepcion(
  barberoId: string,
  fecha: string,
): Promise<void> {
  await apiClient.delete(`/horarios/${barberoId}/excepciones/${fecha}`)
}
