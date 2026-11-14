import { CircleAlert, Scissors } from 'lucide-react'
import { TarjetaServicio } from '../../components/catalogo/TarjetaServicio'
import { TarjetaServicioCargando } from '../../components/catalogo/TarjetaServicioCargando'
import { AdornoTijeras } from '../../components/comunes/AdornoTijeras'
import { useServicios } from '../../hooks/useServicios'

/** Catálogo público con nombre, duración, precio y foto (RF-07, RN-03). */
export function CatalogoPage() {
  const { data: servicios, isPending, isError, refetch } = useServicios()

  return (
    <section className="mx-auto w-full max-w-6xl px-4 py-10 sm:py-14">
      <p className="font-mono text-[11px] font-medium uppercase tracking-[0.14em] text-marca-500">
        Catálogo
      </p>
      <h1 className="mt-3 font-display text-4xl font-semibold leading-[1.05] sm:text-5xl">
        Servicios
      </h1>
      <p className="mt-3 max-w-2xl text-sm leading-relaxed text-texto-suave sm:text-base">
        Elige el servicio que quieres reservar. Cada tarjeta muestra la duración, el precio y la
        foto; el pago se hace en el local.
      </p>

      {isPending && (
        <div
          className="mt-8 grid gap-5 sm:grid-cols-2 lg:grid-cols-3"
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
          className="animar-entrada mt-8 flex items-start gap-3 rounded-2xl border border-error/30 bg-error/5 px-4 py-3 text-sm text-error"
        >
          <CircleAlert aria-hidden="true" className="mt-0.5 size-4 shrink-0" />
          <div className="flex-1">
            <p className="font-semibold">No pudimos cargar el catálogo</p>
            <p className="mt-0.5">Revisa tu conexión e intenta de nuevo.</p>
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

      {!isPending && !isError && servicios && servicios.length === 0 && (
        <div className="mt-8">
          <AdornoTijeras className="mb-6" />
          <div className="rounded-2xl border border-dashed border-marca-300 bg-superficie px-6 py-12 text-center">
            <span className="mx-auto flex size-12 items-center justify-center rounded-full border border-marca-200 bg-marca-50">
              <Scissors aria-hidden="true" className="size-5 text-marca-500" />
            </span>
            <h2 className="mt-4 font-display text-2xl font-semibold">Todavía no hay servicios</h2>
            <p className="mx-auto mt-1 max-w-prose text-sm text-texto-suave">
              El catálogo se está preparando. Vuelve pronto para ver los servicios disponibles.
            </p>
          </div>
        </div>
      )}

      {!isPending && !isError && servicios && servicios.length > 0 && (
        <div className="mt-8">
          <div className="flex items-baseline justify-between gap-4">
            <h2 className="sr-only">Listado de servicios</h2>
            <span className="ml-auto rounded-full border border-marca-200 px-2.5 py-1 font-mono text-[11px] tabular-nums text-marca-500">
              {servicios.length} {servicios.length === 1 ? 'servicio' : 'servicios'}
            </span>
          </div>
          <ul className="mt-3 grid list-none gap-5 sm:grid-cols-2 lg:grid-cols-3">
            {servicios.map((servicio) => (
              <li key={servicio.id}>
                <TarjetaServicio servicio={servicio} />
              </li>
            ))}
          </ul>
        </div>
      )}
    </section>
  )
}
