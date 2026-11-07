# DTOs (entrada y salida) — módulo `servicio`

Paquete: `com.oalbarber.api.modules.servicio.dto`.

## Contexto del módulo

Catálogo único de servicios para todo el negocio (nombre, descripción, duración en minutos, precio y nombre del archivo de imagen estático). Servicios iniciales: corte normal 40 min, premium 90 min, mechas 180 min y ondulación 180 min. El precio es igual para ambos barberos.

## Qué debe ir en esta carpeta

- Los objetos de transferencia del módulo `servicio`: `ServicioRequest`, `ServicioResponse`.
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

- Paquete `com.oalbarber.api.modules.servicio.dto`.
- Sufijos `Request` para entradas y `Response` para salidas: `ServicioRequest`, `ServicioResponse`.
- Nombres en español sin tildes; los términos técnicos estándar (`Request`, `Response`, `DTO`) se mantienen en inglés.
- Campos en camelCase; el JSON conserva camelCase (no se traduce a snake_case).
- La validación de formato y obligatoriedad vive aquí; la validación contra la base, en el service.

## Referencias

- Requisitos relacionados del SRS: RF-07, RF-08, RN-03, RN-04, RN-16.
- Relación con otros módulos: `cita` copia de aquí la duración y el precio vigentes al reservar. La imagen es solo el nombre de un archivo que vive en el frontend (`public/servicios/`); este módulo no sube ni sirve imágenes.
