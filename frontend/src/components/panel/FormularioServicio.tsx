import { zodResolver } from '@hookform/resolvers/zod'
import { CircleAlert, Loader2 } from 'lucide-react'
import { useState } from 'react'
import { useForm, useWatch } from 'react-hook-form'
import { z } from 'zod'
import type { ApiError } from '../../types/error'
import type { ServicioRequest, ServicioResponse } from '../../types/servicio'
import { ImagenServicio } from '../comunes/ImagenServicio'

/** Duraciones que acepta el negocio (RF-08). El backend rechaza el resto. */
const DURACIONES = [40, 90, 180] as const

/** El mismo patrón del backend: solo el nombre del archivo, no una ruta. */
const PATRON_ARCHIVO = /^[\w .-]+\.(jpg|jpeg|png|webp)$/i

const esquemaServicio = z.object({
  nombre: z
    .string()
    .max(100, 'El nombre no puede pasar de 100 caracteres')
    .refine((valor) => valor.trim().length >= 2, 'Escribe el nombre del servicio'),
  descripcion: z.string().max(1000, 'La descripción no puede pasar de 1000 caracteres'),
  duracionMin: z
    .string()
    .refine(
      (valor) => DURACIONES.some((duracion) => String(duracion) === valor),
      'Elige 40, 90 o 180 minutos',
    ),
  precio: z
    .string()
    .refine((valor) => valor.trim().length > 0, 'Escribe el precio')
    .refine(
      (valor) => /^\d{1,10}([.,]\d{1,2})?$/.test(valor.trim()),
      'Usa un número con máximo dos decimales',
    )
    .refine(
      (valor) => Number(valor.trim().replace(',', '.')) >= 0.01,
      'El precio debe ser mayor que cero',
    ),
  imagen: z
    .string()
    .max(255, 'El nombre del archivo no puede pasar de 255 caracteres')
    .refine(
      (valor) => valor.trim() === '' || PATRON_ARCHIVO.test(valor.trim()),
      'Usa solo el nombre del archivo, con extensión .jpg, .png o .webp',
    ),
})

type Formulario = z.infer<typeof esquemaServicio>

interface Props {
  /** Servicio a editar; sin él, el formulario crea uno nuevo. */
  servicio?: ServicioResponse | null
  guardando?: boolean
  alGuardar: (datos: ServicioRequest) => Promise<void>
  alCancelar: () => void
}

const CLASE_ETIQUETA =
  'mb-1.5 block font-mono text-[11px] font-medium uppercase tracking-[0.14em] text-marca-500'

const CLASE_CAMPO =
  'h-11 w-full rounded-xl border border-marca-300 bg-superficie px-3.5 text-base text-texto placeholder:text-marca-400 transition-[border-color,box-shadow] duration-200 hover:border-marca-400 focus:border-marca-900 focus:shadow-[0_0_0_4px_rgba(10,10,10,0.07)] focus:outline-none'

const CLASE_CAMPO_ERROR =
  'border-error focus:border-error focus:shadow-[0_0_0_4px_rgba(185,28,28,0.1)]'

/** Mensaje de error de un campo, anunciado a lectores de pantalla. */
function ErrorCampo({ id, mensaje }: { id: string; mensaje?: string }) {
  if (!mensaje) {
    return null
  }

  return (
    <p id={id} role="alert" className="mt-1.5 flex items-start gap-1.5 text-sm text-error">
      <CircleAlert aria-hidden="true" className="mt-0.5 size-3.5 shrink-0" />
      {mensaje}
    </p>
  )
}

/**
 * Formulario de alta y edición (RF-08). La duración es una lista cerrada, el
 * precio se valida con las mismas reglas que la API y la foto se asocia por
 * nombre de archivo con previsualización (RN-03).
 */
