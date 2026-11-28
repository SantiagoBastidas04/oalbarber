import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { fireEvent, render, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { listarBarberosAdmin } from '../../services/barberosService'
import {
  eliminarExcepcion,
  guardarExcepcion,
  listarExcepciones,
  listarHorarios,
  reemplazarHorarios,
} from '../../services/horariosService'
import type { BarberoAdminResponse } from '../../types/barbero'
import type {
  HorarioDiaResponse,
  HorarioExcepcionResponse,
} from '../../types/horario'
import { HorariosPage } from './HorariosPage'

vi.mock('../../services/barberosService', () => ({
  listarBarberosAdmin: vi.fn(),
}))

vi.mock('../../services/horariosService', () => ({
  listarHorarios: vi.fn(),
  reemplazarHorarios: vi.fn(),
  listarExcepciones: vi.fn(),
  guardarExcepcion: vi.fn(),
  eliminarExcepcion: vi.fn(),
}))

const BARBERO_ID = 'bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb'
const OTRO_BARBERO_ID = 'cccccccc-cccc-4ccc-8ccc-cccccccccccc'
const FECHA = '2026-12-06'

const barbero: BarberoAdminResponse = {
  id: BARBERO_ID,
  usuarioId: '11111111-1111-4111-8111-111111111111',
  nombre: 'Oscar Bastidas',
  celular: '+573000000001',
  nombrePublico: 'Oscar',
  descripcion: null,
  foto: null,
  activo: true,
  servicioIds: [],
}

function dia(overrides: Partial<HorarioDiaResponse> = {}): HorarioDiaResponse {
  return {
    id: 'dddddddd-dddd-4ddd-8ddd-dddddddddddd',
    barberoId: BARBERO_ID,
    diaSemana: 1,
    horaInicio: '10:00',
    horaFin: '19:00',
    almuerzoInicio: '13:00',
    almuerzoFin: '14:00',
    ...overrides,
  }
}

function excepcion(
  overrides: Partial<HorarioExcepcionResponse> = {},
): HorarioExcepcionResponse {
  return {
    id: 'eeeeeeee-eeee-4eee-8eee-eeeeeeeeeeee',
    barberoId: BARBERO_ID,
    fecha: FECHA,
    cerrado: false,
    horaInicio: '11:00',
    horaFin: '17:00',
    almuerzoInicio: null,
    almuerzoFin: null,
    ...overrides,
  }
}

function renderizarPagina() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  })
  return render(
    <QueryClientProvider client={queryClient}>
      <HorariosPage />
    </QueryClientProvider>,
  )
}

