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

## Horario implementado (H-14)

| Método y ruta | Rol | Uso |
|---|---|---|
| `GET /api/horarios?barberoId={id}` | ADMIN, BARBERO propio | Días configurados; los ausentes están cerrados. Sin `barberoId`, el administrador ve todos. |
| `PUT /api/horarios/{barberoId}` | ADMIN | Reemplaza la semana completa; una lista vacía cierra la semana. |
| `GET /api/horarios/{barberoId}/excepciones` | ADMIN, BARBERO propio | Fechas especiales del perfil. |
| `PUT /api/horarios/{barberoId}/excepciones/{fecha}` | ADMIN | Crea o actualiza la fecha (`YYYY-MM-DD`). |
| `DELETE /api/horarios/{barberoId}/excepciones/{fecha}` | ADMIN | Quita la excepción; responde 204 aunque no exista. |

Reglas: una jornada continua por día entre 10:00 y 19:00 (RN-01), con almuerzo opcional dentro de
la jornada (RN-02). El domingo no se configura en la semana; una fecha especial puede abrirlo,
cerrarlo o cambiar sus horas. Los errores de validación responden 400 con `codigo` y el detalle en
`errors`, con una clave por día (`"1"`…`"7"`) o por campo (`"fecha"`, `"horario"`).

## Requisitos del SRS

RN-01, RN-02, RN-12, RN-18, RF-11 a RF-14
