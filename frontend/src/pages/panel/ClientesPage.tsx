import { EstadoVacio } from '../../components/comunes/EstadoVacio'

/** Consultar y desactivar cuentas de clientes y restablecer contraseñas. */
export function ClientesPage() {
  return (
    <section className="px-4 py-8 sm:px-6">
      <p className="text-xs font-semibold uppercase tracking-widest text-marca-500">Panel</p>
      <h1 className="mt-2 text-2xl font-bold tracking-tight sm:text-3xl">Clientes</h1>
      <p className="mt-3 max-w-2xl text-sm text-texto-suave sm:text-base">Consultar y desactivar cuentas de clientes y restablecer contraseñas.</p>
      <div className="mt-6">
        <EstadoVacio detalle="RF-05, RF-27 · Consultar y desactivar cuentas de clientes y restablecer contraseñas." />
      </div>
    </section>
  )
}
