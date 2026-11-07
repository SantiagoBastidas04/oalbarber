import {
  CalendarDays,
  CalendarPlus,
  Clock,
  Scissors,
  UserCog,
  Users,
  type LucideIcon,
} from 'lucide-react'
import { Link, NavLink, Outlet } from 'react-router'
import { ETIQUETAS_ROL } from '../config/roles'
import { RUTAS } from '../config/rutas'
import { useAuth } from '../hooks/useAuth'
import type { Rol } from '../types/auth'

interface ItemPanel {
  etiqueta: string
  ruta: string
  icono: LucideIcon
  roles: readonly Rol[]
}

const ITEMS_PANEL: ItemPanel[] = [
  { etiqueta: 'Agenda', ruta: RUTAS.panelAgenda, icono: CalendarDays, roles: ['ADMIN', 'BARBERO'] },
  {
    etiqueta: 'Nueva cita',
    ruta: RUTAS.panelNuevaCita,
    icono: CalendarPlus,
    roles: ['ADMIN'],
  },
  { etiqueta: 'Servicios', ruta: RUTAS.panelServicios, icono: Scissors, roles: ['ADMIN'] },
  { etiqueta: 'Horarios', ruta: RUTAS.panelHorarios, icono: Clock, roles: ['ADMIN'] },
  { etiqueta: 'Barberos', ruta: RUTAS.panelBarberos, icono: Users, roles: ['ADMIN'] },
  { etiqueta: 'Clientes', ruta: RUTAS.panelClientes, icono: UserCog, roles: ['ADMIN'] },
]

function claseItem({ isActive }: { isActive: boolean }): string {
  const base =
    'inline-flex min-h-11 items-center gap-2 rounded-md px-3 text-sm font-medium transition-colors'
  return isActive
    ? `${base} bg-marca-100 text-marca-900`
    : `${base} text-texto-suave hover:bg-marca-50 hover:text-texto`
}

/** Estructura del panel: navegación lateral en escritorio y desplegable en móvil. */
export function LayoutPanel() {
  const { rol } = useAuth()
  const items = rol ? ITEMS_PANEL.filter((item) => item.roles.includes(rol)) : []

  return (
    <div className="min-h-screen md:flex">
      <a
        href="#contenido-panel"
        className="sr-only focus:not-sr-only focus:absolute focus:left-4 focus:top-4 focus:z-50 focus:rounded-md focus:bg-superficie focus:px-4 focus:py-2 focus:text-sm focus:font-medium focus:shadow"
      >
        Saltar al contenido
      </a>

      <aside className="hidden w-64 shrink-0 border-r border-borde bg-superficie md:block">
        <div className="flex min-h-16 items-center gap-2 border-b border-borde px-5">
          <span aria-hidden="true" className="inline-block size-2.5 rounded-full bg-marca-600" />
          <span className="text-lg font-bold tracking-tight">OalBarber</span>
          <span className="text-xs font-medium text-texto-suave">Panel</span>
        </div>
        <nav aria-label="Navegación del panel" className="p-3">
          <ul className="space-y-1">
            {items.map((item) => (
              <li key={item.ruta}>
                <NavLink to={item.ruta} className={claseItem}>
                  <item.icono size={18} aria-hidden="true" />
                  {item.etiqueta}
                </NavLink>
              </li>
            ))}
          </ul>
        </nav>
      </aside>

      <div className="flex min-w-0 flex-1 flex-col">
        <header className="sticky top-0 z-30 border-b border-borde bg-superficie/95 backdrop-blur">
          <div className="flex min-h-16 items-center justify-between gap-4 px-4 sm:px-6">
            <p className="text-sm text-texto-suave md:hidden">
              <span className="font-bold text-texto">OalBarber</span> · Panel
            </p>
            <p className="hidden text-sm text-texto-suave md:block">
              Sesión: <span className="font-semibold text-texto">{rol ? ETIQUETAS_ROL[rol] : '—'}</span>
            </p>
            <Link
              to={RUTAS.inicio}
              className="inline-flex min-h-11 items-center rounded-md px-3 text-sm font-medium text-marca-700 hover:bg-marca-50"
            >
              Ir al sitio
            </Link>
          </div>

          <nav aria-label="Navegación del panel móvil" className="border-t border-borde md:hidden">
            <ul className="flex gap-2 overflow-x-auto px-4 py-2">
              {items.map((item) => (
                <li key={item.ruta} className="shrink-0">
                  <NavLink to={item.ruta} className={claseItem}>
                    <item.icono size={18} aria-hidden="true" />
                    {item.etiqueta}
                  </NavLink>
                </li>
              ))}
            </ul>
          </nav>
        </header>

        <main id="contenido-panel" tabIndex={-1} className="flex-1">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
