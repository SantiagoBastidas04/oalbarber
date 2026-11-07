# Módulo `cita` — Cita

Paquete raíz: `com.oalbarber.api.modules.cita`.

## Responsabilidad

Cálculo de disponibilidad, creación y cancelación de citas, estados (CONFIRMADA, CANCELADA, COMPLETADA, NO_ASISTIO) y campo `canal` (WEB, WHATSAPP, ADMIN). Cada cita guarda inicio y fin exactos, duración y precio vigentes al reservar. El módulo debe poder usarse desde cualquier canal sin depender de HTTP.

## Subcarpetas

| Carpeta | Contenido |
|---|---|
| `controller/` | Controladores REST del módulo cita. |
| `service/` | Servicios (lógica de negocio) del módulo cita. |
| `repository/` | Repositorios del módulo cita. |
| `domain/` | Dominio (entidades y enums) del módulo cita. |
| `dto/` | DTOs (entrada y salida) del módulo cita. |
| `mapper/` | Mappers (entidad ↔ DTO) del módulo cita. |

## Qué NO debe ir en este módulo

- Entidades o tablas que pertenezcan a otro módulo: se accede por el service del módulo dueño.
- Controladores de otros dominios, utilidades genéricas (van en `common/`) ni configuración transversal (va en `config/`).
- Lógica de disponibilidad duplicada: el cálculo vive en `cita` y todos los canales lo reutilizan.
- Reglas de negocio dentro de los controladores, los mappers o las entidades.
- Dependencias directas del módulo `cita` hacia controladores de `auth` o `usuario`.

## Convenciones

- Paquete `com.oalbarber.api.modules.cita` y subpaquetes por capa.
- Clases en PascalCase con el dominio en español sin tildes (`CitaController y DisponibilidadController`, `CitaService y DisponibilidadService`).
- DTOs con sufijo `Request`/`Response`; entidades sin sufijo.
- Rutas bajo `/api/citas y /api/disponibilidad`.
- Cada decisión relevante se documenta en `docs/decisiones/` y se enlaza desde el Pull Request.

## Relación con otros módulos

Es el núcleo de la agenda. Usa `servicio` (duración y precio), `barbero` y `horario` (atención, almuerzo, bloqueos y parámetros). La web y, en fase 2, WhatsApp llaman a los mismos services; ningún canal accede a la base directamente ni duplica el cálculo.

## Requisitos del SRS

RN-05 a RN-13, RN-16, RN-17, RF-15 a RF-26, RNF-06, RNF-15
