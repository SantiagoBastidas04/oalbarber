import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { listarServicios } from '../../services/serviciosService'
import type { ServicioResponse } from '../../types/servicio'
import { InicioPage } from './InicioPage'

vi.mock('../../services/serviciosService', () => ({
  listarServicios: vi.fn(),
}))

function servicio(nombre: string, id: string): ServicioResponse {
  return {
    id,
    nombre,
    descripcion: `Descripción de ${nombre}`,
    duracionMin: 40,
    precio: 25000,
    imagen: null,
    activo: true,
  }
}

function renderizarPagina() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  })
  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter>
        <InicioPage />
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

describe('InicioPage', () => {
  beforeEach(() => {
    vi.mocked(listarServicios).mockReset()
  })

  it('presenta el negocio y los accesos al catálogo y a la reserva', () => {
    vi.mocked(listarServicios).mockResolvedValue([])
    renderizarPagina()

    expect(
      screen.getByRole('heading', { level: 1, name: 'Bienvenido a OalBarber' }),
    ).toBeInTheDocument()
    expect(screen.getByRole('link', { name: /ver servicios/i })).toHaveAttribute('href', '/servicios')
    expect(screen.getByRole('link', { name: /reservar cita/i })).toHaveAttribute('href', '/reservar')
  })

  it('destaca como máximo tres servicios del catálogo', async () => {
    vi.mocked(listarServicios).mockResolvedValue([
      servicio('Corte normal', '1'),
      servicio('Corte premium', '2'),
      servicio('Mechas', '3'),
      servicio('Ondulación', '4'),
    ])
    renderizarPagina()

    expect(await screen.findByRole('heading', { level: 3, name: 'Corte normal' })).toBeInTheDocument()
    expect(await screen.findAllByRole('heading', { level: 3 })).toHaveLength(3)
    expect(screen.queryByText('Ondulación')).not.toBeInTheDocument()
    expect(screen.getByRole('link', { name: /ver todo el catálogo/i })).toHaveAttribute(
      'href',
      '/servicios',
    )
  })

  it('muestra el aviso de vacío cuando todavía no hay servicios', async () => {
    vi.mocked(listarServicios).mockResolvedValue([])
    renderizarPagina()

    expect(await screen.findByText('Todavía no hay servicios publicados')).toBeInTheDocument()
  })

  it('mantiene la portada y ofrece reintentar si falla el catálogo', async () => {
    const usuario = userEvent.setup()
    vi.mocked(listarServicios)
      .mockRejectedValueOnce(new Error('fallo de red'))
      .mockResolvedValueOnce([servicio('Corte normal', '1')])
    renderizarPagina()

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'No pudimos cargar los servicios destacados',
    )
    expect(
      screen.getByRole('heading', { level: 1, name: 'Bienvenido a OalBarber' }),
    ).toBeInTheDocument()

    await usuario.click(screen.getByRole('button', { name: /reintentar/i }))

    expect(await screen.findByRole('heading', { level: 3, name: 'Corte normal' })).toBeInTheDocument()
  })
})
