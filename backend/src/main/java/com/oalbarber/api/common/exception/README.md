# Excepciones y manejo global de errores

Paquete `com.oalbarber.api.common.exception`.

## Qué debe ir en esta carpeta

- `GlobalExceptionHandler` con `@RestControllerAdvice`: convierte excepciones en respuestas JSON
  uniformes con el formato `ProblemDetail` (RFC 7807), incluyendo `type`, `title`, `status`,
  `detail` e `instance`.
- Excepciones propias del negocio, por ejemplo `RecursoNoEncontradoException`,
  `HorarioNoDisponibleException`, `ReglaNegocioException` y `ConflictoException`.
- El mapeo de errores de validación (`MethodArgumentNotValidException`) a `ProblemDetail` con la
  lista de campos inválidos.
- El mapeo de errores de la base de datos que representan reglas de negocio (por ejemplo, la
  restricción de exclusión que impide solapar citas) a `HorarioNoDisponibleException`.

## Qué NO debe ir en esta carpeta

- `try/catch` dentro de controladores o services para devolver `ResponseEntity` de error.
- Stack traces, nombres de tablas o mensajes internos de PostgreSQL en la respuesta al cliente.
- Excepciones para controlar el flujo normal (por ejemplo, usar una excepción como `if`).
- Excepciones para ignorarlas: nunca se captura una excepción para no hacer nada.

## Convenciones

- Clases PascalCase terminadas en `Exception` y en español sin tildes.
- Heredan de `RuntimeException` y llevan un mensaje apto para el usuario final en español.
- El código HTTP se decide en el handler global, no en el service.
- El `detail` no revela si un celular existe o no cuando eso permita enumerar cuentas.
