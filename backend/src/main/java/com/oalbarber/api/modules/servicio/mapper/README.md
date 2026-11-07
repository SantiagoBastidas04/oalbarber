# Mappers (entidad ↔ DTO) — módulo `servicio`

Paquete: `com.oalbarber.api.modules.servicio.mapper`.

## Contexto del módulo

Catálogo único de servicios para todo el negocio (nombre, descripción, duración en minutos, precio y nombre del archivo de imagen estático). Servicios iniciales: corte normal 40 min, premium 90 min, mechas 180 min y ondulación 180 min. El precio es igual para ambos barberos.

## Qué debe ir en esta carpeta

- La conversión entre entidades del módulo `servicio` y sus DTOs: `ServicioMapper`.
- Métodos explícitos `aEntidad`, `aResponse`, `aListaResponse` y los que hagan falta, con correspondencia campo a campo.
- Conversión de enums, fechas y valores monetarios en un solo lugar, para que el formato sea consistente.
- Mapeo seguro frente a nulos y a relaciones no cargadas (solo se accede a lo que el service ya preparó).

## Qué NO debe ir en esta carpeta

- Consultas a repositorios, transacciones o acceso a la base.
- Reglas de negocio: no decide estados, disponibilidad, precios ni permisos.
- Serialización JSON manual ni anotaciones de controladores.
- Exposición de campos sensibles (`password_hash`) aunque existan en la entidad.
- Lógica de presentación específica del frontend (colores, textos de interfaz).

## Convenciones de nombres

- Paquete `com.oalbarber.api.modules.servicio.mapper`.
- Clase en PascalCase terminada en `Mapper`: `ServicioMapper`.
- Métodos en español: `aEntidad`, `aResponse`, `aListaResponse`.
- Implementación con métodos estáticos en una clase final o como `@Component`; no se mezclan ambos estilos en el mismo módulo.
- Si el mapeo se vuelve complejo, se justifica y se documenta, pero no se delega en la entidad.

## Referencias

- Requisitos relacionados del SRS: RF-07, RF-08, RN-03, RN-04, RN-16.
- Relación con otros módulos: `cita` copia de aquí la duración y el precio vigentes al reservar. La imagen es solo el nombre de un archivo que vive en el frontend (`public/servicios/`); este módulo no sube ni sirve imágenes.
