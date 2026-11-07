# Dominio (entidades y enums) — módulo `horario`

Paquete: `com.oalbarber.api.modules.horario.domain`.

## Contexto del módulo

Horario de atención por barbero, almuerzo, bloqueos de franjas o fechas completas y parámetros configurables (anticipación mínima, límite de cancelación, máximo de días adelante, intervalo entre turnos y margen entre citas).

## Qué debe ir en esta carpeta

- El modelo persistente del módulo `horario`: `HorarioAtencion`, `Bloqueo`, `Configuracion`.
- Mapeo JPA explícito con `@Entity`, `@Table(name = "...")`, `@Column(name = "...")`, claves foráneas y restricciones únicas.
- Enums de dominio con `@Enumerated(EnumType.STRING)` para que la base guarde nombres legibles.
- Fechas con zona: `OffsetDateTime` o `Instant` para columnas `timestamptz` (America/Bogota).
- Constructores protegidos vacíos para JPA y constructores de negocio cuando aporten claridad; `equals` y `hashCode` basados en el identificador.

## Qué NO debe ir en esta carpeta

- DTOs, controladores, servicios, repositorios ni mappers.
- Anotaciones de validación de entrada (`@NotBlank`, `@Size`): esas van en los DTOs.
- `@Data` de Lombok en entidades: genera `equals`/`hashCode`/`toString` peligrosos con relaciones perezosas.
- Lógica de presentación, formato de fechas para la web o textos de interfaz.
- Reglas de negocio complejas: si una operación cruza varias entidades, vive en el service.

## Convenciones de nombres

- Paquete `com.oalbarber.api.modules.horario.domain`.
- Clases en PascalCase, en singular y en español sin tildes: `HorarioAtencion`, `Bloqueo`, `Configuracion`.
- Campos en camelCase; columnas en snake_case mediante `@Column`.
- Tabla en snake_case y singular: `horario_atencion, bloqueo y configuracion`.
- Sin sufijos `Entity` ni `Model`: el nombre del concepto es suficiente (`Cita`, no `CitaEntity`).

## Detalle de este módulo

- `HorarioAtencion` → tabla `horario_atencion`: `id`, `barberoId`, `diaSemana`, `horaInicio`,
  `horaFin`, `inicioAlmuerzo`, `finAlmuerzo`. No existe fila para el domingo.
- `Bloqueo` → tabla `bloqueo`: `id`, `barberoId`, `inicio`, `fin` (`timestamptz`) y `motivo`.
  Una fecha completa se representa con un rango que cubre el día.
- `Configuracion` → tabla `configuracion`: `clave`, `valor`. Agrupa anticipación mínima, límite de
  cancelación, máximo de días, intervalo entre turnos y margen entre citas.
- El almuerzo es el único descanso y se modela dentro de `HorarioAtencion`.
- La disponibilidad no es una entidad: se calcula al consultar y no se guarda aquí.

## Referencias

- Requisitos relacionados del SRS: RN-01, RN-02, RN-12, RN-18, RF-11 a RF-14.
- Relación con otros módulos: Aporta las reglas de tiempo que `cita` usa para calcular disponibilidad. La disponibilidad no se guarda aquí: se calcula al consultar. No cancela citas al crear un bloqueo; solo informa si hay citas afectadas para pedir confirmación.
