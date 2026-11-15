import { zodResolver } from '@hookform/resolvers/zod'
import { CircleAlert, Loader2, Plus, Scissors, UserRound } from 'lucide-react'
import { useState } from 'react'
import { Controller, useForm, useWatch } from 'react-hook-form'
import { z } from 'zod'
import { SelectorPais } from '../../components/comunes/SelectorPais'
import { useBarberosAdmin, useCambiarEstadoBarbero, useCrearBarbero } from '../../hooks/useBarberos'
import type { ApiError } from '../../types/error'
import type { BarberoRequest } from '../../types/barbero'
import { componerCelular, paisPorCodigo, PAISES } from '../../utils/celularPais'

const patronFoto = /^[a-z0-9-]+\.(jpg|jpeg|png|webp)$/

const esquemaBarbero = z
  .object({
    nombre: z.string().trim().min(2, 'Escribe al menos dos caracteres').max(100, 'Máximo 100 caracteres'),
    codigoPais: z.string().min(1, 'Selecciona un país'),
    numero: z.string().regex(/^\d{6,12}$/, 'Escribe solo números'),
    contrasena: z.string().min(8, 'Usa al menos 8 caracteres').max(72, 'Máximo 72 caracteres'),
    nombrePublico: z.string().max(100, 'Máximo 100 caracteres'),
    descripcion: z.string().max(500, 'Máximo 500 caracteres'),
    foto: z
      .string()
      .max(255, 'El nombre de archivo no puede superar 255 caracteres')
      .refine((valor) => valor.length === 0 || patronFoto.test(valor),
        'Usa un nombre simple terminado en .jpg, .jpeg, .png o .webp'),
  })
  .superRefine((valores, contexto) => {
    if (!componerCelular(valores.codigoPais, valores.numero)) {
      contexto.addIssue({
        code: 'custom',
        path: ['numero'],
        message: 'Revisa la cantidad de dígitos para el país seleccionado',
      })
    }
  })

type FormularioBarbero = z.infer<typeof esquemaBarbero>

const CLASE_ETIQUETA =
  'mb-1.5 block font-mono text-[11px] font-medium uppercase tracking-[0.14em] text-marca-500'
const CLASE_ENTRADA =
  'min-h-11 w-full rounded-xl border border-marca-300 bg-superficie px-3.5 text-base text-texto placeholder:text-marca-400 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-marca-900'
const CLASE_CAJA_CELULAR =
  'flex min-h-12 items-center rounded-xl border border-marca-300 bg-superficie focus-within:border-marca-900 focus-within:ring-2 focus-within:ring-marca-900/10'

function ErrorCampo({ id, mensaje }: { id: string; mensaje?: string }) {
  if (!mensaje) {
    return null
  }
  return <p id={id} className="mt-1.5 text-sm text-error" role="alert">{mensaje}</p>
}

function mensajeDeError(error: unknown, respaldo: string): { title: string; detail: string } {
  const apiError = error as Partial<ApiError> | null
  return {
    title: apiError?.title || 'No pudimos completar la operación',
    detail: apiError?.detail || respaldo,
  }
}

