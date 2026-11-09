/** País disponible en el selector de prefijo del celular. */
export interface Pais {
  /** Código ISO de dos letras, usado como clave. */
  iso: string
  nombre: string
  /** Prefijo internacional sin el signo +. */
  codigo: string
  /** Cantidad de dígitos nacionales permitida (mínimo y máximo). */
  digitos: readonly [number, number]
}

/**
 * Países ofrecidos en el formulario. Colombia va primero porque es el mercado
 * principal; el resto permite atender clientes de otros países sin cambiar el
 * componente.
 */
export const PAISES: readonly Pais[] = [
  { iso: 'CO', nombre: 'Colombia', codigo: '57', digitos: [10, 10] },
  { iso: 'AR', nombre: 'Argentina', codigo: '54', digitos: [10, 10] },
  { iso: 'BO', nombre: 'Bolivia', codigo: '591', digitos: [8, 8] },
  { iso: 'BR', nombre: 'Brasil', codigo: '55', digitos: [10, 11] },
  { iso: 'CL', nombre: 'Chile', codigo: '56', digitos: [9, 9] },
  { iso: 'CR', nombre: 'Costa Rica', codigo: '506', digitos: [8, 8] },
  { iso: 'EC', nombre: 'Ecuador', codigo: '593', digitos: [9, 9] },
  { iso: 'ES', nombre: 'España', codigo: '34', digitos: [9, 9] },
  { iso: 'US', nombre: 'Estados Unidos', codigo: '1', digitos: [10, 10] },
  { iso: 'MX', nombre: 'México', codigo: '52', digitos: [10, 10] },
  { iso: 'PA', nombre: 'Panamá', codigo: '507', digitos: [8, 8] },
  { iso: 'PY', nombre: 'Paraguay', codigo: '595', digitos: [9, 9] },
  { iso: 'PE', nombre: 'Perú', codigo: '51', digitos: [9, 9] },
  { iso: 'UY', nombre: 'Uruguay', codigo: '598', digitos: [8, 8] },
  { iso: 'VE', nombre: 'Venezuela', codigo: '58', digitos: [10, 10] },
] as const

/** Busca un país por su prefijo internacional (sin el +). */
export function paisPorCodigo(codigo: string): Pais | undefined {
  return PAISES.find((pais) => pais.codigo === codigo)
}

/**
 * Une el prefijo elegido con los dígitos escritos y devuelve el celular en el
 * formato que valida el backend: `+<código><número>` con 8 a 15 dígitos.
 * Devuelve `null` si el número no corresponde al país.
 */
export function componerCelular(codigoPais: string, numero: string): string | null {
  const pais = paisPorCodigo(codigoPais)
  if (!pais) {
    return null
  }

  const digitos = numero.replace(/\D/g, '')
  const [minimo, maximo] = pais.digitos
  if (digitos.length < minimo || digitos.length > maximo) {
    return null
  }

  const total = pais.codigo.length + digitos.length
  if (total < 8 || total > 15) {
    return null
  }

  return `+${pais.codigo}${digitos}`
}
