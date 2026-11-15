import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import {
  cambiarEstadoBarbero,
  crearBarbero,
  listarBarberosAdmin,
} from '../../services/barberosService'
import type { BarberoAdminResponse } from '../../types/barbero'
import { BarberosPage } from './BarberosPage'

vi.mock('../../services/barberosService', () => ({
  cambiarEstadoBarbero: vi.fn(),
  crearBarbero: vi.fn(),
  listarBarberosActivos: vi.fn(),
  listarBarberosAdmin: vi.fn(),
}))

const barberoActivo: BarberoAdminResponse = {
  id: 'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa',
  usuarioId: '11111111-1111-4111-8111-111111111111',
  nombre: 'Santiago Prueba',
  celular: '+573000000001',
  nombrePublico: 'Santiago',
  descripcion: null,
  foto: null,
  activo: true,
}

const barberoInactivo: BarberoAdminResponse = {
  ...barberoActivo,
  id: 'bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb',
  usuarioId: '22222222-2222-4222-8222-222222222222',
  nombre: 'Andrés Prueba',
  celular: '+573000000002',
  nombrePublico: null,
  activo: false,
}

function renderizarPagina() {
  const cliente = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
      mutations: { retry: false },
    },
  })
  return render(
    <QueryClientProvider client={cliente}>
      <BarberosPage />
    </QueryClientProvider>,
  )
}

async function completarAlta(usuario: ReturnType<typeof userEvent.setup>) {
  await usuario.type(screen.getByLabelText('Nombre de la cuenta'), 'Carlos Prueba')
  await usuario.type(screen.getByRole('textbox', { name: 'Número de celular' }), '3001234567')
  await usuario.type(screen.getByLabelText('Contraseña inicial'), 'ClavePrueba123')
}

describe('BarberosPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(listarBarberosAdmin).mockResolvedValue([])
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('lista perfiles activos e inactivos con su estado', async () => {
    vi.mocked(listarBarberosAdmin).mockResolvedValue([barberoActivo, barberoInactivo])
    renderizarPagina()

    expect(await screen.findByText('Santiago')).toBeInTheDocument()
    expect(screen.getByText('Andrés Prueba')).toBeInTheDocument()
    expect(screen.getByText('Activo')).toBeInTheDocument()
    expect(screen.getByText('Inactivo')).toBeInTheDocument()
    expect(screen.getByText('Andrés Prueba').closest('li')).toHaveTextContent('+573000000002')
  })

  it('crea una cuenta y perfil con el celular compuesto', async () => {
    const usuario = userEvent.setup()
    vi.mocked(crearBarbero).mockResolvedValue(barberoActivo)
    renderizarPagina()

    await completarAlta(usuario)
    await usuario.type(screen.getByLabelText('Nombre público (opcional)'), 'Carlos')
    await usuario.type(screen.getByLabelText('Descripción (opcional)'), 'Descripción ficticia')
    await usuario.type(screen.getByLabelText('Foto estática (opcional)'), 'carlos-01.webp')
    await usuario.click(screen.getByRole('button', { name: 'Crear barbero' }))

    await waitFor(() => expect(crearBarbero).toHaveBeenCalledWith({
      nombre: 'Carlos Prueba',
      celular: '+573001234567',
      contrasena: 'ClavePrueba123',
      nombrePublico: 'Carlos',
      descripcion: 'Descripción ficticia',
      foto: 'carlos-01.webp',
    }))
    expect(await screen.findByRole('status')).toHaveTextContent('Perfil de barbero creado correctamente')
  })

  it('muestra el conflicto de celular duplicado en el formulario', async () => {
    const usuario = userEvent.setup()
    vi.mocked(crearBarbero).mockRejectedValue({
      status: 409,
      title: 'Recurso duplicado',
      detail: 'El celular ya está registrado.',
      erroresCampo: { celular: 'El celular ya está registrado.' },
    })
    renderizarPagina()

    await completarAlta(usuario)
    await usuario.click(screen.getByRole('button', { name: 'Crear barbero' }))

    expect(await screen.findByText('Recurso duplicado')).toBeInTheDocument()
    expect(screen.getAllByRole('alert')[0]).toHaveTextContent('El celular ya está registrado')
    expect(screen.getByRole('textbox', { name: 'Número de celular' })).toHaveAttribute('aria-invalid', 'true')
  })

  it('confirma la desactivación y explica que no se cancelan citas', async () => {
    const usuario = userEvent.setup()
    vi.mocked(listarBarberosAdmin)
      .mockResolvedValueOnce([barberoActivo])
      .mockResolvedValueOnce([barberoInactivo])
    vi.mocked(cambiarEstadoBarbero).mockResolvedValue(barberoInactivo)
    const confirmacion = vi.spyOn(window, 'confirm').mockReturnValueOnce(false).mockReturnValueOnce(true)
    renderizarPagina()

    const boton = await screen.findByRole('button', { name: 'Desactivar perfil de Santiago' })
    await usuario.click(boton)
    expect(cambiarEstadoBarbero).not.toHaveBeenCalled()

    await usuario.click(boton)
    expect(confirmacion).toHaveBeenCalledTimes(2)
    expect(confirmacion.mock.calls[1]?.[0]).toContain('No se cancelan ni se mueven citas')
    await waitFor(() => expect(cambiarEstadoBarbero).toHaveBeenCalledWith(barberoActivo.id, { activo: false }))
    expect(await screen.findByText('Inactivo')).toBeInTheDocument()
  })

  it('muestra un estado vacío si todavía no hay perfiles', async () => {
    renderizarPagina()

    expect(await screen.findByText('Aún no hay perfiles de barbero')).toBeInTheDocument()
  })

  it('muestra un error de carga con opción de reintentar', async () => {
    const usuario = userEvent.setup()
    vi.mocked(listarBarberosAdmin)
      .mockRejectedValueOnce(new Error('fallo de red'))
      .mockResolvedValueOnce([barberoActivo])
    renderizarPagina()

    expect(await screen.findByRole('alert')).toHaveTextContent('No pudimos cargar los barberos')
    await usuario.click(screen.getByRole('button', { name: 'Reintentar' }))
    expect(await screen.findByText('Santiago')).toBeInTheDocument()
  })
})
