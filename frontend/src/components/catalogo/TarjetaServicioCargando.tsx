/**
 * Esqueleto de una tarjeta mientras llega el catálogo. Es decorativo: el
 * mensaje de carga accesible lo pone la página con `role="status"`.
 */
export function TarjetaServicioCargando() {
  return (
    <div
      aria-hidden="true"
      className="overflow-hidden rounded-2xl border border-marca-300 bg-superficie"
    >
      <div className="aspect-[4/3] w-full animate-pulse bg-marca-100" />
      <div className="space-y-3 p-4 sm:p-5">
        <div className="h-5 w-2/3 animate-pulse rounded-full bg-marca-100" />
        <div className="h-3.5 w-full animate-pulse rounded-full bg-marca-100" />
        <div className="h-9 w-full animate-pulse rounded-full bg-marca-100" />
      </div>
    </div>
  )
}
