# Seguridad y JWT — módulo `auth`

Paquete: `com.oalbarber.api.modules.auth.security`.

## Contexto del módulo

Registro de clientes (nombre, celular y contraseña), inicio de sesión con celular y contraseña, emisión y renovación de JWT (token de acceso corto y refresh token revocable), roles ADMIN, BARBERO y CLIENTE, y restablecimiento de contraseña por el administrador.

## Qué debe ir en esta carpeta

- El filtro que lee el token de acceso del encabezado `Authorization: Bearer ...` y carga la identidad en el `SecurityContext` (`JwtFiltro`).
- Las utilidades de firma, expiración y validación del token (`JwtUtil`), con el secreto y los tiempos leídos de variables de entorno.
- La representación del usuario autenticado (`UsuarioPrincipal`) y el servicio que lo carga desde el módulo `usuario`.
- La gestión del refresh token revocable: se guarda su hash en la tabla `refresh_token`, se renueva el acceso y se revoca al cerrar sesión.
- La conexión con `config/SecurityConfig`: hoy ese archivo solo permite `/actuator/health/**` y exige autenticación en el resto; el filtro JWT se registra aquí cuando se implemente el módulo.

## Qué NO debe ir en esta carpeta

- Contraseñas en claro, secretos escritos en el código o tokens impresos en logs.
- Reglas de negocio de citas, horarios o usuarios.
- Controladores REST ni DTOs de la API.
- `SecurityConfig` completo duplicado: la configuración transversal vive en `config/`.
- Listas de permisos hardcodeadas por usuario: los permisos se derivan del rol y de la regla del SRS.

## Convenciones de nombres

- Paquete `com.oalbarber.api.modules.auth.security`.
- Clases en PascalCase: `JwtFiltro`, `JwtUtil`, `UsuarioPrincipal`, `RefreshTokenService`.
- Nada de `@Autowired` en campos; inyección por constructor.
- Los claims usan nombres estándar (`sub`, `roles`, `exp`) y el rol se expone como autoridad `ROLE_...`.
- El refresh token se guarda con hash (BCrypt o SHA-256 con sal) y nunca en texto plano.

## Referencias

- Requisitos relacionados del SRS: RF-01 a RF-06, RN-15, RNF-01 a RNF-05.
- Relación con otros módulos: Usa el módulo `usuario` para leer y crear usuarios; no implementa su propio repositorio. Publica el filtro JWT que consumen todos los módulos protegidos.
