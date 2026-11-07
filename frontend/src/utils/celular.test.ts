import { describe, expect, it } from 'vitest'
import { normalizarCelular } from './celular'

describe('celular', () => {
  it('normaliza un celular con espacios', () => {
    expect(normalizarCelular('300 123 4567')).toBe('+573001234567')
  })

  it('conserva el prefijo +57 si ya viene', () => {
    expect(normalizarCelular('+57 300 123 4567')).toBe('+573001234567')
  })

  it('acepta guiones y paréntesis', () => {
    expect(normalizarCelular('(300)-123-4567')).toBe('+573001234567')
  })

  it('rechaza números que no empiezan por 3', () => {
    expect(normalizarCelular('200 123 4567')).toBeNull()
  })

  it('rechaza longitudes diferentes de 10 dígitos', () => {
    expect(normalizarCelular('300 123 456')).toBeNull()
    expect(normalizarCelular('300 123 45678')).toBeNull()
  })

  it('rechaza valores vacíos o con letras', () => {
    expect(normalizarCelular('')).toBeNull()
    expect(normalizarCelular('abc 123 4567')).toBeNull()
  })
})
