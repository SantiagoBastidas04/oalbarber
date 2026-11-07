# Contexto (`context`)

## Qué debe ir aquí

- `AuthContext.tsx`: el estado de sesión (`usuario`, `rol`, `estaAutenticado`) y las acciones
  `registrar`, `iniciarSesion`, `renovarSesion`, `cerrarSesion` y `restaurarSesion`.
- El proveedor `AuthProvider`, que envuelve la aplicación en `main.tsx`.
- El contexto se exporta para que `hooks/useAuth.ts` lo consuma y para las pruebas de guardas.

## Qué NO debe ir aquí

- Tokens en `localStorage`, `sessionStorage` ni cookies legibles por JavaScript: el acceso vive
  en `services/tokenStore.ts` (memoria) y el refresh token en una cookie `HttpOnly` (RNF-02).
- Reglas de autorización por ruta: eso es `RutaProtegida`.
- Llamadas HTTP directas con `axios`: se usan las funciones de `services/authService.ts`.
- Estado de negocio (servicios, citas, disponibilidad): va en React Query dentro de cada página.
- Lógica de la fase 2.

## Convenciones

- La sesión inicial es **anónima**: no se llama al backend al arrancar.
- `// TODO: restaurar sesión con refresh cuando exista el módulo auth`: la función
  `restaurarSesion` está implementada pero no se invoca; hoy `/api/auth` no existe y el navegador
  mostraría el diálogo de autenticación básica.
- Los valores del contexto se memorizan con `useMemo` y las acciones con `useCallback`.
- Un contexto nuevo se documenta aquí y se consume mediante un hook en `hooks/`.
