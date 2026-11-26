import {
  CalendarPlus,
  Loader2,
  Pencil,
  Trash2,
  X,
} from 'lucide-react'
import { useEffect, useRef, useState, type FormEvent } from 'react'
import { useEliminarExcepcion, useGuardarExcepcion } from '../../hooks/useHorarios'
import type { ApiError } from '../../types/error'
import type { HorarioExcepcionResponse } from '../../types/horario'
import { formatearFechaLocal } from '../../utils/fechas'
import {
  excepcionAFormulario,
  formularioExcepcionVacio,
  solicitudExcepcion,
  validarHoras,
  type ExcepcionFormulario,
} from './horarioFormulario'

const CLASE_ETIQUETA =
  'mb-1.5 block font-mono text-[11px] font-medium uppercase tracking-[0.14em] text-marca-500'
const CLASE_ENTRADA =
  'min-h-11 w-full min-w-0 rounded-xl border border-marca-300 bg-superficie px-3 font-mono text-base tabular-nums text-texto focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-marca-900'
const CLASE_BOTON =
  'flex min-h-11 w-full items-center justify-center gap-2 rounded-full bg-marca-900 px-5 font-mono text-xs font-medium uppercase tracking-[0.14em] text-white transition-colors hover:bg-marca-800 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-marca-900 disabled:cursor-not-allowed disabled:opacity-60 sm:w-auto'
const CLASE_BOTON_SECUNDARIO =
  'flex min-h-11 w-full items-center justify-center gap-2 rounded-full border border-marca-300 px-4 font-mono text-xs font-medium uppercase tracking-[0.12em] text-texto transition-colors hover:border-marca-500 hover:bg-marca-50 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-marca-900 disabled:cursor-not-allowed disabled:opacity-60 sm:w-auto'

interface Props {
  barberoId: string
  excepciones: HorarioExcepcionResponse[]
}

function fechaLegible(fecha: string): string {
  const texto = formatearFechaLocal(fecha)
  return texto.charAt(0).toUpperCase() + texto.slice(1)
}

function detalle(excepcion: HorarioExcepcionResponse): string {
  if (excepcion.cerrado) {
    return 'Cerrado'
  }
  const almuerzo = excepcion.almuerzoInicio
    ? ` · almuerzo ${excepcion.almuerzoInicio} a ${excepcion.almuerzoFin}`
    : ''
  return `${excepcion.horaInicio} a ${excepcion.horaFin}${almuerzo}`
}

/**
 * Fechas que no siguen la semana: un domingo abierto, un festivo cerrado o un
 * día con otro horario. El formulario se abre solo cuando se necesita.
 */
