import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import type { UsuarioResponse } from '../../types/auth'
import { listarClientes } from '../../services/clientesService'
import { ClientesPage } from './ClientesPage'

vi.mock('../../services/clientesService', () => ({
  listarClientes: vi.fn(),
}))

function cliente(nombre: string, celular: string, activo = true): UsuarioResponse {
  return {
    id: '3f1b6c2e-9a4d-4c8b-8f7e-1a2b3c4d5e6f',
    nombre,
    celular,
    rol: 'CLIENTE',
    activo,
    fechaCreacion: '2026-11-07T10:15:30-05:00',
  }
}

function renderizarPagina() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  })
  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter>
        <ClientesPage />
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

describe('ClientesPage', () => {
  beforeEach(() => {
    vi.mocked(listarClientes).mockReset()
  })

  it('muestra la lista de clientes con su celular y estado', async () => {
    vi.mocked(listarClientes).mockResolvedValue([
      cliente('Ana María Restrepo', '+573001234567'),
      cliente('Edwin Ordoñez', '+573186433644', false),
    ])
    renderizarPagina()

    expect(await screen.findByText('Ana María Restrepo')).toBeInTheDocument()
    expect(screen.getByText('+573001234567')).toBeInTheDocument()
    expect(screen.getByText('+573186433644')).toBeInTheDocument()
    expect(screen.getByText('Activo')).toBeInTheDocument()
    expect(screen.getByText('Inactivo')).toBeInTheDocument()
    // El contador del encabezado refleja el total.
    expect(screen.getByText('2/2')).toBeInTheDocument()
  })

  it('filtra por nombre al escribir en el buscador', async () => {
    const usuario = userEvent.setup()
    vi.mocked(listarClientes).mockResolvedValue([
      cliente('Ana María Restrepo', '+573001234567'),
      cliente('Edwin Ordoñez', '+573186433644'),
    ])
    renderizarPagina()

    await screen.findByText('Ana María Restrepo')
    await usuario.type(screen.getByLabelText('Buscar cliente por nombre o celular'), 'ana')

    expect(screen.getByText('Ana María Restrepo')).toBeInTheDocument()
    expect(screen.queryByText('Edwin Ordoñez')).not.toBeInTheDocument()
    expect(screen.getByText('1/2')).toBeInTheDocument()
  })

  it('muestra el aviso de sin resultados cuando la búsqueda no encuentra nada', async () => {
    const usuario = userEvent.setup()
    vi.mocked(listarClientes).mockResolvedValue([cliente('Ana María Restrepo', '+573001234567')])
    renderizarPagina()

    await screen.findByText('Ana María Restrepo')
    await usuario.type(screen.getByLabelText('Buscar cliente por nombre o celular'), 'zzz')

    expect(screen.getByText(/sin resultados para/i)).toBeInTheDocument()
  })

  it('muestra el estado vacío cuando no hay clientes registrados', async () => {
    vi.mocked(listarClientes).mockResolvedValue([])
    renderizarPagina()

    expect(await screen.findByText('Aún no hay clientes')).toBeInTheDocument()
  })

  it('muestra el error con opción de reintentar', async () => {
    const usuario = userEvent.setup()
    vi.mocked(listarClientes).mockRejectedValueOnce(new Error('fallo de red'))
    vi.mocked(listarClientes).mockResolvedValueOnce([cliente('Ana María Restrepo', '+573001234567')])
    renderizarPagina()

    expect(await screen.findByRole('alert')).toHaveTextContent('No pudimos cargar los clientes')

    await usuario.click(screen.getByRole('button', { name: /reintentar/i }))
    expect(await screen.findByText('Ana María Restrepo')).toBeInTheDocument()
  })
})
