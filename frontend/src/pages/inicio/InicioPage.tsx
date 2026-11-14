import { ArrowRight, CircleAlert, Clock, Scissors } from 'lucide-react'
import { Link } from 'react-router'
import { TarjetaServicio } from '../../components/catalogo/TarjetaServicio'
import { TarjetaServicioCargando } from '../../components/catalogo/TarjetaServicioCargando'
import { RUTAS } from '../../config/rutas'
import { useServicios } from '../../hooks/useServicios'

/** Cuántos servicios se destacan en la portada. */
const DESTACADOS = 3

/**
 * Portada del negocio (RF-09) con las tarjetas destacadas del catálogo
 * (RF-07). Si la API falla, el resto de la página sigue funcionando.
 */
export function InicioPage() {
  const { data: servicios, isPending, isError, refetch } = useServicios()
  const destacados = servicios?.slice(0, DESTACADOS) ?? []

  return (
    <>
      <section className="mx-auto w-full max-w-6xl px-4 py-12 sm:py-16">
        <div className="grid items-end gap-10 lg:grid-cols-[1.5fr_1fr]">
          <div>
            <p className="font-mono text-[11px] font-medium uppercase tracking-[0.14em] text-marca-500">
              Barbería en Ipiales
            </p>
            <h1 className="mt-3 font-display text-4xl font-semibold leading-[1.02] sm:text-6xl">
              Bienvenido a OalBarber
            </h1>
            <p className="mt-4 max-w-xl text-base leading-relaxed text-texto-suave">
              Reserva tu cita en línea, elige el servicio y el horario que te sirva. El pago se
              hace en el local, sin pasarelas ni anticipos.
            </p>
            <div className="mt-6 flex flex-wrap gap-3">
              <Link
                to={RUTAS.servicios}
                className="inline-flex min-h-12 items-center gap-2 rounded-full bg-marca-900 px-6 font-mono text-xs font-medium uppercase tracking-[0.14em] text-white shadow-sm transition-[background-color,transform] duration-150 hover:bg-marca-800 active:translate-y-px"
              >
                Ver servicios
                <ArrowRight aria-hidden="true" className="size-4" />
              </Link>
              <Link
                to={RUTAS.reservar}
                className="inline-flex min-h-12 items-center rounded-full border border-marca-300 px-6 font-mono text-xs font-medium uppercase tracking-[0.14em] text-texto transition-colors duration-150 hover:border-marca-900 hover:bg-marca-50 active:translate-y-px"
              >
                Reservar cita
              </Link>
            </div>
          </div>

          <aside
            aria-labelledby="horario-portada"
            className="rounded-3xl border border-marca-300 bg-superficie p-6 shadow-[0_18px_36px_-32px_rgba(0,0,0,0.4)]"
          >
            <h2
              id="horario-portada"
              className="flex items-center gap-2 font-mono text-[11px] font-medium uppercase tracking-[0.16em] text-marca-500"
            >
              <Clock aria-hidden="true" className="size-3.5" />
              Horario
            </h2>
            <p className="mt-4 font-display text-2xl font-semibold">Lunes a sábado</p>
            <p className="mt-1 font-mono text-sm tabular-nums text-texto-suave">
              10:00 a. m. – 7:00 p. m.
            </p>
            <p className="mt-4 border-t border-marca-200 pt-3 text-sm text-texto-suave">
              Domingo cerrado.
            </p>
          </aside>
        </div>
      </section>

      <section
        aria-labelledby="servicios-destacados"
        className="border-t border-borde bg-superficie"
      >
        <div className="mx-auto w-full max-w-6xl px-4 py-12 sm:py-14">
          <div className="flex flex-wrap items-end justify-between gap-4">
            <div>
              <p className="font-mono text-[11px] font-medium uppercase tracking-[0.14em] text-marca-500">
                Catálogo
              </p>
              <h2
                id="servicios-destacados"
                className="mt-2 font-display text-3xl font-semibold sm:text-4xl"
              >
                Servicios destacados
              </h2>
            </div>
            <Link
              to={RUTAS.servicios}
              className="inline-flex min-h-11 items-center gap-1.5 font-mono text-[11px] font-medium uppercase tracking-[0.14em] text-texto underline decoration-marca-300 decoration-1 underline-offset-4 transition-colors hover:decoration-marca-900 hover:decoration-2"
            >
              Ver todo el catálogo
              <ArrowRight aria-hidden="true" className="size-3.5" />
            </Link>
          </div>

          {isPending && (
            <div
              className="mt-6 grid gap-5 sm:grid-cols-2 lg:grid-cols-3"
              role="status"
              aria-live="polite"
            >
              <span className="sr-only">Cargando servicios…</span>
              <TarjetaServicioCargando />
              <TarjetaServicioCargando />
              <TarjetaServicioCargando />
            </div>
          )}

          {isError && (
            <div
              role="alert"
              className="animar-entrada mt-6 flex items-start gap-3 rounded-2xl border border-error/30 bg-error/5 px-4 py-3 text-sm text-error"
            >
              <CircleAlert aria-hidden="true" className="mt-0.5 size-4 shrink-0" />
              <div className="flex-1">
                <p className="font-semibold">No pudimos cargar los servicios destacados</p>
                <p className="mt-0.5">Puedes ver el catálogo completo o intentar de nuevo.</p>
                <button
                  type="button"
                  onClick={() => refetch()}
                  className="mt-3 min-h-11 rounded-full border border-error/40 px-4 font-mono text-xs font-medium uppercase tracking-[0.14em] text-error transition-colors duration-150 hover:bg-error/10 active:translate-y-px"
                >
                  Reintentar
                </button>
              </div>
            </div>
          )}

          {!isPending && !isError && destacados.length === 0 && (
            <div className="mt-6 rounded-2xl border border-dashed border-marca-300 bg-fondo px-6 py-10 text-center">
              <span className="mx-auto flex size-12 items-center justify-center rounded-full border border-marca-200 bg-superficie">
                <Scissors aria-hidden="true" className="size-5 text-marca-500" />
              </span>
              <p className="mt-4 text-base font-semibold">Todavía no hay servicios publicados</p>
              <p className="mx-auto mt-1 max-w-prose text-sm text-texto-suave">
                Cuando el catálogo esté listo, los servicios aparecerán aquí.
              </p>
            </div>
          )}

          {!isPending && !isError && destacados.length > 0 && (
            <ul className="mt-6 grid list-none gap-5 sm:grid-cols-2 lg:grid-cols-3">
              {destacados.map((servicio) => (
                <li key={servicio.id}>
                  <TarjetaServicio servicio={servicio} />
                </li>
              ))}
            </ul>
          )}
        </div>
      </section>
    </>
  )
}
