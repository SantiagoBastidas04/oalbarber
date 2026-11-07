# Controladores REST — módulo `auth`

Paquete: `com.oalbarber.api.modules.auth.controller`.

## Contexto del módulo

Registro de clientes (nombre, celular y contraseña), inicio de sesión con celular y contraseña, emisión y renovación de JWT (token de acceso corto y refresh token revocable), roles ADMIN, BARBERO y CLIENTE, y restablecimiento de contraseña por el administrador.

## Qué debe ir en esta carpeta

- Clases anotadas con `@RestController` que exponen la API HTTP del módulo bajo `/api/auth`. Son la única puerta de entrada desde la web y, en fase 2, desde el adaptador de WhatsApp.
- `AutenticacionController` como punto de entrada principal. Si el módulo crece, un controlador por recurso, siempre con el sufijo `Controller`.
- Métodos que reciben DTOs de entrada con `@Valid`, delegan en `AutenticacionService` y devuelven DTOs de salida o `ResponseEntity` con el código HTTP correcto.
- Autorización por rol con `@PreAuthorize` (`hasRole('ADMIN')`, `hasAnyRole('ADMIN','BARBERO')`, etc.). El controlador decide quién puede llamar; el service no conoce roles HTTP.
- Documentación OpenAPI de cada endpoint con `@Operation` y `@ApiResponse`, incluidos los errores `ProblemDetail`.

## Qué NO debe ir en esta carpeta

- Entidades JPA como parámetro o retorno: los controladores solo hablan DTOs.
- Reglas de negocio: cálculo de disponibilidad, validación de anticipación, copia de precios o duración, hashing de contraseñas.
- Acceso directo a repositorios o a `EntityManager`.
- `try/catch` que oculten excepciones: el manejo global vive en `common/exception/`.
- Lógica exclusiva de WhatsApp o de cualquier otro canal: la API solo adapta la petición al service.

## Convenciones de nombres

- Paquete `com.oalbarber.api.modules.auth.controller`.
- Clases en PascalCase terminadas en `Controller`; el nombre del recurso va en español y sin tildes: `AutenticacionController`.
- Rutas en minúsculas, sin tildes y sin verbos: `/api/auth`. Los verbos HTTP expresan la acción.
- Métodos en camelCase y en español: `listar`, `obtener`, `crear`, `cancelar`.
- No se expone el identificador interno de la base si el SRS no lo requiere; se usan los DTOs del módulo.

## Referencias

- Requisitos relacionados del SRS: RF-01 a RF-06, RN-15, RNF-01 a RNF-05.
- Relación con otros módulos: Usa el módulo `usuario` para leer y crear usuarios; no implementa su propio repositorio. Publica el filtro JWT que consumen todos los módulos protegidos.
