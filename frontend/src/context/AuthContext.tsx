import { createContext, useCallback, useMemo, useState, type ReactNode } from 'react'
import * as authService from '../services/authService'
import type {
  AuthResponse,
  LoginRequest,
  RegistroRequest,
  Rol,
  UsuarioResponse,
} from '../types/auth'

// El contexto y el proveedor conviven en este archivo por la estructura acordada
// del proyecto. El aviso de Fast Refresh no afecta el comportamiento en ejecución.
/* oxlint-disable react/only-export-components */

export interface ContextoAuth {
  usuario: UsuarioResponse | null
  rol: Rol | null
  estaAutenticado: boolean
  registrar: (datos: RegistroRequest) => Promise<UsuarioResponse>
  iniciarSesion: (datos: LoginRequest) => Promise<UsuarioResponse>
  renovarSesion: () => Promise<void>
  cerrarSesion: () => Promise<void>
  /** Deja la sesión lista para cuando el backend exponga /api/auth. */
  restaurarSesion: () => Promise<void>
}

export const AuthContext = createContext<ContextoAuth | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [usuario, setUsuario] = useState<UsuarioResponse | null>(null)

  const aplicarSesion = useCallback((sesion: AuthResponse) => {
    setUsuario(sesion.usuario)
    return sesion.usuario
  }, [])

  const registrar = useCallback(
    async (datos: RegistroRequest) => aplicarSesion(await authService.registrar(datos)),
    [aplicarSesion],
  )

  const iniciarSesion = useCallback(
    async (datos: LoginRequest) => aplicarSesion(await authService.iniciarSesion(datos)),
    [aplicarSesion],
  )

  const renovarSesion = useCallback(async () => {
    aplicarSesion(await authService.renovarSesion())
  }, [aplicarSesion])

  const cerrarSesion = useCallback(async () => {
    try {
      await authService.cerrarSesion()
    } finally {
      setUsuario(null)
    }
  }, [])

  // TODO: restaurar sesión con refresh cuando exista el módulo auth.
  // Esta función está lista, pero no se invoca al arrancar: hoy /api/auth no
  // existe y la petición dispararía el diálogo de autenticación básica del
  // navegador. Cuando el backend implemente JWT, llamarla desde un useEffect.
  const restaurarSesion = useCallback(async () => {
    try {
      aplicarSesion(await authService.renovarSesion())
    } catch {
      setUsuario(null)
    }
  }, [aplicarSesion])

  const valor = useMemo<ContextoAuth>(
    () => ({
      usuario,
      rol: usuario?.rol ?? null,
      estaAutenticado: usuario !== null,
      registrar,
      iniciarSesion,
      renovarSesion,
      cerrarSesion,
      restaurarSesion,
    }),
    [usuario, registrar, iniciarSesion, renovarSesion, cerrarSesion, restaurarSesion],
  )

  return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>
}
