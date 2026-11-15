import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen } from '@testing-library/react'
import { createMemoryRouter, RouterProvider } from 'react-router'
import { describe, expect, it, vi } from 'vitest'
import { AuthProvider } from '../context/AuthContext'
import { rutas } from './router'

vi.mock('../services/serviciosService', () => ({
  listarServicios: vi.fn(() => Promise.resolve([])),
}))

describe('aplicación', () => {
  it('renderiza la página de inicio con el catálogo', async () => {
    const router = createMemoryRouter(rutas, { initialEntries: ['/'] })
    const queryClient = new QueryClient({
      defaultOptions: { queries: { retry: false } },
    })

    render(
      <QueryClientProvider client={queryClient}>
        <AuthProvider>
          <RouterProvider router={router} />
        </AuthProvider>
      </QueryClientProvider>,
    )

    expect(
      await screen.findByRole('heading', { level: 1, name: /oalbarber/i }),
    ).toBeInTheDocument()
    expect(screen.getByRole('link', { name: /ver servicios/i })).toHaveAttribute(
      'href',
      '/servicios',
    )
  })
})
