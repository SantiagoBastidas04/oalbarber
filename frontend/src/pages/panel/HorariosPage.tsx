import { CalendarClock, CircleAlert } from 'lucide-react'
import { useState } from 'react'
import { EstadoVacio } from '../../components/comunes/EstadoVacio'
import { useBarberosAdmin } from '../../hooks/useBarberos'
import { useExcepciones, useHorarios } from '../../hooks/useHorarios'
import { FechasEspecialesCard } from './FechasEspecialesCard'
import { HorarioSemanalCard } from './HorarioSemanalCard'

const CLASE_ETIQUETA =
  'mb-1.5 block font-mono text-[11px] font-medium uppercase tracking-[0.14em] text-marca-500'
const CLASE_ENTRADA =
  'min-h-11 w-full min-w-0 rounded-xl border border-marca-300 bg-superficie px-3 font-mono text-base tabular-nums text-texto focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-marca-900'

/** RF-11: horario semanal por barbero, con almuerzo y fechas especiales. */
export function HorariosPage() {
  const barberos = useBarberosAdmin()
  const [seleccion, setSeleccion] = useState<string | null>(null)
  const barberoId = seleccion ?? barberos.data?.[0]?.id ?? ''
  const horario = useHorarios(barberoId || null)
  const excepciones = useExcepciones(barberoId || null)

  const barberoElegido = barberos.data?.find((barbero) => barbero.id === barberoId)

  return (
    <section className="px-4 py-6 sm:px-6 sm:py-8">
      <div className="mx-auto w-full max-w-5xl">
        <p className="font-mono text-[11px] font-medium uppercase tracking-[0.14em] text-marca-500">
          Panel
        </p>
        <h1 className="mt-3 font-display text-4xl font-semibold leading-[1.05] sm:text-5xl">
          Horarios
        </h1>
        <p className="mt-3 max-w-2xl text-sm leading-relaxed text-texto-suave sm:text-base">
          Configura la semana día por día o usa el horario estándar para hacerlo de una vez.
          Los domingos y los festivos se ajustan en fechas especiales.
        </p>

        <section
          aria-labelledby="horario-barbero-titulo"
          className="mt-7 rounded-2xl border border-marca-300 bg-superficie p-4 shadow-[0_1px_2px_rgba(0,0,0,0.04)] sm:p-6"
        >
          <div className="flex items-start gap-3">
            <span
              aria-hidden="true"
              className="flex size-10 shrink-0 items-center justify-center rounded-xl border border-marca-200 bg-marca-50"
            >
              <CalendarClock className="size-5 text-marca-700" />
            </span>
            <div className="min-w-0 flex-1">
              <h2 id="horario-barbero-titulo" className="text-lg font-semibold text-texto">
                Barbero
              </h2>
              <p className="mt-1 text-sm text-texto-suave">
                Elige el perfil cuyo horario quieres configurar.
              </p>
            </div>
          </div>

          {barberos.isPending && (
            <p role="status" className="mt-5 text-sm text-texto-suave">
              Cargando barberos…
            </p>
          )}

          {barberos.isError && (
            <div
              role="alert"
              className="mt-5 rounded-xl border border-error/30 bg-error/5 p-3.5 text-sm text-error"
            >
              <p className="font-semibold">No pudimos cargar los barberos</p>
              <p className="mt-1">Revisa tu conexión e intenta de nuevo.</p>
              <button
                type="button"
                onClick={() => barberos.refetch()}
                className="mt-3 min-h-11 rounded-full border border-error/40 px-4 font-mono text-xs uppercase tracking-[0.12em] hover:bg-error/10 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-error"
              >
                Reintentar
              </button>
            </div>
          )}

          {barberos.isSuccess && barberos.data.length === 0 && (
            <div className="mt-5">
              <EstadoVacio
                titulo="Aún no hay barberos"
                detalle="Crea un perfil en la sección Barberos para poder asignarle un horario."
              />
            </div>
          )}

          {barberos.isSuccess && barberos.data.length > 0 && (
            <>
              <label htmlFor="horario-barbero" className={`${CLASE_ETIQUETA} mt-5`}>
                Perfil de barbero
              </label>
              <select
                id="horario-barbero"
                value={barberoId}
                onChange={(evento) => setSeleccion(evento.target.value)}
                className={`${CLASE_ENTRADA} pr-8`}
              >
                {barberos.data.map((barbero) => (
                  <option key={barbero.id} value={barbero.id}>
                    {barbero.nombrePublico || barbero.nombre}
                    {barbero.activo ? '' : ' (inactivo)'}
                  </option>
                ))}
              </select>
              {barberoElegido && !barberoElegido.activo && (
                <p className="mt-2 text-xs text-texto-suave">
                  Este perfil está inactivo: el horario se guarda, pero no se ofrecerá en la
                  agenda hasta reactivarlo.
                </p>
              )}
            </>
          )}
        </section>

        {barberoId && horario.isPending && (
          <div className="mt-5 space-y-3" role="status" aria-live="polite">
            <span className="sr-only">Cargando el horario del barbero.</span>
            <div
              aria-hidden="true"
              className="h-16 animate-pulse rounded-2xl border border-marca-200 bg-marca-50"
            />
            <div
              aria-hidden="true"
              className="h-56 animate-pulse rounded-2xl border border-marca-200 bg-marca-50"
            />
          </div>
        )}

        {barberoId && horario.isError && (
          <div
            role="alert"
            className="mt-5 rounded-2xl border border-error/30 bg-error/5 p-4 text-sm text-error"
          >
            <div className="flex items-start gap-3">
              <CircleAlert aria-hidden="true" className="mt-0.5 size-4 shrink-0" />
              <div>
                <p className="font-semibold">No pudimos cargar el horario</p>
                <p className="mt-1">Revisa tu conexión e intenta de nuevo.</p>
                <button
                  type="button"
                  onClick={() => horario.refetch()}
                  className="mt-3 min-h-11 rounded-full border border-error/40 px-4 font-mono text-xs uppercase tracking-[0.12em] hover:bg-error/10 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-error"
                >
                  Reintentar
                </button>
              </div>
            </div>
          </div>
        )}

        {barberoId && horario.isSuccess && (
          <HorarioSemanalCard
            key={`horario-${barberoId}`}
            barberoId={barberoId}
            diasIniciales={horario.data}
          />
        )}

        {barberoId && excepciones.isPending && (
          <div className="mt-5" role="status" aria-live="polite">
            <span className="sr-only">Cargando las fechas especiales.</span>
            <div
              aria-hidden="true"
              className="h-28 animate-pulse rounded-2xl border border-marca-200 bg-marca-50"
            />
          </div>
        )}

        {barberoId && excepciones.isError && (
          <div
            role="alert"
            className="mt-5 rounded-2xl border border-error/30 bg-error/5 p-4 text-sm text-error"
          >
            <div className="flex items-start gap-3">
              <CircleAlert aria-hidden="true" className="mt-0.5 size-4 shrink-0" />
              <div>
                <p className="font-semibold">No pudimos cargar las fechas especiales</p>
                <p className="mt-1">Revisa tu conexión e intenta de nuevo.</p>
                <button
                  type="button"
                  onClick={() => excepciones.refetch()}
                  className="mt-3 min-h-11 rounded-full border border-error/40 px-4 font-mono text-xs uppercase tracking-[0.12em] hover:bg-error/10 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-error"
                >
                  Reintentar
                </button>
              </div>
            </div>
          </div>
        )}

        {barberoId && excepciones.isSuccess && (
          <FechasEspecialesCard
            key={`excepciones-${barberoId}`}
            barberoId={barberoId}
            excepciones={excepciones.data}
          />
        )}
      </div>
    </section>
  )
}
