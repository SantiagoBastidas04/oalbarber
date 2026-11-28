import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { renderHook, waitFor } from '@testing-library/react'
import type { ReactNode } from 'react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import {
  eliminarExcepcion,
  guardarExcepcion,
  listarExcepciones,
  listarHorarios,
  reemplazarHorarios,
} from '../services/horariosService'
import type {
  HorarioDiaResponse,
  HorarioExcepcionResponse,
} from '../types/horario'
import {
  clavesHorarios,
  useEliminarExcepcion,
  useExcepciones,
  useGuardarExcepcion,
  useGuardarHorarios,
  useHorarios,
} from './useHorarios'

vi.mock('../services/horariosService', () => ({
  listarHorarios: vi.fn(),
  reemplazarHorarios: vi.fn(),
  listarExcepciones: vi.fn(),
  guardarExcepcion: vi.fn(),
  eliminarExcepcion: vi.fn(),
}))

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
  cerrado: true,
  horaInicio: null,
  horaFin: null,
  almuerzoInicio: null,
  almuerzoFin: null,
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

describe('hooks de horarios', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('no consulta hasta que hay un barbero elegido', () => {
    const cliente = nuevoCliente()
    renderHook(
      () => ({ horario: useHorarios(null), fechas: useExcepciones(null) }),
      { wrapper: envolver(cliente) },
    )

    expect(listarHorarios).not.toHaveBeenCalled()
    expect(listarExcepciones).not.toHaveBeenCalled()
  })

  it('consulta y cachea el horario del barbero', async () => {
    vi.mocked(listarHorarios).mockResolvedValue([dia])
    const cliente = nuevoCliente()
    const { result } = renderHook(() => useHorarios(BARBERO_ID), {
      wrapper: envolver(cliente),
    })

    await waitFor(() => expect(result.current.data).toEqual([dia]))
    expect(cliente.getQueryCache().find({ queryKey: clavesHorarios.deBarbero(BARBERO_ID) }))
      .toBeDefined()
  })

  it('al guardar deja la caché con la respuesta del servidor', async () => {
    const guardado = { ...dia, horaFin: '18:00' }
    vi.mocked(reemplazarHorarios).mockResolvedValue([guardado])
    const cliente = nuevoCliente()
    const { result } = renderHook(() => useGuardarHorarios(BARBERO_ID), {
      wrapper: envolver(cliente),
    })

    await result.current.mutateAsync([{ diaSemana: 1, horaInicio: '10:00', horaFin: '18:00' }])

    expect(reemplazarHorarios).toHaveBeenCalledWith(BARBERO_ID, {
      dias: [{ diaSemana: 1, horaInicio: '10:00', horaFin: '18:00' }],
    })
    await waitFor(() =>
      expect(cliente.getQueryData(clavesHorarios.deBarbero(BARBERO_ID))).toEqual([guardado]),
    )
  })

  it('consulta las fechas especiales del barbero', async () => {
    vi.mocked(listarExcepciones).mockResolvedValue([excepcion])
    const cliente = nuevoCliente()
    const { result } = renderHook(() => useExcepciones(BARBERO_ID), {
      wrapper: envolver(cliente),
    })

    await waitFor(() => expect(result.current.data).toEqual([excepcion]))
    expect(listarExcepciones).toHaveBeenCalledWith(BARBERO_ID)
  })

  it('al guardar una fecha especial refresca la lista', async () => {
    vi.mocked(listarExcepciones).mockResolvedValue([])
    vi.mocked(guardarExcepcion).mockResolvedValue(excepcion)
    const cliente = nuevoCliente()
    const { result } = renderHook(
      () => ({
        lista: useExcepciones(BARBERO_ID),
        guardar: useGuardarExcepcion(BARBERO_ID),
      }),
      { wrapper: envolver(cliente) },
    )

    await waitFor(() => expect(result.current.lista.data).toEqual([]))

    await result.current.guardar.mutateAsync({
      fecha: FECHA,
      cerrado: true,
      horaInicio: null,
      horaFin: null,
      almuerzoInicio: null,
      almuerzoFin: null,
    })

    expect(guardarExcepcion).toHaveBeenCalledWith(BARBERO_ID, FECHA, {
      cerrado: true,
      horaInicio: null,
      horaFin: null,
      almuerzoInicio: null,
      almuerzoFin: null,
    })
    await waitFor(() => expect(listarExcepciones).toHaveBeenCalledTimes(2))
  })

  it('al eliminar una fecha especial refresca la lista', async () => {
    vi.mocked(listarExcepciones).mockResolvedValue([excepcion])
    vi.mocked(eliminarExcepcion).mockResolvedValue(undefined)
    const cliente = nuevoCliente()
    const { result } = renderHook(
      () => ({
        lista: useExcepciones(BARBERO_ID),
        eliminar: useEliminarExcepcion(BARBERO_ID),
      }),
      { wrapper: envolver(cliente) },
    )

    await waitFor(() => expect(result.current.lista.data).toEqual([excepcion]))

    await result.current.eliminar.mutateAsync(FECHA)

    expect(eliminarExcepcion).toHaveBeenCalledWith(BARBERO_ID, FECHA)
    await waitFor(() => expect(listarExcepciones).toHaveBeenCalledTimes(2))
  })
})
