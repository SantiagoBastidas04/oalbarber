import { Construction } from 'lucide-react'

interface Props {
  titulo?: string
  detalle?: string
}

/** Marcador para las secciones que todavía no se implementan. */
export function EstadoVacio({ titulo = 'Pendiente de implementar', detalle }: Props) {
  return (
    <div className="rounded-lg border border-dashed border-borde bg-superficie p-6 text-center">
      <Construction aria-hidden="true" className="mx-auto size-8 text-marca-400" />
      <p className="mt-3 text-base font-semibold">{titulo}</p>
      {detalle && (
        <p className="mx-auto mt-1 max-w-prose text-sm text-texto-suave">{detalle}</p>
      )}
    </div>
  )
}
