# Módulo `auth` — Autenticación

Paquete raíz: `com.oalbarber.api.modules.auth`.

## Responsabilidad

Registro de clientes (nombre, celular y contraseña), inicio de sesión con celular y contraseña, emisión y renovación de JWT (token de acceso corto y refresh token revocable), roles ADMIN, BARBERO y CLIENTE, y restablecimiento de contraseña por el administrador.

## Subcarpetas

| Carpeta | Contenido |
|---|---|
| `controller/` | Controladores REST del módulo auth. |
| `service/` | Servicios (lógica de negocio) del módulo auth. |
| `dto/` | DTOs (entrada y salida) del módulo auth. |
| `security/` | Seguridad y JWT del módulo auth. |

## Qué NO debe ir en este módulo

- Entidades o tablas que pertenezcan a otro módulo: se accede por el service del módulo dueño.
- Controladores de otros dominios, utilidades genéricas (van en `common/`) ni configuración transversal (va en `config/`).
- Lógica de disponibilidad duplicada: el cálculo vive en `cita` y todos los canales lo reutilizan.
- Reglas de negocio dentro de los controladores, los mappers o las entidades.
- Dependencias directas del módulo `cita` hacia controladores de `auth` o `usuario`.

## Convenciones

- Paquete `com.oalbarber.api.modules.auth` y subpaquetes por capa.
- Clases en PascalCase con el dominio en español sin tildes (`AutenticacionController`, `AutenticacionService`).
- DTOs con sufijo `Request`/`Response`; entidades sin sufijo.
- Rutas bajo `/api/auth`.
- Cada decisión relevante se documenta en `docs/decisiones/` y se enlaza desde el Pull Request.

## Relación con otros módulos

Usa el módulo `usuario` para leer y crear usuarios; no implementa su propio repositorio. Publica el filtro JWT que consumen todos los módulos protegidos.

## Requisitos del SRS

RF-01 a RF-06, RN-15, RNF-01 a RNF-05
