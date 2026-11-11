import { zodResolver } from '@hookform/resolvers/zod'
import {
  ArrowRight,
  Check,
  CircleAlert,
  Eye,
  EyeOff,
  Loader2,
  Scissors,
  TriangleAlert,
} from 'lucide-react'
import { useState } from 'react'
import { Controller, useForm, useWatch } from 'react-hook-form'
import { Link } from 'react-router'
import { z } from 'zod'
import { AdornoTijeras } from '../../components/comunes/AdornoTijeras'
import { BarberoPole } from '../../components/comunes/BarberoPole'
import { Marca } from '../../components/comunes/Marca'
import { SelectorPais } from '../../components/comunes/SelectorPais'
import { RUTAS } from '../../config/rutas'
import { useAuth } from '../../hooks/useAuth'
import type { ApiError } from '../../types/error'
import type { UsuarioResponse } from '../../types/auth'
import { componerCelular, PAISES, paisPorCodigo } from '../../utils/celularPais'
import { ETIQUETAS_FUERZA, medirFuerzaContrasena } from '../../utils/contrasena'

const ANIO_ACTUAL = new Date().getFullYear()

const esquemaRegistro = z
  .object({
    nombre: z
      .string()
      .min(2, 'Escribe tu nombre completo')
      .max(100, 'El nombre no puede superar 100 caracteres'),
    codigoPais: z.string().min(1, 'Selecciona el código del país'),
    numero: z.string().regex(/^\d{6,12}$/, 'Escribe solo números'),
    contrasena: z
      .string()
      .min(8, 'La contraseña debe tener al menos 8 caracteres')
      .max(72, 'La contraseña no puede superar 72 caracteres'),
    acepta: z.boolean().refine((valor) => valor, 'Debes aceptar el tratamiento de datos'),
  })
  .superRefine((valores, contexto) => {
    if (!valores.numero || !componerCelular(valores.codigoPais, valores.numero)) {
      contexto.addIssue({
        code: 'custom',
        path: ['numero'],
        message: 'Revisa la cantidad de dígitos para el país seleccionado',
      })
    }
  })

type Formulario = z.infer<typeof esquemaRegistro>

/** Etiqueta monoespaciada de cada campo. Discreta para no competir con el valor. */
const CLASE_ETIQUETA =
  'mb-1.5 block font-mono text-[11px] font-medium uppercase tracking-[0.14em] text-marca-500'

/** Caja del campo: borde visible, radio moderno y foco con halo suave. */
const CLASE_CAJA =
  'flex h-12 items-center rounded-xl border bg-superficie shadow-[inset_0_1px_2px_rgba(0,0,0,0.03)] transition-[border-color,background-color,box-shadow] duration-200 hover:border-marca-400 hover:bg-marca-50/60 focus-within:border-marca-900 focus-within:bg-superficie focus-within:shadow-[0_0_0_4px_rgba(10,10,10,0.07)] sm:h-13'

/** La caja con contenido escrito se tiñe para confirmar que hay un valor. */
const CLASE_CAJA_LLENA = 'border-marca-300 bg-marca-50'

const CLASE_CAJA_VACIA = 'border-marca-200'

const CLASE_CAJA_ERROR =
  'border-error focus-within:border-error focus-within:shadow-[0_0_0_4px_rgba(185,28,28,0.1)]'

const CLASE_ENTRADA =
  'h-full min-w-0 flex-1 border-0 bg-transparent px-4 text-base text-texto placeholder:text-marca-400 focus:outline-none'

/** El campo de celular comparte altura y foco, pero lleva su propio relleno. */
const CLASE_ENTRADA_NUMERO =
  'h-full min-w-0 flex-1 border-0 bg-transparent py-0 pl-3 pr-2 font-mono text-[14px] tabular-nums text-texto placeholder:text-marca-400 focus:outline-none min-[360px]:text-[15px]'

