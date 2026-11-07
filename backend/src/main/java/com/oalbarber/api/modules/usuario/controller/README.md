# Controladores REST — módulo `usuario`

Paquete: `com.oalbarber.api.modules.usuario.controller`.

## Contexto del módulo

Datos de las personas que usan el sistema: nombre, celular único en formato internacional (`+57...`), contraseña con hash BCrypt, rol y estado activo. Es la base de identidad que consumen auth, barbero y cita.

## Qué debe ir en esta carpeta

- Clases anotadas con `@RestController` que exponen la API HTTP del módulo bajo `/api/usuarios`. Son la única puerta de entrada desde la web y, en fase 2, desde el adaptador de WhatsApp.
- `UsuarioController` como punto de entrada principal. Si el módulo crece, un controlador por recurso, siempre con el sufijo `Controller`.
- Métodos que reciben DTOs de entrada con `@Valid`, delegan en `UsuarioService` y devuelven DTOs de salida o `ResponseEntity` con el código HTTP correcto.
- Autorización por rol con `@PreAuthorize` (`hasRole('ADMIN')`, `hasAnyRole('ADMIN','BARBERO')`, etc.). El controlador decide quién puede llamar; el service no conoce roles HTTP.
- Documentación OpenAPI de cada endpoint con `@Operation` y `@ApiResponse`, incluidos los errores `ProblemDetail`.

## Qué NO debe ir en esta carpeta

- Entidades JPA como parámetro o retorno: los controladores solo hablan DTOs.
- Reglas de negocio: cálculo de disponibilidad, validación de anticipación, copia de precios o duración, hashing de contraseñas.
- Acceso directo a repositorios o a `EntityManager`.
- `try/catch` que oculten excepciones: el manejo global vive en `common/exception/`.
- Lógica exclusiva de WhatsApp o de cualquier otro canal: la API solo adapta la petición al service.

## Convenciones de nombres

- Paquete `com.oalbarber.api.modules.usuario.controller`.
- Clases en PascalCase terminadas en `Controller`; el nombre del recurso va en español y sin tildes: `UsuarioController`.
- Rutas en plural, minúsculas, sin tildes y sin verbos: `/api/usuarios`. Los verbos HTTP expresan la acción.
- Métodos en camelCase y en español: `listar`, `obtener`, `crear`, `cancelar`.
- No se expone el identificador interno de la base si el SRS no lo requiere; se usan los DTOs del módulo.

## Referencias

- Requisitos relacionados del SRS: RN-15, RF-01, RF-27, RNF-01, RNF-10.
- Relación con otros módulos: Es la tabla base de identidad. `barbero` referencia a un usuario con rol BARBERO o ADMIN; `cita` referencia al usuario con rol CLIENTE. Ningún módulo debe leer `password_hash` fuera de `auth`.
