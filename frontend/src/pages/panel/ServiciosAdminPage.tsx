import { CircleAlert, Pencil, Plus, Power, Scissors, X } from 'lucide-react'
import { useState } from 'react'
import { AdornoTijeras } from '../../components/comunes/AdornoTijeras'
import { ImagenServicio } from '../../components/comunes/ImagenServicio'
import { FormularioServicio } from '../../components/panel/FormularioServicio'
import {
  useActualizarServicio,
  useCambiarEstadoServicio,
  useCrearServicio,
  useServicios,
} from '../../hooks/useServicios'
import type { ServicioRequest, ServicioResponse } from '../../types/servicio'
import { formatearMoneda } from '../../utils/moneda'

const CLASE_ACCION =
  'inline-flex min-h-9 items-center gap-1.5 rounded-full border border-marca-300 px-3 font-mono text-[10px] font-medium uppercase tracking-[0.12em] text-texto transition-colors duration-150 hover:border-marca-900 hover:bg-marca-50 active:translate-y-px disabled:cursor-not-allowed disabled:opacity-60'

/**
 * RF-08: el administrador crea, edita y desactiva servicios. Desactivar es
 * lógico (RN-16): el servicio sale del catálogo público pero su registro y sus
 * datos siguen intactos para las citas históricas.
 */
