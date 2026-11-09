import { Check, ChevronDown } from 'lucide-react'
import {
  useCallback,
  useEffect,
  useId,
  useRef,
  useState,
  type KeyboardEvent,
} from 'react'
import type { Pais } from '../../utils/celularPais'
import { Bandera } from './Bandera'

interface Props {
  paises: readonly Pais[]
  /** Prefijo seleccionado, sin el signo +. */
  valor: string
  onCambio: (codigo: string) => void
  /** Marca el campo como inválido para lectores de pantalla. */
  invalido?: boolean
}

/**
 * Selector de país para el celular.
 *
 * Un `<select>` nativo no puede mostrar banderas SVG, así que se usa el patrón
 * listbox de WAI-ARIA: se abre con clic, Enter, Espacio o flechas, se navega
 * con las flechas y se cierra con Escape o al hacer clic fuera.
 */
export function SelectorPais({ paises, valor, onCambio, invalido = false }: Props) {
  const id = useId()
  const [abierto, setAbierto] = useState(false)
  const [activo, setActivo] = useState(0)

  const contenedor = useRef<HTMLDivElement>(null)
  const boton = useRef<HTMLButtonElement>(null)
  const lista = useRef<HTMLUListElement>(null)

  const indiceElegido = Math.max(
    0,
    paises.findIndex((pais) => pais.codigo === valor),
  )
  const pais = paises[indiceElegido] ?? paises[0]

  const idOpcion = (indice: number) => `${id}-opcion-${indice}`

  const abrir = useCallback(() => {
    setActivo(indiceElegido)
    setAbierto(true)
  }, [indiceElegido])

  const cerrar = useCallback((devolverFoco = false) => {
    setAbierto(false)
    if (devolverFoco) {
      boton.current?.focus()
    }
  }, [])

  const elegir = useCallback(
    (paisElegido: Pais) => {
      onCambio(paisElegido.codigo)
      cerrar(true)
    },
    [cerrar, onCambio],
  )

  // Cierra al pulsar fuera del componente.
  useEffect(() => {
    if (!abierto) {
      return
    }

    const alSenalar = (evento: PointerEvent) => {
      if (!contenedor.current?.contains(evento.target as Node)) {
        setAbierto(false)
      }
    }

    document.addEventListener('pointerdown', alSenalar)
    return () => document.removeEventListener('pointerdown', alSenalar)
  }, [abierto])

  // Al abrir, el foco pasa a la lista para poder navegar con el teclado.
  useEffect(() => {
    if (abierto) {
      lista.current?.focus()
    }
  }, [abierto])

  // Mantiene visible la opción activa dentro del desplegable.
  useEffect(() => {
    if (!abierto) {
      return
    }
    const opcion = lista.current?.querySelector<HTMLElement>('[data-activo="true"]')
    opcion?.scrollIntoView?.({ block: 'nearest' })
  }, [abierto, activo])

  const alTeclearBoton = (evento: KeyboardEvent<HTMLButtonElement>) => {
    if (
      evento.key === 'ArrowDown' ||
      evento.key === 'ArrowUp' ||
      evento.key === 'Enter' ||
      evento.key === ' '
    ) {
      evento.preventDefault()
      abrir()
    }
  }

  const alTeclearLista = (evento: KeyboardEvent<HTMLUListElement>) => {
    const total = paises.length

    switch (evento.key) {
      case 'ArrowDown':
        evento.preventDefault()
        setActivo((indice) => (indice + 1) % total)
        break
      case 'ArrowUp':
        evento.preventDefault()
        setActivo((indice) => (indice - 1 + total) % total)
        break
      case 'Home':
        evento.preventDefault()
        setActivo(0)
        break
      case 'End':
        evento.preventDefault()
        setActivo(total - 1)
        break
      case 'Enter':
      case ' ': {
        evento.preventDefault()
        const elegido = paises[activo]
        if (elegido) {
          elegir(elegido)
        }
        break
      }
      case 'Escape':
        evento.preventDefault()
        cerrar(true)
        break
      case 'Tab':
        cerrar()
        break
      default:
        break
    }
  }

  if (!pais) {
    return null
  }

  return (
    <div ref={contenedor} className="relative shrink-0">
      <button
        ref={boton}
        type="button"
        onClick={() => (abierto ? cerrar() : abrir())}
        onKeyDown={alTeclearBoton}
        aria-haspopup="listbox"
        aria-expanded={abierto}
        aria-controls={`${id}-lista`}
        aria-invalid={invalido || undefined}
        aria-label={`País del celular: ${pais.nombre}, prefijo más ${pais.codigo}`}
        className="flex h-full items-center gap-2 pl-2.5 pr-2 text-texto transition-colors duration-150 hover:bg-marca-100 active:bg-marca-200 focus-visible:bg-marca-100/70"
      >
        <Bandera iso={pais.iso} />
        <span className="font-mono text-sm font-medium tabular-nums">+{pais.codigo}</span>
        <ChevronDown
          aria-hidden="true"
          className={`size-3.5 text-texto-suave transition-transform duration-200 ${
            abierto ? 'rotate-180' : ''
          }`}
        />
      </button>

      {abierto && (
        <ul
          ref={lista}
          id={`${id}-lista`}
          role="listbox"
          aria-label="País del celular"
          aria-activedescendant={idOpcion(activo)}
          tabIndex={-1}
          onKeyDown={alTeclearLista}
          className="absolute left-0 top-full z-30 mt-2 max-h-72 w-80 max-w-[calc(100vw-2.5rem)] overflow-y-auto overscroll-contain border border-marca-300 bg-superficie py-1 shadow-xl shadow-marca-900/10 focus:outline-none"
        >
          {paises.map((opcion, indice) => {
            const seleccionado = opcion.codigo === pais.codigo
            const esActivo = indice === activo

            return (
              <li
                key={opcion.iso}
                id={idOpcion(indice)}
                role="option"
                aria-selected={seleccionado}
                data-activo={esActivo}
                onMouseEnter={() => setActivo(indice)}
                onClick={() => elegir(opcion)}
                className={`flex min-h-11 cursor-pointer items-center gap-3 px-3 py-2.5 text-sm transition-colors duration-150 hover:bg-marca-50 active:bg-marca-200 ${
                  esActivo ? 'bg-marca-100' : 'bg-superficie'
                }`}
              >
                <Bandera iso={opcion.iso} />
                <span className="min-w-0 flex-1 truncate text-texto">{opcion.nombre}</span>
                <span className="font-mono text-xs tabular-nums text-texto-suave">
                  +{opcion.codigo}
                </span>
                <Check
                  aria-hidden="true"
                  className={`size-3.5 shrink-0 ${seleccionado ? 'opacity-100' : 'opacity-0'}`}
                />
              </li>
            )
          })}
        </ul>
      )}
    </div>
  )
}
