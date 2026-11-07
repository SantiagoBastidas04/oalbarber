import { render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router'
import { describe, expect, it, vi } from 'vitest'
import { AuthContext, type ContextoAuth } from '../context/AuthContext'
import type { Rol, UsuarioResponse } from '../types/auth'
import { RutaProtegida } from './RutaProtegida'

function crearContexto(rol: Rol | null): ContextoAuth {
  const usuario: UsuarioResponse | null = rol
    ? { id: 1, nombre: 'Persona de prueba', telefono: '+573001234567', rol, activo: true }
    : null

  return {
    usuario,
    rol,
    estaAutenticado: usuario !== null,
    registrar: vi.fn(),
    iniciarSesion: vi.fn(),
    renovarSesion: vi.fn(),
    cerrarSesion: vi.fn(),
    restaurarSesion: vi.fn(),
  }
}

function renderizar(rol: Rol | null) {
  return render(
    <AuthContext.Provider value={crearContexto(rol)}>
      <MemoryRouter initialEntries={['/mis-citas']}>
        <Routes>
          <Route path="/ingresar" element={<p>Pantalla de ingreso</p>} />
          <Route path="/no-autorizado" element={<p>Pantalla de no autorizado</p>} />
          <Route
            path="/mis-citas"
            element={
              <RutaProtegida roles={['CLIENTE']}>
                <p>Contenido de mis citas</p>
              </RutaProtegida>
            }
          />
        </Routes>
      </MemoryRouter>
    </AuthContext.Provider>,
  )
}

describe('RutaProtegida', () => {
  it('redirige a /ingresar cuando no hay sesión', () => {
    renderizar(null)
    expect(screen.getByText('Pantalla de ingreso')).toBeInTheDocument()
  })

  it('redirige a /no-autorizado cuando el rol no está permitido', () => {
    renderizar('BARBERO')
    expect(screen.getByText('Pantalla de no autorizado')).toBeInTheDocument()
  })

  it('muestra el contenido cuando el rol está permitido', () => {
    renderizar('CLIENTE')
    expect(screen.getByText('Contenido de mis citas')).toBeInTheDocument()
  })
})
