/**
 * Token de acceso en memoria (RNF-02).
 *
 * No se usa localStorage ni sessionStorage: al recargar la página el token se
 * pierde y la sesión se restaura con el refresh token de la cookie HttpOnly.
 */
let tokenAcceso: string | null = null

export function obtenerToken(): string | null {
  return tokenAcceso
}

export function guardarToken(token: string): void {
  tokenAcceso = token
}

export function limpiarToken(): void {
  tokenAcceso = null
}
