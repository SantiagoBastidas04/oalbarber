import { EstadoVacio } from '../../components/comunes/EstadoVacio'

/** Vista diaria y semanal de la agenda. El administrador ve a ambos barberos; el barbero, solo la suya. */
export function AgendaPage() {
  return (
    <section className="px-4 py-8 sm:px-6">
      <p className="text-xs font-semibold uppercase tracking-widest text-marca-500">Panel</p>
      <h1 className="mt-2 text-2xl font-bold tracking-tight sm:text-3xl">Agenda</h1>
      <p className="mt-3 max-w-2xl text-sm text-texto-suave sm:text-base">Vista diaria y semanal de la agenda. El administrador ve a ambos barberos; el barbero, solo la suya.</p>
      <div className="mt-6">
        <EstadoVacio detalle="RF-22 a RF-25 · Vista diaria y semanal de la agenda. El administrador ve a ambos barberos; el barbero, solo la suya." />
      </div>
    </section>
  )
}
