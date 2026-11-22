# Servicios (lógica de negocio) — módulo `barbero`

Paquete: `com.oalbarber.api.modules.barbero.service`.

## Contexto del módulo

Perfil profesional enlazado a una cuenta con rol `BARBERO` o `ADMINISTRADOR`.

## Qué debe ir en esta carpeta

- La lógica del módulo `barbero`: `BarberoService`, creación de perfiles, listados y cambio de estado. Disponibilidad y asignación de servicios todavía no están implementadas.
- Métodos públicos con Javadoc en español, indicando qué regla del SRS aplican y qué excepciones de negocio lanzan.
- Transacciones con `@Transactional` en los métodos que modifican datos; lecturas con `@Transactional(readOnly = true)` cuando corresponda.
- Uso de excepciones propias del dominio (`RecursoNoEncontradoException`, `HorarioNoDisponibleException`, etc.), nunca `null` ni códigos de error ambiguos.
- Dependencias por constructor (obligatorias, sin campos mutables) hacia repositorios y hacia services de otros módulos.

## Qué NO debe ir en esta carpeta

- Tipos HTTP: `HttpServletRequest`, `HttpServletResponse`, `ResponseEntity`, `@RequestHeader` o `SecurityContextHolder`. El service debe poder invocarse desde WhatsApp o desde una tarea programada.
- Anotaciones de autorización `@PreAuthorize`: la autorización se valida en el controlador o en el adaptador del canal.
- Entidades JPA como contrato público de vuelta al controlador: se devuelven DTOs o resultados de dominio internos.
- SQL manual, `EntityManager` o acceso a la base desde otro módulo.
- `try/catch` que ignoren errores o que devuelvan valores por defecto silenciosos.

## Convenciones de nombres

- Paquete `com.oalbarber.api.modules.barbero.service`.
- Clases en PascalCase terminadas en `Service`: `BarberoService`. Solo se extrae una interfaz si hay varias implementaciones.
- Métodos en camelCase y español para operaciones de perfil (`crear`, `listarActivos`, `cambiarEstado`).
- Nada de estado mutable compartido: los services son stateless.
- Los mensajes de las excepciones se escriben en español y son aptos para el cliente final.

## Detalle de este módulo

- `BarberoService` lista los barberos activos y obtiene el perfil por identificador o por usuario.
- Crea una cuenta con rol `BARBERO` mediante `UsuarioService` y su perfil dentro de una única
  transacción; la contraseña se cifra en el módulo `usuario`.
- Registra explícitamente al administrador autenticado como barbero reutilizando su cuenta; valida
  el rol real `ADMINISTRADOR` y nunca crea ni modifica datos de `usuario`. El inicializador no crea
  el perfil automáticamente.
- Cambia el estado del perfil sin cambiar `usuario.activo`. Por ello, el estado de cuenta y el del
  perfil son independientes.
- La lista pública incluye solo perfiles activos y nunca expone nombre ni celular de la cuenta.
- El listado administrativo incluye perfiles activos e inactivos y datos de cuenta.
- Un perfil inactivo no debe aparecer en la disponibilidad; las consultas deben aplicar el filtro.

## Referencias

- Requisitos relacionados del SRS: RF-10, RN-01, RN-12.
- Relación con otros módulos: consume `UsuarioService` para crear y consultar cuentas. Desactivar el perfil conserva activa la cuenta y sus sesiones. Las relaciones con horarios y citas se implementarán en tareas posteriores.
