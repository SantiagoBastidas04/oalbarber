import { ArrowRight, Clock } from 'lucide-react'
import { Link } from 'react-router'
import { RUTAS } from '../../config/rutas'
import type { ServicioResponse } from '../../types/servicio'
import { formatearMoneda } from '../../utils/moneda'
import { ImagenServicio } from '../comunes/ImagenServicio'

interface Props {
  servicio: ServicioResponse
}

/**
 * Tarjeta del catálogo: foto, nombre, duración y precio (RF-07). La foto se
 * sirve desde `/servicios/<archivo>` y, si falla, `ImagenServicio` dibuja el
 * marcador sin romper la página (RN-03).
 */
export function TarjetaServicio({ servicio }: Props) {
  return (
    <article className="group flex h-full flex-col overflow-hidden rounded-2xl border border-marca-300 bg-superficie shadow-[0_1px_2px_rgba(0,0,0,0.04)] transition-[border-color,box-shadow,transform] duration-200 hover:-translate-y-0.5 hover:border-marca-400 hover:shadow-[0_18px_36px_-28px_rgba(0,0,0,0.45)]">
      <ImagenServicio
        archivo={servicio.imagen}
        nombre={servicio.nombre}
        className="aspect-[4/3] w-full border-b border-marca-200"
      />

      <div className="flex flex-1 flex-col p-4 sm:p-5">
        <h3 className="font-display text-xl font-semibold leading-tight text-texto">
          {servicio.nombre}
        </h3>
        {servicio.descripcion && (
          <p className="mt-1.5 line-clamp-2 text-sm leading-relaxed text-texto-suave">
            {servicio.descripcion}
          </p>
        )}

        <dl className="mt-auto flex items-end justify-between gap-3 border-t border-marca-200 pt-3.5">
          <div className="flex items-center gap-1.5 text-sm text-texto-suave">
            <Clock aria-hidden="true" className="size-4 text-marca-400" />
            <dt className="sr-only">Duración</dt>
            <dd className="font-mono text-[13px] tabular-nums">{servicio.duracionMin} min</dd>
          </div>
          <div className="text-right">
            <dt className="sr-only">Precio</dt>
            <dd className="font-mono text-base font-medium tabular-nums text-texto">
              {formatearMoneda(servicio.precio)}
            </dd>
          </div>
        </dl>

        <Link
          to={`${RUTAS.reservar}?servicio=${encodeURIComponent(servicio.id)}`}
          aria-label={`Reservar ${servicio.nombre}`}
          className="mt-4 inline-flex min-h-11 items-center justify-center gap-2 rounded-full border border-marca-300 px-4 font-mono text-[11px] font-medium uppercase tracking-[0.14em] text-texto transition-colors duration-150 hover:border-marca-900 hover:bg-marca-900 hover:text-white active:translate-y-px"
        >
          Reservar
          <ArrowRight aria-hidden="true" className="size-3.5" />
        </Link>
      </div>
    </article>
  )
}
