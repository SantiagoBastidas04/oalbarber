import { EstadoVacio } from '../../components/comunes/EstadoVacio'

/** Aquí se mostrarán los servicios con nombre, duración, precio y foto. La lista y los precios los entrega el backend. */
export function CatalogoPage() {
  return (
    <section className="mx-auto w-full max-w-6xl px-4 py-10">
      <p className="text-xs font-semibold uppercase tracking-widest text-marca-500">Catálogo</p>
      <h1 className="mt-2 text-2xl font-bold tracking-tight sm:text-3xl">Servicios</h1>
      <p className="mt-3 max-w-2xl text-sm text-texto-suave sm:text-base">Aquí se mostrarán los servicios con nombre, duración, precio y foto. La lista y los precios los entrega el backend.</p>
      <div className="mt-6">
        <EstadoVacio detalle="RF-07 · Aquí se mostrarán los servicios con nombre, duración, precio y foto. La lista y los precios los entrega el backend." />
      </div>
    </section>
  )
}
