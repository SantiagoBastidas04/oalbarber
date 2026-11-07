# Módulo `usuario` — Usuario

Paquete raíz: `com.oalbarber.api.modules.usuario`.

## Responsabilidad

Datos de las personas que usan el sistema: nombre, celular único en formato internacional (`+57...`), contraseña con hash BCrypt, rol y estado activo. Es la base de identidad que consumen auth, barbero y cita.

## Subcarpetas

| Carpeta | Contenido |
|---|---|
| `controller/` | Controladores REST del módulo usuario. |
| `service/` | Servicios (lógica de negocio) del módulo usuario. |
| `repository/` | Repositorios del módulo usuario. |
| `domain/` | Dominio (entidades y enums) del módulo usuario. |
| `dto/` | DTOs (entrada y salida) del módulo usuario. |
| `mapper/` | Mappers (entidad ↔ DTO) del módulo usuario. |

## Qué NO debe ir en este módulo

- Entidades o tablas que pertenezcan a otro módulo: se accede por el service del módulo dueño.
- Controladores de otros dominios, utilidades genéricas (van en `common/`) ni configuración transversal (va en `config/`).
- Lógica de disponibilidad duplicada: el cálculo vive en `cita` y todos los canales lo reutilizan.
- Reglas de negocio dentro de los controladores, los mappers o las entidades.
- Dependencias directas del módulo `cita` hacia controladores de `auth` o `usuario`.

## Convenciones

- Paquete `com.oalbarber.api.modules.usuario` y subpaquetes por capa.
- Clases en PascalCase con el dominio en español sin tildes (`UsuarioController`, `UsuarioService`).
- DTOs con sufijo `Request`/`Response`; entidades sin sufijo.
- Rutas bajo `/api/usuarios`.
- Cada decisión relevante se documenta en `docs/decisiones/` y se enlaza desde el Pull Request.

## Relación con otros módulos

Es la tabla base de identidad. `barbero` referencia a un usuario con rol BARBERO o ADMIN; `cita` referencia al usuario con rol CLIENTE. Ningún módulo debe leer `password_hash` fuera de `auth`.

## Requisitos del SRS

RN-15, RF-01, RF-27, RNF-01, RNF-10
