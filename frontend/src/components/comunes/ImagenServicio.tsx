import { Scissors } from 'lucide-react'
import { useState } from 'react'

interface Props {
  /** Nombre del archivo guardado en la base, por ejemplo `corte-normal.jpg`. */
  archivo?: string | null
  /** Nombre del servicio, usado como texto alternativo. */
  nombre: string
  className?: string
}

/**
 * Muestra la foto estática de `public/servicios/`. Si el servicio no tiene
 * archivo o la imagen falla, dibuja un marcador de posición accesible.
 */
export function ImagenServicio({ archivo, nombre, className = '' }: Props) {
  // Se guarda el archivo que falló, no un booleano: así el estado se reinicia
  // solo cuando cambia el servicio, sin un efecto que provoque otro render.
  const [archivoConFallo, setArchivoConFallo] = useState<string | null>(null)
  const sinImagen = !archivo || archivoConFallo === archivo

  if (sinImagen) {
    return (
      <div
        role="img"
        aria-label={`Imagen no disponible para ${nombre}`}
        className={`flex items-center justify-center bg-marca-100 text-marca-400 ${className}`}
      >
        <Scissors size={32} aria-hidden="true" />
      </div>
    )
  }

  return (
    <img
      src={`/servicios/${archivo}`}
      alt={nombre}
      loading="lazy"
      onError={() => setArchivoConFallo(archivo)}
      className={`object-cover ${className}`}
    />
  )
}
