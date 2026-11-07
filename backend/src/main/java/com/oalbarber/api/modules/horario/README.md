# Módulo `horario` — Horario

Paquete raíz: `com.oalbarber.api.modules.horario`.

## Responsabilidad

Horario de atención por barbero, almuerzo, bloqueos de franjas o fechas completas y parámetros configurables (anticipación mínima, límite de cancelación, máximo de días adelante, intervalo entre turnos y margen entre citas).

## Subcarpetas

| Carpeta | Contenido |
|---|---|
| `controller/` | Controladores REST del módulo horario. |
| `service/` | Servicios (lógica de negocio) del módulo horario. |
| `repository/` | Repositorios del módulo horario. |
| `domain/` | Dominio (entidades y enums) del módulo horario. |
| `dto/` | DTOs (entrada y salida) del módulo horario. |
| `mapper/` | Mappers (entidad ↔ DTO) del módulo horario. |

## Qué NO debe ir en este módulo

- Entidades o tablas que pertenezcan a otro módulo: se accede por el service del módulo dueño.
- Controladores de otros dominios, utilidades genéricas (van en `common/`) ni configuración transversal (va en `config/`).
- Lógica de disponibilidad duplicada: el cálculo vive en `cita` y todos los canales lo reutilizan.
- Reglas de negocio dentro de los controladores, los mappers o las entidades.
- Dependencias directas del módulo `cita` hacia controladores de `auth` o `usuario`.

## Convenciones

- Paquete `com.oalbarber.api.modules.horario` y subpaquetes por capa.
- Clases en PascalCase con el dominio en español sin tildes (`HorarioController`, `HorarioService`).
- DTOs con sufijo `Request`/`Response`; entidades sin sufijo.
- Rutas bajo `/api/horarios, /api/bloqueos y /api/configuracion`.
- Cada decisión relevante se documenta en `docs/decisiones/` y se enlaza desde el Pull Request.

## Relación con otros módulos

Aporta las reglas de tiempo que `cita` usa para calcular disponibilidad. La disponibilidad no se guarda aquí: se calcula al consultar. No cancela citas al crear un bloqueo; solo informa si hay citas afectadas para pedir confirmación.

## Requisitos del SRS

RN-01, RN-02, RN-12, RN-18, RF-11 a RF-14
