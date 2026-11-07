import { EstadoVacio } from '../../components/comunes/EstadoVacio'

/** Aquí se elegirán servicio, barbero (o «cualquiera disponible») y fecha. La disponibilidad y las reglas de tiempo las calcula el backend. */
export function ReservaPage() {
  return (
    <section className="mx-auto w-full max-w-6xl px-4 py-10">
      <p className="text-xs font-semibold uppercase tracking-widest text-marca-500">Reserva</p>
      <h1 className="mt-2 text-2xl font-bold tracking-tight sm:text-3xl">Reservar cita</h1>
      <p className="mt-3 max-w-2xl text-sm text-texto-suave sm:text-base">Aquí se elegirán servicio, barbero (o «cualquiera disponible») y fecha. La disponibilidad y las reglas de tiempo las calcula el backend.</p>
      <div className="mt-6">
        <EstadoVacio detalle="RF-15 a RF-18 · Aquí se elegirán servicio, barbero (o «cualquiera disponible») y fecha. La disponibilidad y las reglas de tiempo las calcula el backend." />
      </div>
    </section>
  )
}
