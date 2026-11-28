import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  eliminarExcepcion,
  guardarExcepcion,
  listarExcepciones,
  listarHorarios,
  reemplazarHorarios,
} from '../services/horariosService'
import type {
  HorarioDiaRequest,
  HorarioExcepcionRequest,
} from '../types/horario'

/** Claves estables de caché para el horario de cada barbero (RF-11). */
export const clavesHorarios = {
  todos: ['horarios'] as const,
  deBarbero: (barberoId: string) => ['horarios', barberoId] as const,
  excepciones: (barberoId: string) => ['horarios', barberoId, 'excepciones'] as const,
}

/** Consulta la semana de un barbero; no se ejecuta sin barbero elegido. */
export function useHorarios(barberoId: string | null) {
  return useQuery({
    queryKey: clavesHorarios.deBarbero(barberoId ?? 'sin-barbero'),
    queryFn: () => listarHorarios(barberoId as string),
    enabled: Boolean(barberoId),
  })
}

/** Guarda la semana completa y deja la caché con la respuesta del servidor. */
export function useGuardarHorarios(barberoId: string | null) {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (dias: HorarioDiaRequest[]) =>
      reemplazarHorarios(barberoId as string, { dias }),
    onSuccess: async (diasGuardados) => {
      if (barberoId) {
        queryClient.setQueryData(clavesHorarios.deBarbero(barberoId), diasGuardados)
      }
    },
  })
}

/** Consulta las fechas especiales del barbero. */
export function useExcepciones(barberoId: string | null) {
  return useQuery({
    queryKey: clavesHorarios.excepciones(barberoId ?? 'sin-barbero'),
    queryFn: () => listarExcepciones(barberoId as string),
    enabled: Boolean(barberoId),
  })
}

/** Crea o actualiza una fecha especial y refresca la lista. */
export function useGuardarExcepcion(barberoId: string | null) {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (datos: HorarioExcepcionRequest & { fecha: string }) => {
      const { fecha, ...solicitud } = datos
      return guardarExcepcion(barberoId as string, fecha, solicitud)
    },
    onSuccess: async () => {
      if (barberoId) {
        await queryClient.invalidateQueries({
          queryKey: clavesHorarios.excepciones(barberoId),
        })
      }
    },
  })
}

/** Elimina una fecha especial y refresca la lista. */
export function useEliminarExcepcion(barberoId: string | null) {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (fecha: string) => eliminarExcepcion(barberoId as string, fecha),
    onSuccess: async () => {
      if (barberoId) {
        await queryClient.invalidateQueries({
          queryKey: clavesHorarios.excepciones(barberoId),
        })
      }
    },
  })
}