export function FechasEspecialesCard({ barberoId, excepciones }: Props) {
  const guardar = useGuardarExcepcion(barberoId)
  const eliminar = useEliminarExcepcion(barberoId)
  const [formulario, setFormulario] = useState<ExcepcionFormulario | null>(null)
  const [editando, setEditando] = useState<string | null>(null)
  const [confirmando, setConfirmando] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [mensaje, setMensaje] = useState<string | null>(null)
  const formularioRef = useRef<HTMLFormElement | null>(null)
  const errorRef = useRef<HTMLParagraphElement | null>(null)

  const ordenadas = [...excepciones].sort((a, b) => a.fecha.localeCompare(b.fecha))

  /** Al abrir o editar, lleva el formulario a la vista para no dejarlo arriba. */
  useEffect(() => {
    if (formulario) {
      formularioRef.current?.scrollIntoView?.({ block: 'start', behavior: 'smooth' })
    }
  }, [formulario])

  /** El aviso de error recibe el foco para que se anuncie y sea fácil de ubicar. */
  useEffect(() => {
    if (error) {
      errorRef.current?.focus()
    }
  }, [error])

  function cambiar(cambios: Partial<ExcepcionFormulario>) {
    setFormulario((actual) => (actual ? { ...actual, ...cambios } : actual))
    setError(null)
    setMensaje(null)
  }

  function abrirNueva() {
    setFormulario(formularioExcepcionVacio())
    setEditando(null)
    setConfirmando(null)
    setError(null)
    setMensaje(null)
  }

  function abrirEdicion(excepcion: HorarioExcepcionResponse) {
    setFormulario(excepcionAFormulario(excepcion))
    setEditando(excepcion.fecha)
    setConfirmando(null)
    setError(null)
    setMensaje(null)
  }

  function cerrar() {
    setFormulario(null)
    setEditando(null)
    setError(null)
  }

  function enviar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    if (!formulario) {
      return
    }
    if (!formulario.fecha) {
      setError('Elige la fecha de la excepción.')
      return
    }
    if (formulario.atiende) {
      const problema = validarHoras(
        formulario.horaInicio,
        formulario.horaFin,
        formulario.almuerzo,
        formulario.almuerzoInicio,
        formulario.almuerzoFin,
      )
      if (problema) {
        setError(problema)
        return
      }
    }

    setError(null)
    guardar.mutate(
      { fecha: formulario.fecha, ...solicitudExcepcion(formulario) },
      {
        onSuccess: () => {
          setFormulario(null)
          setEditando(null)
          setMensaje('Fecha especial guardada.')
        },
        onError: (fallo) => {
          const api = fallo as Partial<ApiError> | null
          setError(
            api?.erroresCampo?.horario ??
              api?.erroresCampo?.fecha ??
              api?.detail ??
              'No pudimos guardar la fecha especial.',
          )
        },
      },
    )
  }

  function quitar(fecha: string) {
    setError(null)
    setMensaje(null)
    eliminar.mutate(fecha, {
      onSuccess: () => {
        setConfirmando(null)
        setMensaje('Fecha especial eliminada.')
        if (editando === fecha) {
          cerrar()
        }
      },
      onError: (fallo) => {
        setConfirmando(null)
        const api = fallo as Partial<ApiError> | null
        setError(api?.detail ?? 'No pudimos eliminar la fecha especial.')
      },
    })
  }

  return (
    <section
      id="fechas-especiales"
      aria-labelledby="fechas-especiales-titulo"
      className="mt-5 scroll-mt-24 rounded-2xl border border-marca-300 bg-superficie p-4 shadow-[0_1px_2px_rgba(0,0,0,0.04)] sm:p-6"
    >
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div className="flex min-w-0 items-start gap-3">
          <span
            aria-hidden="true"
            className="flex size-10 shrink-0 items-center justify-center rounded-xl border border-marca-200 bg-marca-50"
          >
            <CalendarPlus className="size-5 text-marca-700" />
          </span>
          <div className="min-w-0">
            <h2 id="fechas-especiales-titulo" className="text-lg font-semibold text-texto">
              Fechas especiales
            </h2>
            <p className="mt-1 text-sm text-texto-suave">
              Abre un domingo, cierra un festivo o cambia el horario de una fecha concreta.
              Solo aplica a ese día.
            </p>
          </div>
        </div>
        {!formulario && (
          <button type="button" onClick={abrirNueva} className={CLASE_BOTON_SECUNDARIO}>
            <CalendarPlus aria-hidden="true" className="size-4" />
            Agregar fecha
          </button>
        )}
      </div>

      {formulario ? (
        <form
          ref={formularioRef}
          onSubmit={enviar}
          noValidate
          className="mt-5 scroll-mt-24 rounded-xl border border-marca-200 bg-marca-50/40 p-3.5 sm:p-4"
        >
          <div className="flex items-center justify-between gap-2">
            <h3 className="font-semibold text-texto">
              {editando ? 'Editar fecha especial' : 'Nueva fecha especial'}
            </h3>
            <button
              type="button"
              onClick={cerrar}
              aria-label="Cerrar el formulario"
              className="flex size-11 items-center justify-center rounded-full text-texto-suave hover:bg-marca-100 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-marca-900"
            >
              <X aria-hidden="true" className="size-5" />
            </button>
          </div>

          <div className="mt-3 grid gap-3 sm:grid-cols-2">
            <div className="min-w-0">
              <label htmlFor="excepcion-fecha" className={CLASE_ETIQUETA}>
                Fecha
              </label>
              <input
                id="excepcion-fecha"
                type="date"
                value={formulario.fecha}
                disabled={Boolean(editando)}
                aria-label="Fecha de la excepción"
                aria-invalid={Boolean(error)}
                aria-describedby={error ? 'excepcion-error' : undefined}
                onChange={(evento) => cambiar({ fecha: evento.target.value })}
                className={CLASE_ENTRADA}
              />
            </div>
            <label className="flex min-h-11 items-center gap-3 sm:mt-6">
              <input
                type="checkbox"
                className="size-4 accent-marca-900"
                checked={formulario.atiende}
                aria-label="Atiende ese día"
                onChange={(evento) => cambiar({ atiende: evento.target.checked })}
              />
              <span className="text-sm font-medium text-texto">Atiende ese día</span>
            </label>
          </div>

          {formulario.atiende ? (
            <div className="mt-3 space-y-3">
              <div className="grid grid-cols-2 gap-3">
                <div className="min-w-0">
                  <label htmlFor="excepcion-inicio" className={CLASE_ETIQUETA}>
                    Inicio
                  </label>
                  <input
                    id="excepcion-inicio"
                    type="time"
                    step={1800}
                    value={formulario.horaInicio}
                    aria-label="Hora de inicio de la fecha especial"
                    aria-invalid={Boolean(error)}
                    aria-describedby={error ? 'excepcion-error' : undefined}
                    onChange={(evento) => cambiar({ horaInicio: evento.target.value })}
                    className={CLASE_ENTRADA}
                  />
                </div>
                <div className="min-w-0">
                  <label htmlFor="excepcion-fin" className={CLASE_ETIQUETA}>
                    Fin
                  </label>
                  <input
                    id="excepcion-fin"
                    type="time"
                    step={1800}
                    value={formulario.horaFin}
                    aria-label="Hora de fin de la fecha especial"
                    aria-invalid={Boolean(error)}
                    aria-describedby={error ? 'excepcion-error' : undefined}
                    onChange={(evento) => cambiar({ horaFin: evento.target.value })}
                    className={CLASE_ENTRADA}
                  />
                </div>
              </div>

              <label className="flex min-h-11 items-center gap-3">
                <input
                  type="checkbox"
                  className="size-4 accent-marca-900"
                  checked={formulario.almuerzo}
                  aria-label="Almuerzo de la fecha especial"
                  onChange={(evento) => cambiar({ almuerzo: evento.target.checked })}
                />
                <span className="text-sm text-texto">Almuerzo</span>
              </label>

              {formulario.almuerzo && (
                <div className="grid grid-cols-2 gap-3">
                  <div className="min-w-0">
                    <label htmlFor="excepcion-almuerzo-inicio" className={CLASE_ETIQUETA}>
                      Inicio almuerzo
                    </label>
                    <input
                      id="excepcion-almuerzo-inicio"
                      type="time"
                      step={1800}
                      value={formulario.almuerzoInicio}
                      aria-label="Inicio del almuerzo de la fecha especial"
                      aria-invalid={Boolean(error)}
                      aria-describedby={error ? 'excepcion-error' : undefined}
                      onChange={(evento) =>
                        cambiar({ almuerzoInicio: evento.target.value })
                      }
                      className={CLASE_ENTRADA}
                    />
                  </div>
                  <div className="min-w-0">
                    <label htmlFor="excepcion-almuerzo-fin" className={CLASE_ETIQUETA}>
                      Fin almuerzo
                    </label>
                    <input
                      id="excepcion-almuerzo-fin"
                      type="time"
                      step={1800}
                      value={formulario.almuerzoFin}
                      aria-label="Fin del almuerzo de la fecha especial"
                      aria-invalid={Boolean(error)}
                      aria-describedby={error ? 'excepcion-error' : undefined}
                      onChange={(evento) => cambiar({ almuerzoFin: evento.target.value })}
                      className={CLASE_ENTRADA}
                    />
                  </div>
                </div>
              )}
            </div>
          ) : (
            <p className="mt-3 rounded-xl border border-marca-200 bg-superficie p-3 text-sm text-texto-suave">
              Esa fecha quedará cerrada y no ofrecerá turnos.
            </p>
          )}

          {error && (
            <p
              ref={errorRef}
              id="excepcion-error"
              role="alert"
              tabIndex={-1}
              className="mt-3 rounded-xl border border-error/30 bg-error/5 p-3 text-sm text-error focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-error"
            >
              {error}
            </p>
          )}

          <div className="mt-4 flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-end">
            <button
              type="button"
              onClick={cerrar}
              className={CLASE_BOTON_SECUNDARIO}
            >
              Cancelar
            </button>
            <button type="submit" disabled={guardar.isPending} className={CLASE_BOTON}>
              {guardar.isPending ? (
                <Loader2 aria-hidden="true" className="size-4 animate-spin" />
              ) : (
                <CalendarPlus aria-hidden="true" className="size-4" />
              )}
              {guardar.isPending
                ? 'Guardando'
                : editando
                  ? 'Actualizar fecha'
                  : 'Guardar fecha'}
            </button>
          </div>
        </form>
      ) : (
        <>
          {mensaje && (
            <p
              role="status"
              className="mt-4 rounded-xl border border-marca-300 bg-marca-50 p-3.5 text-sm text-texto"
            >
              {mensaje}
            </p>
          )}

          {error && (
            <p
              role="alert"
              className="mt-4 rounded-xl border border-error/30 bg-error/5 p-3.5 text-sm text-error"
            >
              {error}
            </p>
          )}

          {ordenadas.length === 0 ? (
            <p className="mt-4 rounded-xl border border-marca-200 bg-marca-50 p-3.5 text-sm text-texto-suave">
              No hay fechas especiales. Por ejemplo, puedes abrir un domingo de diciembre o
              cerrar un festivo.
            </p>
          ) : (
            <ul className="mt-4 space-y-2">
              {ordenadas.map((excepcion) => (
                <li
                  key={excepcion.fecha}
                  className="flex flex-wrap items-center justify-between gap-3 rounded-xl border border-marca-200 bg-superficie p-3"
                >
                  <div className="min-w-0">
                    <p className="font-medium text-texto">{fechaLegible(excepcion.fecha)}</p>
                    <p className="mt-0.5 text-sm text-texto-suave">{detalle(excepcion)}</p>
                  </div>
                  {confirmando === excepcion.fecha ? (
                    <div className="w-full rounded-xl border border-error/30 bg-error/5 p-3">
                      <p className="text-sm text-texto">
                        ¿Quitar esta fecha? Volverá a regirse por el horario semanal.
                      </p>
                      <div className="mt-2 flex flex-wrap gap-2">
                        <button
                          type="button"
                          onClick={() => quitar(excepcion.fecha)}
                          disabled={eliminar.isPending}
                          className="flex min-h-11 items-center justify-center gap-1.5 rounded-full bg-error px-4 font-mono text-[11px] uppercase tracking-[0.08em] text-white hover:opacity-90 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-error disabled:cursor-not-allowed disabled:opacity-60"
                        >
                          {eliminar.isPending ? (
                            <Loader2 aria-hidden="true" className="size-3.5 animate-spin" />
                          ) : (
                            <Trash2 aria-hidden="true" className="size-3.5" />
                          )}
                          Sí, quitar
                        </button>
                        <button
                          type="button"
                          onClick={() => setConfirmando(null)}
                          className={CLASE_BOTON_SECUNDARIO}
                        >
                          Cancelar
                        </button>
                      </div>
                    </div>
                  ) : (
                    <div className="flex gap-2">
                      <button
                        type="button"
                        onClick={() => abrirEdicion(excepcion)}
                        aria-label={`Editar la fecha ${excepcion.fecha}`}
                        className="flex min-h-11 items-center gap-1.5 rounded-full border border-marca-300 px-3 font-mono text-[11px] uppercase tracking-[0.08em] text-texto hover:bg-marca-50 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-marca-900"
                      >
                        <Pencil aria-hidden="true" className="size-3.5" />
                        Editar
                      </button>
                      <button
                        type="button"
                        onClick={() => setConfirmando(excepcion.fecha)}
                        aria-label={`Quitar la fecha ${excepcion.fecha}`}
                        className="flex min-h-11 items-center gap-1.5 rounded-full border border-error/30 px-3 font-mono text-[11px] uppercase tracking-[0.08em] text-error hover:bg-error/5 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-error disabled:cursor-not-allowed disabled:opacity-60"
                      >
                        <Trash2 aria-hidden="true" className="size-3.5" />
                        Quitar
                      </button>
                    </div>
                  )}
                </li>
              ))}
            </ul>
          )}
        </>
      )}
    </section>
  )
}
