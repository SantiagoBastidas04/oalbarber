# Dominio (entidades y enums) — módulo `servicio`

Paquete: `com.oalbarber.api.modules.servicio.domain`.

## Contexto del módulo

Catálogo único de servicios para todo el negocio (nombre, descripción, duración en minutos, precio y nombre del archivo de imagen estático). Servicios iniciales: corte normal 40 min, premium 90 min, mechas 180 min y ondulación 180 min. El precio es igual para ambos barberos.

## Qué debe ir en esta carpeta

- El modelo persistente del módulo `servicio`: `Servicio`.
- Mapeo JPA explícito con `@Entity`, `@Table(name = "...")`, `@Column(name = "...")`, claves foráneas y restricciones únicas.
- Precio como `BigDecimal` y duración como entero de minutos (columnas `precio` y `duracion_min`).
- `imagen` es un `String` con el nombre del archivo; no se guarda la ruta absoluta ni el binario.
- Constructores protegidos vacíos para JPA y constructores de negocio cuando aporten claridad; `equals` y `hashCode` basados en el identificador.

## Qué NO debe ir en esta carpeta

- DTOs, controladores, servicios, repositorios ni mappers.
- Anotaciones de validación de entrada (`@NotBlank`, `@Size`): esas van en los DTOs.
- `@Data` de Lombok en entidades: genera `equals`/`hashCode`/`toString` peligrosos con relaciones perezosas.
- Lógica de presentación, formato de fechas para la web o textos de interfaz.
- Reglas de negocio complejas: si una operación cruza varias entidades, vive en el service.

## Convenciones de nombres

- Paquete `com.oalbarber.api.modules.servicio.domain`.
- Clases en PascalCase, en singular y en español sin tildes: `Servicio`.
- Campos en camelCase; columnas en snake_case mediante `@Column`.
- Tabla en snake_case y singular: `servicio`.
- Sin sufijos `Entity` ni `Model`: el nombre del concepto es suficiente (`Cita`, no `CitaEntity`).

## Detalle de este módulo

- Entidad `Servicio` → tabla `servicio`.
- Campos: `id`, `nombre`, `descripcion`, `duracionMin` (columna `duracion_min`), `precio`
  (`numeric(10,2)`), `imagen`, `activo`.
- `imagen` es solo el nombre de un archivo estático del frontend (por ejemplo
  `corte-normal.jpg`); no es una URL ni un binario.
- No tiene enums ni fechas propias: la duración y el precio que importan a una cita se copian al
  reservar.
- Servicios iniciales del SRS: corte normal 40 min, premium 90 min, mechas 180 min y ondulación
  180 min; se cargan en una migración.

## Referencias

- Requisitos relacionados del SRS: RF-07, RF-08, RN-03, RN-04, RN-16.
- Relación con otros módulos: `cita` copia de aquí la duración y el precio vigentes al reservar. La imagen es solo el nombre de un archivo que vive en el frontend (`public/servicios/`); este módulo no sube ni sirve imágenes.
