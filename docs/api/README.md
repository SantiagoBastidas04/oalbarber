# Documentación de la API

## Qué va aquí

- El contrato OpenAPI 3 de la API (`openapi.yaml`), que se mantiene junto al código y se publica
  en Swagger UI (`/swagger-ui/index.html`) y en `/v3/api-docs` cuando se agregue springdoc.
- Las convenciones de la API: prefijo `/api`, recursos en plural y en español (`/api/citas`,
  `/api/servicios`), verbos HTTP estándar y códigos de estado coherentes.
- Ejemplos de petición y respuesta para los flujos principales: registro, inicio de sesión,
  consulta de disponibilidad, creación y cancelación de citas.
- El formato uniforme de errores `ProblemDetail` (RFC 7807) con ejemplos de validación, 401, 403,
  404 y 409.
- La autenticación: `Authorization: Bearer <token>` para el acceso y refresh token en cookie
  `HttpOnly` según el SRS (RNF-02).

## Qué NO va aquí

- Controladores, DTOs o cualquier código Java.
- Documentación de endpoints que no existen: primero el endpoint y su requisito en el SRS.
- Contraseñas, tokens reales o datos personales en los ejemplos: se usan valores ficticios.
- Reglas de negocio completas: se referencian con su identificador (`RN-xx`) y se explican en el
  SRS.

## Convenciones

- OpenAPI 3.1 en YAML, con `operationId` en camelCase y etiquetas por módulo.
- Cada operación indica seguridad (`bearerAuth` o pública), roles permitidos y errores posibles.
- Los ejemplos de fechas usan ISO-8601 con offset de Bogotá:
  `2026-11-10T10:00:00-05:00`.
- Un cambio incompatible se documenta y se versiona; no se rompe el contrato sin avisar al
  frontend.
- El contrato se actualiza en el mismo Pull Request que el endpoint.
