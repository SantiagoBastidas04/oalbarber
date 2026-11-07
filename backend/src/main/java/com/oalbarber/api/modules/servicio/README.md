# Módulo `servicio` — Servicio

Paquete raíz: `com.oalbarber.api.modules.servicio`.

## Responsabilidad

Catálogo único de servicios para todo el negocio (nombre, descripción, duración en minutos, precio y nombre del archivo de imagen estático). Servicios iniciales: corte normal 40 min, premium 90 min, mechas 180 min y ondulación 180 min. El precio es igual para ambos barberos.

## Subcarpetas

| Carpeta | Contenido |
|---|---|
| `controller/` | Controladores REST del módulo servicio. |
| `service/` | Servicios (lógica de negocio) del módulo servicio. |
| `repository/` | Repositorios del módulo servicio. |
| `domain/` | Dominio (entidades y enums) del módulo servicio. |
| `dto/` | DTOs (entrada y salida) del módulo servicio. |
| `mapper/` | Mappers (entidad ↔ DTO) del módulo servicio. |

## Qué NO debe ir en este módulo

- Entidades o tablas que pertenezcan a otro módulo: se accede por el service del módulo dueño.
- Controladores de otros dominios, utilidades genéricas (van en `common/`) ni configuración transversal (va en `config/`).
- Lógica de disponibilidad duplicada: el cálculo vive en `cita` y todos los canales lo reutilizan.
- Reglas de negocio dentro de los controladores, los mappers o las entidades.
- Dependencias directas del módulo `cita` hacia controladores de `auth` o `usuario`.

## Convenciones

- Paquete `com.oalbarber.api.modules.servicio` y subpaquetes por capa.
- Clases en PascalCase con el dominio en español sin tildes (`ServicioController`, `ServicioService`).
- DTOs con sufijo `Request`/`Response`; entidades sin sufijo.
- Rutas bajo `/api/servicios`.
- Cada decisión relevante se documenta en `docs/decisiones/` y se enlaza desde el Pull Request.

## Relación con otros módulos

`cita` copia de aquí la duración y el precio vigentes al reservar. La imagen es solo el nombre de un archivo que vive en el frontend (`public/servicios/`); este módulo no sube ni sirve imágenes.

## Requisitos del SRS

RF-07, RF-08, RN-03, RN-04, RN-16
