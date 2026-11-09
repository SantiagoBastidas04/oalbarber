/** Nivel de seguridad de la contraseña, de 0 a 3. */
export type FuerzaContrasena = 0 | 1 | 2 | 3

/**
 * Calcula la fuerza de la contraseña mientras se escribe.
 *
 * Es orientativa y nunca bloquea el registro: el backend solo exige 8
 * caracteres. Sirve para que el usuario vea cómo mejorar la suya.
 */
export function medirFuerzaContrasena(valor: string): FuerzaContrasena {
  if (valor.length < 8) {
    return 0
  }

  const conLetrasYNumeros = /[A-Za-z]/.test(valor) && /\d/.test(valor)
  const conSimbolo = /[^A-Za-z0-9]/.test(valor)

  if (valor.length >= 12 && conLetrasYNumeros && conSimbolo) {
    return 3
  }

  if (valor.length >= 10 && conLetrasYNumeros) {
    return 2
  }

  return 1
}

/** Texto visible para cada nivel de fuerza. */
export const ETIQUETAS_FUERZA = ['Aún no cumple', 'Básica', 'Buena', 'Fuerte'] as const