export function ServiciosAdminPage() {
  const { data: servicios, isPending, isError, refetch } = useServicios(true)
  const crear = useCrearServicio()
  const actualizar = useActualizarServicio()
  const cambiarEstado = useCambiarEstadoServicio()

  const [formularioAbierto, setFormularioAbierto] = useState(false)
  const [enEdicion, setEnEdicion] = useState<ServicioResponse | null>(null)
  const [errorEstado, setErrorEstado] = useState<string | null>(null)

  const abrirParaCrear = () => {
    setEnEdicion(null)
    setFormularioAbierto(true)
  }

  const abrirParaEditar = (servicio: ServicioResponse) => {
    setEnEdicion(servicio)
    setFormularioAbierto(true)
  }

  const cerrarFormulario = () => {
    setFormularioAbierto(false)
    setEnEdicion(null)
  }

  const guardar = async (datos: ServicioRequest) => {
    if (enEdicion) {
      await actualizar.mutateAsync({ id: enEdicion.id, datos })
    } else {
      await crear.mutateAsync(datos)
    }
    cerrarFormulario()
  }

  const alternarEstado = async (servicio: ServicioResponse) => {
    setErrorEstado(null)
    try {
      await cambiarEstado.mutateAsync({ id: servicio.id, activo: !servicio.activo })
    } catch {
      setErrorEstado('No pudimos cambiar el estado del servicio. Intenta de nuevo.')
    }
  }

  return (
    <section className="px-4 py-6 sm:px-6 sm:py-8">
      <div className="mx-auto w-full max-w-5xl">
        <div className="flex items-baseline justify-between gap-4">
          <p className="font-mono text-[11px] font-medium uppercase tracking-[0.14em] text-marca-500">
            Panel
          </p>
          {!isPending && !isError && (
            <span className="rounded-full border border-marca-200 px-2.5 py-1 font-mono text-[11px] tabular-nums text-marca-500">
              {servicios?.length ?? 0}{' '}
              {(servicios?.length ?? 0) === 1 ? 'servicio' : 'servicios'}
            </span>
          )}
        </div>

        <div className="mt-3 flex flex-wrap items-end justify-between gap-4">
          <div>
            <h1 className="font-display text-4xl font-semibold leading-[1.05] sm:text-5xl">
              Servicios
            </h1>
            <p className="mt-3 max-w-xl text-sm text-texto-suave sm:text-base">
              Crea, edita y desactiva los servicios del catálogo. Desactivar no borra: las
              citas anteriores conservan su precio y su duración.
            </p>
          </div>
          <button
            type="button"
            onClick={abrirParaCrear}
            className="inline-flex min-h-11 items-center gap-2 rounded-full bg-marca-900 px-5 font-mono text-[11px] font-medium uppercase tracking-[0.14em] text-white transition-[background-color,transform] duration-150 hover:bg-marca-800 active:translate-y-px"
          >
            <Plus aria-hidden="true" className="size-3.5" />
            Nuevo servicio
          </button>
        </div>

        {errorEstado && (
          <div
            role="alert"
            className="mt-5 flex items-start gap-2.5 rounded-2xl border border-error/30 bg-error/5 px-4 py-3 text-sm text-error"
          >
            <CircleAlert aria-hidden="true" className="mt-0.5 size-4 shrink-0" />
            <p>{errorEstado}</p>
          </div>
        )}

        {isPending && (
          <div className="mt-6 space-y-2" role="status" aria-live="polite">
            <span className="sr-only">Cargando servicios…</span>
            {[1, 2, 3].map((sombra) => (
              <div
                key={sombra}
                aria-hidden="true"
                className="flex h-16 animate-pulse items-center gap-4 rounded-2xl border border-marca-200 bg-superficie px-4"
              >
                <span className="size-11 shrink-0 rounded-lg bg-marca-100" />
                <span className="h-3.5 w-40 rounded-full bg-marca-100" />
                <span className="ml-auto h-3.5 w-20 rounded-full bg-marca-100" />
              </div>
            ))}
          </div>
        )}

        {isError && (
          <div
            role="alert"
            className="animar-entrada mt-6 flex items-start gap-3 rounded-2xl border border-error/30 bg-error/5 px-4 py-3 text-sm text-error"
          >
            <CircleAlert aria-hidden="true" className="mt-0.5 size-4 shrink-0" />
            <div className="flex-1">
              <p className="font-semibold">No pudimos cargar los servicios</p>
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
          <div className="mt-6">
            <AdornoTijeras className="mb-6" />
            <div className="rounded-2xl border border-dashed border-marca-300 bg-superficie px-6 py-10 text-center">
              <span className="mx-auto flex size-12 items-center justify-center rounded-full border border-marca-200 bg-marca-50">
                <Scissors aria-hidden="true" className="size-5 text-marca-500" />
              </span>
              <p className="mt-4 text-base font-semibold">Todavía no hay servicios</p>
              <p className="mx-auto mt-1 max-w-prose text-sm text-texto-suave">
                Crea el primero y aparecerá en el catálogo público.
              </p>
              <button
                type="button"
                onClick={abrirParaCrear}
                className="mt-4 inline-flex min-h-11 items-center gap-2 rounded-full border border-marca-300 px-5 font-mono text-[11px] font-medium uppercase tracking-[0.14em] text-texto transition-colors duration-150 hover:border-marca-900 hover:bg-marca-50 active:translate-y-px"
              >
                <Plus aria-hidden="true" className="size-3.5" />
                Crear el primero
              </button>
            </div>
          </div>
        )}

        {!isPending && !isError && servicios && servicios.length > 0 && (
          <div className="mt-6 overflow-x-auto rounded-2xl border border-marca-300 bg-superficie shadow-[0_1px_2px_rgba(0,0,0,0.04)]">
            <table className="w-full min-w-[620px] border-collapse text-left text-sm">
              <caption className="sr-only">Servicios del catálogo</caption>
              <thead>
                <tr className="border-b border-borde">
                  <th
                    scope="col"
                    className="px-4 py-3 font-mono text-[10px] font-medium uppercase tracking-[0.14em] text-marca-500 sm:px-5"
                  >
                    Servicio
                  </th>
                  <th
                    scope="col"
                    className="px-3 py-3 font-mono text-[10px] font-medium uppercase tracking-[0.14em] text-marca-500"
                  >
                    Duración
                  </th>
                  <th
                    scope="col"
                    className="px-3 py-3 font-mono text-[10px] font-medium uppercase tracking-[0.14em] text-marca-500"
                  >
                    Precio
                  </th>
                  <th
                    scope="col"
                    className="px-3 py-3 font-mono text-[10px] font-medium uppercase tracking-[0.14em] text-marca-500"
                  >
                    Estado
                  </th>
                  <th scope="col" className="px-4 py-3 sm:px-5">
                    <span className="sr-only">Acciones</span>
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-borde">
                {servicios.map((servicio) => (
                  <tr
                    key={servicio.id}
                    className="transition-colors duration-150 hover:bg-marca-50"
                  >
                    <td className="px-4 py-3.5 sm:px-5">
                      <div className="flex items-center gap-3">
                        <ImagenServicio
                          archivo={servicio.imagen}
                          nombre={servicio.nombre}
                          className="size-11 shrink-0 rounded-lg border border-marca-200"
                        />
                        <div className="min-w-0">
                          <p className="truncate font-medium text-texto">{servicio.nombre}</p>
                          {servicio.descripcion && (
                            <p className="mt-0.5 max-w-[24ch] truncate text-xs text-marca-500 sm:max-w-[36ch]">
                              {servicio.descripcion}
                            </p>
                          )}
                        </div>
                      </div>
                    </td>
                    <td className="whitespace-nowrap px-3 py-3.5 font-mono text-[13px] tabular-nums text-texto-suave">
                      {servicio.duracionMin} min
                    </td>
                    <td className="whitespace-nowrap px-3 py-3.5 font-mono text-[13px] tabular-nums text-texto">
                      {formatearMoneda(servicio.precio)}
                    </td>
                    <td className="whitespace-nowrap px-3 py-3.5">
                      <span
                        className={`inline-block rounded-full px-2.5 py-1 font-mono text-[10px] uppercase tracking-[0.12em] ${
                          servicio.activo
                            ? 'bg-marca-900 text-white'
                            : 'border border-marca-300 text-marca-500'
                        }`}
                      >
                        {servicio.activo ? 'Activo' : 'Inactivo'}
                      </span>
                    </td>
                    <td className="px-4 py-3.5 sm:px-5">
                      <div className="flex justify-end gap-2">
                        <button
                          type="button"
                          onClick={() => abrirParaEditar(servicio)}
                          aria-label={`Editar ${servicio.nombre}`}
                          className={CLASE_ACCION}
                        >
                          <Pencil aria-hidden="true" className="size-3" />
                          Editar
                        </button>
                        <button
                          type="button"
                          onClick={() => alternarEstado(servicio)}
                          aria-label={`${servicio.activo ? 'Desactivar' : 'Activar'} ${
                            servicio.nombre
                          }`}
                          disabled={cambiarEstado.isPending}
                          className={CLASE_ACCION}
                        >
                          <Power aria-hidden="true" className="size-3" />
                          {servicio.activo ? 'Desactivar' : 'Activar'}
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {formularioAbierto && (
        <div className="fixed inset-0 z-50 flex items-start justify-center overflow-y-auto bg-marca-950/40 p-4 sm:p-8">
          <div
            role="dialog"
            aria-modal="true"
            aria-labelledby="titulo-formulario-servicio"
            className="w-full max-w-lg rounded-3xl border border-marca-300 bg-superficie p-5 shadow-[0_24px_60px_-24px_rgba(0,0,0,0.45)] sm:p-6"
          >
            <div className="flex items-start justify-between gap-4">
              <div>
                <p className="font-mono text-[11px] font-medium uppercase tracking-[0.14em] text-marca-500">
                  {enEdicion ? 'Editar' : 'Nuevo'}
                </p>
                <h2
                  id="titulo-formulario-servicio"
                  className="mt-1 font-display text-2xl font-semibold"
                >
                  {enEdicion ? 'Editar servicio' : 'Nuevo servicio'}
                </h2>
              </div>
              <button
                type="button"
                onClick={cerrarFormulario}
                aria-label="Cerrar formulario"
                className="inline-flex size-9 items-center justify-center rounded-full border border-marca-200 text-marca-500 transition-colors duration-150 hover:border-marca-900 hover:text-texto"
              >
                <X aria-hidden="true" className="size-4" />
              </button>
            </div>

            <FormularioServicio
              key={enEdicion?.id ?? 'nuevo'}
              servicio={enEdicion}
              guardando={crear.isPending || actualizar.isPending}
              alGuardar={guardar}
              alCancelar={cerrarFormulario}
            />
          </div>
        </div>
      )}
    </section>
  )
}