function caja(llena: boolean, error: boolean): string {
  return `${CLASE_CAJA} ${llena ? CLASE_CAJA_LLENA : CLASE_CAJA_VACIA} ${
    error ? CLASE_CAJA_ERROR : ''
  }`
}

/** Mensaje de error de un campo, anunciado a lectores de pantalla. */
function ErrorCampo({ id, mensaje }: { id: string; mensaje?: string }) {
  if (!mensaje) {
    return null
  }

  return (
    <p id={id} className="mt-2 flex items-start gap-1.5 text-sm text-error" role="alert">
      <CircleAlert aria-hidden="true" className="mt-0.5 size-3.5 shrink-0" />
      {mensaje}
    </p>
  )
}

/** RF-01: registro de un cliente con nombre, celular y contraseña. */
export function RegistroPage() {
  const { registrar } = useAuth()
  const [verContrasena, setVerContrasena] = useState(false)
  const [errorGeneral, setErrorGeneral] = useState<ApiError | null>(null)
  const [registrado, setRegistrado] = useState<UsuarioResponse | null>(null)

  const {
    control,
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<Formulario>({
    resolver: zodResolver(esquemaRegistro),
    mode: 'onBlur',
    defaultValues: {
      nombre: '',
      codigoPais: '57',
      numero: '',
      contrasena: '',
      acepta: false,
    },
  })

  const codigoPais = useWatch({ control, name: 'codigoPais' }) ?? '57'
  const nombreValor = useWatch({ control, name: 'nombre' }) ?? ''
  const numeroValor = useWatch({ control, name: 'numero' }) ?? ''
  const contrasenaValor = useWatch({ control, name: 'contrasena' }) ?? ''
  const acepta = useWatch({ control, name: 'acepta' }) ?? false

  const pais = paisPorCodigo(codigoPais)
  const ayudaNumero = pais
    ? `${pais.nombre} · ${
        pais.digitos[0] === pais.digitos[1]
          ? `${pais.digitos[0]} dígitos`
          : `${pais.digitos[0]} a ${pais.digitos[1]} dígitos`
      }`
    : 'Selecciona un país'

  const nombreValido = nombreValor.trim().length >= 2
  const telefonoValido = Boolean(componerCelular(codigoPais, numeroValor))
  const contrasenaValida = contrasenaValor.length >= 8
  const fuerza = medirFuerzaContrasena(contrasenaValor)

  const pasosListos = [nombreValido, telefonoValido, contrasenaValida, acepta].filter(
    Boolean,
  ).length

  const alEnviar = handleSubmit(async (valores) => {
    setErrorGeneral(null)

    const celular = componerCelular(valores.codigoPais, valores.numero)
    if (!celular) {
      setError('numero', { message: 'Revisa el número de celular' })
      return
    }

    try {
      // Registro + sesión iniciada de una vez: el comprobante va en la cookie
      // y el pase corto queda en memoria, listo para usar la app.
      const sesion = await registrar({
        nombre: valores.nombre.trim(),
        celular,
        contrasena: valores.contrasena,
      })
      setRegistrado(sesion)
    } catch (fallo) {
      const error = fallo as ApiError

      if (error.erroresCampo) {
        for (const [campo, mensaje] of Object.entries(error.erroresCampo)) {
          const destino =
            campo === 'celular' ? 'numero' : campo === 'contrasena' ? 'contrasena' : 'nombre'
          if (destino === 'nombre' || destino === 'numero' || destino === 'contrasena') {
            setError(destino, { type: 'servidor', message: mensaje })
          }
        }
      }

      setErrorGeneral({
        status: error.status ?? 0,
        title: error.title ?? 'No pudimos crear la cuenta',
        detail: error.detail ?? 'Revisa los datos e intenta de nuevo.',
      })
    }
  })

  return (
    <section className="border-b border-borde bg-fondo lg:flex lg:min-h-[calc(100svh-4rem)] lg:flex-col">
      <div className="mx-auto grid w-full max-w-6xl lg:flex-1 lg:grid-cols-[0.9fr_1fr]">
        <aside className="group relative hidden flex-col justify-between overflow-hidden bg-marca-950 px-10 py-14 text-white lg:m-4 lg:flex lg:rounded-[32px] lg:py-10 corto:px-8 corto:py-6 xl:m-5 xl:px-16">
          <div
            aria-hidden="true"
            className="pointer-events-none absolute -right-28 top-1/2 size-96 rounded-full border border-white/[0.09] transition-transform duration-700 ease-out group-hover:-translate-x-3 group-hover:scale-105"
          />
          <div
            aria-hidden="true"
            className="pointer-events-none absolute -left-16 bottom-10 size-52 rounded-full border border-white/[0.06]"
          />
          <Scissors
            aria-hidden="true"
            className="pointer-events-none absolute -bottom-12 -right-10 size-64 rotate-[30deg] text-white/[0.06]"
          />
          <BarberoPole
            oscuro
            className="absolute right-10 top-20 h-28 w-4 xl:right-16 xl:h-32 xl:w-[18px]"
          />

          <div className="relative flex flex-wrap items-center justify-between gap-3">
            <Marca className="text-[28px]" />
            <span className="font-mono text-xs uppercase tracking-[0.12em] text-white/70">
              Ipiales, Colombia
            </span>
          </div>

          <div className="relative mt-16 corto:mt-6">
            <h2 className="max-w-md font-display text-5xl font-semibold leading-[1.05] text-white xl:text-6xl corto:text-5xl!">
              El detalle también es parte del corte.
            </h2>
            <p className="mt-6 max-w-sm text-base leading-relaxed text-white/75 corto:mt-3">
              Crea tu cuenta una vez y reserva en segundos, sin llamadas ni esperas.
            </p>
          </div>

          <div className="relative mt-16 border-t border-white/20 pt-5 corto:mt-6">
            <p className="font-mono text-xs uppercase tracking-[0.12em] text-white/70">
              OalBarber © {ANIO_ACTUAL}
            </p>
          </div>
        </aside>

        <div className="flex min-w-0 items-start justify-center px-4 py-4 bajito:py-3 sm:px-8 sm:py-12 lg:px-12 lg:py-6 corto:py-3">
          <div className="w-full min-w-0 max-w-md lg:my-auto">
            <div className="animar-entrada rounded-3xl border border-marca-300 bg-superficie p-4 shadow-[0_1px_2px_rgba(0,0,0,0.04),0_24px_48px_-32px_rgba(0,0,0,0.25)] transition-[border-color,box-shadow] duration-300 hover:border-marca-400 hover:shadow-[0_2px_4px_rgba(0,0,0,0.05),0_32px_64px_-36px_rgba(0,0,0,0.32)] bajito:p-3.5 sm:p-7 lg:p-6 corto:p-4">
              {registrado ? (
                <div>
                  <div className="flex size-14 items-center justify-center rounded-2xl border border-marca-300 bg-marca-50">
                    <Check aria-hidden="true" className="size-6" />
                  </div>

                  <p className="mt-8 font-mono text-[11px] font-medium uppercase tracking-[0.14em] text-marca-500">
                    Registro completado
                  </p>
                  <h1 className="mt-3 font-display text-[2.25rem] font-semibold leading-[1.05] sm:text-5xl">
                    Cuenta creada
                  </h1>
                  <p className="mt-4 text-base leading-relaxed text-texto-suave">
                    Listo, {registrado.nombre.split(' ')[0]}. Tu cuenta quedó registrada y
                    tu sesión ya empezó: no necesitas ingresar otra vez.
                  </p>

                  <dl className="mt-6 border-t border-borde text-base sm:mt-7">
                    <div className="flex items-baseline justify-between gap-6 border-b border-borde py-3.5">
                      <dt className="font-mono text-[11px] uppercase tracking-[0.14em] text-marca-500">
                        Nombre
                      </dt>
                      <dd className="text-right font-medium">{registrado.nombre}</dd>
                    </div>
                    <div className="flex items-baseline justify-between gap-6 border-b border-borde py-3.5">
                      <dt className="font-mono text-[11px] uppercase tracking-[0.14em] text-marca-500">
                        Celular
                      </dt>
                      <dd className="text-right font-mono text-sm font-medium tabular-nums">
                        {registrado.celular}
                      </dd>
                    </div>
                  </dl>

                  <AdornoTijeras className="mt-6" />

                  <Link
                    to={RUTAS.misCitas}
                    className="group mt-5 flex h-12 w-full items-center justify-between gap-3 rounded-full border border-marca-900 bg-marca-900 py-1.5 pl-5 pr-1.5 font-mono text-xs font-medium uppercase tracking-[0.16em] text-white transition-[background-color,transform] duration-150 hover:bg-marca-800 active:translate-y-px sm:h-13"
                  >
                    Ver mis citas
                    <span className="flex size-8 shrink-0 items-center justify-center rounded-full bg-white text-marca-900 transition-transform duration-200 group-hover:translate-x-0.5">
                      <ArrowRight aria-hidden="true" className="size-4" />
                    </span>
                  </Link>
                  <Link
                    to={RUTAS.inicio}
                    className="mt-3 flex h-12 w-full items-center justify-center rounded-full border border-marca-300 font-mono text-xs font-medium uppercase tracking-[0.16em] text-texto transition-colors duration-150 hover:border-marca-900 hover:bg-marca-50 active:translate-y-px sm:h-13"
                  >
                    Volver al inicio
                  </Link>
                </div>
              ) : (
                <div>
                  <div className="flex items-center justify-between gap-4">
                    <div className="flex items-center gap-2.5">
                      <BarberoPole className="h-8 w-1.5 bajito:h-6" />
                      <p className="font-mono text-[11px] font-medium uppercase tracking-[0.14em] text-marca-500">
                        Cuenta nueva
                      </p>
                    </div>
                    <span
                      className="rounded-full border border-marca-200 px-2.5 py-1 font-mono text-[11px] tabular-nums text-marca-500"
                      aria-hidden="true"
                    >
                      {pasosListos}/4
                    </span>
                  </div>
                  <div
                    role="progressbar"
                    aria-label="Campos completados"
                    aria-valuemin={0}
                    aria-valuemax={4}
                    aria-valuenow={pasosListos}
                    className="mt-2.5 h-1.5 w-full overflow-hidden rounded-full bg-marca-100 ring-1 ring-marca-200 ring-inset sm:mt-3"
                  >
                    <div
                      className="h-full rounded-full bg-marca-900 transition-[width] duration-300 ease-out"
                      style={{ width: `${(pasosListos / 4) * 100}%` }}
                    />
                  </div>

                  <h1 className="mt-4 font-display text-[2rem] font-semibold leading-[1.05] bajito:mt-3 sm:text-5xl">
                    Crea tu cuenta
                  </h1>
                  <p className="mt-2 text-sm leading-relaxed text-marca-500 sm:mt-3 sm:text-base">
                    Nombre, celular y contraseña. Menos de un minuto.
                  </p>

                  {errorGeneral && (
                    <div
                      role="alert"
                      className="animar-entrada mt-4 flex gap-3 rounded-2xl border border-error/30 bg-error/5 px-4 py-3 text-sm text-error sm:mt-5"
                    >
                      <TriangleAlert aria-hidden="true" className="mt-0.5 size-4 shrink-0" />
                      <div>
                        <p className="font-semibold">{errorGeneral.title}</p>
                        <p className="mt-0.5">{errorGeneral.detail}</p>
                      </div>
                    </div>
                  )}

                  <form
                    noValidate
                    onSubmit={alEnviar}
                    className="mt-4 space-y-4 bajito:space-y-3 sm:mt-6 sm:space-y-6 lg:mt-5 lg:space-y-4 corto:space-y-3"
                  >
                    <div>
                      <label htmlFor="nombre" className={CLASE_ETIQUETA}>
                        Nombre completo
                      </label>
                      <div className={caja(nombreValido, Boolean(errors.nombre))}>
                        <input
                          id="nombre"
                          type="text"
                          autoComplete="name"
                          autoCapitalize="words"
                          enterKeyHint="next"
                          placeholder="Ej. Santiago Bastidas"
                          aria-invalid={Boolean(errors.nombre)}
                          aria-describedby={errors.nombre ? 'error-nombre' : undefined}
                          className={CLASE_ENTRADA}
                          {...register('nombre')}
                        />
                        {nombreValido && (
                          <Check
                            aria-hidden="true"
                            className="mr-4 size-4 shrink-0 text-marca-900"
                          />
                        )}
                      </div>
                      <ErrorCampo id="error-nombre" mensaje={errors.nombre?.message} />
                    </div>

                    <div>
                      <div className="flex items-baseline justify-between gap-3">
                        <label htmlFor="numero" className={CLASE_ETIQUETA}>
                          Celular
                        </label>
                        <span
                          id="ayuda-numero"
                          className="truncate font-mono text-[10px] uppercase tracking-[0.1em] text-marca-400 sm:text-[11px]"
                        >
                          {ayudaNumero}
                        </span>
                      </div>
                      <div className={caja(telefonoValido, Boolean(errors.numero))}>
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

                        <span aria-hidden="true" className="mx-0.5 h-6 w-px bg-borde" />

                        <Controller
                          control={control}
                          name="numero"
                          render={({ field }) => (
                            <input
                              {...field}
                              id="numero"
                              type="tel"
                              inputMode="numeric"
                              autoComplete="tel-national"
                              enterKeyHint="next"
                              maxLength={12}
                              placeholder="3001234567"
                              aria-invalid={Boolean(errors.numero)}
                              aria-describedby={errors.numero ? 'error-numero' : 'ayuda-numero'}
                              onChange={(evento) =>
                                field.onChange(
                                  evento.target.value.replace(/\D/g, '').slice(0, 12),
                                )
                              }
                              className={CLASE_ENTRADA_NUMERO}
                            />
                          )}
                        />

                        {telefonoValido && (
                          <Check
                            aria-hidden="true"
                            className="mr-2 size-4 shrink-0 text-marca-900"
                          />
                        )}
                      </div>
                      <ErrorCampo id="error-numero" mensaje={errors.numero?.message} />
                    </div>

                    <div>
                      <label htmlFor="contrasena" className={CLASE_ETIQUETA}>
                        Contraseña
                      </label>
                      <div
                        className={`relative ${caja(contrasenaValida, Boolean(errors.contrasena))}`}
                      >
                        <input
                          id="contrasena"
                          type={verContrasena ? 'text' : 'password'}
                          autoComplete="new-password"
                          autoCapitalize="none"
                          spellCheck={false}
                          enterKeyHint="done"
                          placeholder="Mínimo 8 caracteres"
                          aria-invalid={Boolean(errors.contrasena)}
                          aria-describedby={
                            errors.contrasena ? 'error-contrasena' : 'ayuda-contrasena'
                          }
                          className={`${CLASE_ENTRADA} pr-12`}
                          {...register('contrasena')}
                        />
                        <button
                          type="button"
                          onClick={() => setVerContrasena((visible) => !visible)}
                          aria-label={verContrasena ? 'Ocultar contraseña' : 'Mostrar contraseña'}
                          aria-pressed={verContrasena}
                          className="absolute right-1 top-1/2 flex size-10 -translate-y-1/2 items-center justify-center rounded-full text-marca-500 transition-colors duration-150 hover:bg-marca-100 hover:text-texto active:bg-marca-200"
                        >
                          {verContrasena ? (
                            <EyeOff aria-hidden="true" className="size-4" />
                          ) : (
                            <Eye aria-hidden="true" className="size-4" />
                          )}
                        </button>
                      </div>

                      <div id="ayuda-contrasena">
                        {contrasenaValor.length > 0 && (
                          <div className="mt-2 flex items-center gap-3">
                            <div className="flex w-20 shrink-0 gap-1" aria-hidden="true">
                              {[1, 2, 3].map((nivel) => (
                                <span
                                  key={nivel}
                                  className={`h-1 flex-1 rounded-full transition-colors duration-200 ${
                                    fuerza >= nivel ? 'bg-marca-900' : 'bg-marca-200'
                                  }`}
                                />
                              ))}
                            </div>
                            <p
                              className="truncate font-mono text-[11px] uppercase tracking-[0.12em] text-marca-500"
                              aria-live="polite"
                            >
                              <span className="sr-only">Seguridad de la contraseña: </span>
                              <span className="text-texto">{ETIQUETAS_FUERZA[fuerza]}</span>
                            </p>
                          </div>
                        )}
                      </div>
                      <ErrorCampo id="error-contrasena" mensaje={errors.contrasena?.message} />
                    </div>

                    <div>
                      <label className="group flex min-h-10 cursor-pointer items-start gap-3 text-[15px] leading-relaxed text-texto-suave">
                        <input
                          type="checkbox"
                          className="peer sr-only"
                          aria-invalid={Boolean(errors.acepta)}
                          aria-describedby={errors.acepta ? 'error-acepta' : undefined}
                          {...register('acepta')}
                        />
                        <span className="mt-0.5 flex size-6 shrink-0 items-center justify-center rounded-lg border border-marca-300 bg-superficie shadow-sm transition-[border-color,background-color] duration-150 group-hover:border-marca-500 peer-checked:border-marca-900 peer-checked:bg-marca-900 peer-checked:[&>svg]:opacity-100 peer-focus-visible:outline-2 peer-focus-visible:outline-offset-2 peer-focus-visible:outline-marca-900">
                          <Check aria-hidden="true" className="size-3.5 text-white opacity-0" />
                        </span>
                        <span>
                          Acepto la política de tratamiento de datos personales.
                        </span>
                      </label>
                      <ErrorCampo id="error-acepta" mensaje={errors.acepta?.message} />
                    </div>

                    <AdornoTijeras className="bajito:hidden" />

                    <button
                      type="submit"
                      disabled={isSubmitting}
                      className="group flex h-12 w-full items-center justify-between gap-3 rounded-full border border-marca-900 bg-marca-900 py-1.5 pl-5 pr-1.5 font-mono text-xs font-medium uppercase tracking-[0.16em] text-white transition-[background-color,transform] duration-150 hover:bg-marca-800 active:translate-y-px disabled:cursor-not-allowed disabled:opacity-60 sm:h-13"
                    >
                      <span>{isSubmitting ? 'Creando cuenta…' : 'Crear cuenta'}</span>
                      <span className="flex size-8 shrink-0 items-center justify-center rounded-full bg-white text-marca-900 transition-transform duration-200 group-hover:translate-x-0.5">
                        {isSubmitting ? (
                          <Loader2 aria-hidden="true" className="size-4 animate-spin" />
                        ) : (
                          <ArrowRight aria-hidden="true" className="size-4" />
                        )}
                      </span>
                    </button>
                  </form>
                </div>
              )}
            </div>

            {!registrado && (
              <p className="mt-4 text-center text-sm text-texto-suave corto:mt-2">
                ¿Ya tienes cuenta?{' '}
                <Link
                  to={RUTAS.ingresar}
                  className="font-medium text-texto underline decoration-marca-300 underline-offset-4 transition-[text-decoration-color,text-decoration-thickness] duration-200 hover:decoration-2 hover:decoration-marca-900"
                >
                  Inicia sesión
                </Link>
              </p>
            )}
          </div>
        </div>
      </div>
    </section>
  )
}
