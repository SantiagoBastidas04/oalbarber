/**
 * Normaliza un celular colombiano al formato internacional.
 *
 * Acepta 10 dígitos que empiezan por 3, con o sin el prefijo +57 y con
 * espacios, guiones o paréntesis. Devuelve `null` si no es válido.
 */
export function normalizarCelular(valor: string): string | null {
  const limpio = valor.replace(/[\s()-]/g, '')
  const sinPrefijo = limpio.startsWith('+57') ? limpio.slice(3) : limpio

  if (!/^3\d{9}$/.test(sinPrefijo)) {
    return null
  }

  return `+57${sinPrefijo}`
}
