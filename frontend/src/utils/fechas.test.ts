import { describe, expect, it } from 'vitest'
import { formatearFecha, formatearFechaHora, formatearHora } from './fechas'

describe('fechas', () => {
  it('formatea la fecha en la zona de Bogotá', () => {
    const texto = formatearFecha('2026-11-10T10:00:00-05:00')
    expect(texto).toContain('10')
    expect(texto).toContain('noviembre')
    expect(texto).toContain('2026')
  })

  it('convierte a hora de Bogotá una fecha en UTC', () => {
    // 15:00 UTC son las 10:00 en Bogotá (UTC-5).
    expect(formatearHora('2026-11-10T15:00:00Z')).toContain('10:00')
  })

  it('junta fecha y hora', () => {
    const texto = formatearFechaHora('2026-11-10T15:00:00Z')
    expect(texto).toContain('10 de noviembre')
    expect(texto).toContain('10:00')
  })
})
