interface BarberoPoleProps {
  /** Tamaño con utilidades `h-* w-*`; el contenido se reparte solo. */
  className?: string
  /** Sobre fondos oscuros: rayas claras para que no se pierda el dibujo. */
  oscuro?: boolean
}

/**
 * Polo de barbería decorativo, en la paleta monocroma de la marca.
 *
 * Las rayas diagonales giran despacio (ver `.pole-tubo` en los estilos); con
 * `prefers-reduced-motion` quedan quietas. Es un adorno: `aria-hidden` para
 * que los lectores de pantalla lo ignoren.
 */
export function BarberoPole({ className = '', oscuro = false }: BarberoPoleProps) {
  return (
    <span
      aria-hidden="true"
      className={`inline-flex flex-col overflow-hidden rounded-full ring-1 ${
        oscuro ? 'ring-white/30' : 'ring-marca-900/20'
      } ${className}`}
    >
      <span className={`h-1.5 w-full shrink-0 ${oscuro ? 'bg-white/80' : 'bg-marca-950'}`} />
      <span className={`pole-tubo w-full flex-1 ${oscuro ? 'pole-tubo-oscuro' : ''}`} />
      <span className={`h-1.5 w-full shrink-0 ${oscuro ? 'bg-white/80' : 'bg-marca-950'}`} />
    </span>
  )
}
