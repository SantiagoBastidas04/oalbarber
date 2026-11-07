# Servicios (lógica de negocio) — módulo `auth`

Paquete: `com.oalbarber.api.modules.auth.service`.

## Contexto del módulo

Registro de clientes (nombre, celular y contraseña), inicio de sesión con celular y contraseña, emisión y renovación de JWT (token de acceso corto y refresh token revocable), roles ADMIN, BARBERO y CLIENTE, y restablecimiento de contraseña por el administrador.

## Qué debe ir en esta carpeta

- La lógica del módulo `auth`: `AutenticacionService` y las clases de apoyo que hagan falta (por ejemplo, cálculo de disponibilidad, validación de reglas o generación de tokens).
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

- Paquete `com.oalbarber.api.modules.auth.service`.
- Clases en PascalCase terminadas en `Service`: `AutenticacionService`. Solo se extrae una interfaz si hay varias implementaciones.
- Métodos en camelCase y en español: `crear`, `actualizar`, `cancelar`, `calcularDisponibilidad`.
- Nada de estado mutable compartido: los services son stateless.
- Los mensajes de las excepciones se escriben en español y son aptos para el cliente final.

## Referencias

- Requisitos relacionados del SRS: RF-01 a RF-06, RN-15, RNF-01 a RNF-05.
- Relación con otros módulos: Usa el módulo `usuario` para leer y crear usuarios; no implementa su propio repositorio. Publica el filtro JWT que consumen todos los módulos protegidos.
