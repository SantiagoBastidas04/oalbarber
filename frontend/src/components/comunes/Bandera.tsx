import AR from 'country-flag-icons/react/3x2/AR'
import BO from 'country-flag-icons/react/3x2/BO'
import BR from 'country-flag-icons/react/3x2/BR'
import CL from 'country-flag-icons/react/3x2/CL'
import CO from 'country-flag-icons/react/3x2/CO'
import CR from 'country-flag-icons/react/3x2/CR'
import EC from 'country-flag-icons/react/3x2/EC'
import ES from 'country-flag-icons/react/3x2/ES'
import MX from 'country-flag-icons/react/3x2/MX'
import PA from 'country-flag-icons/react/3x2/PA'
import PE from 'country-flag-icons/react/3x2/PE'
import PY from 'country-flag-icons/react/3x2/PY'
import US from 'country-flag-icons/react/3x2/US'
import UY from 'country-flag-icons/react/3x2/UY'
import VE from 'country-flag-icons/react/3x2/VE'

/** Tipo de los componentes de bandera que exporta `country-flag-icons`. */
type ComponenteBandera = typeof CO

/** Banderas SVG de los países disponibles en el selector de celular. */
const BANDERAS: Record<string, ComponenteBandera> = {
  AR,
  BO,
  BR,
  CL,
  CO,
  CR,
  EC,
  ES,
  MX,
  PA,
  PE,
  PY,
  US,
  UY,
  VE,
}

interface Props {
  /** Código ISO de dos letras, p. ej. `CO`. */
  iso: string
  className?: string
}

/**
 * Bandera de un país como SVG (no emoji: Windows no dibuja banderas con
 * emojis). Si el país no está en el mapa, deja un rectángulo neutro.
 */
export function Bandera({ iso, className = '' }: Props) {
  const base = `inline-block h-4 w-6 shrink-0 rounded-[2px] ring-1 ring-black/10 ${className}`
  const Componente = BANDERAS[iso]

  if (!Componente) {
    return <span aria-hidden="true" className={`${base} bg-marca-200`} />
  }

  return <Componente aria-hidden="true" className={base} />
}
