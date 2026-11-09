import { describe, expect, it } from 'vitest'
import { componerCelular, PAISES, paisPorCodigo } from './celularPais'

describe('componerCelular', () => {
  it('une el prefijo de Colombia con diez dígitos', () => {
    expect(componerCelular('57', '300 123 4567')).toBe('+573001234567')
  })

  it('elimina separadores escritos por el usuario', () => {
    expect(componerCelular('57', '300-123-4567')).toBe('+573001234567')
  })

  it('acepta prefijos de varios dígitos', () => {
    expect(componerCelular('593', '991234567')).toBe('+593991234567')
  })

  it('rechaza longitudes que no corresponden al país', () => {
    expect(componerCelular('57', '300123456')).toBeNull()
    expect(componerCelular('57', '30012345678')).toBeNull()
  })

  it('rechaza un prefijo desconocido', () => {
    expect(componerCelular('999', '3001234567')).toBeNull()
  })

  it('respeta el máximo de quince dígitos del backend', () => {
    expect(componerCelular('598', '12345678')).toBe('+59812345678')
  })
})

describe('PAISES', () => {
  it('empieza por Colombia', () => {
    expect(PAISES[0]?.codigo).toBe('57')
    expect(PAISES[0]?.nombre).toBe('Colombia')
  })

  it('no repite códigos ISO ni prefijos', () => {
    const isos = new Set(PAISES.map((pais) => pais.iso))
    const codigos = new Set(PAISES.map((pais) => pais.codigo))
    expect(isos.size).toBe(PAISES.length)
    expect(codigos.size).toBe(PAISES.length)
  })

  it('encuentra un país por su prefijo', () => {
    expect(paisPorCodigo('52')?.nombre).toBe('México')
    expect(paisPorCodigo('000')).toBeUndefined()
  })
})
