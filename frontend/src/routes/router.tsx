import { createBrowserRouter, Navigate, type RouteObject } from 'react-router'
import { LayoutPanel } from '../layouts/LayoutPanel'
import { LayoutPublico } from '../layouts/LayoutPublico'
import { CatalogoPage } from '../pages/catalogo/CatalogoPage'
import { IngresarPage } from '../pages/cuenta/IngresarPage'
import { RegistroPage } from '../pages/cuenta/RegistroPage'
import { NoAutorizadoPage } from '../pages/errores/NoAutorizadoPage'
import { NoEncontradoPage } from '../pages/errores/NoEncontradoPage'
import { InicioPage } from '../pages/inicio/InicioPage'
import { MisCitasPage } from '../pages/mis-citas/MisCitasPage'
import { AgendaPage } from '../pages/panel/AgendaPage'
import { BarberosPage } from '../pages/panel/BarberosPage'
import { ClientesPage } from '../pages/panel/ClientesPage'
import { HorariosPage } from '../pages/panel/HorariosPage'
import { NuevaCitaPage } from '../pages/panel/NuevaCitaPage'
import { ServiciosAdminPage } from '../pages/panel/ServiciosAdminPage'
import { ReservaPage } from '../pages/reserva/ReservaPage'
import { RutaProtegida } from './RutaProtegida'

/** Rutas de la fase 1. El rol exigido está en cada `RutaProtegida`. */
export const rutas: RouteObject[] = [
  {
    element: <LayoutPublico />,
    children: [
      { index: true, element: <InicioPage /> },
      { path: 'servicios', element: <CatalogoPage /> },
      { path: 'reservar', element: <ReservaPage /> },
      { path: 'registro', element: <RegistroPage /> },
      { path: 'ingresar', element: <IngresarPage /> },
      {
        path: 'mis-citas',
        element: (
          <RutaProtegida roles={['CLIENTE']}>
            <MisCitasPage />
          </RutaProtegida>
        ),
      },
      { path: 'no-autorizado', element: <NoAutorizadoPage /> },
      { path: '*', element: <NoEncontradoPage /> },
    ],
  },
  {
    path: 'panel',
    element: (
      <RutaProtegida roles={['ADMIN', 'BARBERO']}>
        <LayoutPanel />
      </RutaProtegida>
    ),
    children: [
      { index: true, element: <Navigate to="agenda" replace /> },
      {
        path: 'agenda',
        element: (
          <RutaProtegida roles={['ADMIN', 'BARBERO']}>
            <AgendaPage />
          </RutaProtegida>
        ),
      },
      {
        path: 'citas/nueva',
        element: (
          <RutaProtegida roles={['ADMIN']}>
            <NuevaCitaPage />
          </RutaProtegida>
        ),
      },
      {
        path: 'servicios',
        element: (
          <RutaProtegida roles={['ADMIN']}>
            <ServiciosAdminPage />
          </RutaProtegida>
        ),
      },
      {
        path: 'horarios',
        element: (
          <RutaProtegida roles={['ADMIN']}>
            <HorariosPage />
          </RutaProtegida>
        ),
      },
      {
        path: 'barberos',
        element: (
          <RutaProtegida roles={['ADMIN']}>
            <BarberosPage />
          </RutaProtegida>
        ),
      },
      {
        path: 'clientes',
        element: (
          <RutaProtegida roles={['ADMIN']}>
            <ClientesPage />
          </RutaProtegida>
        ),
      },
    ],
  },
]

export const router = createBrowserRouter(rutas)
