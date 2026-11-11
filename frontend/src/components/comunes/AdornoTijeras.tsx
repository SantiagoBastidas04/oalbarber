import { Scissors } from 'lucide-react'

interface AdornoTijerasProps {
  className?: string
  /** Variante para fondos oscuros (panel de marca). */
  oscuro?: boolean
}

/**
 * Divisor ornamental: línea que se desvanece, tijeras y línea.
 *
 * El guiño de barbería del formulario. Es decorativo (`aria-hidden`); nunca
 * reemplaza un separador semántico.
 */
export function AdornoTijeras({ className = '', oscuro = false }: AdornoTijerasProps) {
  return (
    <div aria-hidden="true" className={`flex items-center gap-2.5 ${className}`}>
      <span
        className={`h-px flex-1 ${
          oscuro
            ? 'bg-gradient-to-l from-white/30 to-transparent'
            : 'bg-gradient-to-l from-marca-300 to-transparent'
        }`}
      />
      <Scissors
        className={`size-3.5 shrink-0 -scale-x-100 ${oscuro ? 'text-white/60' : 'text-marca-400'}`}
      />
      <span
        className={`h-px flex-1 ${
          oscuro
            ? 'bg-gradient-to-r from-white/30 to-transparent'
            : 'bg-gradient-to-r from-marca-300 to-transparent'
        }`}
      />
    </div>
  )
}
