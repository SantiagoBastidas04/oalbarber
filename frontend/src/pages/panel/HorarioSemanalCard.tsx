import {
  CalendarCheck,
  CalendarClock,
  CircleAlert,
  Copy,
  Loader2,
  Save,
} from 'lucide-react'
import { useState, type FormEvent } from 'react'
import { useGuardarHorarios } from '../../hooks/useHorarios'
import type { ApiError } from '../../types/error'
import type { HorarioDiaResponse } from '../../types/horario'
import {
  aSolicitud,
  DIAS,
  DOMINGO,
  nombreDia,
  resumenDia,
  semanaDesdeRespuesta,
  semanaPorDefecto,
  validarSemana,
  type DiaFormulario,
} from './horarioFormulario'

const CLASE_ETIQUETA =
  'mb-1.5 block font-mono text-[11px] font-medium uppercase tracking-[0.14em] text-marca-500'
const CLASE_ENTRADA =
  'min-h-11 w-full min-w-0 rounded-xl border border-marca-300 bg-superficie px-3 font-mono text-base tabular-nums text-texto focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-marca-900'
const CLASE_BOTON =
  'flex min-h-11 w-full items-center justify-center gap-2 rounded-full bg-marca-900 px-5 font-mono text-xs font-medium uppercase tracking-[0.14em] text-white transition-colors hover:bg-marca-800 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-marca-900 disabled:cursor-not-allowed disabled:opacity-60 sm:w-auto'
const CLASE_BOTON_SECUNDARIO =
  'flex min-h-11 w-full items-center justify-center gap-2 rounded-full border border-marca-300 px-5 font-mono text-xs font-medium uppercase tracking-[0.14em] text-texto transition-colors hover:border-marca-500 hover:bg-marca-50 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-marca-900 disabled:cursor-not-allowed disabled:opacity-60 sm:w-auto'

interface Props {
  barberoId: string
  diasIniciales: HorarioDiaResponse[]
}

/**
 * Semana en pestañas: solo se ve el día elegido, sin una lista larga.
 * Incluye el atajo de horario estándar para configurar lunes a sábado de una vez.
 */
