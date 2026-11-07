import { describe, expect, it } from 'vitest'
import { formatearMoneda } from './moneda'

describe('moneda', () => {
  it('formatea pesos colombianos sin decimales', () => {
    const texto = formatearMoneda(45000)
    expect(texto).toContain('45.000')
    expect(texto).not.toContain(',00')
  })

  it('incluye el símbolo de peso', () => {
    expect(formatearMoneda(0)).toContain('$')
  })
})
