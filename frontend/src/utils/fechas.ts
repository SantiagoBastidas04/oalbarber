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
export function formatearHora(iso: string): string {  return new Intl.DateTimeFormat(IDIOMA, {
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

/**
 * Fecha sin hora (YYYY-MM-DD) formateada sin correr el día por la zona horaria.
 * Se interpreta al mediodía UTC para que ningún huso cambie la fecha.
 */
export function formatearFechaLocal(fecha: string): string {
  const [anio, mes, dia] = fecha.split('-').map(Number)
  const fechaUtc = new Date(Date.UTC(anio, mes - 1, dia, 12))
  return new Intl.DateTimeFormat(IDIOMA, {
    weekday: 'long',
    day: 'numeric',
    month: 'long',
    year: 'numeric',
    timeZone: 'UTC',
  }).format(fechaUtc)
}
