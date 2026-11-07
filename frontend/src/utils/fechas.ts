const ZONA_HORARIA = 'America/Bogota'
const IDIOMA = 'es-CO'

/** Fecha larga en la zona de Bogotá: "martes, 10 de noviembre de 2026". */
export function formatearFecha(iso: string): string {
  return new Intl.DateTimeFormat(IDIOMA, {
    weekday: 'long',
    day: 'numeric',
    month: 'long',
    year: 'numeric',
    timeZone: ZONA_HORARIA,
  }).format(new Date(iso))
}

/** Hora en formato de 12 horas: "10:00 a. m.". */
export function formatearHora(iso: string): string {
  return new Intl.DateTimeFormat(IDIOMA, {
    hour: 'numeric',
    minute: '2-digit',
    hour12: true,
    timeZone: ZONA_HORARIA,
  }).format(new Date(iso))
}

/** Fecha y hora juntas, separadas por coma. */
export function formatearFechaHora(iso: string): string {
  return `${formatearFecha(iso)}, ${formatearHora(iso)}`
}
