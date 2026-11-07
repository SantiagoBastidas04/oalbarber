# Servicios (lógica de negocio) — módulo `usuario`

Paquete: `com.oalbarber.api.modules.usuario.service`.

## Contexto del módulo

Datos de las personas que usan el sistema: nombre, celular único en formato internacional (`+57...`), contraseña con hash BCrypt, rol y estado activo. Es la base de identidad que consumen auth, barbero y cita.

## Qué debe ir en esta carpeta

- La lógica del módulo `usuario`: `UsuarioService` y las clases de apoyo que hagan falta (por ejemplo, cálculo de disponibilidad, validación de reglas o generación de tokens).
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

- Paquete `com.oalbarber.api.modules.usuario.service`.
- Clases en PascalCase terminadas en `Service`: `UsuarioService`. Solo se extrae una interfaz si hay varias implementaciones.
- Métodos en camelCase y en español: `crear`, `actualizar`, `cancelar`, `calcularDisponibilidad`.
- Nada de estado mutable compartido: los services son stateless.
- Los mensajes de las excepciones se escriben en español y son aptos para el cliente final.

## Detalle de este módulo

- `UsuarioService` crea y actualiza usuarios, valida que el celular sea único y guarda la
  contraseña con `PasswordEncoder` (BCrypt).
- Desactiva cuentas (baja lógica) sin borrar su historial de citas.
- Devuelve DTOs o resultados internos, nunca la entidad con `passwordHash` hacia el controlador.
- No emite tokens ni valida credenciales: eso pertenece al módulo `auth`.

## Referencias

- Requisitos relacionados del SRS: RN-15, RF-01, RF-27, RNF-01, RNF-10.
- Relación con otros módulos: Es la tabla base de identidad. `barbero` referencia a un usuario con rol BARBERO o ADMIN; `cita` referencia al usuario con rol CLIENTE. Ningún módulo debe leer `password_hash` fuera de `auth`.
