# DTOs (entrada y salida) — módulo `cita`

Paquete: `com.oalbarber.api.modules.cita.dto`.

## Contexto del módulo

Cálculo de disponibilidad, creación y cancelación de citas, estados (CONFIRMADA, CANCELADA, COMPLETADA, NO_ASISTIO) y campo `canal` (WEB, WHATSAPP, ADMIN). Cada cita guarda inicio y fin exactos, duración y precio vigentes al reservar. El módulo debe poder usarse desde cualquier canal sin depender de HTTP.

## Qué debe ir en esta carpeta

- Los objetos de transferencia del módulo `cita`: `CrearCitaRequest`, `CancelarCitaRequest`, `CitaResponse`, `DisponibilidadResponse`.
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

- Paquete `com.oalbarber.api.modules.cita.dto`.
- Sufijos `Request` para entradas y `Response` para salidas: `CrearCitaRequest`, `CancelarCitaRequest`, `CitaResponse`, `DisponibilidadResponse`.
- Nombres en español sin tildes; los términos técnicos estándar (`Request`, `Response`, `DTO`) se mantienen en inglés.
- Campos en camelCase; el JSON conserva camelCase (no se traduce a snake_case).
- La validación de formato y obligatoriedad vive aquí; la validación contra la base, en el service.

## Referencias

- Requisitos relacionados del SRS: RN-05 a RN-13, RN-16, RN-17, RF-15 a RF-26, RNF-06, RNF-15.
- Relación con otros módulos: Es el núcleo de la agenda. Usa `servicio` (duración y precio), `barbero` y `horario` (atención, almuerzo, bloqueos y parámetros). La web y, en fase 2, WhatsApp llaman a los mismos services; ningún canal accede a la base directamente ni duplica el cálculo.
