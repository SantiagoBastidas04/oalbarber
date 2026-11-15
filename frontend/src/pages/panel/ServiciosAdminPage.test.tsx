import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import {
  actualizarServicio,
  cambiarEstadoServicio,
  crearServicio,
  listarServicios,
} from '../../services/serviciosService'
import type { ServicioResponse } from '../../types/servicio'
import { ServiciosAdminPage } from './ServiciosAdminPage'

vi.mock('../../services/serviciosService', () => ({
  listarServicios: vi.fn(),
  crearServicio: vi.fn(),
  actualizarServicio: vi.fn(),
  cambiarEstadoServicio: vi.fn(),
}))

const CORTE = '3f1b6c2e-9a4d-4c8b-8f7e-1a2b3c4d5e6f'
const MECHAS = '9a7c1e2d-4b3f-4a6e-8c1d-2f5a7b9c0d1e'

function servicio(overrides: Partial<ServicioResponse> = {}): ServicioResponse {
  return {
    id: CORTE,
    nombre: 'Corte normal',
    descripcion: 'Corte clásico',
    duracionMin: 90,
    precio: 25000,
    imagen: 'corte-normal.jpg',
    activo: true,
    ...overrides,
  }
}

function renderizarPagina() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  })
  return render(
    <QueryClientProvider client={queryClient}>
      <ServiciosAdminPage />
    </QueryClientProvider>,
  )
}

async function abrirFormularioNuevo() {
  const usuario = userEvent.setup()
  vi.mocked(listarServicios).mockResolvedValue([])
  renderizarPagina()
  await usuario.click(screen.getByRole('button', { name: /nuevo servicio/i }))
  return usuario
}

