import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { listarServicios } from '../../services/serviciosService'
import type { ServicioResponse } from '../../types/servicio'
import { CatalogoPage } from './CatalogoPage'

vi.mock('../../services/serviciosService', () => ({
  listarServicios: vi.fn(),
}))

function servicio(overrides: Partial<ServicioResponse> = {}): ServicioResponse {
  return {
    id: '3f1b6c2e-9a4d-4c8b-8f7e-1a2b3c4d5e6f',
    nombre: 'Corte normal',
    descripcion: 'Corte clásico',
    duracionMin: 40,
    precio: 25000,
    imagen: 'corte-normal.jpg',
    activo: true,
    ...overrides,
  }
}

function renderizarPagina() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  })
  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter>
        <CatalogoPage />
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

describe('CatalogoPage', () => {
  beforeEach(() => {
    vi.mocked(listarServicios).mockReset()
  })

  it('muestra las tarjetas con nombre, duración, precio y foto', async () => {
    vi.mocked(listarServicios).mockResolvedValue([
      servicio(),
      servicio({
        id: '9a7c1e2d-4b3f-4a6e-8c1d-2f5a7b9c0d1e',
        nombre: 'Mechas',
        descripcion: null,
        duracionMin: 180,
        precio: 120000,
        imagen: null,
      }),
    ])
    renderizarPagina()

    expect(await screen.findByRole('heading', { level: 3, name: 'Corte normal' })).toBeInTheDocument()
    expect(screen.getByText('40 min')).toBeInTheDocument()
    expect(screen.getByText(/25\.000/)).toBeInTheDocument()
    expect(screen.getByRole('img', { name: 'Corte normal' })).toHaveAttribute(
      'src',
      '/servicios/corte-normal.jpg',
    )

    // Un servicio sin archivo usa el marcador en vez de romper la página.
    expect(screen.getByRole('img', { name: 'Imagen no disponible para Mechas' })).toBeInTheDocument()
    expect(screen.getByText('2 servicios')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Reservar Corte normal' })).toHaveAttribute(
      'href',
      '/reservar?servicio=3f1b6c2e-9a4d-4c8b-8f7e-1a2b3c4d5e6f',
    )
  })

  it('muestra el estado de carga mientras llega el catálogo', () => {
    vi.mocked(listarServicios).mockReturnValue(new Promise<ServicioResponse[]>(() => {}))
    renderizarPagina()

    expect(screen.getByRole('status')).toHaveTextContent('Cargando servicios…')
  })

  it('muestra el estado vacío cuando no hay servicios', async () => {
    vi.mocked(listarServicios).mockResolvedValue([])
    renderizarPagina()

    expect(await screen.findByText('Todavía no hay servicios')).toBeInTheDocument()
  })

  it('muestra el error con opción de reintentar', async () => {
    const usuario = userEvent.setup()
    vi.mocked(listarServicios)
      .mockRejectedValueOnce(new Error('fallo de red'))
      .mockResolvedValueOnce([servicio()])
    renderizarPagina()

    expect(await screen.findByRole('alert')).toHaveTextContent('No pudimos cargar el catálogo')

    await usuario.click(screen.getByRole('button', { name: /reintentar/i }))

    expect(await screen.findByRole('heading', { level: 3, name: 'Corte normal' })).toBeInTheDocument()
  })
})
