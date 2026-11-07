# DTOs comunes

Paquete `com.oalbarber.api.common.dto`.

## Qué debe ir en esta carpeta

- DTOs usados por dos o más módulos con el mismo significado: por ejemplo, una envoltura de
  paginación (`PaginaResponse<T>`) o un DTO con el identificador y el nombre visible de un usuario.
- Tipos de respuesta compartidos que no pertenezcan a un dominio concreto.

## Qué NO debe ir en esta carpeta

- DTOs de un solo módulo (`CitaResponse`, `ServicioRequest`): viven en
  `modules/<modulo>/dto/`.
- Entidades JPA, repositorios o lógica de negocio.
- Un DTO "comodín" con todos los campos posibles del sistema.
- DTOs de error: el formato de error es `ProblemDetail` y vive en `common/exception/`.

## Convenciones

- Sufijos `Request`, `Response` o `DTO`, en PascalCase.
- Inmutables cuando sea posible (`record`).
- Sin anotaciones de persistencia; sí Bean Validation y `@Schema` si el DTO se expone en la API.
- Todo campo debe tener un consumidor claro; no se copian campos "por si acaso".
