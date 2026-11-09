import { zodResolver } from '@hookform/resolvers/zod'
import {
  ArrowRight,
  Check,
  CircleAlert,
  Eye,
  EyeOff,
  Loader2,
  TriangleAlert,
} from 'lucide-react'
import { useState } from 'react'
import { Controller, useForm, useWatch } from 'react-hook-form'
import { Link } from 'react-router'
import { z } from 'zod'
import { Marca } from '../../components/comunes/Marca'
import { SelectorPais } from '../../components/comunes/SelectorPais'
import { RUTAS } from '../../config/rutas'
import { registrarUsuario } from '../../services/usuarioService'
import type { ApiError } from '../../types/error'
import type { UsuarioRegistroResponse } from '../../types/usuario'
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

/** Etiqueta monoespaciada de cada campo. 12 px para leerse bien en móvil. */
const CLASE_ETIQUETA =
  'mb-2 block font-mono text-xs font-medium uppercase tracking-[0.12em] text-texto-suave'

/** Caja del campo: borde visible y cambio de fondo, borde y sombra al interactuar. */
const CLASE_CAJA =
  'flex h-13 items-center border bg-marca-50 transition-[border-color,background-color,box-shadow] duration-200 hover:border-marca-500 focus-within:border-marca-900 focus-within:bg-superficie focus-within:shadow-[0_0_0_3px_rgba(10,10,10,0.08)] sm:h-12'

/** La caja con contenido escrito se sombrea para confirmar que hay un valor. */
const CLASE_CAJA_LLENA = 'border-marca-400 bg-marca-100'

const CLASE_CAJA_VACIA = 'border-marca-300'

const CLASE_CAJA_ERROR =
  'border-error focus-within:border-error focus-within:shadow-[0_0_0_3px_rgba(185,28,28,0.12)]'

const CLASE_ENTRADA =
  'h-full min-w-0 flex-1 border-0 bg-transparent px-4 text-base text-texto placeholder:text-marca-500 focus:outline-none'

