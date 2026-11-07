import { Link } from 'react-router'
import { RUTAS } from '../../config/rutas'

/** Se muestra cuando la sesión existe pero el rol no permite la ruta. */
export function NoAutorizadoPage() {
  return (
    <section className="mx-auto w-full max-w-2xl px-4 py-16 text-center">
      <p className="text-xs font-semibold uppercase tracking-widest text-marca-500">Error 403</p>
      <h1 className="mt-2 text-2xl font-bold tracking-tight sm:text-3xl">No autorizado</h1>
      <p className="mt-3 text-sm text-texto-suave sm:text-base">
        Tu rol no permite acceder a esta sección.
      </p>
      <Link
        to={RUTAS.inicio}
        className="mt-6 inline-flex min-h-11 items-center rounded-md bg-marca-700 px-4 text-sm font-semibold text-white hover:bg-marca-800"
      >
        Volver al inicio
      </Link>
    </section>
  )
}
