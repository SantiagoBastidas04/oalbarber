/**
 * Rutas de la aplicación en un solo lugar. Se usan en los enlaces, en las
 * redirecciones y en `RutaProtegida`; nunca se escriben rutas a mano.
 */
export const RUTAS = {
  inicio: '/',
  servicios: '/servicios',
  reservar: '/reservar',
  registro: '/registro',
  ingresar: '/ingresar',
  misCitas: '/mis-citas',
  panelAgenda: '/panel/agenda',
  panelNuevaCita: '/panel/citas/nueva',
  panelServicios: '/panel/servicios',
  panelHorarios: '/panel/horarios',
  panelBarberos: '/panel/barberos',
  panelClientes: '/panel/clientes',
  noAutorizado: '/no-autorizado',
} as const
