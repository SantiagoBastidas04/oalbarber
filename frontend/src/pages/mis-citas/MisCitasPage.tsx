import { EstadoVacio } from '../../components/comunes/EstadoVacio'

/** Citas próximas, historial y cancelación hasta 2 horas antes de la cita. */
export function MisCitasPage() {
  return (
    <section className="mx-auto w-full max-w-6xl px-4 py-10">
      <p className="text-xs font-semibold uppercase tracking-widest text-marca-500">Cuenta</p>
      <h1 className="mt-2 text-2xl font-bold tracking-tight sm:text-3xl">Mis citas</h1>
      <p className="mt-3 max-w-2xl text-sm text-texto-suave sm:text-base">Citas próximas, historial y cancelación hasta 2 horas antes de la cita.</p>
      <div className="mt-6">
        <EstadoVacio detalle="RF-19 a RF-21 · Citas próximas, historial y cancelación hasta 2 horas antes de la cita." />
      </div>
    </section>
  )
}
