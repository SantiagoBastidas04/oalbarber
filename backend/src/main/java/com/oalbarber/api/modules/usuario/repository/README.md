# Repositorios — módulo `usuario`

Paquete: `com.oalbarber.api.modules.usuario.repository`.

## Contexto del módulo

Datos de las personas que usan el sistema: nombre, celular único en formato internacional (`+57...`), contraseña con hash BCrypt, rol y estado activo. Es la base de identidad que consumen auth, barbero y cita.

## Qué debe ir en esta carpeta

- Interfaces Spring Data JPA del módulo `usuario`: `UsuarioRepository`.
- Métodos derivados con nombres claros (`findByActivoTrue`, `existsByTelefono`, `findByBarberoIdAndInicioLessThan`).
- Consultas `@Query` solo cuando el nombre derivado no alcance; siempre con parámetros nombrados y sin concatenar SQL.
- Proyecciones o DTOs de consulta cuando se necesite leer solo unas columnas.
- Bloqueos (`@Lock`) o consultas de solapamiento cuando la regla de negocio lo exija, documentando el motivo.

## Qué NO debe ir en esta carpeta

- Lógica de negocio, cálculos de disponibilidad, validaciones de reglas o decisiones de estado.
- Transacciones: se declaran en el service, no en el repositorio.
- Entidades de otros módulos o acceso cruzado a sus tablas.
- SQL nativo salvo una necesidad justificada y documentada (por ejemplo, la restricción de exclusión de PostgreSQL, que va en la migración Flyway).
- DTOs de la API: el repositorio trabaja con entidades o proyecciones internas.

## Convenciones de nombres

- Paquete `com.oalbarber.api.modules.usuario.repository`.
- Interfaces en PascalCase terminadas en `Repository`: `UsuarioRepository`.
- Métodos `findBy...`, `existsBy...`, `countBy...`, `deleteBy...`, en inglés técnico estándar.
- Tablas y columnas en snake_case: `usuario`.

## Detalle de este módulo

- `UsuarioRepository extends JpaRepository<Usuario, Long>`.
- Consultas previstas: `findByTelefono`, `existsByTelefono`, `findByRolAndActivoTrue`,
  `findByIdAndActivoTrue`.
- El teléfono es la clave de búsqueda del login; su índice único lo define la migración Flyway,
  no la entidad.

## Referencias

- Requisitos relacionados del SRS: RN-15, RF-01, RF-27, RNF-01, RNF-10.
- Relación con otros módulos: Es la tabla base de identidad. `barbero` referencia a un usuario con rol BARBERO o ADMIN; `cita` referencia al usuario con rol CLIENTE. Ningún módulo debe leer `password_hash` fuera de `auth`.
