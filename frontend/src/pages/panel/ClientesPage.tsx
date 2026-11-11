import { useQuery } from '@tanstack/react-query'
import { CircleAlert, Search, Users } from 'lucide-react'
import { useMemo, useState } from 'react'
import { AdornoTijeras } from '../../components/comunes/AdornoTijeras'
import { listarClientes } from '../../services/clientesService'

/** Dos iniciales para el avatar: primer nombre + primer apellido. */
function iniciales(nombre: string): string {
  return nombre
    .trim()
    .split(/\s+/)
    .slice(0, 2)
    .map((parte) => parte.charAt(0).toUpperCase())
    .join('')
}

function fechaCorta(valor: string): string {
  return new Date(valor).toLocaleDateString('es-CO', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  })
}

/** RF-05: los clientes registrados, con buscador y estado de cada cuenta. */
export function ClientesPage() {
  const { data: clientes, isPending, isError, refetch } = useQuery({
    queryKey: ['clientes'],
    queryFn: listarClientes,
  })
  const [busqueda, setBusqueda] = useState('')

  const filtrados = useMemo(() => {
    const consulta = busqueda.trim().toLowerCase()
    if (!clientes) {
      return []
    }
    if (!consulta) {
      return clientes
    }
    return clientes.filter(
      (cliente) =>
        cliente.nombre.toLowerCase().includes(consulta) ||
        cliente.celular.includes(consulta),
    )
  }, [clientes, busqueda])

  return (
    <section className="px-4 py-6 sm:px-6 sm:py-8">
      <div className="mx-auto w-full max-w-4xl">
        <div className="flex items-baseline justify-between gap-4">
          <p className="font-mono text-[11px] font-medium uppercase tracking-[0.14em] text-marca-500">
            Panel
          </p>
          {!isPending && !isError && (
            <span className="rounded-full border border-marca-200 px-2.5 py-1 font-mono text-[11px] tabular-nums text-marca-500">
              {filtrados.length}/{clientes?.length ?? 0}
            </span>
          )}
        </div>

        <h1 className="mt-3 font-display text-4xl font-semibold leading-[1.05] sm:text-5xl">
          Clientes
        </h1>
        <p className="mt-3 max-w-xl text-sm text-texto-suave sm:text-base">
          Las cuentas registradas en la barbería. Busca por nombre o celular.
        </p>

        <div className="relative mt-6">
          <Search
            aria-hidden="true"
            className="pointer-events-none absolute left-3.5 top-1/2 size-4 -translate-y-1/2 text-marca-400"
          />
          <input
            type="search"
            value={busqueda}
            onChange={(evento) => setBusqueda(evento.target.value)}
            placeholder="Buscar cliente…"
            aria-label="Buscar cliente por nombre o celular"
            className="h-11 w-full rounded-xl border border-marca-300 bg-superficie pl-10 pr-4 text-base text-texto placeholder:text-marca-400 transition-[border-color,box-shadow] duration-200 hover:border-marca-400 focus:border-marca-900 focus:shadow-[0_0_0_4px_rgba(10,10,10,0.07)] focus:outline-none"
          />
        </div>

        {isPending && (
          <div className="mt-5 space-y-2" role="status" aria-live="polite">
            <span className="sr-only">Cargando clientes…</span>
            {[1, 2, 3].map((sombra) => (
              <div
                key={sombra}
                aria-hidden="true"
                className="flex h-16 animate-pulse items-center gap-4 rounded-2xl border border-marca-200 bg-superficie px-4"
              >
                <span className="size-10 shrink-0 rounded-full bg-marca-100" />
                <span className="h-3.5 w-40 rounded-full bg-marca-100" />
              </div>
            ))}
          </div>
        )}

        {isError && (
          <div
            role="alert"
            className="animar-entrada mt-5 flex items-start gap-3 rounded-2xl border border-error/30 bg-error/5 px-4 py-3 text-sm text-error"
          >
            <CircleAlert aria-hidden="true" className="mt-0.5 size-4 shrink-0" />
            <div className="flex-1">
              <p className="font-semibold">No pudimos cargar los clientes</p>
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

        {!isPending && !isError && clientes && clientes.length === 0 && (
          <div className="mt-6">
            <AdornoTijeras className="mb-6" />
            <div className="rounded-2xl border border-dashed border-marca-300 bg-superficie px-6 py-10 text-center">
              <span className="mx-auto flex size-12 items-center justify-center rounded-full border border-marca-200 bg-marca-50">
                <Users aria-hidden="true" className="size-5 text-marca-500" />
              </span>
              <p className="mt-4 text-base font-semibold">Aún no hay clientes</p>
              <p className="mx-auto mt-1 max-w-prose text-sm text-texto-suave">
                Cuando alguien cree su cuenta, aparecerá aquí.
              </p>
            </div>
          </div>
        )}

        {!isPending && !isError && clientes && clientes.length > 0 && filtrados.length === 0 && (
          <p className="mt-6 rounded-2xl border border-dashed border-marca-300 bg-superficie px-6 py-8 text-center text-sm text-texto-suave">
            Sin resultados para <span className="font-medium text-texto">“{busqueda.trim()}”</span>.
          </p>
        )}

        {!isPending && !isError && filtrados.length > 0 && (
          <ul className="mt-5 divide-y divide-borde overflow-hidden rounded-2xl border border-marca-300 bg-superficie shadow-[0_1px_2px_rgba(0,0,0,0.04)]">
            {filtrados.map((cliente) => (
              <li
                key={cliente.id}
                className="flex items-center gap-3.5 px-4 py-3.5 transition-colors duration-150 hover:bg-marca-50 sm:px-5"
              >
                <span
                  aria-hidden="true"
                  className="flex size-10 shrink-0 items-center justify-center rounded-full border border-marca-200 bg-marca-900 font-mono text-xs font-medium tracking-wide text-white"
                >
                  {iniciales(cliente.nombre)}
                </span>
                <div className="min-w-0 flex-1">
                  <p className="truncate font-medium text-texto">{cliente.nombre}</p>
                  <p className="mt-0.5 font-mono text-[13px] tabular-nums text-marca-500">
                    {cliente.celular}
                  </p>
                </div>
                <span
                  className="hidden shrink-0 text-right text-[13px] text-marca-500 sm:block"
                  aria-hidden="true"
                >
                  {fechaCorta(cliente.fechaCreacion)}
                </span>
                <span
                  className={`shrink-0 rounded-full px-2.5 py-1 font-mono text-[10px] uppercase tracking-[0.12em] ${
                    cliente.activo
                      ? 'bg-marca-900 text-white'
                      : 'border border-marca-300 text-marca-500'
                  }`}
                >
                  {cliente.activo ? 'Activo' : 'Inactivo'}
                </span>
              </li>
            ))}
          </ul>
        )}
      </div>
    </section>
  )
}
