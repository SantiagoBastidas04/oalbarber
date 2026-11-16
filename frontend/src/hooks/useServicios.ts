import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  actualizarServicio,
  cambiarEstadoServicio,
  crearServicio,
  listarServicios,
} from '../services/serviciosService'
import type { ServicioRequest } from '../types/servicio'

/** Clave de caché del catálogo; la comparten la home, `/servicios` y el panel. */
export const CLAVE_SERVICIOS = ['servicios'] as const

/**
 * Catálogo de servicios (RF-07). El panel pasa `true` para ver también los
 * inactivos; es una clave distinta para no mezclar la caché pública.
 */
export function useServicios(incluirInactivos = false) {
  return useQuery({
    queryKey: [...CLAVE_SERVICIOS, { incluirInactivos }],
    queryFn: () => listarServicios(incluirInactivos),
  })
}

/**
 * Invalida todas las variantes del catálogo. Se invalida por prefijo para que
 * el cambio del panel se vea también en `/servicios` y en la portada.
 */
function useInvalidarCatalogo() {
  const queryClient = useQueryClient()
  return () => queryClient.invalidateQueries({ queryKey: CLAVE_SERVICIOS })
}

/** RF-08: alta de un servicio. */
export function useCrearServicio() {
  const invalidar = useInvalidarCatalogo()
  return useMutation({
    mutationFn: (datos: ServicioRequest) => crearServicio(datos),
    onSuccess: invalidar,
  })
}

/** RF-08: edición de los datos de un servicio. */
export function useActualizarServicio() {
  const invalidar = useInvalidarCatalogo()
  return useMutation({
    mutationFn: ({ id, datos }: { id: string; datos: ServicioRequest }) =>
      actualizarServicio(id, datos),
    onSuccess: invalidar,
  })
}

/** RF-08 / RN-16: activar o desactivar sin borrar. */
export function useCambiarEstadoServicio() {
  const invalidar = useInvalidarCatalogo()
  return useMutation({
    mutationFn: ({ id, activo }: { id: string; activo: boolean }) =>
      cambiarEstadoServicio(id, activo),
    onSuccess: invalidar,
  })
}
