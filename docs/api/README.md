# Documentación de la API

La documentación interactiva de la API está disponible en Swagger UI en `/swagger-ui.html`.
Cada operación indica su método, ruta, parámetros, formato de entrada y respuesta, seguridad,
roles permitidos y errores conocidos.

## Convenciones

- Prefijo `/api`, recursos en plural y en español, verbos HTTP estándar y códigos coherentes.
- Los ejemplos usan datos ficticios y fechas ISO-8601 con offset de Bogotá, por ejemplo
  `2026-11-10T10:00:00-05:00`.
- Los errores usan `ProblemDetail` (RFC 7807), con detalles de validación y estados HTTP
  apropiados.
- El acceso usa `Authorization: Bearer <token>`; el refresh token se envía en cookie `HttpOnly`
  según RNF-02.
- La documentación se mantiene junto con el código de cada endpoint.

## Perfiles de barbero

La API expone consulta pública de perfiles activos, listado administrativo de todos los perfiles,
alta y cambio de estado. La lista pública no incluye datos personales de la cuenta; las operaciones
administrativas requieren rol `ADMINISTRADOR`. El cambio de estado conserva activa la cuenta y sus
sesiones. La asignación de servicios y la agenda no están disponibles actualmente.
