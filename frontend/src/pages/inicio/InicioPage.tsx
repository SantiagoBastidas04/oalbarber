import { EstadoVacio } from '../../components/comunes/EstadoVacio'

/** Aquí irá la presentación del negocio, el horario de atención y los accesos al catálogo y a la reserva. */
export function InicioPage() {
  return (
    <section className="mx-auto w-full max-w-6xl px-4 py-10">
      <p className="text-xs font-semibold uppercase tracking-widest text-marca-500">Inicio</p>
      <h1 className="mt-2 text-2xl font-bold tracking-tight sm:text-3xl">Bienvenido a OalBarber</h1>
      <p className="mt-3 max-w-2xl text-sm text-texto-suave sm:text-base">Aquí irá la presentación del negocio, el horario de atención y los accesos al catálogo y a la reserva.</p>
      <div className="mt-6">
        <EstadoVacio detalle="RF-09 · Aquí irá la presentación del negocio, el horario de atención y los accesos al catálogo y a la reserva." />
      </div>
    </section>
  )
}
