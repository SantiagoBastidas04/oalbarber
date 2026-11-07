import { Menu, X } from 'lucide-react'
import { useState } from 'react'
import { Link, NavLink, Outlet } from 'react-router'
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
    'inline-flex min-h-11 items-center rounded-md px-3 text-sm font-medium transition-colors'
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
            className="inline-flex min-h-11 items-center gap-2 text-lg font-bold tracking-tight"
          >
            <span aria-hidden="true" className="inline-block size-2.5 rounded-full bg-marca-600" />
            OalBarber
          </Link>

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

          <button
            type="button"
            onClick={() => setMenuAbierto((abierto) => !abierto)}
            aria-expanded={menuAbierto}
            aria-controls="menu-movil"
            aria-label={menuAbierto ? 'Cerrar menú' : 'Abrir menú'}
            className="inline-flex size-11 items-center justify-center rounded-md border border-borde text-texto md:hidden"
          >
            {menuAbierto ? <X size={20} aria-hidden="true" /> : <Menu size={20} aria-hidden="true" />}
          </button>
        </div>

        {menuAbierto && (
          <nav
            id="menu-movil"
            aria-label="Navegación principal móvil"
            className="border-t border-borde bg-superficie md:hidden"
          >
            <ul className="mx-auto flex w-full max-w-6xl flex-col gap-1 px-4 py-3">
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
            </ul>
          </nav>
        )}
      </header>

      <main id="contenido" tabIndex={-1} className="flex-1">
        <Outlet />
      </main>

      <footer className="border-t border-borde bg-superficie">
        <div className="mx-auto grid w-full max-w-6xl gap-6 px-4 py-10 sm:grid-cols-2 lg:grid-cols-3">
          <div>
            <p className="text-base font-semibold">OalBarber</p>
            <p className="mt-2 text-sm text-texto-suave">
              Barbería en Ipiales (Colombia). Reserva en línea y paga en el local.
            </p>
          </div>

          <div>
            <h2 className="text-sm font-semibold uppercase tracking-wide text-texto-suave">
              Contacto
            </h2>
            <dl className="mt-3 space-y-2 text-sm">
              <div>
                <dt className="font-medium">Dirección</dt>
                <dd className="text-texto-suave">[POR DEFINIR]</dd>
              </div>
              <div>
                <dt className="font-medium">Celular</dt>
                <dd className="text-texto-suave">[POR DEFINIR]</dd>
              </div>
            </dl>
          </div>

          <div>
            <h2 className="text-sm font-semibold uppercase tracking-wide text-texto-suave">
              Horario
            </h2>
            <dl className="mt-3 space-y-2 text-sm">
              <div>
                <dt className="font-medium">Atención</dt>
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
