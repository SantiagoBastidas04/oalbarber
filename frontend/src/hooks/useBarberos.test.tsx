import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { act, renderHook, waitFor } from '@testing-library/react'
import type { ReactNode } from 'react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import {
  cambiarEstadoBarbero,
  crearBarbero,
  listarBarberosActivos,
  listarBarberosAdmin,
  reemplazarServiciosDeBarbero,
} from '../services/barberosService'
import type {
  BarberoAdminResponse,
  BarberoPublicoResponse,
  BarberoRequest,
} from '../types/barbero'
import {
  clavesBarberos,
  useBarberos,
  useBarberosAdmin,
  useCambiarEstadoBarbero,
  useCrearBarbero,
  useGuardarServiciosBarbero,
} from './useBarberos'

vi.mock('../services/barberosService', () => ({
  cambiarEstadoBarbero: vi.fn(),
  crearBarbero: vi.fn(),
  listarBarberosActivos: vi.fn(),
  listarBarberosAdmin: vi.fn(),
  reemplazarServiciosDeBarbero: vi.fn(),
}))

const publico: BarberoPublicoResponse = {
  id: 'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa',
  nombrePublico: 'Barbero visible',
  descripcion: null,
  foto: null,
  activo: true,
}

const admin: BarberoAdminResponse = {
  id: publico.id,
  nombrePublico: publico.nombrePublico,
  descripcion: publico.descripcion,
  foto: publico.foto,
  activo: publico.activo,
  usuarioId: '11111111-1111-4111-8111-111111111111',
  nombre: 'Cuenta ficticia',
  celular: '+573000000001',
  servicioIds: [],
}

function nuevoCliente() {
  return new QueryClient({
    defaultOptions: {
      queries: { retry: false },
      mutations: { retry: false },
    },
  })
}

function envolver(cliente: QueryClient) {
  return function Proveedor({ children }: { children: ReactNode }) {
    return <QueryClientProvider client={cliente}>{children}</QueryClientProvider>
  }
}

describe('hooks de barberos', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('consulta el listado público con una clave estable', async () => {
    vi.mocked(listarBarberosActivos).mockResolvedValue([publico])
    const cliente = nuevoCliente()
    const { result } = renderHook(() => useBarberos(), { wrapper: envolver(cliente) })

    await waitFor(() => expect(result.current.data).toEqual([publico]))
    expect(cliente.getQueryCache().find({ queryKey: clavesBarberos.publicos })).toBeDefined()
  })

  it('consulta el listado administrativo', async () => {
    vi.mocked(listarBarberosAdmin).mockResolvedValue([admin])
    const cliente = nuevoCliente()
    const { result } = renderHook(() => useBarberosAdmin(), { wrapper: envolver(cliente) })

    await waitFor(() => expect(result.current.data).toEqual([admin]))
    expect(cliente.getQueryCache().find({ queryKey: clavesBarberos.admin })).toBeDefined()
  })

  it('al crear un perfil invalida los listados público y administrativo', async () => {
    const solicitud: BarberoRequest = {
      nombre: 'Cuenta ficticia',
      celular: '+573000000001',
      contrasena: 'ClavePrueba123',
    }
    vi.mocked(crearBarbero).mockResolvedValue(admin)
    const cliente = nuevoCliente()
    cliente.setQueryData(clavesBarberos.publicos, [publico])
    cliente.setQueryData(clavesBarberos.admin, [admin])
    const { result } = renderHook(() => useCrearBarbero(), { wrapper: envolver(cliente) })

    await act(async () => result.current.mutateAsync(solicitud))

    expect(crearBarbero).toHaveBeenCalledWith(solicitud)
    expect(cliente.getQueryState(clavesBarberos.publicos)?.isInvalidated).toBe(true)
    expect(cliente.getQueryState(clavesBarberos.admin)?.isInvalidated).toBe(true)
  })

  it('al cambiar el estado invalida los listados público y administrativo', async () => {
    vi.mocked(cambiarEstadoBarbero).mockResolvedValue({ ...admin, activo: false })
    const cliente = nuevoCliente()
    cliente.setQueryData(clavesBarberos.publicos, [publico])
    cliente.setQueryData(clavesBarberos.admin, [admin])
    const { result } = renderHook(() => useCambiarEstadoBarbero(), {
      wrapper: envolver(cliente),
    })

    await act(async () => {
      await result.current.mutateAsync({ id: admin.id, activo: false })
    })

    expect(cambiarEstadoBarbero).toHaveBeenCalledWith(admin.id, { activo: false })
    expect(cliente.getQueryState(clavesBarberos.publicos)?.isInvalidated).toBe(true)
    expect(cliente.getQueryState(clavesBarberos.admin)?.isInvalidated).toBe(true)
  })

  it('al guardar servicios invalida los listados público y administrativo', async () => {
    vi.mocked(reemplazarServiciosDeBarbero).mockResolvedValue([])
    const cliente = nuevoCliente()
    cliente.setQueryData(clavesBarberos.publicos, [publico])
    cliente.setQueryData(clavesBarberos.admin, [admin])
    const { result } = renderHook(() => useGuardarServiciosBarbero(), {
      wrapper: envolver(cliente),
    })

    await act(async () => {
      await result.current.mutateAsync({ id: admin.id, servicioIds: ['bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb'] })
    })

    expect(reemplazarServiciosDeBarbero).toHaveBeenCalledWith(admin.id, {
      servicioIds: ['bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb'],
    })
    expect(cliente.getQueryState(clavesBarberos.publicos)?.isInvalidated).toBe(true)
    expect(cliente.getQueryState(clavesBarberos.admin)?.isInvalidated).toBe(true)
  })
})