/** El campo de celular comparte altura y foco, pero lleva su propio relleno. */
const CLASE_ENTRADA_NUMERO =
  'h-full min-w-0 flex-1 border-0 bg-transparent py-0 pl-3 pr-2 font-mono text-[14px] tabular-nums text-texto placeholder:text-marca-500 focus:outline-none min-[360px]:text-[15px]'

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
  const [verContrasena, setVerContrasena] = useState(false)
  const [errorGeneral, setErrorGeneral] = useState<ApiError | null>(null)
  const [registrado, setRegistrado] = useState<UsuarioRegistroResponse | null>(null)

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
      const usuario = await registrarUsuario({
        nombre: valores.nombre.trim(),
        celular,
        contrasena: valores.contrasena,
      })
      setRegistrado(usuario)
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
        <aside className="group relative hidden flex-col justify-between overflow-hidden bg-marca-950 px-10 py-14 text-white lg:flex lg:h-full lg:py-10 xl:px-16">
          <div
            aria-hidden="true"
            className="pointer-events-none absolute -right-28 top-1/2 size-96 rounded-full border border-white/[0.07] transition-transform duration-700 ease-out group-hover:-translate-x-3 group-hover:scale-105"
          />

          <div className="relative flex flex-wrap items-center justify-between gap-3">
            <Marca className="text-[28px]" />
            <span className="font-mono text-xs uppercase tracking-[0.12em] text-white/70">
              Ipiales, Colombia
            </span>
          </div>

          <div className="relative mt-16">
            <h2 className="max-w-md font-display text-5xl font-semibold leading-[1.05] text-white xl:text-6xl">
              El detalle también es parte del corte.
            </h2>
            <p className="mt-6 max-w-sm text-base leading-relaxed text-white/75">
              Crea tu cuenta una vez y reserva en segundos, sin llamadas ni esperas.
            </p>
          </div>

          <div className="relative mt-16 border-t border-white/20 pt-5">
            <p className="font-mono text-xs uppercase tracking-[0.12em] text-white/70">
              OalBarber © {ANIO_ACTUAL}
            </p>
          </div>
        </aside>

        <div className="flex min-w-0 items-start justify-center px-5 py-8 sm:px-8 sm:py-12 lg:px-12 lg:py-6 corto:py-4">
          <div className="w-full min-w-0 max-w-md lg:my-auto">
            <div className="animar-entrada border-x border-b border-t-2 border-marca-300 border-t-marca-900 bg-superficie p-5 shadow-[0_1px_2px_rgba(0,0,0,0.05)] sm:p-8 lg:p-6 lg:transition-[border-color,box-shadow] lg:duration-300 lg:hover:border-marca-400 lg:hover:shadow-[0_18px_40px_-28px_rgba(0,0,0,0.45)] corto:p-5">
              {registrado ? (
                <div>
                  <div className="flex size-14 items-center justify-center rounded-full border border-marca-900">
                    <Check aria-hidden="true" className="size-6" />
                  </div>

                  <p className="mt-8 font-mono text-xs font-medium uppercase tracking-[0.12em] text-texto-suave">
                    Registro completado
                  </p>
                  <h1 className="mt-3 font-display text-[2.25rem] font-semibold leading-[1.05] sm:text-5xl">
                    Cuenta creada
                  </h1>
                  <p className="mt-4 text-base leading-relaxed text-texto-suave">
                    Listo, {registrado.nombre.split(' ')[0]}. Tu cuenta quedó registrada y ya puedes
                    ingresar con tu celular y contraseña.
                  </p>

                  <dl className="mt-7 border-t border-borde text-base">
                    <div className="flex items-baseline justify-between gap-6 border-b border-borde py-3.5">
                      <dt className="font-mono text-xs uppercase tracking-[0.12em] text-texto-suave">
                        Nombre
                      </dt>
                      <dd className="text-right font-medium">{registrado.nombre}</dd>
                    </div>
                    <div className="flex items-baseline justify-between gap-6 border-b border-borde py-3.5">
                      <dt className="font-mono text-xs uppercase tracking-[0.12em] text-texto-suave">
                        Celular
                      </dt>
                      <dd className="text-right font-mono text-sm font-medium tabular-nums">
                        {registrado.celular}
                      </dd>
                    </div>
                  </dl>

                  <Link
                    to={RUTAS.ingresar}
                    className="group mt-7 flex h-13 w-full items-center justify-between border border-marca-900 bg-marca-900 px-5 font-mono text-xs font-medium uppercase tracking-[0.16em] text-white transition-[background-color,transform] duration-150 hover:bg-marca-800 active:translate-y-px sm:h-12"
                  >
                    Iniciar sesión
                    <ArrowRight
                      aria-hidden="true"
                      className="size-4 transition-transform group-hover:translate-x-0.5"
                    />
                  </Link>
                  <Link
                    to={RUTAS.inicio}
                    className="mt-3 flex h-13 w-full items-center justify-center border border-marca-300 font-mono text-xs font-medium uppercase tracking-[0.16em] text-texto transition-colors duration-150 hover:border-marca-900 hover:bg-marca-50 active:translate-y-px sm:h-12"
                  >
                    Volver al inicio
                  </Link>
                </div>
              ) : (
                <div>
                  <div className="flex items-center justify-between gap-4">
                    <p className="font-mono text-xs font-medium uppercase tracking-[0.12em] text-texto-suave">
                      Cuenta nueva
                    </p>
                    <p className="font-mono text-xs tabular-nums text-texto-suave" aria-hidden="true">
                      {pasosListos}/4
                    </p>
                  </div>
                  <div
                    role="progressbar"
                    aria-label="Campos completados"
                    aria-valuemin={0}
                    aria-valuemax={4}
                    aria-valuenow={pasosListos}
                    className="mt-2 h-0.5 w-full overflow-hidden bg-marca-200"
                  >
                    <div
                      className="h-full bg-marca-900 transition-[width] duration-300 ease-out"
                      style={{ width: `${(pasosListos / 4) * 100}%` }}
                    />
                  </div>

                  <h1 className="mt-5 font-display text-[2.25rem] font-semibold leading-[1.05] sm:text-5xl lg:mt-4 lg:text-[2.5rem]">
                    Crea tu cuenta
                  </h1>
                  <p className="mt-3 text-[15px] leading-relaxed text-texto-suave sm:text-base">
                    Solo necesitas tu nombre, un celular y una contraseña. Toma menos de un minuto.
                  </p>

                  {errorGeneral && (
                    <div
                      role="alert"
                      className="animar-entrada mt-5 flex gap-3 border border-error/40 bg-error/5 px-4 py-3 text-sm text-error"
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
                    className="mt-6 space-y-5 sm:space-y-6 lg:mt-5 lg:space-y-4 corto:space-y-3"
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
                      <label htmlFor="numero" className={CLASE_ETIQUETA}>
                        Celular
                      </label>
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
                      <p id="ayuda-numero" className="mt-2 text-sm text-texto-suave">
                        {ayudaNumero}
                      </p>
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
                          className="absolute right-0 top-1/2 flex size-11 -translate-y-1/2 items-center justify-center text-texto-suave transition-colors duration-150 hover:text-texto active:text-marca-900"
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
                                  className={`h-1 flex-1 transition-colors duration-200 ${
                                    fuerza >= nivel ? 'bg-marca-900' : 'bg-marca-200'
                                  }`}
                                />
                              ))}
                            </div>
                            <p
                              className="truncate font-mono text-[11px] uppercase tracking-[0.12em] text-texto-suave"
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
                      <label className="group flex min-h-11 cursor-pointer items-start gap-3 text-[15px] leading-relaxed text-texto-suave">
                        <input
                          type="checkbox"
                          className="peer sr-only"
                          aria-invalid={Boolean(errors.acepta)}
                          aria-describedby={errors.acepta ? 'error-acepta' : undefined}
                          {...register('acepta')}
                        />
                        <span className="mt-0.5 flex size-6 shrink-0 items-center justify-center border border-marca-400 transition-colors duration-150 group-hover:border-marca-700 peer-checked:border-marca-900 peer-checked:bg-marca-900 peer-checked:[&>svg]:opacity-100 peer-focus-visible:outline-2 peer-focus-visible:outline-offset-2 peer-focus-visible:outline-marca-900">
                          <Check aria-hidden="true" className="size-3.5 text-white opacity-0" />
                        </span>
                        <span>
                          Acepto la política de tratamiento de datos personales de OalBarber.
                        </span>
                      </label>
                      <ErrorCampo id="error-acepta" mensaje={errors.acepta?.message} />
                    </div>

                    <button
                      type="submit"
                      disabled={isSubmitting}
                      className="group flex h-13 w-full items-center justify-between border border-marca-900 bg-marca-900 px-5 font-mono text-xs font-medium uppercase tracking-[0.16em] text-white transition-[background-color,transform] duration-150 hover:bg-marca-800 active:translate-y-px disabled:cursor-not-allowed disabled:opacity-60 sm:h-12"
                    >
                      <span>{isSubmitting ? 'Creando cuenta…' : 'Crear cuenta'}</span>
                      {isSubmitting ? (
                        <Loader2 aria-hidden="true" className="size-4 animate-spin" />
                      ) : (
                        <ArrowRight
                          aria-hidden="true"
                          className="size-4 transition-transform group-hover:translate-x-0.5"
                        />
                      )}
                    </button>
                  </form>
                </div>
              )}
            </div>

            {!registrado && (
              <p className="mt-5 text-center text-sm text-texto-suave corto:mt-3">
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
