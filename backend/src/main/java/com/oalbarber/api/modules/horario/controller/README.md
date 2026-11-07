# Controladores REST — módulo `horario`

Paquete: `com.oalbarber.api.modules.horario.controller`.

## Contexto del módulo

Horario de atención por barbero, almuerzo, bloqueos de franjas o fechas completas y parámetros configurables (anticipación mínima, límite de cancelación, máximo de días adelante, intervalo entre turnos y margen entre citas).

## Qué debe ir en esta carpeta

- Clases anotadas con `@RestController` que exponen la API HTTP del módulo bajo `/api/horarios, /api/bloqueos y /api/configuracion`. Son la única puerta de entrada desde la web y, en fase 2, desde el adaptador de WhatsApp.
- `HorarioController` como punto de entrada principal. Si el módulo crece, un controlador por recurso, siempre con el sufijo `Controller`.
- Métodos que reciben DTOs de entrada con `@Valid`, delegan en `HorarioService` y devuelven DTOs de salida o `ResponseEntity` con el código HTTP correcto.
- Autorización por rol con `@PreAuthorize` (`hasRole('ADMIN')`, `hasAnyRole('ADMIN','BARBERO')`, etc.). El controlador decide quién puede llamar; el service no conoce roles HTTP.
- Documentación OpenAPI de cada endpoint con `@Operation` y `@ApiResponse`, incluidos los errores `ProblemDetail`.

## Qué NO debe ir en esta carpeta

- Entidades JPA como parámetro o retorno: los controladores solo hablan DTOs.
- Reglas de negocio: cálculo de disponibilidad, validación de anticipación, copia de precios o duración, hashing de contraseñas.
- Acceso directo a repositorios o a `EntityManager`.
- `try/catch` que oculten excepciones: el manejo global vive en `common/exception/`.
- Lógica exclusiva de WhatsApp o de cualquier otro canal: la API solo adapta la petición al service.

## Convenciones de nombres

- Paquete `com.oalbarber.api.modules.horario.controller`.
- Clases en PascalCase terminadas en `Controller`; el nombre del recurso va en español y sin tildes: `HorarioController`.
- Rutas en plural, minúsculas, sin tildes y sin verbos: `/api/horarios, /api/bloqueos y /api/configuracion`. Los verbos HTTP expresan la acción.
- Métodos en camelCase y en español: `listar`, `obtener`, `crear`, `cancelar`.
- No se expone el identificador interno de la base si el SRS no lo requiere; se usan los DTOs del módulo.

## Referencias

- Requisitos relacionados del SRS: RN-01, RN-02, RN-12, RN-18, RF-11 a RF-14.
- Relación con otros módulos: Aporta las reglas de tiempo que `cita` usa para calcular disponibilidad. La disponibilidad no se guarda aquí: se calcula al consultar. No cancela citas al crear un bloqueo; solo informa si hay citas afectadas para pedir confirmación.
