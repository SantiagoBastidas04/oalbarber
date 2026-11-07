import { EstadoVacio } from '../../components/comunes/EstadoVacio'

/** Formulario de registro con nombre, celular y contraseña, con la aceptación de la política de tratamiento de datos. */
export function RegistroPage() {
  return (
    <section className="mx-auto w-full max-w-6xl px-4 py-10">
      <p className="text-xs font-semibold uppercase tracking-widest text-marca-500">Cuenta</p>
      <h1 className="mt-2 text-2xl font-bold tracking-tight sm:text-3xl">Crear cuenta</h1>
      <p className="mt-3 max-w-2xl text-sm text-texto-suave sm:text-base">Formulario de registro con nombre, celular y contraseña, con la aceptación de la política de tratamiento de datos.</p>
      <div className="mt-6">
        <EstadoVacio detalle="RF-01 · Formulario de registro con nombre, celular y contraseña, con la aceptación de la política de tratamiento de datos." />
      </div>
    </section>
  )
}