describe('HorariosPage', () => {
  beforeEach(() => {
    vi.mocked(listarBarberosAdmin).mockReset()
    vi.mocked(listarHorarios).mockReset()
    vi.mocked(reemplazarHorarios).mockReset()
    vi.mocked(listarExcepciones).mockReset()
    vi.mocked(guardarExcepcion).mockReset()
    vi.mocked(eliminarExcepcion).mockReset()
    vi.mocked(listarBarberosAdmin).mockResolvedValue([barbero])
    vi.mocked(listarHorarios).mockResolvedValue([])
    vi.mocked(listarExcepciones).mockResolvedValue([])
  })

  it('muestra una pestaña por día y solo el día elegido', async () => {
    const usuario = userEvent.setup()
    renderizarPagina()

    expect(await screen.findByRole('button', { name: /configurar lunes/i })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /configurar domingo/i })).toBeInTheDocument()
    expect(screen.getByLabelText('Hora de inicio del lunes')).toBeInTheDocument()
    expect(screen.queryByLabelText('Hora de inicio del martes')).not.toBeInTheDocument()

    await usuario.click(screen.getByRole('button', { name: /configurar martes/i }))

    expect(screen.getByLabelText('Hora de inicio del martes')).toBeInTheDocument()
    expect(screen.queryByLabelText('Hora de inicio del lunes')).not.toBeInTheDocument()
    expect(screen.getByRole('button', { name: /configurar martes/i })).toHaveAttribute(
      'aria-pressed',
      'true',
    )
  })

  it('al cambiar de barbero deja una sola tarjeta de horario y una de fechas', async () => {
    const usuario = userEvent.setup()
    const otroBarbero: BarberoAdminResponse = {
      ...barbero,
      id: OTRO_BARBERO_ID,
      nombre: 'Santiago Bastidas',
      nombrePublico: 'Santiago',
    }
    vi.mocked(listarBarberosAdmin).mockResolvedValue([barbero, otroBarbero])
    renderizarPagina()

    await screen.findByLabelText('Hora de inicio del lunes')
    expect(screen.getAllByRole('heading', { name: /horario semanal/i })).toHaveLength(1)

    await usuario.selectOptions(screen.getByLabelText('Perfil de barbero'), OTRO_BARBERO_ID)
    await waitFor(() => expect(listarHorarios).toHaveBeenCalledWith(OTRO_BARBERO_ID))

    expect(await screen.findByLabelText('Hora de inicio del lunes')).toBeInTheDocument()
    expect(screen.getAllByRole('heading', { name: /horario semanal/i })).toHaveLength(1)
    expect(screen.getAllByRole('heading', { name: /fechas especiales/i })).toHaveLength(1)
    expect(screen.getAllByRole('button', { name: /horario estándar l-s/i })).toHaveLength(1)
  })

  it('el domingo se explica como cerrado y remite a las fechas especiales', async () => {    const usuario = userEvent.setup()
    renderizarPagina()

    await screen.findByLabelText('Hora de inicio del lunes')
    await usuario.click(screen.getByRole('button', { name: /configurar domingo/i }))

    expect(screen.getByText(/el domingo permanece cerrado \(rn-01\)/i)).toBeInTheDocument()
    expect(screen.getByRole('link', { name: /ir a fechas especiales/i })).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /guardar cambios/i })).not.toBeInTheDocument()
  })

  it('configura lunes a sábado con un clic y guarda de una vez', async () => {
    const usuario = userEvent.setup()
    vi.mocked(reemplazarHorarios).mockResolvedValue([dia()])
    renderizarPagina()

    await screen.findByLabelText('Hora de inicio del lunes')
    await usuario.click(screen.getByRole('button', { name: /horario estándar l-s/i }))

    await waitFor(() => expect(reemplazarHorarios).toHaveBeenCalledTimes(1))
    const [barberoId, solicitud] = vi.mocked(reemplazarHorarios).mock.calls[0]
    expect(barberoId).toBe(BARBERO_ID)
    expect(solicitud.dias).toHaveLength(6)
    expect(solicitud.dias[0]).toEqual({
      diaSemana: 1,
      horaInicio: '10:00',
      horaFin: '19:00',
      almuerzoInicio: '13:00',
      almuerzoFin: '14:00',
    })
    expect(solicitud.dias[5].diaSemana).toBe(6)
    expect(await screen.findByText(/horario estándar aplicado a lunes a sábado/i))
      .toBeInTheDocument()
  })

  it('copia el día activo a lunes a sábado y guarda', async () => {
    const usuario = userEvent.setup()
    vi.mocked(reemplazarHorarios).mockResolvedValue([dia()])
    renderizarPagina()

    fireEvent.change(await screen.findByLabelText('Hora de inicio del lunes'), {
      target: { value: '11:00' },
    })
    await usuario.click(screen.getByRole('button', { name: /aplicar a l-s/i }))

    await waitFor(() => expect(reemplazarHorarios).toHaveBeenCalledTimes(1))
    const [, solicitud] = vi.mocked(reemplazarHorarios).mock.calls[0]
    expect(solicitud.dias).toHaveLength(6)
    expect(solicitud.dias.every((item) => item.horaInicio === '11:00')).toBe(true)
    expect(await screen.findByText(/horario aplicado a lunes a sábado/i)).toBeInTheDocument()
  })

  it('valida antes de enviar y no llama a la API', async () => {
    const usuario = userEvent.setup()
    renderizarPagina()

    fireEvent.change(await screen.findByLabelText('Hora de inicio del lunes'), {
      target: { value: '19:00' },
    })
    await usuario.click(screen.getByRole('button', { name: /guardar cambios/i }))

    expect(await screen.findByText('La hora de inicio debe ser menor que la de fin.'))
      .toBeInTheDocument()
    expect(reemplazarHorarios).not.toHaveBeenCalled()
  })

  it('respeta el horario guardado y los días cerrados', async () => {
    vi.mocked(listarHorarios).mockResolvedValue([
      dia({ diaSemana: 1, horaInicio: '11:00', horaFin: '18:00' }),
      dia({ diaSemana: 3, almuerzoInicio: null, almuerzoFin: null }),
    ])
    renderizarPagina()

    expect(await screen.findByLabelText('Hora de inicio del lunes')).toHaveValue('11:00')
    expect(screen.getByLabelText('Hora de fin del lunes')).toHaveValue('18:00')
  })

  it('al recibir un error del backend abre el día con el problema', async () => {
    const usuario = userEvent.setup()
    vi.mocked(reemplazarHorarios).mockRejectedValue({
      status: 400,
      title: 'Revisa los datos enviados',
      detail: 'Revisa los horarios marcados.',
      codigo: 'HORARIO_INVALIDO',
      erroresCampo: { '2': 'El almuerzo debe quedar dentro de la jornada.' },
    })
    renderizarPagina()

    await screen.findByLabelText('Hora de inicio del lunes')
    await usuario.click(screen.getByRole('button', { name: /guardar cambios/i }))

    expect(await screen.findByText('El almuerzo debe quedar dentro de la jornada.'))
      .toBeInTheDocument()
    expect(screen.getByText('Revisa los datos enviados')).toBeInTheDocument()
    expect(screen.getByLabelText('Hora de inicio del martes')).toBeInTheDocument()
  })

  it('abre un domingo concreto como fecha especial', async () => {
    const usuario = userEvent.setup()
    vi.mocked(guardarExcepcion).mockResolvedValue(excepcion())
    renderizarPagina()

    await screen.findByLabelText('Hora de inicio del lunes')
    await usuario.click(screen.getByRole('button', { name: /agregar fecha/i }))

    fireEvent.change(screen.getByLabelText('Fecha de la excepción'), {
      target: { value: FECHA },
    })
    fireEvent.change(screen.getByLabelText('Hora de inicio de la fecha especial'), {
      target: { value: '11:00' },
    })
    fireEvent.change(screen.getByLabelText('Hora de fin de la fecha especial'), {
      target: { value: '17:00' },
    })
    await usuario.click(screen.getByLabelText('Almuerzo de la fecha especial'))
    await usuario.click(screen.getByRole('button', { name: /guardar fecha/i }))

    await waitFor(() => expect(guardarExcepcion).toHaveBeenCalledTimes(1))
    expect(guardarExcepcion).toHaveBeenCalledWith(BARBERO_ID, FECHA, {
      cerrado: false,
      horaInicio: '11:00',
      horaFin: '17:00',
      almuerzoInicio: null,
      almuerzoFin: null,
    })
    expect(await screen.findByText('Fecha especial guardada.')).toBeInTheDocument()
  })

  it('permite cerrar una fecha concreta sin horas', async () => {
    const usuario = userEvent.setup()
    vi.mocked(guardarExcepcion).mockResolvedValue(excepcion({ cerrado: true }))
    renderizarPagina()

    await screen.findByLabelText('Hora de inicio del lunes')
    await usuario.click(screen.getByRole('button', { name: /agregar fecha/i }))
    fireEvent.change(screen.getByLabelText('Fecha de la excepción'), {
      target: { value: FECHA },
    })
    await usuario.click(screen.getByLabelText('Atiende ese día'))
    await usuario.click(screen.getByRole('button', { name: /guardar fecha/i }))

    await waitFor(() => expect(guardarExcepcion).toHaveBeenCalledTimes(1))
    expect(guardarExcepcion).toHaveBeenCalledWith(BARBERO_ID, FECHA, {
      cerrado: true,
      horaInicio: null,
      horaFin: null,
      almuerzoInicio: null,
      almuerzoFin: null,
    })
  })

  it('lista, edita y quita las fechas especiales', async () => {
    const usuario = userEvent.setup()
    vi.mocked(listarExcepciones).mockResolvedValue([excepcion()])
    vi.mocked(guardarExcepcion).mockResolvedValue(excepcion())
    vi.mocked(eliminarExcepcion).mockResolvedValue(undefined)
    renderizarPagina()

    const lista = await screen.findByRole('list')
    expect(within(lista).getByText(/6 de diciembre de 2026/i)).toBeInTheDocument()
    expect(within(lista).getByText('11:00 a 17:00')).toBeInTheDocument()

    await usuario.click(screen.getByRole('button', { name: `Editar la fecha ${FECHA}` }))
    expect(screen.getByLabelText('Fecha de la excepción')).toBeDisabled()
    await usuario.click(screen.getByRole('button', { name: /actualizar fecha/i }))

    await waitFor(() => expect(guardarExcepcion).toHaveBeenCalledTimes(1))
    expect(guardarExcepcion).toHaveBeenCalledWith(BARBERO_ID, FECHA, {
      cerrado: false,
      horaInicio: '11:00',
      horaFin: '17:00',
      almuerzoInicio: null,
      almuerzoFin: null,
    })

    await usuario.click(screen.getByRole('button', { name: `Quitar la fecha ${FECHA}` }))
    expect(await screen.findByText(/¿quitar esta fecha\?/i)).toBeInTheDocument()
    expect(eliminarExcepcion).not.toHaveBeenCalled()

    await usuario.click(screen.getByRole('button', { name: /sí, quitar/i }))
    await waitFor(() => expect(eliminarExcepcion).toHaveBeenCalledWith(BARBERO_ID, FECHA))
    expect(await screen.findByText('Fecha especial eliminada.')).toBeInTheDocument()
  })

  it('cancelar la confirmación conserva la fecha especial', async () => {
    const usuario = userEvent.setup()
    vi.mocked(listarExcepciones).mockResolvedValue([excepcion()])
    renderizarPagina()

    await screen.findByRole('list')
    await usuario.click(screen.getByRole('button', { name: `Quitar la fecha ${FECHA}` }))
    await usuario.click(screen.getByRole('button', { name: /^cancelar$/i }))

    expect(eliminarExcepcion).not.toHaveBeenCalled()
    expect(screen.getByText(/6 de diciembre de 2026/i)).toBeInTheDocument()
  })
})
