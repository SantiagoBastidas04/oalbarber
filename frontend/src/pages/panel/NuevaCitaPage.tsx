import { EstadoVacio } from '../../components/comunes/EstadoVacio'

/** Atención sin reserva a la hora exacta, sin las restricciones de anticipación del cliente. */
export function NuevaCitaPage() {
  return (
    <section className="px-4 py-8 sm:px-6">
      <p className="text-xs font-semibold uppercase tracking-widest text-marca-500">Panel</p>
      <h1 className="mt-2 text-2xl font-bold tracking-tight sm:text-3xl">Registrar cita manual</h1>
      <p className="mt-3 max-w-2xl text-sm text-texto-suave sm:text-base">Atención sin reserva a la hora exacta, sin las restricciones de anticipación del cliente.</p>
      <div className="mt-6">
        <EstadoVacio detalle="RF-26 · Atención sin reserva a la hora exacta, sin las restricciones de anticipación del cliente." />
      </div>
    </section>
  )
}
