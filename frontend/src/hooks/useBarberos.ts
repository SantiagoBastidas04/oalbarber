import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  cambiarEstadoBarbero,
  crearBarbero,
  listarBarberosActivos,
  listarBarberosAdmin,
  registrarAdministradorComoBarbero,
  reemplazarServiciosDeBarbero,
} from '../services/barberosService'
import type { BarberoPerfilRequest, BarberoRequest } from '../types/barbero'

/** Claves estables de caché para los listados público y administrativo (RF-10). */
export const clavesBarberos = {
  todos: ['barberos'] as const,
  publicos: ['barberos', 'publicos'] as const,
  admin: ['barberos', 'admin'] as const,
}

/** Consulta perfiles activos para la selección pública de barberos. */
export function useBarberos() {
  return useQuery({
    queryKey: clavesBarberos.publicos,
    queryFn: listarBarberosActivos,
  })
}

/** Consulta todos los perfiles, con sus datos de cuenta, para el panel ADMIN. */
export function useBarberosAdmin() {
  return useQuery({
    queryKey: clavesBarberos.admin,
    queryFn: listarBarberosAdmin,
  })
}

/** Crea una cuenta BARBERO y refresca ambos listados. */
export function useCrearBarbero() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (solicitud: BarberoRequest) => crearBarbero(solicitud),
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: clavesBarberos.publicos }),
        queryClient.invalidateQueries({ queryKey: clavesBarberos.admin }),
      ])
    },
  })
}

/** Agrega la cuenta del administrador autenticado como barbero y refresca. */
export function useRegistrarAdministradorComoBarbero() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (solicitud: BarberoPerfilRequest) =>
      registrarAdministradorComoBarbero(solicitud),
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: clavesBarberos.publicos }),
        queryClient.invalidateQueries({ queryKey: clavesBarberos.admin }),
      ])
    },
  })
}

/** Cambia el estado del perfil y refresca ambos listados. */export function useCambiarEstadoBarbero() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ id, activo }: { id: string; activo: boolean }) =>
      cambiarEstadoBarbero(id, { activo }),
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: clavesBarberos.publicos }),
        queryClient.invalidateQueries({ queryKey: clavesBarberos.admin }),
      ])
    },
  })
}

/** Guarda las asignaciones y actualiza el listado administrativo compartido. */
export function useGuardarServiciosBarbero() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ id, servicioIds }: { id: string; servicioIds: string[] }) =>
      reemplazarServiciosDeBarbero(id, { servicioIds }),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: clavesBarberos.todos })
    },
  })
}