export function HorarioSemanalCard({ barberoId, diasIniciales }: Props) {
  const guardar = useGuardarHorarios(barberoId)
  const [semana, setSemana] = useState<DiaFormulario[]>(
    () => semanaDesdeRespuesta(diasIniciales),
  )
  const [diaActivo, setDiaActivo] = useState(() => {
    const inicial = semanaDesdeRespuesta(diasIniciales)
    return inicial.find((dia) => dia.trabaja && dia.numero !== DOMINGO)?.numero ?? 1
  })
  const [errores, setErrores] = useState<Record<number, string>>({})
  const [mensaje, setMensaje] = useState<string | null>(null)
  const [errorGeneral, setErrorGeneral] = useState<{ title: string; detail: string } | null>(
    null,
  )

  const dia = semana.find((opcion) => opcion.numero === diaActivo) ?? semana[0]
  const esDomingo = diaActivo === DOMINGO
  const nombre = nombreDia(diaActivo).toLowerCase()

  function limpiarAvisos() {
    setMensaje(null)
    setErrorGeneral(null)
  }

  function guardarSemana(nueva: DiaFormulario[], exito: string) {
    limpiarAvisos()
    const locales = validarSemana(nueva)
    if (Object.keys(locales).length > 0) {
      setErrores(locales)
      setDiaActivo(Number(Object.keys(locales)[0]))
      return
    }

    setErrores({})
    guardar.mutate(aSolicitud(nueva), {
      onSuccess: () => {
        setSemana(nueva)
        setMensaje(exito)
      },
      onError: (error) => {
        const api = error as Partial<ApiError> | null
        const campos = (api?.erroresCampo ?? {}) as Record<string, string>
        setErrores(campos)
        const primero = Object.keys(campos)[0]
        if (primero && /^\d+$/.test(primero)) {
          setDiaActivo(Number(primero))
        }
        setErrorGeneral({
          title: api?.title || 'No pudimos guardar el horario',
          detail: api?.detail || 'Revisa tu conexión e intenta de nuevo.',
        })
      },
    })
  }

  function actualizarDia(numero: number, cambios: Partial<DiaFormulario>) {
    setSemana((actual) =>
      actual.map((opcion) =>
        opcion.numero === numero ? { ...opcion, ...cambios } : opcion,
      ),
    )
    setErrores((actual) => {
      if (!(numero in actual)) {
        return actual
      }
      const copia = { ...actual }
      delete copia[numero]
      return copia
    })
    limpiarAvisos()
  }

  function enviar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    guardarSemana(semana, 'Horario guardado. Los días cerrados no ofrecerán turnos.')
  }

  function aplicarEstandar() {
    guardarSemana(semanaPorDefecto(), 'Horario estándar aplicado a lunes a sábado.')
  }

  function aplicarATodos() {
    if (esDomingo) {
      return
    }
    const nueva = semana.map((opcion) =>
      opcion.numero === DOMINGO
        ? opcion
        : {
            ...opcion,
            trabaja: dia.trabaja,
            horaInicio: dia.horaInicio,
            horaFin: dia.horaFin,
            almuerzo: dia.almuerzo,
            almuerzoInicio: dia.almuerzoInicio,
            almuerzoFin: dia.almuerzoFin,
          },
    )
    guardarSemana(nueva, 'Horario aplicado a lunes a sábado.')
  }

  return (
    <section
      aria-labelledby="horario-semanal-titulo"
      className="mt-5 rounded-2xl border border-marca-300 bg-superficie p-4 shadow-[0_1px_2px_rgba(0,0,0,0.04)] sm:p-6"
    >
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div className="flex min-w-0 items-start gap-3">
          <span
            aria-hidden="true"
            className="flex size-10 shrink-0 items-center justify-center rounded-xl border border-marca-200 bg-marca-50"
          >
            <CalendarClock className="size-5 text-marca-700" />
          </span>
          <div className="min-w-0">
            <h2 id="horario-semanal-titulo" className="text-lg font-semibold text-texto">
              Horario semanal
            </h2>
            <p className="mt-1 text-sm text-texto-suave">
              Elige un día para verlo y editarlo. Solo se muestra el día seleccionado.
            </p>
          </div>
        </div>
        <button
          type="button"
          onClick={aplicarEstandar}
          disabled={guardar.isPending}
          className={CLASE_BOTON_SECUNDARIO}
        >
          <CalendarCheck aria-hidden="true" className="size-4" />
          Horario estándar L-S
        </button>
      </div>

      {diasIniciales.length === 0 && (
        <p className="mt-4 rounded-xl border border-marca-200 bg-marca-50 p-3.5 text-sm text-texto-suave">
          Este barbero aún no tiene horario guardado. Usa el horario estándar o configura
          día por día.
        </p>
      )}

      <div
        role="group"
        aria-label="Días de la semana"
        className="mt-5 grid grid-cols-7 gap-1.5"
      >
        {DIAS.map(({ numero, nombre: nombreCompleto, corto }) => {
          const opcion = semana.find((d) => d.numero === numero)
          const activo = numero === diaActivo
          const abierto = Boolean(opcion?.trabaja) && numero !== DOMINGO
          const estado = numero === DOMINGO
            ? 'cerrado por regla'
            : opcion?.trabaja
              ? `abierto, ${resumenDia(opcion)}`
              : 'cerrado'
          return (
            <button
              key={numero}
              type="button"
              aria-pressed={activo}
              aria-label={`Configurar ${nombreCompleto.toLowerCase()}: ${estado}`}
              title={`${nombreCompleto}: ${estado}`}
              onClick={() => {
                setDiaActivo(numero)
                limpiarAvisos()
              }}
              className={`flex min-h-14 min-w-0 flex-col items-center justify-center gap-1 rounded-xl border px-1 py-2 font-mono text-[11px] font-medium uppercase tracking-[0.08em] transition-colors focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-marca-900 ${
                activo
                  ? 'border-marca-900 bg-marca-900 text-white'
                  : 'border-marca-200 bg-superficie text-texto-suave hover:border-marca-400 hover:text-texto'
              }`}
            >
              <span>{corto}</span>
              <span
                aria-hidden="true"
                className={`size-1.5 rounded-full ${
                  abierto ? (activo ? 'bg-white' : 'bg-marca-700') : 'bg-marca-300'
                }`}
              />
            </button>
          )
        })}
      </div>

      <div className="mt-4 rounded-xl border border-marca-200 bg-marca-50/40 p-3.5 sm:p-4">
        <div className="flex flex-wrap items-center justify-between gap-2">
          <h3 className="text-lg font-semibold text-texto">{nombreDia(diaActivo)}</h3>
          <span className="rounded-full border border-marca-200 bg-superficie px-2.5 py-1 font-mono text-[11px] uppercase tracking-[0.08em] text-marca-500">
            {esDomingo ? 'Cerrado por regla' : resumenDia(dia)}
          </span>
        </div>

        {esDomingo ? (
          <div className="mt-3 rounded-xl border border-marca-200 bg-superficie p-3.5 text-sm text-texto-suave">
            <p>
              El domingo permanece cerrado (RN-01). Si necesitas abrir un domingo concreto,
              regístralo como fecha especial.
            </p>
            <a
              href="#fechas-especiales"
              className="mt-3 inline-flex min-h-11 items-center rounded-full border border-marca-300 px-4 font-mono text-xs uppercase tracking-[0.12em] text-texto hover:bg-marca-50 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-marca-900"
            >
              Ir a fechas especiales
            </a>
          </div>
        ) : (
          <form onSubmit={enviar} noValidate className="mt-3 space-y-3">
            <label className="flex min-h-11 items-center gap-3">
              <input
                type="checkbox"
                className="size-4 accent-marca-900"
                checked={dia.trabaja}
                aria-label={`Atiende el ${nombre}`}
                onChange={(evento) =>
                  actualizarDia(dia.numero, { trabaja: evento.target.checked })
                }
              />
              <span className="text-sm font-medium text-texto">Atiende este día</span>
            </label>

            {dia.trabaja ? (
              <div className="space-y-3">
                <div className="grid grid-cols-2 gap-3">
                  <div className="min-w-0">
                    <label htmlFor="semanal-inicio" className={CLASE_ETIQUETA}>
                      Inicio
                    </label>
                    <input
                      id="semanal-inicio"
                      type="time"
                      step={1800}
                      value={dia.horaInicio}
                      aria-label={`Hora de inicio del ${nombre}`}
                      aria-invalid={Boolean(errores[dia.numero])}
                      aria-describedby={
                        errores[dia.numero] ? `error-dia-${dia.numero}` : undefined
                      }
                      onChange={(evento) =>
                        actualizarDia(dia.numero, { horaInicio: evento.target.value })
                      }
                      className={CLASE_ENTRADA}
                    />
                  </div>
                  <div className="min-w-0">
                    <label htmlFor="semanal-fin" className={CLASE_ETIQUETA}>
                      Fin
                    </label>
                    <input
                      id="semanal-fin"
                      type="time"
                      step={1800}
                      value={dia.horaFin}
                      aria-label={`Hora de fin del ${nombre}`}
                      aria-invalid={Boolean(errores[dia.numero])}
                      aria-describedby={
                        errores[dia.numero] ? `error-dia-${dia.numero}` : undefined
                      }
                      onChange={(evento) =>
                        actualizarDia(dia.numero, { horaFin: evento.target.value })
                      }
                      className={CLASE_ENTRADA}
                    />
                  </div>
                </div>

                <label className="flex min-h-11 items-center gap-3">
                  <input
                    type="checkbox"
                    className="size-4 accent-marca-900"
                    checked={dia.almuerzo}
                    aria-label={`Almuerzo del ${nombre}`}
                    onChange={(evento) =>
                      actualizarDia(dia.numero, { almuerzo: evento.target.checked })
                    }
                  />
                  <span className="text-sm text-texto">Almuerzo</span>
                </label>

                {dia.almuerzo && (
                  <div className="grid grid-cols-2 gap-3">
                    <div className="min-w-0">
                      <label htmlFor="semanal-almuerzo-inicio" className={CLASE_ETIQUETA}>
                        Inicio almuerzo
                      </label>
                      <input
                        id="semanal-almuerzo-inicio"
                        type="time"
                        step={1800}
                        value={dia.almuerzoInicio}
                        aria-label={`Inicio del almuerzo del ${nombre}`}
                        aria-invalid={Boolean(errores[dia.numero])}
                        aria-describedby={
                          errores[dia.numero] ? `error-dia-${dia.numero}` : undefined
                        }
                        onChange={(evento) =>
                          actualizarDia(dia.numero, {
                            almuerzoInicio: evento.target.value,
                          })
                        }
                        className={CLASE_ENTRADA}
                      />
                    </div>
                    <div className="min-w-0">
                      <label htmlFor="semanal-almuerzo-fin" className={CLASE_ETIQUETA}>
                        Fin almuerzo
                      </label>
                      <input
                        id="semanal-almuerzo-fin"
                        type="time"
                        step={1800}
                        value={dia.almuerzoFin}
                        aria-label={`Fin del almuerzo del ${nombre}`}
                        aria-invalid={Boolean(errores[dia.numero])}
                        aria-describedby={
                          errores[dia.numero] ? `error-dia-${dia.numero}` : undefined
                        }
                        onChange={(evento) =>
                          actualizarDia(dia.numero, {
                            almuerzoFin: evento.target.value,
                          })
                        }
                        className={CLASE_ENTRADA}
                      />
                    </div>
                  </div>
                )}
              </div>
            ) : (
              <p className="rounded-xl border border-marca-200 bg-superficie p-3 text-sm text-texto-suave">
                Este día quedará cerrado y no ofrecerá turnos.
              </p>
            )}

            {errores[dia.numero] && (
              <p
                id={`error-dia-${dia.numero}`}
                role="alert"
                className="rounded-xl border border-error/30 bg-error/5 p-3 text-sm text-error"
              >
                {errores[dia.numero]}
              </p>
            )}

            <div className="flex flex-col gap-2 pt-1 sm:flex-row sm:items-center sm:justify-end">
              <button
                type="button"
                onClick={aplicarATodos}
                disabled={guardar.isPending}
                className={CLASE_BOTON_SECUNDARIO}
              >
                <Copy aria-hidden="true" className="size-4" />
                Aplicar a L-S
              </button>
              <button type="submit" disabled={guardar.isPending} className={CLASE_BOTON}>
                {guardar.isPending ? (
                  <Loader2 aria-hidden="true" className="size-4 animate-spin" />
                ) : (
                  <Save aria-hidden="true" className="size-4" />
                )}
                {guardar.isPending ? 'Guardando' : 'Guardar cambios'}
              </button>
            </div>
          </form>
        )}
      </div>

      {mensaje && (
        <p
          role="status"
          className="mt-4 rounded-xl border border-marca-300 bg-marca-50 p-3.5 text-sm text-texto"
        >
          {mensaje}
        </p>
      )}

      {errorGeneral && (
        <div
          role="alert"
          className="mt-4 rounded-xl border border-error/30 bg-error/5 p-3.5 text-sm text-error"
        >
          <p className="flex items-center gap-2 font-semibold">
            <CircleAlert aria-hidden="true" className="size-4 shrink-0" />
            {errorGeneral.title}
          </p>
          <p className="mt-1">{errorGeneral.detail}</p>
        </div>
      )}
    </section>
  )
}
