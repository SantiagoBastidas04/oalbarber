import { EstadoVacio } from '../../components/comunes/EstadoVacio'

/** Horario por día y barbero, almuerzo, bloqueos y parámetros configurables. */
export function HorariosPage() {
  return (
    <section className="px-4 py-8 sm:px-6">
      <p className="text-xs font-semibold uppercase tracking-widest text-marca-500">Panel</p>
      <h1 className="mt-2 text-2xl font-bold tracking-tight sm:text-3xl">Horarios</h1>
      <p className="mt-3 max-w-2xl text-sm text-texto-suave sm:text-base">Horario por día y barbero, almuerzo, bloqueos y parámetros configurables.</p>
      <div className="mt-6">
        <EstadoVacio detalle="RF-11 a RF-14 · Horario por día y barbero, almuerzo, bloqueos y parámetros configurables." />
      </div>
    </section>
  )
}
