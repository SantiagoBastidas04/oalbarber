# Controladores REST — módulo `servicio`

Paquete: `com.oalbarber.api.modules.servicio.controller`.

## Contexto del módulo

Catálogo único de servicios para todo el negocio (nombre, descripción, duración en minutos, precio y nombre del archivo de imagen estático). Servicios iniciales: corte normal 40 min, premium 90 min, mechas 180 min y ondulación 180 min. El precio es igual para ambos barberos.

## Qué debe ir en esta carpeta

- Clases anotadas con `@RestController` que exponen la API HTTP del módulo bajo `/api/servicios`. Son la única puerta de entrada desde la web y, en fase 2, desde el adaptador de WhatsApp.
- `ServicioController` como punto de entrada principal. Si el módulo crece, un controlador por recurso, siempre con el sufijo `Controller`.
- Métodos que reciben DTOs de entrada con `@Valid`, delegan en `ServicioService` y devuelven DTOs de salida o `ResponseEntity` con el código HTTP correcto.
- Autorización por rol con `@PreAuthorize` (`hasRole('ADMIN')`, `hasAnyRole('ADMIN','BARBERO')`, etc.). El controlador decide quién puede llamar; el service no conoce roles HTTP.
- Documentación OpenAPI de cada endpoint con `@Operation` y `@ApiResponse`, incluidos los errores `ProblemDetail`.

## Qué NO debe ir en esta carpeta

- Entidades JPA como parámetro o retorno: los controladores solo hablan DTOs.
- Reglas de negocio: cálculo de disponibilidad, validación de anticipación, copia de precios o duración, hashing de contraseñas.
- Acceso directo a repositorios o a `EntityManager`.
- `try/catch` que oculten excepciones: el manejo global vive en `common/exception/`.
- Lógica exclusiva de WhatsApp o de cualquier otro canal: la API solo adapta la petición al service.

## Convenciones de nombres

- Paquete `com.oalbarber.api.modules.servicio.controller`.
- Clases en PascalCase terminadas en `Controller`; el nombre del recurso va en español y sin tildes: `ServicioController`.
- Rutas en plural, minúsculas, sin tildes y sin verbos: `/api/servicios`. Los verbos HTTP expresan la acción.
- Métodos en camelCase y en español: `listar`, `obtener`, `crear`, `cancelar`.
- No se expone el identificador interno de la base si el SRS no lo requiere; se usan los DTOs del módulo.

## Referencias

- Requisitos relacionados del SRS: RF-07, RF-08, RN-03, RN-04, RN-16.
- Relación con otros módulos: `cita` copia de aquí la duración y el precio vigentes al reservar. La imagen es solo el nombre de un archivo que vive en el frontend (`public/servicios/`); este módulo no sube ni sirve imágenes.
