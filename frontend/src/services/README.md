# Servicios (`services`)

Capa de comunicación con la API. Es la única que conoce `axios` y las rutas del backend.

## Qué debe ir aquí

- `tokenStore.ts`: token de acceso en una variable de módulo (memoria), con
  `obtenerToken`, `guardarToken` y `limpiarToken`.
- `apiClient.ts`: instancia de `axios` con `baseURL` de `config/env.ts`, `withCredentials: true`,
  cabecera `Authorization: Bearer` y normalización de errores al tipo `ApiError`
  (`ProblemDetail` o error de red).
- `authService.ts`: `registrar`, `iniciarSesion`, `renovarSesion` y `cerrarSesion`; guarda o
  limpia el token según corresponda.

## Comportamiento ante un 401

1. Si la petición **no** es de `/auth/*` y no se reintentó antes, se llama una sola vez a
   `POST /api/auth/refresh`. Las peticiones simultáneas comparten la misma promesa.
2. Se reintenta la petición original con el token nuevo.
3. Si la renovación falla, se limpia el token y se redirige a `/ingresar?origen=...`.

## Qué NO debe ir aquí

- Reglas de negocio: disponibilidad, precios, anticipación o cancelación viven en el backend.
- Tokens en `localStorage` o `sessionStorage`, ni logs con tokens o contraseñas.
- URLs absolutas del backend: siempre relativas a `env.apiBaseUrl`, para que el proxy funcione.
- `try/catch` que devuelvan datos falsos: los errores se propagan como `ApiError`.
- Un servicio por pantalla: un archivo por recurso de la API (`citasService`, `serviciosService`).

## Convenciones

- Funciones `async` en camelCase y en español (`iniciarSesion`, `cancelarCita`).
- Los tipos de entrada y salida salen de `types/`; no se usa `any`.
- Cada función nueva se documenta con el RF o la regla que cubre.
