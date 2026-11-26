import { describe, expect, it } from 'vitest'
import type {
  HorarioDiaResponse,
  HorarioExcepcionResponse,
} from '../../types/horario'
import {
  aSolicitud,
  diaPorDefecto,
  DOMINGO,
  excepcionAFormulario,
  formularioExcepcionVacio,
  nombreDia,
  semanaDesdeRespuesta,
  semanaPorDefecto,
  solicitudExcepcion,
  validarDia,
  validarHoras,
  validarSemana,
} from './horarioFormulario'

const BARBERO_ID = 'bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb'

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
    fecha: '2026-12-06',
    cerrado: false,
    horaInicio: '11:00',
    horaFin: '17:00',
    almuerzoInicio: null,
    almuerzoFin: null,
    ...overrides,
  }
}

describe('horarioFormulario', () => {
  it('propone lunes a sábado de 10:00 a 19:00 con almuerzo 13:00-14:00', () => {
    const lunes = diaPorDefecto(1)
    expect(lunes).toMatchObject({
      trabaja: true,
      horaInicio: '10:00',
      horaFin: '19:00',
      almuerzo: true,
      almuerzoInicio: '13:00',
      almuerzoFin: '14:00',
    })
    expect(diaPorDefecto(DOMINGO).trabaja).toBe(false)
    expect(semanaPorDefecto()).toHaveLength(7)
  })

  it('sin datos guardados propone la semana estándar', () => {
    const semana = semanaDesdeRespuesta([])
    expect(semana.filter((opcion) => opcion.trabaja)).toHaveLength(6)
    expect(semana[6]).toMatchObject({ numero: DOMINGO, trabaja: false })
  })

  it('marca cerrados los días que no vienen del backend', () => {
    const semana = semanaDesdeRespuesta([
      dia({ diaSemana: 1, horaInicio: '11:00:00', horaFin: '18:00:00' }),
      dia({ diaSemana: 3, almuerzoInicio: null, almuerzoFin: null }),
    ])

    expect(semana[0]).toMatchObject({
      trabaja: true,
      horaInicio: '11:00',
      horaFin: '18:00',
    })
    expect(semana[1].trabaja).toBe(false)
    expect(semana[2].almuerzo).toBe(false)
    expect(semana[6].trabaja).toBe(false)
  })

  it('valida la jornada y el almuerzo con las reglas del backend', () => {
    expect(validarHoras('10:00', '19:00', true, '13:00', '14:00')).toBeNull()
    expect(validarHoras('19:00', '10:00', false, '', '')).toBe(
      'La hora de inicio debe ser menor que la de fin.',
    )
    expect(validarHoras('08:00', '19:00', false, '', '')).toBe(
      'La jornada debe estar entre las 10:00 y las 19:00 (RN-01).',
    )
    expect(validarHoras('10:00', '19:00', true, '', '')).toBe(
      'El almuerzo debe tener hora de inicio y de fin, o quedar vacío.',
    )
    expect(validarHoras('10:00', '19:00', true, '14:00', '13:00')).toBe(
      'El almuerzo debe empezar antes de terminar.',
    )
    expect(validarHoras('10:00', '19:00', true, '18:30', '19:00')).toBe(
      'El almuerzo debe quedar dentro de la jornada.',
    )
  })

  it('un día cerrado no exige horas', () => {
    expect(
      validarDia({
        numero: 2,
        trabaja: false,
        horaInicio: '',
        horaFin: '',
        almuerzo: false,
        almuerzoInicio: '',
        almuerzoFin: '',
      }),
    ).toBeNull()
  })

  it('la validación semanal ignora el domingo y los días cerrados', () => {
    const semana = semanaPorDefecto()
    semana[1] = { ...semana[1], trabaja: false, horaInicio: '', horaFin: '' }
    semana[6] = { ...semana[6], trabaja: true, horaInicio: '09:00', horaFin: '20:00' }

    expect(validarSemana(semana)).toEqual({})
  })

  it('la solicitud omite el domingo, los días cerrados y el almuerzo vacío', () => {
    const semana = semanaPorDefecto()
    semana[2] = { ...semana[2], trabaja: false }
    semana[3] = { ...semana[3], almuerzo: false }

    const solicitud = aSolicitud(semana)

    expect(solicitud.map((item) => item.diaSemana)).toEqual([1, 2, 4, 5, 6])
    expect(solicitud.find((item) => item.diaSemana === 4)).toMatchObject({
      almuerzoInicio: null,
      almuerzoFin: null,
    })
  })

  it('arma la solicitud de una fecha cerrada sin horas', () => {
    expect(
      solicitudExcepcion({
        fecha: '2026-12-06',
        atiende: false,
        horaInicio: '10:00',
        horaFin: '19:00',
        almuerzo: true,
        almuerzoInicio: '13:00',
        almuerzoFin: '14:00',
      }),
    ).toEqual({
      cerrado: true,
      horaInicio: null,
      horaFin: null,
      almuerzoInicio: null,
      almuerzoFin: null,
    })
  })

  it('arma la solicitud de una fecha abierta con sus horas', () => {
    expect(
      solicitudExcepcion({
        fecha: '2026-12-06',
        atiende: true,
        horaInicio: '11:00',
        horaFin: '17:00',
        almuerzo: false,
        almuerzoInicio: '13:00',
        almuerzoFin: '14:00',
      }),
    ).toEqual({
      cerrado: false,
      horaInicio: '11:00',
      horaFin: '17:00',
      almuerzoInicio: null,
      almuerzoFin: null,
    })
  })

  it('convierte una fecha especial al formulario', () => {
    expect(excepcionAFormulario(excepcion())).toMatchObject({
      fecha: '2026-12-06',
      atiende: true,
      horaInicio: '11:00',
      horaFin: '17:00',
      almuerzo: false,
    })
    expect(
      excepcionAFormulario(
        excepcion({ cerrado: true, horaInicio: null, horaFin: null }),
      ),
    ).toMatchObject({
      atiende: false,
      horaInicio: '10:00',
      horaFin: '19:00',
    })
  })

  it('el formulario vacío abre una fecha estándar', () => {
    expect(formularioExcepcionVacio()).toMatchObject({
      fecha: '',
      atiende: true,
      horaInicio: '10:00',
      horaFin: '19:00',
      almuerzo: true,
    })
  })

  it('conoce el nombre de cada día', () => {
    expect(nombreDia(1)).toBe('Lunes')
    expect(nombreDia(7)).toBe('Domingo')
    expect(nombreDia(99)).toBe('')
  })
})