export function FormularioServicio({
  servicio,
  guardando = false,
  alGuardar,
  alCancelar,
}: Props) {
  const [errorGeneral, setErrorGeneral] = useState<string | null>(null)

  const {
    register,
    handleSubmit,
    setError,
    control,
    formState: { errors, isSubmitting },
  } = useForm<Formulario>({
    resolver: zodResolver(esquemaServicio),
    mode: 'onBlur',
    defaultValues: {
      nombre: servicio?.nombre ?? '',
      descripcion: servicio?.descripcion ?? '',
      duracionMin: String(servicio?.duracionMin ?? DURACIONES[0]),
      precio: servicio ? String(servicio.precio) : '',
      imagen: servicio?.imagen ?? '',
    },
  })

  const nombreValor = useWatch({ control, name: 'nombre' }) ?? ''
  const imagenValor = useWatch({ control, name: 'imagen' }) ?? ''
  const guardandoAhora = isSubmitting || guardando

  const alEnviar = handleSubmit(async (valores) => {
    setErrorGeneral(null)

    const datos: ServicioRequest = {
      nombre: valores.nombre.trim(),
      descripcion: valores.descripcion.trim() || null,
      duracionMin: Number(valores.duracionMin),
      precio: Number(valores.precio.trim().replace(',', '.')),
      imagen: valores.imagen.trim() || null,
    }

    try {
      await alGuardar(datos)
    } catch (error) {
      const apiError = error as ApiError
      let ubicado = false

      // 409 concreto: el nombre ya lo usa otro servicio.
      if (apiError.codigo === 'NOMBRE_DUPLICADO') {
        setError('nombre', {
          type: 'server',
          message: 'Ya existe un servicio con ese nombre',
        })
        ubicado = true
      }

      // 400 de Bean Validation: el backend nombra el campo.
      Object.entries(apiError.erroresCampo ?? {}).forEach(([campo, mensaje]) => {
        if (campo in valores) {
          setError(campo as keyof Formulario, { type: 'server', message: mensaje })
          ubicado = true
        }
      })

      if (!ubicado) {
        setErrorGeneral(apiError.detail ?? 'No pudimos guardar el servicio. Intenta de nuevo.')
      }
    }
  })

  return (
    <form noValidate onSubmit={alEnviar} className="mt-4 space-y-4">
      <div>
        <label htmlFor="servicio-nombre" className={CLASE_ETIQUETA}>
          Nombre del servicio
        </label>
        <input
          id="servicio-nombre"
          type="text"
          autoFocus
          placeholder="Corte normal"
          aria-invalid={Boolean(errors.nombre)}
          aria-describedby={errors.nombre ? 'servicio-nombre-error' : undefined}
          className={`${CLASE_CAMPO} ${errors.nombre ? CLASE_CAMPO_ERROR : ''}`}
          {...register('nombre')}
        />
        <ErrorCampo id="servicio-nombre-error" mensaje={errors.nombre?.message} />
      </div>

      <div>
        <label htmlFor="servicio-descripcion" className={CLASE_ETIQUETA}>
          Descripción
        </label>
        <textarea
          id="servicio-descripcion"
          rows={3}
          placeholder="Corte clásico con lavado y peinado"
          aria-invalid={Boolean(errors.descripcion)}
          aria-describedby={errors.descripcion ? 'servicio-descripcion-error' : undefined}
          className={`${CLASE_CAMPO} h-auto resize-y py-2.5 leading-relaxed ${
            errors.descripcion ? CLASE_CAMPO_ERROR : ''
          }`}
          {...register('descripcion')}
        />
        <ErrorCampo id="servicio-descripcion-error" mensaje={errors.descripcion?.message} />
      </div>

      <div className="grid gap-4 sm:grid-cols-2">
        <div>
          <label htmlFor="servicio-duracion" className={CLASE_ETIQUETA}>
            Duración
          </label>
          <select
            id="servicio-duracion"
            aria-invalid={Boolean(errors.duracionMin)}
            aria-describedby={errors.duracionMin ? 'servicio-duracion-error' : undefined}
            className={`${CLASE_CAMPO} ${errors.duracionMin ? CLASE_CAMPO_ERROR : ''}`}
            {...register('duracionMin')}
          >
            {DURACIONES.map((duracion) => (
              <option key={duracion} value={String(duracion)}>
                {duracion} minutos
              </option>
            ))}
          </select>
          <ErrorCampo id="servicio-duracion-error" mensaje={errors.duracionMin?.message} />
        </div>

        <div>
          <label htmlFor="servicio-precio" className={CLASE_ETIQUETA}>
            Precio
          </label>
          <input
            id="servicio-precio"
            type="text"
            inputMode="decimal"
            placeholder="25000"
            aria-invalid={Boolean(errors.precio)}
            aria-describedby={errors.precio ? 'servicio-precio-error' : undefined}
            className={`${CLASE_CAMPO} font-mono tabular-nums ${
              errors.precio ? CLASE_CAMPO_ERROR : ''
            }`}
            {...register('precio')}
          />
          <ErrorCampo id="servicio-precio-error" mensaje={errors.precio?.message} />
        </div>
      </div>

      <div>
        <label htmlFor="servicio-imagen" className={CLASE_ETIQUETA}>
          Archivo de la foto
        </label>
        <div className="flex items-start gap-3">
          <div className="min-w-0 flex-1">
            <input
              id="servicio-imagen"
              type="text"
              placeholder="corte-normal.jpg"
              aria-invalid={Boolean(errors.imagen)}
              aria-describedby={
                errors.imagen ? 'servicio-imagen-error' : 'servicio-imagen-ayuda'
              }
              className={`${CLASE_CAMPO} font-mono text-sm ${
                errors.imagen ? CLASE_CAMPO_ERROR : ''
              }`}
              {...register('imagen')}
            />
            <p id="servicio-imagen-ayuda" className="mt-1.5 text-xs text-marca-500">
              Solo el nombre del archivo de <code>public/servicios/</code>.
            </p>
            <ErrorCampo id="servicio-imagen-error" mensaje={errors.imagen?.message} />
          </div>

          <ImagenServicio
            archivo={imagenValor.trim() || null}
            nombre={nombreValor.trim() || 'Servicio'}
            className="aspect-[4/3] w-28 shrink-0 rounded-xl border border-marca-200"
          />
        </div>
      </div>

      {errorGeneral && (
        <div
          role="alert"
          className="flex items-start gap-2.5 rounded-xl border border-error/30 bg-error/5 px-3.5 py-2.5 text-sm text-error"
        >
          <CircleAlert aria-hidden="true" className="mt-0.5 size-4 shrink-0" />
          <p>{errorGeneral}</p>
        </div>
      )}

      <div className="flex flex-col-reverse gap-2 border-t border-marca-200 pt-4 sm:flex-row sm:justify-end">
        <button
          type="button"
          onClick={alCancelar}
          disabled={guardandoAhora}
          className="inline-flex min-h-11 items-center justify-center rounded-full border border-marca-300 px-5 font-mono text-[11px] font-medium uppercase tracking-[0.14em] text-texto transition-colors duration-150 hover:border-marca-900 hover:bg-marca-50 active:translate-y-px disabled:cursor-not-allowed disabled:opacity-60"
        >
          Cancelar
        </button>
        <button
          type="submit"
          disabled={guardandoAhora}
          className="inline-flex min-h-11 items-center justify-center gap-2 rounded-full bg-marca-900 px-5 font-mono text-[11px] font-medium uppercase tracking-[0.14em] text-white transition-[background-color,transform] duration-150 hover:bg-marca-800 active:translate-y-px disabled:cursor-not-allowed disabled:opacity-60"
        >
          {guardandoAhora && <Loader2 aria-hidden="true" className="size-3.5 animate-spin" />}
          {servicio ? 'Guardar cambios' : 'Crear servicio'}
        </button>
      </div>
    </form>
  )
}