describe('ServiciosAdminPage', () => {
  beforeEach(() => {
    vi.mocked(listarServicios).mockReset()
    vi.mocked(crearServicio).mockReset()
    vi.mocked(actualizarServicio).mockReset()
    vi.mocked(cambiarEstadoServicio).mockReset()
  })

  it('muestra la tabla con los servicios activos y los inactivos', async () => {
    vi.mocked(listarServicios).mockResolvedValue([
      servicio(),
      servicio({
        id: MECHAS,
        nombre: 'Mechas',
        descripcion: null,
        duracionMin: 180,
        precio: 120000,
        imagen: null,
        activo: false,
      }),
    ])
    renderizarPagina()

    expect(await screen.findByText('Corte normal')).toBeInTheDocument()
    expect(screen.getByText('Mechas')).toBeInTheDocument()
    expect(screen.getByText('90 min')).toBeInTheDocument()
    expect(screen.getByText('180 min')).toBeInTheDocument()
    expect(screen.getByText(/120\.000/)).toBeInTheDocument()
    expect(screen.getByText('Activo')).toBeInTheDocument()
    expect(screen.getByText('Inactivo')).toBeInTheDocument()
    expect(screen.getByText('2 servicios')).toBeInTheDocument()
    // El panel pide el catálogo completo, no solo los activos.
    expect(listarServicios).toHaveBeenCalledWith(true)
  })

  it('crea un servicio con los datos del formulario', async () => {
    vi.mocked(crearServicio).mockResolvedValue(servicio({ nombre: 'Corte premium' }))
    const usuario = await abrirFormularioNuevo()

    await usuario.type(screen.getByLabelText('Nombre del servicio'), 'Corte premium')
    await usuario.type(screen.getByLabelText('Descripción'), 'Corte con lavado')
    await usuario.selectOptions(screen.getByLabelText('Duración'), '90')
    await usuario.type(screen.getByLabelText('Precio'), '45000')
    await usuario.type(screen.getByLabelText('Archivo de la foto'), 'corte-premium.jpg')
    await usuario.click(screen.getByRole('button', { name: /crear servicio/i }))

    await waitFor(() =>
      expect(crearServicio).toHaveBeenCalledWith({
        nombre: 'Corte premium',
        descripcion: 'Corte con lavado',
        duracionMin: 90,
        precio: 45000,
        imagen: 'corte-premium.jpg',
      }),
    )
    // Al guardar, el diálogo se cierra y vuelve la tabla.
    await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument())
  })

  it('edita un servicio existente y conserva la duración elegida', async () => {
    const usuario = userEvent.setup()
    vi.mocked(listarServicios).mockResolvedValue([servicio()])
    vi.mocked(actualizarServicio).mockResolvedValue(servicio({ precio: 30000 }))
    renderizarPagina()

    await usuario.click(await screen.findByRole('button', { name: 'Editar Corte normal' }))
    expect(screen.getByRole('heading', { name: 'Editar servicio' })).toBeInTheDocument()

    await usuario.clear(screen.getByLabelText('Precio'))
    await usuario.type(screen.getByLabelText('Precio'), '30000')
    await usuario.click(screen.getByRole('button', { name: /guardar cambios/i }))

    await waitFor(() =>
      expect(actualizarServicio).toHaveBeenCalledWith(
        CORTE,
        expect.objectContaining({ nombre: 'Corte normal', precio: 30000, duracionMin: 90 }),
      ),
    )
  })

  it('muestra el error de NOMBRE_DUPLICADO sobre el campo nombre', async () => {
    vi.mocked(crearServicio).mockRejectedValue({
      status: 409,
      title: 'Recurso duplicado',
      detail: 'Ya existe un servicio con el nombre :Corte premium',
      codigo: 'NOMBRE_DUPLICADO',
    })
    const usuario = await abrirFormularioNuevo()

    await usuario.type(screen.getByLabelText('Nombre del servicio'), 'Corte premium')
    await usuario.type(screen.getByLabelText('Precio'), '45000')
    await usuario.click(screen.getByRole('button', { name: /crear servicio/i }))

    expect(
      await screen.findByText('Ya existe un servicio con ese nombre'),
    ).toBeInTheDocument()
    // El diálogo sigue abierto para corregir el nombre.
    expect(screen.getByRole('dialog')).toBeInTheDocument()
  })

  it('previsualiza la foto con el nombre de archivo del formulario', async () => {
    const usuario = await abrirFormularioNuevo()

    await usuario.type(screen.getByLabelText('Nombre del servicio'), 'Corte premium')
    await usuario.type(screen.getByLabelText('Archivo de la foto'), 'corte-premium.jpg')

    expect(screen.getByRole('img', { name: 'Corte premium' })).toHaveAttribute(
      'src',
      '/servicios/corte-premium.jpg',
    )
  })

  it('solo ofrece las duraciones que acepta el negocio', async () => {
    await abrirFormularioNuevo()

    const opciones = within(screen.getByLabelText('Duración')).getAllByRole('option')

    expect(opciones.map((opcion) => opcion.textContent)).toEqual([
      '40 minutos',
      '90 minutos',
      '180 minutos',
    ])
  })

  it('no envía el formulario si faltan el nombre y el precio', async () => {
    const usuario = await abrirFormularioNuevo()

    await usuario.click(screen.getByRole('button', { name: /crear servicio/i }))

    expect(await screen.findByText('Escribe el nombre del servicio')).toBeInTheDocument()
    expect(screen.getByText('Escribe el precio')).toBeInTheDocument()
    expect(crearServicio).not.toHaveBeenCalled()
  })

  it('desactiva un servicio sin borrarlo', async () => {
    const usuario = userEvent.setup()
    vi.mocked(listarServicios).mockResolvedValue([servicio()])
    vi.mocked(cambiarEstadoServicio).mockResolvedValue(servicio({ activo: false }))
    renderizarPagina()

    await usuario.click(
      await screen.findByRole('button', { name: 'Desactivar Corte normal' }),
    )

    await waitFor(() =>
      expect(cambiarEstadoServicio).toHaveBeenCalledWith(CORTE, false),
    )
  })

  it('ofrece reintentar cuando el catálogo no carga', async () => {
    const usuario = userEvent.setup()
    vi.mocked(listarServicios)
      .mockRejectedValueOnce(new Error('fallo de red'))
      .mockResolvedValueOnce([servicio()])
    renderizarPagina()

    expect(await screen.findByRole('alert')).toHaveTextContent('No pudimos cargar los servicios')

    await usuario.click(screen.getByRole('button', { name: /reintentar/i }))

    expect(await screen.findByText('Corte normal')).toBeInTheDocument()
  })
})
