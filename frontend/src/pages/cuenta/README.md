# Cuenta (`pages/cuenta`)

Páginas públicas de registro e ingreso.

## Qué implementa

- **RF-01** (`/registro`): formulario real de nombre, celular con prefijo de país, contraseña y
  aceptación de la política de tratamiento de datos (RNF-10). Envía `POST /api/usuarios/registro`
  y muestra el usuario creado sin iniciar sesión.
- **RF-02** (`/ingresar`): inicio de sesión con celular y contraseña. Pendiente del módulo `auth`
  del backend.
- **RF-04**: cierre de sesión, que revoca el refresh token. Pendiente del módulo `auth`.

## Cómo está armado el registro

- `react-hook-form` + `zod` para validación; los mensajes se muestran por campo.
- El celular se compone con `utils/celularPais.ts`: el usuario escribe solo los dígitos y elige el
  prefijo en `SelectorPais` (bandera + código, Colombia `+57` por defecto).
- Los campos cambian de fondo, borde y sombra al enfocar y cuando ya tienen un valor escrito; los
  campos válidos muestran un check y el encabezado de la tarjeta lleva una barra de avance.
- La contraseña muestra un medidor de seguridad en vivo (`utils/contrasena.ts`) en una sola fila
  compacta; es orientativo y no bloquea el registro, porque el backend solo exige 8 caracteres.
- En escritorio (`lg`) la sección ocupa el alto restante de la pantalla, de modo que la imagen y el
  formulario completo se ven sin desplazarse, y el pie con los datos de la barbería queda justo
  debajo. Con poca altura (`corto`, ver `styles/README.md`) los espacios se reducen; si aun así no
  entra, la página crece de forma natural. En móvil el formulario se desplaza normalmente.
- La llamada vive en `services/usuarioService.ts`; los tipos, en `types/usuario.ts`.
- Un 400 del backend se reparte por campo con `ApiError.erroresCampo`; un 409 se muestra en el
  aviso general.

## Qué debe ir aquí

- Formularios con `react-hook-form` y esquemas `zod`; el celular se compone antes de enviarlo.
- Mensajes de error por campo y un mensaje general para el fallo de la petición.
- Las llamadas a `registrarUsuario`, `iniciarSesion` y `cerrarSesion` de los servicios.
- La redirección al origen guardado en `location.state.origen` tras ingresar.

## Qué NO debe ir aquí

- Reglas de complejidad de contraseña inventadas: se usan las que defina el backend y el SRS.
- Tokens en `localStorage` o `sessionStorage`.
- La recuperación de contraseña por el propio cliente: en la fase 1 la hace el administrador
  (RF-05).
- Validar el rol en el cliente: lo hace el backend en cada operación.
