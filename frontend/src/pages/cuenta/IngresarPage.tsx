import { zodResolver } from '@hookform/resolvers/zod'
import {
  ArrowRight,
  Eye,
  EyeOff,
  Loader2,
  TriangleAlert,
} from 'lucide-react'
import { useState } from 'react'
import { Controller, useForm } from 'react-hook-form'
import { Navigate, Link, useLocation, useNavigate } from 'react-router'
import { z } from 'zod'
import { AdornoTijeras } from '../../components/comunes/AdornoTijeras'
import { BarberoPole } from '../../components/comunes/BarberoPole'
import { Marca } from '../../components/comunes/Marca'
import { SelectorPais } from '../../components/comunes/SelectorPais'
import { RUTAS } from '../../config/rutas'
import { useAuth } from '../../hooks/useAuth'
import type { ApiError } from '../../types/error'
import { componerCelular, PAISES } from '../../utils/celularPais'

const esquemaIngreso = z.object({
  codigoPais: z.string(),
  numero: z.string().min(1, 'Escribe tu celular'),
  contrasena: z.string().min(1, 'Escribe tu contraseña'),
})

type Formulario = z.infer<typeof esquemaIngreso>

/** Etiqueta monoespaciada discreta, igual que en el registro. */
const CLASE_ETIQUETA =
  'mb-1.5 block font-mono text-[11px] font-medium uppercase tracking-[0.14em] text-marca-500'

/** Caja del campo: borde visible, radio moderno y foco con halo suave. */
const CLASE_CAJA =
  'flex h-12 items-center rounded-xl border bg-superficie shadow-[inset_0_1px_2px_rgba(0,0,0,0.03)] transition-[border-color,background-color,box-shadow] duration-200 hover:border-marca-400 focus-within:border-marca-900 focus-within:shadow-[0_0_0_4px_rgba(10,10,10,0.07)] sm:h-13'

const CLASE_CAJA_ERROR =
  'border-error focus-within:border-error focus-within:shadow-[0_0_0_4px_rgba(185,28,28,0.1)]'

const CLASE_ENTRADA =
  'h-full min-w-0 flex-1 border-0 bg-transparent px-4 text-base text-texto placeholder:text-marca-400 focus:outline-none'

/** ¿A dónde manda cada rol al entrar? */
function destinoDe(rol: string | null): string {
  return rol === 'CLIENTE' ? RUTAS.misCitas : RUTAS.panelAgenda
}

