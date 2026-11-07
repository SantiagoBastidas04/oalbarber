# Cuenta (`pages/cuenta`)

Páginas públicas de registro e ingreso.

## Qué implementa

- **RF-01** (`/registro`): nombre, celular en formato internacional, contraseña y aceptación de la
  política de tratamiento de datos (RNF-10).
- **RF-02** (`/ingresar`): inicio de sesión con celular y contraseña.
- **RF-04**: cierre de sesión, que revoca el refresh token.

## Qué debe ir aquí

- Formularios con `react-hook-form` y esquemas `zod`; el celular se normaliza con
  `utils/celular.ts` antes de enviarlo.
- Mensajes de error por campo y un mensaje general para credenciales inválidas.
- Las llamadas a `registrar`, `iniciarSesion` y `cerrarSesion` del contexto de autenticación.
- La redirección al origen guardado en `location.state.origen` tras ingresar.

## Qué NO debe ir aquí

- Reglas de complejidad de contraseña inventadas: se usan las que defina el backend y el SRS.
- Tokens en `localStorage` o `sessionStorage`.
- La recuperación de contraseña por el propio cliente: en la fase 1 la hace el administrador
  (RF-05).
- Validar el rol en el cliente: lo hace el backend en cada operación.
