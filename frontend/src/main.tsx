import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { RouterProvider } from 'react-router'
import { AuthProvider } from './context/AuthContext'
import { router } from './routes/router'
import type { ApiError } from './types/error'
import './styles/index.css'

// Sin reintentos en errores 4xx: un 401, 403 o 404 no mejora al repetirse.
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: (intento, error) => {
        const estado = (error as Partial<ApiError>).status ?? 0
        if (estado >= 400 && estado < 500) {
          return false
        }
        return intento < 2
      },
    },
  },
})

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <RouterProvider router={router} />
      </AuthProvider>
    </QueryClientProvider>
  </StrictMode>,
)
