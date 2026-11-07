import { EstadoVacio } from '../../components/comunes/EstadoVacio'

/** Inicio y cierre de sesión con celular y contraseña. La sesión se renovará con el refresh token. */
export function IngresarPage() {
  return (
    <section className="mx-auto w-full max-w-6xl px-4 py-10">
      <p className="text-xs font-semibold uppercase tracking-widest text-marca-500">Cuenta</p>
      <h1 className="mt-2 text-2xl font-bold tracking-tight sm:text-3xl">Ingresar</h1>
      <p className="mt-3 max-w-2xl text-sm text-texto-suave sm:text-base">Inicio y cierre de sesión con celular y contraseña. La sesión se renovará con el refresh token.</p>
      <div className="mt-6">
        <EstadoVacio detalle="RF-02, RF-04 · Inicio y cierre de sesión con celular y contraseña. La sesión se renovará con el refresh token." />
      </div>
    </section>
  )
}
