import type {
  HorarioDiaRequest,
  HorarioDiaResponse,
  HorarioExcepcionRequest,
  HorarioExcepcionResponse,
} from '../../types/horario'

/** Días ISO de la semana con su nombre corto para las pestañas. */
export const DIAS = [
  { numero: 1, nombre: 'Lunes', corto: 'Lun' },
  { numero: 2, nombre: 'Martes', corto: 'Mar' },
  { numero: 3, nombre: 'Miércoles', corto: 'Mié' },
  { numero: 4, nombre: 'Jueves', corto: 'Jue' },
  { numero: 5, nombre: 'Viernes', corto: 'Vie' },
  { numero: 6, nombre: 'Sábado', corto: 'Sáb' },
  { numero: 7, nombre: 'Domingo', corto: 'Dom' },
] as const

export const DOMINGO = 7
export const APERTURA = '10:00'
export const CIERRE = '19:00'

export interface DiaFormulario {
  numero: number
  trabaja: boolean
  horaInicio: string
  horaFin: string
  almuerzo: boolean
  almuerzoInicio: string
  almuerzoFin: string
}

export interface ExcepcionFormulario {
  fecha: string
  atiende: boolean
  horaInicio: string
  horaFin: string
  almuerzo: boolean
  almuerzoInicio: string
  almuerzoFin: string
}

/** Punto de partida de P-01: lunes a sábado de 10:00 a 19:00 con almuerzo 13:00-14:00. */
export function diaPorDefecto(numero: number): DiaFormulario {
  const esDomingo = numero === DOMINGO
  return {
    numero,
    trabaja: !esDomingo,
    horaInicio: APERTURA,
    horaFin: CIERRE,
    almuerzo: !esDomingo,
    almuerzoInicio: '13:00',
    almuerzoFin: '14:00',
  }
}

export function semanaPorDefecto(): DiaFormulario[] {
  return DIAS.map(({ numero }) => diaPorDefecto(numero))
}

function recortarHora(hora: string): string {
  return hora.slice(0, 5)
}

/** Convierte la respuesta del backend en el estado del formulario. */
export function semanaDesdeRespuesta(dias: HorarioDiaResponse[]): DiaFormulario[] {
  if (dias.length === 0) {
    return semanaPorDefecto()
  }

  const porDia = new Map(dias.map((dia) => [dia.diaSemana, dia]))
  return DIAS.map(({ numero }) => {
    const configurado = porDia.get(numero)
    if (!configurado) {
      return { ...diaPorDefecto(numero), trabaja: false, almuerzo: false }
    }
    return {
      numero,
      trabaja: true,
      horaInicio: recortarHora(configurado.horaInicio),
      horaFin: recortarHora(configurado.horaFin),
      almuerzo: Boolean(configurado.almuerzoInicio && configurado.almuerzoFin),
      almuerzoInicio: configurado.almuerzoInicio
        ? recortarHora(configurado.almuerzoInicio)
        : '13:00',
      almuerzoFin: configurado.almuerzoFin
        ? recortarHora(configurado.almuerzoFin)
        : '14:00',
    }
  })
}

/** Reglas comunes a la semana y a las fechas especiales (RN-01 y RN-02). */
export function validarHoras(
  inicio: string,
  fin: string,
  conAlmuerzo: boolean,
  almuerzoInicio: string,
  almuerzoFin: string,
): string | null {
  if (!inicio || !fin) {
    return 'Indica la hora de inicio y la hora de fin.'
  }
  if (inicio >= fin) {
    return 'La hora de inicio debe ser menor que la de fin.'
  }
  if (inicio < APERTURA || fin > CIERRE) {
    return 'La jornada debe estar entre las 10:00 y las 19:00 (RN-01).'
  }
  if (!conAlmuerzo) {
    return null
  }
  if (!almuerzoInicio || !almuerzoFin) {
    return 'El almuerzo debe tener hora de inicio y de fin, o quedar vacío.'
  }
  if (almuerzoInicio >= almuerzoFin) {
    return 'El almuerzo debe empezar antes de terminar.'
  }
  if (almuerzoInicio <= inicio || almuerzoFin >= fin) {
    return 'El almuerzo debe quedar dentro de la jornada.'
  }
  return null
}

export function validarDia(dia: DiaFormulario): string | null {
  if (!dia.trabaja) {
    return null
  }
  return validarHoras(
    dia.horaInicio,
    dia.horaFin,
    dia.almuerzo,
    dia.almuerzoInicio,
    dia.almuerzoFin,
  )
}

export function validarSemana(semana: DiaFormulario[]): Record<number, string> {
  const errores: Record<number, string> = {}
  for (const dia of semana) {
    if (!dia.trabaja || dia.numero === DOMINGO) {
      continue
    }
    const error = validarDia(dia)
    if (error) {
      errores[dia.numero] = error
    }
  }
  return errores
}

export function aSolicitud(semana: DiaFormulario[]): HorarioDiaRequest[] {
  return semana
    .filter((dia) => dia.trabaja && dia.numero !== DOMINGO)
    .map((dia) => ({
      diaSemana: dia.numero,
      horaInicio: dia.horaInicio,
      horaFin: dia.horaFin,
      almuerzoInicio: dia.almuerzo ? dia.almuerzoInicio : null,
      almuerzoFin: dia.almuerzo ? dia.almuerzoFin : null,
    }))
}

export function resumenDia(dia: DiaFormulario): string {
  if (!dia.trabaja) {
    return 'Cerrado'
  }
  const almuerzo = dia.almuerzo
    ? ` · almuerzo ${dia.almuerzoInicio} a ${dia.almuerzoFin}`
    : ''
  return `${dia.horaInicio} a ${dia.horaFin}${almuerzo}`
}

export function nombreDia(numero: number): string {
  return DIAS.find((dia) => dia.numero === numero)?.nombre ?? ''
}

export function formularioExcepcionVacio(): ExcepcionFormulario {
  return {
    fecha: '',
    atiende: true,
    horaInicio: APERTURA,
    horaFin: CIERRE,
    almuerzo: true,
    almuerzoInicio: '13:00',
    almuerzoFin: '14:00',
  }
}

export function excepcionAFormulario(
  excepcion: HorarioExcepcionResponse,
): ExcepcionFormulario {
  return {
    fecha: excepcion.fecha,
    atiende: !excepcion.cerrado,
    horaInicio: excepcion.horaInicio ? recortarHora(excepcion.horaInicio) : APERTURA,
    horaFin: excepcion.horaFin ? recortarHora(excepcion.horaFin) : CIERRE,
    almuerzo: Boolean(excepcion.almuerzoInicio && excepcion.almuerzoFin),
    almuerzoInicio: excepcion.almuerzoInicio
      ? recortarHora(excepcion.almuerzoInicio)
      : '13:00',
    almuerzoFin: excepcion.almuerzoFin
      ? recortarHora(excepcion.almuerzoFin)
      : '14:00',
  }
}

export function solicitudExcepcion(
  formulario: ExcepcionFormulario,
): HorarioExcepcionRequest {
  if (!formulario.atiende) {
    return {
      cerrado: true,
      horaInicio: null,
      horaFin: null,
      almuerzoInicio: null,
      almuerzoFin: null,
    }
  }
  return {
    cerrado: false,
    horaInicio: formulario.horaInicio,
    horaFin: formulario.horaFin,
    almuerzoInicio: formulario.almuerzo ? formulario.almuerzoInicio : null,
    almuerzoFin: formulario.almuerzo ? formulario.almuerzoFin : null,
  }
}
