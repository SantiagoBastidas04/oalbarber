# Controladores REST — módulo `barbero`

Paquete: `com.oalbarber.api.modules.barbero.controller`.

## Contexto del módulo

Perfil profesional enlazado a una cuenta con rol `BARBERO` o `ADMINISTRADOR`. El sistema no presupone una cantidad fija de perfiles.

## Qué debe ir en esta carpeta

- Clases anotadas con `@RestController` que exponen la API HTTP del módulo bajo `/api/barberos`. Son la única puerta de entrada desde la web y, en fase 2, desde el adaptador de WhatsApp.
- `BarberoController` como punto de entrada principal. Si el módulo crece, un controlador por recurso, siempre con el sufijo `Controller`.
- Métodos que reciben DTOs de entrada con `@Valid`, delegan en `BarberoService` y devuelven DTOs de salida o `ResponseEntity` con el código HTTP correcto.
- Autorización administrativa con `@PreAuthorize("hasRole('ADMINISTRADOR')")`. El controlador decide quién puede llamar; el service no conoce roles HTTP.
- `GET /api/barberos` es público y devuelve solo perfiles activos con `BarberoPublicoResponse`.
- `GET /api/barberos/{id}/servicios` es público; `PUT /api/barberos/{id}/servicios` reemplaza las asignaciones y requiere `ADMINISTRADOR`.
- `GET /api/barberos/admin`, `POST /api/barberos` y `PATCH /api/barberos/{id}/estado` son exclusivos de `ADMINISTRADOR`; las respuestas administrativas usan `BarberoAdminResponse`.
- El cambio de estado solo altera el perfil: conserva activa la cuenta del usuario y sus sesiones.
- Documentación de cada endpoint visible en Swagger UI, con sus respuestas y errores `ProblemDetail`.

## Qué NO debe ir en esta carpeta

- Entidades JPA como parámetro o retorno: los controladores solo hablan DTOs.
- Reglas de negocio: cálculo de disponibilidad, validación de anticipación, copia de precios o duración, hashing de contraseñas.
- Acceso directo a repositorios o a `EntityManager`.
- `try/catch` que oculten excepciones: el manejo global vive en `common/exception/`.
- Lógica exclusiva de WhatsApp o de cualquier otro canal: la API solo adapta la petición al service.

## Convenciones de nombres

- Paquete `com.oalbarber.api.modules.barbero.controller`.
- Clases en PascalCase terminadas en `Controller`; el nombre del recurso va en español y sin tildes: `BarberoController`.
- Rutas en plural, minúsculas, sin tildes y sin verbos: `/api/barberos`. Los verbos HTTP expresan la acción.
- Métodos en camelCase y en español: `listar`, `obtener`, `crear`, `cancelar`.
- No se expone el identificador interno de la base si el SRS no lo requiere; se usan los DTOs del módulo.

## Referencias

- Antes de retirar una asignación todavía se debe consultar si hay citas futuras afectadas; ese aviso queda pendiente de otra historia.
- Requisitos relacionados del SRS: RF-10, RN-01, RN-12.
- Relación con otros módulos: delega la creación y consulta de cuentas en `BarberoService`/`UsuarioService`; las respuestas públicas no revelan datos personales de la cuenta.
