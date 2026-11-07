import { EstadoVacio } from '../../components/comunes/EstadoVacio'

/** Crear y desactivar barberos y asignar los servicios que sabe hacer cada uno. */
export function BarberosPage() {
  return (
    <section className="px-4 py-8 sm:px-6">
      <p className="text-xs font-semibold uppercase tracking-widest text-marca-500">Panel</p>
      <h1 className="mt-2 text-2xl font-bold tracking-tight sm:text-3xl">Barberos</h1>
      <p className="mt-3 max-w-2xl text-sm text-texto-suave sm:text-base">Crear y desactivar barberos y asignar los servicios que sabe hacer cada uno.</p>
      <div className="mt-6">
        <EstadoVacio detalle="RF-10 · Crear y desactivar barberos y asignar los servicios que sabe hacer cada uno." />
      </div>
    </section>
  )
}