/** Inicio de sesión: celular internacional + contraseña, mobile-first. */
export function IngresarPage() {
  const { iniciarSesion, estaAutenticado, rol, cargando } = useAuth()
  const navegar = useNavigate()
  const ubicacion = useLocation()
  const [verContrasena, setVerContrasena] = useState(false)
  const [errorGeneral, setErrorGeneral] = useState<ApiError | null>(null)

  const {
    control,
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<Formulario>({
    resolver: zodResolver(esquemaIngreso),
    defaultValues: { codigoPais: '57', numero: '', contrasena: '' },
  })

  // Si ya entró, no tiene sentido ver este formulario otra vez.
  if (estaAutenticado && !cargando) {
    return <Navigate to={destinoDe(rol)} replace />
  }

  const alEnviar = handleSubmit(async (valores) => {
    setErrorGeneral(null)

    const celular = componerCelular(valores.codigoPais, valores.numero)
    if (!celular) {
      setError('numero', { message: 'Revisa el número de celular' })
      return
    }

    try {
      const sesion = await iniciarSesion({ celular, contrasena: valores.contrasena })
      // Si la persona venía de una ruta protegida, la devolvemos a donde iba.
      const origen = (ubicacion.state as { origen?: { pathname?: string } } | null)?.origen
        ?.pathname
      navegar(origen ?? destinoDe(sesion.rol), { replace: true })
    } catch (fallo) {
      const error = fallo as ApiError
      if (error.erroresCampo) {
        for (const [campo, mensaje] of Object.entries(error.erroresCampo)) {
          if (campo === 'celular') {
            setError('numero', { type: 'servidor', message: mensaje })
          }
        }
      }
      setErrorGeneral({
        status: error.status ?? 0,
        title: error.title ?? 'No pudimos iniciar sesión',
        detail: error.detail ?? 'Revisa tus datos e intenta de nuevo.',
      })
    }
  })

  const cajaNumero = `${CLASE_CAJA} ${errors.numero ? CLASE_CAJA_ERROR : 'border-marca-300'}`
  const cajaContrasena = `${CLASE_CAJA} ${errors.contrasena ? CLASE_CAJA_ERROR : 'border-marca-300'}`

  return (
    <section className="border-b border-borde bg-fondo px-4 py-4 sm:px-6 sm:py-8 lg:py-12 corto:py-3">
      <div className="mx-auto w-full max-w-md">
        <div className="animar-entrada rounded-3xl border border-marca-300 bg-superficie p-4 shadow-[0_1px_2px_rgba(0,0,0,0.04),0_24px_48px_-32px_rgba(0,0,0,0.25)] transition-[border-color,box-shadow] duration-300 hover:border-marca-400 hover:shadow-[0_2px_4px_rgba(0,0,0,0.05),0_32px_64px_-36px_rgba(0,0,0,0.32)] bajito:p-3.5 sm:p-8 corto:p-5">
          <div className="flex items-center justify-between gap-3">
            <div className="flex items-center gap-2.5">
              <BarberoPole className="h-8 w-1.5" />
              <Marca className="text-[24px]" />
            </div>
            <span className="font-mono text-[11px] font-medium uppercase tracking-[0.14em] text-marca-500">
              Ipiales, Colombia
            </span>
          </div>

          <h1 className="mt-6 font-display text-[2rem] font-semibold leading-[1.05] sm:mt-8 sm:text-[2.25rem] corto:mt-5">
            Qué bueno verte de nuevo
          </h1>
          <p className="mt-2 text-sm leading-relaxed text-marca-500 sm:mt-3 sm:text-base">
            Ingresa con tu celular y tu contraseña. Tu sesión sigue viva aunque
            cierres el navegador.
          </p>

          {errorGeneral && (
            <div
              role="alert"
              className="animar-entrada mt-4 flex gap-3 rounded-2xl border border-error/30 bg-error/5 px-4 py-3 text-sm text-error"
            >
              <TriangleAlert aria-hidden="true" className="mt-0.5 size-4 shrink-0" />
              <div>
                <p className="font-semibold">{errorGeneral.title}</p>
                <p className="mt-0.5">{errorGeneral.detail}</p>
              </div>
            </div>
          )}

          <form onSubmit={alEnviar} noValidate className="mt-5 space-y-4 bajito:space-y-3 sm:mt-7 sm:space-y-5 corto:mt-4 corto:space-y-4">
            <div>
              <div className="flex items-baseline justify-between gap-3">
                <label htmlFor="numero" className={CLASE_ETIQUETA}>
                  Celular
                </label>
                <span
                  id="ayuda-numero"
                  className="truncate font-mono text-[10px] uppercase tracking-[0.1em] text-marca-400 sm:text-[11px]"
                >
                  Sin el +57
                </span>
              </div>
              <div className={cajaNumero}>
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

                <input
                  {...register('numero')}
                  id="numero"
                  type="tel"
                  inputMode="numeric"
                  autoComplete="tel-national"
                  enterKeyHint="next"
                  placeholder="300 123 4567"
                  aria-invalid={Boolean(errors.numero)}
                  aria-describedby={
                    errors.numero ? 'error-numero' : 'ayuda-numero'
                  }
                  className="h-full min-w-0 flex-1 border-0 bg-transparent py-0 pl-3 pr-2 font-mono text-[14px] tabular-nums text-texto placeholder:text-marca-400 focus:outline-none min-[360px]:text-[15px]"
                />
              </div>
              {errors.numero ? (
                <p id="error-numero" role="alert" className="mt-2 text-sm text-error">
                  {errors.numero.message}
                </p>
              ) : null}
            </div>

            <div>
              <label htmlFor="contrasena" className={CLASE_ETIQUETA}>
                Contraseña
              </label>
              <div className={`relative ${cajaContrasena}`}>
                <input
                  {...register('contrasena')}
                  id="contrasena"
                  type={verContrasena ? 'text' : 'password'}
                  autoComplete="current-password"
                  enterKeyHint="go"
                  placeholder="Tu contraseña"
                  aria-invalid={Boolean(errors.contrasena)}
                  aria-describedby={
                    errors.contrasena ? 'error-contrasena' : undefined
                  }
                  className={`${CLASE_ENTRADA} pr-12`}
                />
                <button
                  type="button"
                  onClick={() => setVerContrasena((visible) => !visible)}
                  aria-label={verContrasena ? 'Ocultar contraseña' : 'Mostrar contraseña'}
                  className="absolute right-1 top-1/2 flex size-10 -translate-y-1/2 items-center justify-center rounded-full text-marca-500 transition-colors duration-150 hover:bg-marca-100 hover:text-texto active:bg-marca-200"
                >
                  {verContrasena ? (
                    <EyeOff aria-hidden="true" className="size-4" />
                  ) : (
                    <Eye aria-hidden="true" className="size-4" />
                  )}
                </button>
              </div>
              {errors.contrasena && (
                <p id="error-contrasena" role="alert" className="mt-2 text-sm text-error">
                  {errors.contrasena.message}
                </p>
              )}
            </div>

            <AdornoTijeras className="bajito:hidden" />

            <button
              type="submit"
              disabled={isSubmitting}
              className="group flex h-12 w-full items-center justify-between gap-3 rounded-full border border-marca-900 bg-marca-900 py-1.5 pl-5 pr-1.5 font-mono text-xs font-medium uppercase tracking-[0.16em] text-white transition-[background-color,transform] duration-150 hover:bg-marca-800 active:translate-y-px disabled:cursor-not-allowed disabled:opacity-60 sm:h-13"
            >
              <span>{isSubmitting ? 'Ingresando…' : 'Ingresar'}</span>
              <span className="flex size-8 shrink-0 items-center justify-center rounded-full bg-white text-marca-900 transition-transform duration-200 group-hover:translate-x-0.5">
                {isSubmitting ? (
                  <Loader2 aria-hidden="true" className="size-4 animate-spin" />
                ) : (
                  <ArrowRight aria-hidden="true" className="size-4" />
                )}
              </span>
            </button>
          </form>

          <p className="mt-4 text-center text-sm text-marca-500 corto:mt-3">
            ¿No tienes cuenta?{' '}
            <Link className="font-medium text-texto underline decoration-marca-300 decoration-1 underline-offset-4 transition-colors duration-200 hover:decoration-marca-900 hover:decoration-2" to={RUTAS.registro}>
              Crear cuenta
            </Link>
          </p>
        </div>
      </div>
    </section>
  )
}
