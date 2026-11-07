import type { ReactNode } from 'react'
import { Navigate, useLocation } from 'react-router'
import { RUTAS } from '../config/rutas'
import { useAuth } from '../hooks/useAuth'
import type { Rol } from '../types/auth'

interface Props {
  /** Roles permitidos. Si se omite, basta con tener sesión. */
  roles?: readonly Rol[]
  children: ReactNode
}

/**
 * Envuelve una ruta que exige sesión. Sin sesión redirige a /ingresar
 * guardando el origen; con un rol no permitido, a /no-autorizado.
 */
export function RutaProtegida({ roles, children }: Props) {
  const { estaAutenticado, rol } = useAuth()
  const ubicacion = useLocation()

  if (!estaAutenticado) {
    return <Navigate to={RUTAS.ingresar} replace state={{ origen: ubicacion }} />
  }

  if (roles && (!rol || !roles.includes(rol))) {
    return <Navigate to={RUTAS.noAutorizado} replace />
  }

  return <>{children}</>
}
