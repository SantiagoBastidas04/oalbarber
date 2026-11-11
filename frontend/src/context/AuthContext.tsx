import { createContext, useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'
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
  /** Mientras se pregunta al backend si hay sesión viva en la cookie. */
  cargando: boolean
  estaAutenticado: boolean
  registrar: (datos: RegistroRequest) => Promise<UsuarioResponse>
  iniciarSesion: (datos: LoginRequest) => Promise<UsuarioResponse>
  renovarSesion: () => Promise<void>
  cerrarSesion: () => Promise<void>
  /** Pregunta al backend si hay sesión viva y la restaura si la hay. */
  restaurarSesion: () => Promise<void>
}

export const AuthContext = createContext<ContextoAuth | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [usuario, setUsuario] = useState<UsuarioResponse | null>(null)
  const [cargando, setCargando] = useState(true)

  /**
   * Al arrancar preguntamos con la cookie si hay sesión viva: el pase corto
   * vive solo en memoria y se pierde al recargar, así que la sesión vuelve
   * sin pedir de nuevo el celular y la contraseña.
   */
  useEffect(() => {
    let montado = true
    authService
      .renovarSesion()
      .then((sesion) => {
        if (montado) setUsuario(sesion.usuario)
      })
      .catch(() => {
        if (montado) setUsuario(null)
      })
      .finally(() => {
        if (montado) setCargando(false)
      })
    return () => {
      montado = false
    }
  }, [])

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

  const restaurarSesion = useCallback(async () => {
    try {
      aplicarSesion(await authService.renovarSesion())
    } catch {
      setUsuario(null)
    }
  }, [aplicarSesion])

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

  const valor = useMemo<ContextoAuth>(
    () => ({
      usuario,
      rol: usuario?.rol ?? null,
      cargando,
      estaAutenticado: usuario !== null,
      registrar,
      iniciarSesion,
      restaurarSesion,
      renovarSesion,
      cerrarSesion,
    }),
    [usuario, cargando, registrar, iniciarSesion, restaurarSesion, renovarSesion, cerrarSesion],
  )

  return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>
}
