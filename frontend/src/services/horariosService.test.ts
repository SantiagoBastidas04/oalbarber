import { beforeEach, describe, expect, it, vi } from 'vitest'
import {
  eliminarExcepcion,
  guardarExcepcion,
  listarExcepciones,
  listarHorarios,
  reemplazarHorarios,
} from './horariosService'
import type {
  HorarioDiaResponse,
  HorarioExcepcionResponse,
  HorarioSemanaRequest,
} from '../types/horario'

const apiClientMock = vi.hoisted(() => ({
  get: vi.fn(),
  put: vi.fn(),
  delete: vi.fn(),
}))

vi.mock('./apiClient', () => ({ apiClient: apiClientMock }))

const BARBERO_ID = 'bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb'
const FECHA = '2026-12-06'

const dia: HorarioDiaResponse = {
  id: 'dddddddd-dddd-4ddd-8ddd-dddddddddddd',
  barberoId: BARBERO_ID,
  diaSemana: 1,
  horaInicio: '10:00',
  horaFin: '19:00',
  almuerzoInicio: '13:00',
  almuerzoFin: '14:00',
}

const excepcion: HorarioExcepcionResponse = {
  id: 'eeeeeeee-eeee-4eee-8eee-eeeeeeeeeeee',
  barberoId: BARBERO_ID,
  fecha: FECHA,
  cerrado: false,
  horaInicio: '11:00',
  horaFin: '17:00',
  almuerzoInicio: null,
  almuerzoFin: null,
}

describe('horariosService', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('consulta el horario del barbero con su identificador', async () => {
    apiClientMock.get.mockResolvedValue({ data: [dia] })

    await expect(listarHorarios(BARBERO_ID)).resolves.toEqual([dia])
    expect(apiClientMock.get).toHaveBeenCalledWith('/horarios', {
      params: { barberoId: BARBERO_ID },
    })
  })

  it('reemplaza la semana completa mediante PUT', async () => {
    const solicitud: HorarioSemanaRequest = {
      dias: [
        {
          diaSemana: 1,
          horaInicio: '10:00',
          horaFin: '19:00',
          almuerzoInicio: '13:00',
          almuerzoFin: '14:00',
        },
      ],
    }
    apiClientMock.put.mockResolvedValue({ data: [dia] })

    await expect(reemplazarHorarios(BARBERO_ID, solicitud)).resolves.toEqual([dia])
    expect(apiClientMock.put).toHaveBeenCalledWith(`/horarios/${BARBERO_ID}`, solicitud)
  })

  it('lista las fechas especiales del barbero', async () => {
    apiClientMock.get.mockResolvedValue({ data: [excepcion] })

    await expect(listarExcepciones(BARBERO_ID)).resolves.toEqual([excepcion])
    expect(apiClientMock.get).toHaveBeenCalledWith(
      `/horarios/${BARBERO_ID}/excepciones`,
    )
  })

  it('guarda una fecha especial en su ruta con la fecha ISO', async () => {
    const solicitud = {
      cerrado: false,
      horaInicio: '11:00',
      horaFin: '17:00',
      almuerzoInicio: null,
      almuerzoFin: null,
    }
    apiClientMock.put.mockResolvedValue({ data: excepcion })

    await expect(guardarExcepcion(BARBERO_ID, FECHA, solicitud)).resolves.toEqual(excepcion)
    expect(apiClientMock.put).toHaveBeenCalledWith(
      `/horarios/${BARBERO_ID}/excepciones/${FECHA}`,
      solicitud,
    )
  })

  it('elimina la fecha especial con DELETE', async () => {
    apiClientMock.delete.mockResolvedValue({ status: 204 })

    await expect(eliminarExcepcion(BARBERO_ID, FECHA)).resolves.toBeUndefined()
    expect(apiClientMock.delete).toHaveBeenCalledWith(
      `/horarios/${BARBERO_ID}/excepciones/${FECHA}`,
    )
  })
})
