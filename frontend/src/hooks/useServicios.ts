import { useQuery } from '@tanstack/react-query'
import { listarServicios } from '../services/serviciosService'

/** Clave de caché del catálogo; la comparten la home y `/servicios`. */
export const CLAVE_SERVICIOS = ['servicios'] as const

/** Catálogo público de servicios (RF-07) con sus estados de carga y error. */
export function useServicios() {
  return useQuery({
    queryKey: CLAVE_SERVICIOS,
    queryFn: listarServicios,
  })
}
