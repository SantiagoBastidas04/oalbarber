import { beforeEach, describe, expect, it, vi } from 'vitest'
import {
  cambiarEstadoBarbero,
  crearBarbero,
  listarBarberosActivos,
  listarBarberosAdmin,
  listarServiciosDeBarbero,
  reemplazarServiciosDeBarbero,
} from './barberosService'
import type {
  BarberoAdminResponse,
  BarberoPublicoResponse,
  BarberoRequest,
} from '../types/barbero'

const apiClientMock = vi.hoisted(() => ({
  get: vi.fn(),
  post: vi.fn(),
  patch: vi.fn(),
  put: vi.fn(),
}))

vi.mock('./apiClient', () => ({ apiClient: apiClientMock }))

const perfilPublico: BarberoPublicoResponse = {
  id: 'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa',
  nombrePublico: 'Barbero visible',
  descripcion: 'Descripción ficticia',
  foto: 'barbero-01.webp',
  activo: true,
}

const perfilAdmin: BarberoAdminResponse = {
  ...perfilPublico,
  usuarioId: '11111111-1111-4111-8111-111111111111',
  nombre: 'Cuenta ficticia',
  celular: '+573000000001',
  servicioIds: [],
}

describe('barberosService', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('consulta los barberos activos por la ruta pública', async () => {
    apiClientMock.get.mockResolvedValue({ data: [perfilPublico] })

    await expect(listarBarberosActivos()).resolves.toEqual([perfilPublico])
    expect(apiClientMock.get).toHaveBeenCalledWith('/barberos')
  })

  it('consulta el listado administrativo, incluidos los inactivos', async () => {
    apiClientMock.get.mockResolvedValue({ data: [perfilAdmin] })

    await expect(listarBarberosAdmin()).resolves.toEqual([perfilAdmin])
    expect(apiClientMock.get).toHaveBeenCalledWith('/barberos/admin')
  })

  it('crea un barbero sin enviar un rol desde el cliente', async () => {
    const solicitud: BarberoRequest = {
      nombre: 'Cuenta ficticia',
      celular: '+573000000001',
      contrasena: 'ClavePrueba123',
      nombrePublico: 'Barbero visible',
      descripcion: 'Descripción ficticia',
      foto: 'barbero-01.webp',
    }
    apiClientMock.post.mockResolvedValue({ data: perfilAdmin })

    await expect(crearBarbero(solicitud)).resolves.toEqual(perfilAdmin)
    expect(apiClientMock.post).toHaveBeenCalledWith('/barberos', solicitud)
  })

  it('cambia el estado del perfil mediante PATCH', async () => {
    const inactivo = { ...perfilAdmin, activo: false }
    apiClientMock.patch.mockResolvedValue({ data: inactivo })

    await expect(cambiarEstadoBarbero(perfilAdmin.id, { activo: false })).resolves.toEqual(inactivo)
    expect(apiClientMock.patch).toHaveBeenCalledWith(
      `/barberos/${perfilAdmin.id}/estado`,
      { activo: false },
    )
  })

  it('propaga errores normalizados por apiClient', async () => {
    const error = { status: 409, detail: 'Celular duplicado' }
    apiClientMock.post.mockRejectedValue(error)

    await expect(crearBarbero({
      nombre: 'Cuenta ficticia',
      celular: '+573000000001',
      contrasena: 'ClavePrueba123',
    })).rejects.toBe(error)
  })

  it('consulta los servicios activos de un perfil', async () => {
    apiClientMock.get.mockResolvedValue({ data: [] })

    await expect(listarServiciosDeBarbero(perfilAdmin.id)).resolves.toEqual([])
    expect(apiClientMock.get).toHaveBeenCalledWith(`/barberos/${perfilAdmin.id}/servicios`)
  })

  it('reemplaza las asignaciones por el endpoint administrativo', async () => {
    const solicitud = { servicioIds: ['bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb'] }
    apiClientMock.put.mockResolvedValue({ data: [] })

    await expect(reemplazarServiciosDeBarbero(perfilAdmin.id, solicitud)).resolves.toEqual([])
    expect(apiClientMock.put).toHaveBeenCalledWith(
      `/barberos/${perfilAdmin.id}/servicios`, solicitud,
    )
  })
})