/** RF-10: alta y estado de perfiles; no gestiona servicios por barbero (Parte B). */
export function BarberosPage() {
  const consulta = useBarberosAdmin()
  const alta = useCrearBarbero()
  const cambioEstado = useCambiarEstadoBarbero()
  const [errorAlta, setErrorAlta] = useState<{ title: string; detail: string } | null>(null)
  const [altaCorrecta, setAltaCorrecta] = useState(false)

  const {
    control,
    register,
    handleSubmit,
    reset,
    setError,
    clearErrors,
    formState: { errors },
  } = useForm<FormularioBarbero>({
    resolver: zodResolver(esquemaBarbero),
    mode: 'onBlur',
    defaultValues: {
      nombre: '',
      codigoPais: '57',
      numero: '',
      contrasena: '',
      nombrePublico: '',
      descripcion: '',
      foto: '',
    },
  })

  const codigoPais = useWatch({ control, name: 'codigoPais' }) ?? '57'
  const numero = useWatch({ control, name: 'numero' }) ?? ''
  const celularValido = Boolean(componerCelular(codigoPais, numero))
  const pais = paisPorCodigo(codigoPais)

  const enviarAlta = handleSubmit(async (valores) => {
    setErrorAlta(null)
    setAltaCorrecta(false)
    clearErrors()

    const celular = componerCelular(valores.codigoPais, valores.numero)
    if (!celular) {
      setError('numero', { type: 'manual', message: 'Revisa el número de celular' })
      return
    }

    const solicitud: BarberoRequest = {
      nombre: valores.nombre.trim(),
      celular,
      contrasena: valores.contrasena,
      nombrePublico: valores.nombrePublico.trim() || undefined,
      descripcion: valores.descripcion.trim() || undefined,
      foto: valores.foto.trim() || undefined,
    }

    try {
      await alta.mutateAsync(solicitud)
      reset()
      setAltaCorrecta(true)
    } catch (error) {
      const detalle = mensajeDeError(error, 'Revisa los datos e intenta de nuevo.')
      const apiError = error as Partial<ApiError> | null
      const camposFormulario: Record<string, keyof FormularioBarbero> = {
        nombre: 'nombre',
        celular: 'numero',
        contrasena: 'contrasena',
        nombrePublico: 'nombrePublico',
        descripcion: 'descripcion',
        foto: 'foto',
      }
      Object.entries(apiError?.erroresCampo ?? {}).forEach(([campo, mensaje]) => {
        const destino = camposFormulario[campo]
        if (destino) {
          setError(destino, { type: 'servidor', message: mensaje })
        }
      })
      if (apiError?.status === 409 && !apiError.erroresCampo?.celular) {
        setError('numero', { type: 'servidor', message: detalle.detail })
      }
      setErrorAlta(detalle)
    }
  })

  const confirmarCambioEstado = (id: string, nombre: string, activo: boolean) => {
    const proximoEstado = !activo
    const accion = proximoEstado ? 'activar' : 'desactivar'
    const consecuencia = activo
      ? 'El perfil dejará de aparecer en el listado público. La cuenta y sus sesiones seguirán activas.'
      : 'El perfil volverá a aparecer en el listado público.'
    const confirmado = window.confirm(
      `¿Quieres ${accion} el perfil de ${nombre}?\n\n${consecuencia}\nNo se cancelan ni se mueven citas.`,
    )
    if (confirmado) {
      cambioEstado.mutate({ id, activo: proximoEstado })
    }
  }

  const errorEstado = cambioEstado.isError
    ? mensajeDeError(cambioEstado.error, 'Intenta de nuevo en unos momentos.')
    : null

  return (
    <section className="px-4 py-6 sm:px-6 sm:py-8">
      <div className="mx-auto w-full max-w-5xl">
        <p className="font-mono text-[11px] font-medium uppercase tracking-[0.14em] text-marca-500">
          Panel
        </p>
        <h1 className="mt-3 font-display text-4xl font-semibold leading-[1.05] sm:text-5xl">
          Barberos
        </h1>
        <p className="mt-3 max-w-2xl text-sm leading-relaxed text-texto-suave sm:text-base">
          Administra perfiles y cuentas. Desactivar un perfil no cancela ni mueve sus citas.
        </p>

        <section aria-labelledby="alta-titulo" className="mt-7 rounded-2xl border border-marca-300 bg-superficie p-4 shadow-[0_1px_2px_rgba(0,0,0,0.04)] sm:p-6">
          <div className="flex items-start gap-3">
            <span aria-hidden="true" className="flex size-10 shrink-0 items-center justify-center rounded-xl border border-marca-200 bg-marca-50">
              <Plus className="size-5 text-marca-700" />
            </span>
            <div>
              <h2 id="alta-titulo" className="text-lg font-semibold text-texto">Crear perfil</h2>
              <p className="mt-1 text-sm text-texto-suave">La cuenta se crea con rol Barbero; la contraseña inicial se cifra en el servidor.</p>
            </div>
          </div>

          {errorAlta && (
            <div role="alert" className="mt-5 rounded-xl border border-error/30 bg-error/5 p-3.5 text-sm text-error">
              <p className="font-semibold">{errorAlta.title}</p>
              <p className="mt-1">{errorAlta.detail}</p>
            </div>
          )}
          {altaCorrecta && (
            <p role="status" className="mt-5 rounded-xl border border-marca-300 bg-marca-50 p-3.5 text-sm text-texto">
              Perfil de barbero creado correctamente.
            </p>
          )}

          <form noValidate onSubmit={enviarAlta} className="mt-5 grid gap-x-5 gap-y-4 sm:grid-cols-2">
            <div>
              <label htmlFor="barbero-nombre" className={CLASE_ETIQUETA}>Nombre de la cuenta</label>
              <input
                id="barbero-nombre"
                autoComplete="name"
                autoCapitalize="words"
                aria-invalid={Boolean(errors.nombre)}
                aria-describedby={errors.nombre ? 'barbero-nombre-error' : undefined}
                className={CLASE_ENTRADA}
                {...register('nombre')}
              />
              <ErrorCampo id="barbero-nombre-error" mensaje={errors.nombre?.message} />
            </div>

            <div>
              <label htmlFor="barbero-numero" className={CLASE_ETIQUETA}>Celular</label>
              <div className={`${CLASE_CAJA_CELULAR} ${errors.numero ? 'border-error' : ''}`}>
                <Controller
                  control={control}
                  name="codigoPais"
                  render={({ field }) => (
                    <SelectorPais
                      paises={PAISES}
                      valor={field.value}
                      onCambio={field.onChange}
                      invalido={Boolean(errors.numero)}
                    />
                  )}
                />
                <span aria-hidden="true" className="mx-1 h-6 w-px bg-borde" />
                <Controller
                  control={control}
                  name="numero"
                  render={({ field }) => (
                    <input
                      {...field}
                      id="barbero-numero"
                      type="tel"
                      inputMode="numeric"
                      autoComplete="tel-national"
                      aria-label="Número de celular"
                      aria-invalid={Boolean(errors.numero)}
                      aria-describedby={errors.numero ? 'barbero-numero-error' : 'barbero-numero-ayuda'}
                      maxLength={12}
                      onChange={(evento) => field.onChange(evento.target.value.replace(/\D/g, '').slice(0, 12))}
                      className="min-h-11 min-w-0 flex-1 rounded-r-xl bg-transparent px-3 font-mono text-base tabular-nums text-texto outline-none focus-visible:ring-2 focus-visible:ring-inset focus-visible:ring-marca-900"
                    />
                  )}
                />
              </div>
              <p id="barbero-numero-ayuda" className="mt-1 text-xs text-texto-suave">
                {pais ? `${pais.nombre}; prefijo +${pais.codigo}.` : 'Selecciona el país.'}
                {celularValido ? ' Número válido.' : ''}
              </p>
              <ErrorCampo id="barbero-numero-error" mensaje={errors.numero?.message} />
            </div>

            <div>
              <label htmlFor="barbero-contrasena" className={CLASE_ETIQUETA}>Contraseña inicial</label>
              <input
                id="barbero-contrasena"
                type="password"
                autoComplete="new-password"
                aria-invalid={Boolean(errors.contrasena)}
                aria-describedby={errors.contrasena ? 'barbero-contrasena-error' : undefined}
                className={CLASE_ENTRADA}
                {...register('contrasena')}
              />
              <p className="mt-1 text-xs text-texto-suave">Entre 8 y 72 caracteres.</p>
              <ErrorCampo id="barbero-contrasena-error" mensaje={errors.contrasena?.message} />
            </div>

            <div>
              <label htmlFor="barbero-nombre-publico" className={CLASE_ETIQUETA}>Nombre público (opcional)</label>
              <input
                id="barbero-nombre-publico"
                maxLength={100}
                aria-invalid={Boolean(errors.nombrePublico)}
                aria-describedby={errors.nombrePublico ? 'barbero-nombre-publico-error' : undefined}
                className={CLASE_ENTRADA}
                {...register('nombrePublico')}
              />
              <ErrorCampo id="barbero-nombre-publico-error" mensaje={errors.nombrePublico?.message} />
            </div>

            <div className="sm:col-span-2">
              <label htmlFor="barbero-descripcion" className={CLASE_ETIQUETA}>Descripción (opcional)</label>
              <textarea
                id="barbero-descripcion"
                rows={3}
                maxLength={500}
                aria-invalid={Boolean(errors.descripcion)}
                aria-describedby={errors.descripcion ? 'barbero-descripcion-error' : undefined}
                className={`${CLASE_ENTRADA} py-3`}
                {...register('descripcion')}
              />
              <ErrorCampo id="barbero-descripcion-error" mensaje={errors.descripcion?.message} />
            </div>

            <div className="sm:col-span-2">
              <label htmlFor="barbero-foto" className={CLASE_ETIQUETA}>Foto estática (opcional)</label>
              <input
                id="barbero-foto"
                placeholder="barbero-01.webp"
                maxLength={255}
                aria-invalid={Boolean(errors.foto)}
                aria-describedby={errors.foto ? 'barbero-foto-ayuda barbero-foto-error' : 'barbero-foto-ayuda'}
                className={CLASE_ENTRADA}
                {...register('foto')}
              />
              <p id="barbero-foto-ayuda" className="mt-1 text-xs text-texto-suave">
                Escribe solo el nombre del archivo en minúsculas; no se suben archivos. Formatos: JPG, JPEG, PNG o WebP.
              </p>
              <ErrorCampo id="barbero-foto-error" mensaje={errors.foto?.message} />
            </div>

            <div className="sm:col-span-2">
              <button
                type="submit"
                disabled={alta.isPending}
                className="flex min-h-11 w-full items-center justify-center gap-2 rounded-full bg-marca-900 px-5 font-mono text-xs font-medium uppercase tracking-[0.14em] text-white transition-colors hover:bg-marca-800 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-marca-900 disabled:cursor-not-allowed disabled:opacity-60 sm:w-auto"
              >
                {alta.isPending ? <Loader2 aria-hidden="true" className="size-4 animate-spin" /> : <Plus aria-hidden="true" className="size-4" />}
                {alta.isPending ? 'Creando perfil…' : 'Crear barbero'}
              </button>
            </div>
          </form>
        </section>

        <section aria-labelledby="lista-titulo" className="mt-9">
          <div className="flex items-baseline justify-between gap-4">
            <div>
              <h2 id="lista-titulo" className="font-display text-2xl font-semibold text-texto">Perfiles</h2>
              <p className="mt-1 text-sm text-texto-suave">Se muestran activos e inactivos. El estado del perfil no desactiva la cuenta.</p>
            </div>
            {!consulta.isPending && !consulta.isError && (
              <span className="rounded-full border border-marca-200 px-2.5 py-1 font-mono text-[11px] tabular-nums text-marca-500">
                {consulta.data?.length ?? 0}
              </span>
            )}
          </div>

          {consulta.isPending && (
            <div className="mt-4 space-y-2" role="status" aria-live="polite">
              <span className="sr-only">Cargando perfiles…</span>
              {[1, 2].map((item) => (
                <div key={item} aria-hidden="true" className="h-20 animate-pulse rounded-2xl border border-marca-200 bg-marca-50" />
              ))}
            </div>
          )}

          {consulta.isError && (
            <div role="alert" className="mt-4 rounded-2xl border border-error/30 bg-error/5 p-4 text-sm text-error">
              <div className="flex items-start gap-3">
                <CircleAlert aria-hidden="true" className="mt-0.5 size-4 shrink-0" />
                <div>
                  <p className="font-semibold">No pudimos cargar los barberos</p>
                  <p className="mt-1">Revisa tu conexión e intenta de nuevo.</p>
                  <button type="button" onClick={() => consulta.refetch()} className="mt-3 min-h-11 rounded-full border border-error/40 px-4 font-mono text-xs uppercase tracking-[0.12em] hover:bg-error/10 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-error">
                    Reintentar
                  </button>
                </div>
              </div>
            </div>
          )}

          {errorEstado && (
            <div role="alert" className="mt-4 rounded-xl border border-error/30 bg-error/5 p-3.5 text-sm text-error">
              <p className="font-semibold">{errorEstado.title}</p>
              <p className="mt-1">{errorEstado.detail}</p>
            </div>
          )}

          {!consulta.isPending && !consulta.isError && consulta.data?.length === 0 && (
            <div className="mt-4 rounded-2xl border border-dashed border-marca-300 bg-superficie px-6 py-8 text-center">
              <Scissors aria-hidden="true" className="mx-auto size-6 text-marca-400" />
              <p className="mt-3 font-medium text-texto">Aún no hay perfiles de barbero</p>
              <p className="mt-1 text-sm text-texto-suave">Crea el primer perfil desde el formulario.</p>
            </div>
          )}

          {!consulta.isPending && !consulta.isError && Boolean(consulta.data?.length) && (
            <ul className="mt-4 divide-y divide-borde overflow-hidden rounded-2xl border border-marca-300 bg-superficie">
              {consulta.data?.map((barbero) => {
                const nombre = barbero.nombrePublico?.trim() || barbero.nombre
                return (
                  <li key={barbero.id} className="flex flex-wrap items-center gap-3 px-4 py-4 sm:flex-nowrap sm:px-5">
                    <span aria-hidden="true" className="flex size-11 shrink-0 items-center justify-center rounded-full border border-marca-200 bg-marca-50 text-marca-700">
                      <UserRound className="size-5" />
                    </span>
                    <div className="min-w-0 flex-1">
                      <p className="truncate font-medium text-texto">{nombre}</p>
                      <p className="mt-0.5 truncate text-sm text-texto-suave">{barbero.nombre} · {barbero.celular}</p>
                    </div>
                    <span className={`rounded-full px-2.5 py-1 font-mono text-[10px] uppercase tracking-[0.12em] ${barbero.activo ? 'bg-marca-900 text-white' : 'border border-marca-300 text-marca-500'}`}>
                      {barbero.activo ? 'Activo' : 'Inactivo'}
                    </span>
                    <button
                      type="button"
                      disabled={cambioEstado.isPending}
                      onClick={() => confirmarCambioEstado(barbero.id, nombre, barbero.activo)}
                      aria-label={`${barbero.activo ? 'Desactivar' : 'Activar'} perfil de ${nombre}`}
                      className="min-h-11 w-full rounded-full border border-marca-300 px-4 font-mono text-xs font-medium uppercase tracking-[0.1em] text-texto transition-colors hover:border-marca-900 hover:bg-marca-50 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-marca-900 disabled:cursor-not-allowed disabled:opacity-50 sm:w-auto"
                    >
                      {cambioEstado.isPending ? 'Guardando…' : barbero.activo ? 'Desactivar' : 'Activar'}
                    </button>
                  </li>
                )
              })}
            </ul>
          )}
        </section>
      </div>
    </section>
  )
}
