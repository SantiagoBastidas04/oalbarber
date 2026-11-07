# Módulo `barbero` — Barbero

Paquete raíz: `com.oalbarber.api.modules.barbero`.

## Responsabilidad

Perfil del barbero. La barbería tiene exactamente dos: el administrador, que también corta, y un barbero contratado. El perfil enlaza un usuario con rol BARBERO o ADMIN con su agenda.

## Subcarpetas

| Carpeta | Contenido |
|---|---|
| `controller/` | Controladores REST del módulo barbero. |
| `service/` | Servicios (lógica de negocio) del módulo barbero. |
| `repository/` | Repositorios del módulo barbero. |
| `domain/` | Dominio (entidades y enums) del módulo barbero. |
| `dto/` | DTOs (entrada y salida) del módulo barbero. |
| `mapper/` | Mappers (entidad ↔ DTO) del módulo barbero. |

## Qué NO debe ir en este módulo

- Entidades o tablas que pertenezcan a otro módulo: se accede por el service del módulo dueño.
- Controladores de otros dominios, utilidades genéricas (van en `common/`) ni configuración transversal (va en `config/`).
- Lógica de disponibilidad duplicada: el cálculo vive en `cita` y todos los canales lo reutilizan.
- Reglas de negocio dentro de los controladores, los mappers o las entidades.
- Dependencias directas del módulo `cita` hacia controladores de `auth` o `usuario`.

## Convenciones

- Paquete `com.oalbarber.api.modules.barbero` y subpaquetes por capa.
- Clases en PascalCase con el dominio en español sin tildes (`BarberoController`, `BarberoService`).
- DTOs con sufijo `Request`/`Response`; entidades sin sufijo.
- Rutas bajo `/api/barberos`.
- Cada decisión relevante se documenta en `docs/decisiones/` y se enlaza desde el Pull Request.

## Relación con otros módulos

Depende de `usuario` para el nombre y las credenciales; `horario` y `cita` referencian al barbero por su identificador. No crea usuarios ni contraseñas.

## Requisitos del SRS

RF-10, RN-01, RN-12
