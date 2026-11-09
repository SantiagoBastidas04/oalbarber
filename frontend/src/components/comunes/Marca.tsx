interface MarcaProps {
  /** Clases del contenedor; controla el tamaño con utilidades `text-*`. */
  className?: string
}

/**
 * Wordmark de OalBarber.
 *
 * Usa Bebas Neue (condensada, en mayúsculas) para diferenciar la marca de los
 * títulos en Playfair Display. El punto y el espaciado reaccionan al pasar el
 * cursor si el contenedor padre lleva la clase `group`.
 */
export function Marca({ className = '' }: MarcaProps) {
  return (
    <span className={`inline-flex items-center gap-2 ${className}`}>
      <span
        aria-hidden="true"
        className="size-2 shrink-0 rounded-full bg-current transition-[transform,border-radius] duration-300 group-hover:scale-110 group-hover:rotate-45 group-hover:rounded-[2px]"
      />
      <span className="font-marca leading-none tracking-[0.08em] uppercase transition-[letter-spacing] duration-300 group-hover:tracking-[0.14em]">
        OalBarber
      </span>
    </span>
  )
}
