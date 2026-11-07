# Dominio (entidades y enums) — módulo `cita`

Paquete: `com.oalbarber.api.modules.cita.domain`.

## Contexto del módulo

Cálculo de disponibilidad, creación y cancelación de citas, estados (CONFIRMADA, CANCELADA, COMPLETADA, NO_ASISTIO) y campo `canal` (WEB, WHATSAPP, ADMIN). Cada cita guarda inicio y fin exactos, duración y precio vigentes al reservar. El módulo debe poder usarse desde cualquier canal sin depender de HTTP.

## Qué debe ir en esta carpeta

- El modelo persistente del módulo `cita`: `Cita`, `EstadoCita`, `CanalCita`.
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

- Paquete `com.oalbarber.api.modules.cita.domain`.
- Clases en PascalCase, en singular y en español sin tildes: `Cita`, `EstadoCita`, `CanalCita`.
- Campos en camelCase; columnas en snake_case mediante `@Column`.
- Tabla en snake_case y singular: `cita`.
- Sin sufijos `Entity` ni `Model`: el nombre del concepto es suficiente (`Cita`, no `CitaEntity`).

## Detalle de este módulo

- `Cita` → tabla `cita`: `id`, `clienteId`, `barberoId`, `servicioId`, `inicio`, `fin`
  (`timestamptz`), `duracionMin`, `precio`, `estado`, `canal`, `creadaEn`.
- `EstadoCita`: `CONFIRMADA`, `CANCELADA`, `COMPLETADA`, `NO_ASISTIO`.
- `CanalCita`: `WEB`, `WHATSAPP`, `ADMIN`.
- `duracionMin` y `precio` son copias de los valores vigentes del servicio al reservar; no se
  recalculan al leer.
- La restricción de exclusión de PostgreSQL (tabla `cita`, rango de `inicio`/`fin` por barbero,
  ignorando `CANCELADA`) se crea en una migración Flyway, no con anotaciones JPA.

## Referencias

- Requisitos relacionados del SRS: RN-05 a RN-13, RN-16, RN-17, RF-15 a RF-26, RNF-06, RNF-15.
- Relación con otros módulos: Es el núcleo de la agenda. Usa `servicio` (duración y precio), `barbero` y `horario` (atención, almuerzo, bloqueos y parámetros). La web y, en fase 2, WhatsApp llaman a los mismos services; ningún canal accede a la base directamente ni duplica el cálculo.
