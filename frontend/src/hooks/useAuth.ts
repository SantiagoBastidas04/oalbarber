import { useContext } from 'react'
import { AuthContext, type ContextoAuth } from '../context/AuthContext'

/** Acceso al contexto de autenticación; falla si se usa fuera del proveedor. */
export function useAuth(): ContextoAuth {
  const contexto = useContext(AuthContext)
  if (!contexto) {
    throw new Error('useAuth debe usarse dentro de <AuthProvider>.')
  }
  return contexto
}
