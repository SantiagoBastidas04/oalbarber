# Controladores REST — módulo `cita`

Paquete: `com.oalbarber.api.modules.cita.controller`.

## Contexto del módulo

Cálculo de disponibilidad, creación y cancelación de citas, estados (CONFIRMADA, CANCELADA, COMPLETADA, NO_ASISTIO) y campo `canal` (WEB, WHATSAPP, ADMIN). Cada cita guarda inicio y fin exactos, duración y precio vigentes al reservar. El módulo debe poder usarse desde cualquier canal sin depender de HTTP.

## Qué debe ir en esta carpeta

- Clases anotadas con `@RestController` que exponen la API HTTP del módulo bajo `/api/citas y /api/disponibilidad`. Son la única puerta de entrada desde la web y, en fase 2, desde el adaptador de WhatsApp.
- `CitaController y DisponibilidadController` como punto de entrada principal. Si el módulo crece, un controlador por recurso, siempre con el sufijo `Controller`.
- Métodos que reciben DTOs de entrada con `@Valid`, delegan en `CitaService y DisponibilidadService` y devuelven DTOs de salida o `ResponseEntity` con el código HTTP correcto.
- Autorización por rol con `@PreAuthorize` (`hasRole('ADMIN')`, `hasAnyRole('ADMIN','BARBERO')`, etc.). El controlador decide quién puede llamar; el service no conoce roles HTTP.
- Documentación OpenAPI de cada endpoint con `@Operation` y `@ApiResponse`, incluidos los errores `ProblemDetail`.

## Qué NO debe ir en esta carpeta

- Entidades JPA como parámetro o retorno: los controladores solo hablan DTOs.
- Reglas de negocio: cálculo de disponibilidad, validación de anticipación, copia de precios o duración, hashing de contraseñas.
- Acceso directo a repositorios o a `EntityManager`.
- `try/catch` que oculten excepciones: el manejo global vive en `common/exception/`.
- Lógica exclusiva de WhatsApp o de cualquier otro canal: la API solo adapta la petición al service.

## Convenciones de nombres

- Paquete `com.oalbarber.api.modules.cita.controller`.
- Clases en PascalCase terminadas en `Controller`; el nombre del recurso va en español y sin tildes: `CitaController y DisponibilidadController`.
- Rutas en plural, minúsculas, sin tildes y sin verbos: `/api/citas y /api/disponibilidad`. Los verbos HTTP expresan la acción.
- Métodos en camelCase y en español: `listar`, `obtener`, `crear`, `cancelar`.
- No se expone el identificador interno de la base si el SRS no lo requiere; se usan los DTOs del módulo.

## Referencias

- Requisitos relacionados del SRS: RN-05 a RN-13, RN-16, RN-17, RF-15 a RF-26, RNF-06, RNF-15.
- Relación con otros módulos: Es el núcleo de la agenda. Usa `servicio` (duración y precio), `barbero` y `horario` (atención, almuerzo, bloqueos y parámetros). La web y, en fase 2, WhatsApp llaman a los mismos services; ningún canal accede a la base directamente ni duplica el cálculo.
