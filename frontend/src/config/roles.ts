import type { Rol } from '../types/auth'

/** Roles del SRS, en el orden usado por los filtros del panel. */
export const ROLES: readonly Rol[] = ['ADMIN', 'BARBERO', 'CLIENTE']

/** Nombres visibles de cada rol. */
export const ETIQUETAS_ROL: Record<Rol, string> = {
  ADMIN: 'Administrador',
  BARBERO: 'Barbero',
  CLIENTE: 'Cliente',
}
