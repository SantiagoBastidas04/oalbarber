# DTOs (entrada y salida) — módulo `horario`

Paquete: `com.oalbarber.api.modules.horario.dto`.

## Contexto del módulo

Horario de atención por barbero, almuerzo, bloqueos de franjas o fechas completas y parámetros configurables (anticipación mínima, límite de cancelación, máximo de días adelante, intervalo entre turnos y margen entre citas).

## Qué debe ir en esta carpeta

- Los objetos de transferencia del módulo `horario`: `HorarioRequest`, `BloqueoRequest`, `ConfiguracionRequest`, `HorarioResponse`.
- DTOs de entrada con Bean Validation (`@NotBlank`, `@NotNull`, `@Positive`, `@Pattern` para el celular `+57...`) y documentación OpenAPI con `@Schema`.
- DTOs de salida que solo incluyen los campos que el cliente puede ver; nunca `password_hash`, tokens internos ni datos de otras personas.
- Registros (`record`) inmutables cuando no se necesite mutabilidad; clases solo si el framework lo exige.
- Representación de fechas en ISO-8601 con offset (`2026-11-05T10:00:00-05:00`) y precios como número decimal, no como texto.

## Qué NO debe ir en esta carpeta

- Entidades JPA ni anotaciones de persistencia (`@Entity`, `@Column`, `@Id`).
- Lógica de negocio, cálculos de disponibilidad o validaciones que dependan de la base.
- Contraseñas en claro en respuestas, hashes, secretos o rutas absolutas del servidor.
- Campos que el frontend no necesita solo porque existen en la entidad.
- DTOs compartidos entre módulos sin justificación: lo común va en `common/dto/`.

## Convenciones de nombres

- Paquete `com.oalbarber.api.modules.horario.dto`.
- Sufijos `Request` para entradas y `Response` para salidas: `HorarioRequest`, `BloqueoRequest`, `ConfiguracionRequest`, `HorarioResponse`.
- Nombres en español sin tildes; los términos técnicos estándar (`Request`, `Response`, `DTO`) se mantienen en inglés.
- Campos en camelCase; el JSON conserva camelCase (no se traduce a snake_case).
- La validación de formato y obligatoriedad vive aquí; la validación contra la base, en el service.

## Referencias

- Requisitos relacionados del SRS: RN-01, RN-02, RN-12, RN-18, RF-11 a RF-14.
- Relación con otros módulos: Aporta las reglas de tiempo que `cita` usa para calcular disponibilidad. La disponibilidad no se guarda aquí: se calcula al consultar. No cancela citas al crear un bloqueo; solo informa si hay citas afectadas para pedir confirmación.
