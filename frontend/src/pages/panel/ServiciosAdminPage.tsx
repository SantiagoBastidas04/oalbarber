import { EstadoVacio } from '../../components/comunes/EstadoVacio'

/** Crear, editar y desactivar los servicios del catálogo. */
export function ServiciosAdminPage() {
  return (
    <section className="px-4 py-8 sm:px-6">
      <p className="text-xs font-semibold uppercase tracking-widest text-marca-500">Panel</p>
      <h1 className="mt-2 text-2xl font-bold tracking-tight sm:text-3xl">Servicios</h1>
      <p className="mt-3 max-w-2xl text-sm text-texto-suave sm:text-base">Crear, editar y desactivar los servicios del catálogo.</p>
      <div className="mt-6">
        <EstadoVacio detalle="RF-08 · Crear, editar y desactivar los servicios del catálogo." />
      </div>
    </section>
  )
}
