import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen } from '@testing-library/react'
import { createMemoryRouter, RouterProvider } from 'react-router'
import { describe, expect, it } from 'vitest'
import { AuthProvider } from '../context/AuthContext'
import { rutas } from './router'

describe('aplicación', () => {
  it('renderiza la página de inicio', async () => {
    const router = createMemoryRouter(rutas, { initialEntries: ['/'] })
    const queryClient = new QueryClient()

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
    expect(screen.getAllByText(/pendiente de implementar/i).length).toBeGreaterThan(0)
  })
})
