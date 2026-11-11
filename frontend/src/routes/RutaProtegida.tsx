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
 * Envuelve una ruta que exige sesión. Mientras se restaura la sesión muestra
 * una pantalla de espera; sin sesión redirige a /ingresar guardando el origen;
 * con un rol no permitido, a /no-autorizado.
 */
export function RutaProtegida({ roles, children }: Props) {
  const { estaAutenticado, rol, cargando } = useAuth()
  const ubicacion = useLocation()

  if (cargando) {
    return (
      <div className="flex min-h-[60vh] items-center justify-center px-4">
        <p
          role="status"
          aria-live="polite"
          className="flex items-center gap-2.5 text-sm text-marca-500"
        >
          <span className="flex gap-1" aria-hidden="true">
            <span className="size-2 animate-pulse rounded-full bg-marca-900 [animation-delay:-200ms]" />
            <span className="size-2 animate-pulse rounded-full bg-marca-900 [animation-delay:-100ms]" />
            <span className="size-2 animate-pulse rounded-full bg-marca-900" />
          </span>
          Cargando tu sesión…
        </p>
      </div>
    )
  }

  if (!estaAutenticado) {
    return <Navigate to={RUTAS.ingresar} replace state={{ origen: ubicacion }} />
  }

  if (roles && (!rol || !roles.includes(rol))) {
    return <Navigate to={RUTAS.noAutorizado} replace />
  }

  return <>{children}</>
}
