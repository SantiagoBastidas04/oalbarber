import { Menu, X } from 'lucide-react'
import { useState } from 'react'
import { Link, NavLink, Outlet } from 'react-router'
import { Marca } from '../components/comunes/Marca'
import { RUTAS } from '../config/rutas'
import { useAuth } from '../hooks/useAuth'

interface Enlace {
  etiqueta: string
  ruta: string
}

/** Año del pie de página; se calcula una vez al cargar el módulo. */
const ANIO_ACTUAL = new Date().getFullYear()

const ENLACES_BASE: Enlace[] = [
  { etiqueta: 'Inicio', ruta: RUTAS.inicio },
  { etiqueta: 'Servicios', ruta: RUTAS.servicios },
  { etiqueta: 'Reservar', ruta: RUTAS.reservar },
]

function claseEnlace({ isActive }: { isActive: boolean }): string {
  const base =
    'relative inline-flex min-h-11 items-center px-3 text-[15px] transition-colors after:absolute after:inset-x-3 after:bottom-2 after:h-px after:origin-left after:scale-x-0 after:bg-marca-900 after:transition-transform after:duration-200 hover:after:scale-x-100'
  return isActive
    ? `${base} bg-marca-100 text-marca-900`
    : `${base} text-texto-suave hover:bg-marca-50 hover:text-texto`
}

/** Encabezado, navegación y pie de las páginas públicas. */
export function LayoutPublico() {
  const [menuAbierto, setMenuAbierto] = useState(false)
  const { estaAutenticado, rol } = useAuth()

  const enlaces: Enlace[] = [...ENLACES_BASE]
  if (!estaAutenticado) {
    enlaces.push({ etiqueta: 'Ingresar', ruta: RUTAS.ingresar })
  }
  if (rol === 'CLIENTE') {
    enlaces.push({ etiqueta: 'Mis citas', ruta: RUTAS.misCitas })
  }
  if (rol === 'ADMIN' || rol === 'BARBERO') {
    enlaces.push({ etiqueta: 'Panel', ruta: RUTAS.panelAgenda })
  }

  const cerrarMenu = () => setMenuAbierto(false)

  return (
    <div className="flex min-h-screen flex-col">
      <a
        href="#contenido"
        className="sr-only focus:not-sr-only focus:absolute focus:left-4 focus:top-4 focus:z-50 focus:rounded-md focus:bg-superficie focus:px-4 focus:py-2 focus:text-sm focus:font-medium focus:shadow"
      >
        Saltar al contenido
      </a>

      <header className="sticky top-0 z-40 border-b border-borde bg-superficie/95 backdrop-blur">
        <div className="mx-auto flex min-h-16 w-full max-w-6xl items-center justify-between gap-4 px-4">
          <Link
            to={RUTAS.inicio}
            onClick={cerrarMenu}
            className="group inline-flex min-h-11 items-center text-[26px]"
          >
            <Marca />
          </Link>

          <div className="flex items-center gap-2">
            <nav aria-label="Navegación principal" className="hidden md:block">
              <ul className="flex items-center gap-1">
                {enlaces.map((enlace) => (
                  <li key={enlace.ruta}>
                    <NavLink to={enlace.ruta} end={enlace.ruta === RUTAS.inicio} className={claseEnlace}>
                      {enlace.etiqueta}
                    </NavLink>
                  </li>
                ))}
              </ul>
            </nav>

            {!estaAutenticado && (
              <Link
                to={RUTAS.registro}
                className="hidden min-h-11 items-center bg-marca-900 px-5 font-mono text-xs font-medium uppercase tracking-[0.14em] text-white transition-colors duration-150 hover:bg-marca-800 active:translate-y-px md:inline-flex"
              >
                Crear cuenta
              </Link>
            )}

            <button
              type="button"
              onClick={() => setMenuAbierto((abierto) => !abierto)}
              aria-expanded={menuAbierto}
              aria-controls="menu-movil"
              aria-label={menuAbierto ? 'Cerrar menú' : 'Abrir menú'}
              className="inline-flex size-12 items-center justify-center border border-marca-300 text-texto transition-colors duration-150 hover:border-marca-900 hover:bg-marca-50 active:bg-marca-100 md:hidden"
            >
              {menuAbierto ? <X size={20} aria-hidden="true" /> : <Menu size={20} aria-hidden="true" />}
            </button>
          </div>
        </div>

        {menuAbierto && (
          <nav
            id="menu-movil"
            aria-label="Navegación principal móvil"
            className="border-t border-borde bg-superficie md:hidden"
          >
            <ul className="mx-auto flex w-full max-w-6xl flex-col gap-2 px-4 py-3">
              {enlaces.map((enlace) => (
                <li key={enlace.ruta}>
                  <NavLink
                    to={enlace.ruta}
                    end={enlace.ruta === RUTAS.inicio}
                    onClick={cerrarMenu}
                    className={claseEnlace}
                  >
                    {enlace.etiqueta}
                  </NavLink>
                </li>
              ))}
              {!estaAutenticado && (
                <li>
                  <Link
                    to={RUTAS.registro}
                    onClick={cerrarMenu}
                    className="mt-2 flex min-h-12 items-center justify-center bg-marca-900 px-4 font-mono text-xs font-medium uppercase tracking-[0.14em] text-white transition-colors duration-150 hover:bg-marca-800 active:translate-y-px"
                  >
                    Crear cuenta
                  </Link>
                </li>
              )}
            </ul>
          </nav>
        )}
      </header>

      <main id="contenido" tabIndex={-1} className="flex-1">
        <Outlet />
      </main>

      <footer className="border-t border-borde bg-superficie">
        <div className="mx-auto grid w-full max-w-6xl gap-6 px-4 py-10 sm:grid-cols-2 md:grid-cols-3">
          <div>
            <Link
              to={RUTAS.inicio}
              className="group inline-flex text-xl transition-colors hover:text-marca-900"
            >
              <Marca />
            </Link>
            <p className="mt-2 text-sm text-texto-suave">
              Barbería en Ipiales (Colombia). Reserva en línea y paga en el local.
            </p>
          </div>

          <div>
            <h2 className="font-mono text-[11px] font-medium uppercase tracking-[0.16em] text-texto-suave">
              Contacto
            </h2>
            <dl className="mt-3 space-y-2 text-sm">
              <div>
                <dt className="font-semibold">Dirección</dt>
                <dd className="text-texto-suave">[POR DEFINIR]</dd>
              </div>
              <div>
                <dt className="font-semibold">Celular</dt>
                <dd className="text-texto-suave">[POR DEFINIR]</dd>
              </div>
            </dl>
          </div>

          <div>
            <h2 className="font-mono text-[11px] font-medium uppercase tracking-[0.16em] text-texto-suave">
              Horario
            </h2>
            <dl className="mt-3 space-y-2 text-sm">
              <div>
                <dt className="font-semibold">Atención</dt>
                <dd className="text-texto-suave">[POR DEFINIR]</dd>
              </div>
            </dl>
          </div>
        </div>

        <div className="border-t border-borde">
          <p className="mx-auto w-full max-w-6xl px-4 py-4 text-xs text-texto-suave">
            © {ANIO_ACTUAL} OalBarber. Todos los derechos reservados.
          </p>
        </div>
      </footer>
    </div>
  )
}
