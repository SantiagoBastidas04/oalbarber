import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import type { ApiError } from '../../types/error'
import type { UsuarioResponse } from '../../types/auth'
import { RegistroPage } from './RegistroPage'

const registrar = vi.hoisted(() => vi.fn())

vi.mock('../../hooks/useAuth', () => ({
  useAuth: () => ({
    registrar,
    estaAutenticado: false,
    cargando: false,
    usuario: null,
    rol: null,
    iniciarSesion: vi.fn(),
    renovarSesion: vi.fn(),
    cerrarSesion: vi.fn(),
    restaurarSesion: vi.fn(),
  }),
}))

/** El contexto `registrar` devuelve el usuario creado. */
function usuarioDe(nombre: string, celular: string): UsuarioResponse {
  return {
    id: '3f1b6c2e-9a4d-4c8b-8f7e-1a2b3c4d5e6f',
    nombre,
    celular,
    rol: 'CLIENTE',
    activo: true,
    fechaCreacion: '2026-11-07T10:15:30-05:00',
  }
}

function renderizarPagina() {
  return render(
    <MemoryRouter>
      <RegistroPage />
    </MemoryRouter>,
  )
}

async function completarFormulario(usuario: ReturnType<typeof userEvent.setup>) {
  await usuario.type(screen.getByLabelText('Nombre completo'), 'Ana María Restrepo')
  await usuario.type(screen.getByLabelText('Celular'), '3001234567')
  await usuario.type(screen.getByLabelText('Contraseña'), 'ClaveSegura123')
  await usuario.click(
    screen.getByRole('checkbox', { name: /tratamiento de datos personales/i }),
  )
}

describe('RegistroPage', () => {
  beforeEach(() => {
    registrar.mockReset()
  })

  it('muestra los errores de validación sin llamar a la API', async () => {
    const usuario = userEvent.setup()
    renderizarPagina()

    await usuario.click(screen.getByRole('button', { name: /crear cuenta/i }))

    expect(await screen.findByText('Escribe tu nombre completo')).toBeInTheDocument()
    expect(screen.getByText('Debes aceptar el tratamiento de datos')).toBeInTheDocument()
    expect(registrar).not.toHaveBeenCalled()
  })

  it('envía el celular con el prefijo del país seleccionado y abre la sesión', async () => {
    const usuario = userEvent.setup()
    registrar.mockResolvedValue(usuarioDe('Ana María Restrepo', '+573001234567'))
    renderizarPagina()

    await completarFormulario(usuario)
    await usuario.click(screen.getByRole('button', { name: /crear cuenta/i }))

    await waitFor(() =>
      expect(registrar).toHaveBeenCalledWith({
        nombre: 'Ana María Restrepo',
        celular: '+573001234567',
        contrasena: 'ClaveSegura123',
      }),
    )
    expect(await screen.findByRole('heading', { name: 'Cuenta creada' })).toBeInTheDocument()
    expect(screen.getByText('+573001234567')).toBeInTheDocument()
    // La sesión ya inició: el botón principal lleva a las citas.
    expect(
      screen.getByRole('link', { name: /ver mis citas/i }),
    ).toHaveAttribute('href', '/mis-citas')
  })

  it('usa el prefijo del país que elige el usuario', async () => {
    const usuario = userEvent.setup()
    registrar.mockResolvedValue(usuarioDe('Ana María Restrepo', '+593991234567'))
    renderizarPagina()

    await usuario.click(screen.getByRole('button', { name: /país del celular/i }))
    await usuario.click(screen.getByRole('option', { name: /Ecuador/i }))
    await usuario.type(screen.getByLabelText('Nombre completo'), 'Ana María Restrepo')
    await usuario.type(screen.getByLabelText('Celular'), '991234567')
    await usuario.type(screen.getByLabelText('Contraseña'), 'ClaveSegura123')
    await usuario.click(screen.getByRole('checkbox', { name: /tratamiento de datos personales/i }))
    await usuario.click(screen.getByRole('button', { name: /crear cuenta/i }))

    await waitFor(() =>
      expect(registrar).toHaveBeenCalledWith({
        nombre: 'Ana María Restrepo',
        celular: '+593991234567',
        contrasena: 'ClaveSegura123',
      }),
    )
  })

  it('muestra el mensaje del backend cuando el celular ya existe', async () => {
    const usuario = userEvent.setup()
    const error: ApiError = {
      status: 409,
      title: 'Recurso duplicado',
      detail: 'El celular ya está registrado.',
    }
    registrar.mockRejectedValue(error)
    renderizarPagina()

    await completarFormulario(usuario)
    await usuario.click(screen.getByRole('button', { name: /crear cuenta/i }))

    expect(await screen.findByRole('alert')).toHaveTextContent('El celular ya está registrado.')
  })
})
