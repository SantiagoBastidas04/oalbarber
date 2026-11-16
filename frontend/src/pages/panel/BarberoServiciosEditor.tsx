import { CircleAlert, Loader2, Save } from 'lucide-react'
import { useMemo, useState } from 'react'
import { Link } from 'react-router'
import { useGuardarServiciosBarbero } from '../../hooks/useBarberos'
import type { ApiError } from '../../types/error'
import type { BarberoAdminResponse } from '../../types/barbero'
import type { ServicioResponse } from '../../types/servicio'

const formatoPrecio = new Intl.NumberFormat('es-CO', {
  style: 'currency',
  currency: 'COP',
  maximumFractionDigits: 0,
})

interface BarberoServiciosEditorProps {
  barbero: BarberoAdminResponse
  servicios?: ServicioResponse[]
  cargandoCatalogo: boolean
  errorCatalogo: boolean
  reintentarCatalogo: () => void
}

function errorGuardar(error: unknown) {
  const apiError = error as Partial<ApiError> | null
  return {
    title: apiError?.title || 'No pudimos guardar los servicios',
    detail: apiError?.detail || 'Revisa la conexión e intenta de nuevo.',
    codigo: apiError?.codigo,
    erroresCampo: apiError?.erroresCampo ?? {},
  }
}

function mismosIds(primero: string[], segundo: string[]) {
  return primero.length === segundo.length && primero.every((id) => segundo.includes(id))
}

/** Selector de servicios activos del catálogo para un perfil. */
export function BarberoServiciosEditor({
  barbero,
  servicios,
  cargandoCatalogo,
  errorCatalogo,
  reintentarCatalogo,
}: BarberoServiciosEditorProps) {
  const guardar = useGuardarServiciosBarbero()
  const idsActivosAsignados = useMemo(
    () => (servicios ?? [])
      .filter((servicio) => barbero.servicioIds.includes(servicio.id))
      .map((servicio) => servicio.id),
    [barbero.servicioIds, servicios],
  )
  const claveInicial = [...idsActivosAsignados].sort().join(',')
  const [estadoSeleccion, setEstadoSeleccion] = useState({
    base: claveInicial,
    ids: idsActivosAsignados,
  })
  const seleccionados = estadoSeleccion.base === claveInicial
    ? estadoSeleccion.ids
    : idsActivosAsignados
  const [guardado, setGuardado] = useState(false)
  const error = guardar.isError ? errorGuardar(guardar.error) : null
  const errorCampo = error?.erroresCampo.servicioIds

  const hayCambios = !mismosIds(seleccionados, idsActivosAsignados)
  const nombre = barbero.nombrePublico?.trim() || barbero.nombre

  const alternarServicio = (id: string, marcado: boolean) => {
    setGuardado(false)
    const nuevosIds = marcado
      ? [...seleccionados, id]
      : seleccionados.filter((actual) => actual !== id)
    setEstadoSeleccion({ base: claveInicial, ids: nuevosIds })
  }

  const guardarCambios = async () => {
    try {
      await guardar.mutateAsync({ id: barbero.id, servicioIds: seleccionados })
      setGuardado(true)
    } catch {
      setGuardado(false)
    }
  }

  return (
    <div className="mt-4 w-full rounded-xl border border-marca-200 bg-marca-50/50 p-4 sm:ml-14 sm:w-auto sm:flex-1">
      <fieldset disabled={guardar.isPending}>
        <legend className="font-medium text-texto">Servicios de {nombre}</legend>

        {cargandoCatalogo && (
          <p className="mt-2 text-sm text-texto-suave" role="status">Cargando servicios…</p>
        )}

        {errorCatalogo && (
          <div role="alert" className="mt-2 flex flex-wrap items-center gap-2 text-sm text-error">
            <CircleAlert aria-hidden="true" className="size-4 shrink-0" />
            <span>No pudimos cargar el catálogo.</span>
            <button type="button" onClick={reintentarCatalogo} className="min-h-9 rounded-full border border-error/40 px-3 font-medium hover:bg-error/10">
              Reintentar
            </button>
          </div>
        )}

        {!cargandoCatalogo && !errorCatalogo && servicios?.length === 0 && (
          <p className="mt-2 text-sm text-texto-suave">
            No hay servicios activos.{' '}
            <Link to="/panel/servicios" className="font-medium text-marca-800 underline underline-offset-2">
              Administrar catálogo
            </Link>
          </p>
        )}

        {!cargandoCatalogo && !errorCatalogo && Boolean(servicios?.length) && (
          <>
            <ul className="mt-2 grid gap-2 sm:grid-cols-2">
              {servicios?.map((servicio) => (
                <li key={servicio.id}>
                  <label className="flex min-h-12 cursor-pointer items-start gap-3 rounded-lg border border-marca-200 bg-superficie p-3 hover:border-marca-500">
                    <input
                      type="checkbox"
                      checked={seleccionados.includes(servicio.id)}
                      onChange={(evento) => alternarServicio(servicio.id, evento.target.checked)}
                      className="mt-0.5 size-4 accent-marca-900 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-marca-900"
                    />
                    <span className="min-w-0">
                      <span className="block font-medium text-texto">{servicio.nombre}</span>
                      <span className="mt-0.5 block text-xs text-texto-suave">
                        {servicio.duracionMin} min · {formatoPrecio.format(servicio.precio)}
                      </span>
                    </span>
                  </label>
                </li>
              ))}
            </ul>
            {seleccionados.length === 0 && (
              <p className="mt-2 text-sm text-texto-suave">
                Aún no ofrece servicios; no aparecerá para reservar.
              </p>
            )}
            {errorCampo && <p className="mt-2 text-sm text-error" role="alert">{errorCampo}</p>}
            <div className="mt-3 flex flex-wrap items-center gap-3">
              <button
                type="button"
                disabled={guardar.isPending || !hayCambios}
                onClick={guardarCambios}
                className="flex min-h-10 items-center justify-center gap-2 rounded-full bg-marca-900 px-4 font-mono text-[11px] font-medium uppercase tracking-[0.12em] text-white transition-colors hover:bg-marca-800 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-marca-900 disabled:cursor-not-allowed disabled:opacity-50"
              >
                {guardar.isPending
                  ? <Loader2 aria-hidden="true" className="size-4 animate-spin" />
                  : <Save aria-hidden="true" className="size-4" />}
                {guardar.isPending ? 'Guardando…' : 'Guardar servicios'}
              </button>
              {guardado && <span role="status" className="text-sm text-marca-800">Servicios actualizados.</span>}
            </div>
            {error && (
              <div role="alert" className="mt-3 rounded-lg border border-error/30 bg-error/5 p-3 text-sm text-error">
                <p className="font-semibold">{error.title}</p>
                <p className="mt-1">{error.detail}</p>
                {error.codigo && <p className="mt-1 font-mono text-xs">Código: {error.codigo}</p>}
                {Object.entries(error.erroresCampo).map(([campo, mensaje]) => (
                  <p key={campo} className="mt-1">{campo}: {mensaje}</p>
                ))}
              </div>
            )}
          </>
        )}
      </fieldset>
    </div>
  )
}
