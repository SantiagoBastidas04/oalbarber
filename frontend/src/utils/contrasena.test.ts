import { describe, expect, it } from 'vitest'
import { ETIQUETAS_FUERZA, medirFuerzaContrasena } from './contrasena'

describe('medirFuerzaContrasena', () => {
  it('marca cero mientras no llega al mínimo', () => {
    expect(medirFuerzaContrasena('')).toBe(0)
    expect(medirFuerzaContrasena('corta')).toBe(0)
  })

  it('marca básica con ocho caracteres', () => {
    expect(medirFuerzaContrasena('clave123')).toBe(1)
  })

  it('marca buena con diez caracteres y letras y números', () => {
    expect(medirFuerzaContrasena('ClaveSegura123')).toBe(2)
  })

  it('marca fuerte con doce caracteres, números y símbolo', () => {
    expect(medirFuerzaContrasena('ClaveSegura123!')).toBe(3)
  })

  it('no sube de nivel sin variedad de caracteres', () => {
    expect(medirFuerzaContrasena('aaaaaaaaaaaa')).toBe(1)
  })

  it('tiene una etiqueta para cada nivel', () => {
    expect(ETIQUETAS_FUERZA).toHaveLength(4)
  })
})
