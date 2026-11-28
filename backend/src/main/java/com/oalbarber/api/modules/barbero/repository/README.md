# Repositorios — módulo `barbero`

Paquete: `com.oalbarber.api.modules.barbero.repository`.

## Contexto del módulo

Repositorio de perfiles profesionales enlazados a usuarios con rol `BARBERO` o `ADMINISTRADOR`.

## Qué debe ir en esta carpeta

- Interfaces Spring Data JPA del módulo `barbero`: `BarberoRepository` y `BarberoServicioRepository`.
- Métodos derivados con nombres claros (`findByActivoTrue`, `findByUsuarioId`, `existsByUsuarioId`).
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

- Paquete `com.oalbarber.api.modules.barbero.repository`.
- Interfaces en PascalCase terminadas en `Repository`: `BarberoRepository`.
- Métodos `findBy...`, `existsBy...`, `countBy...`, `deleteBy...`, en inglés técnico estándar.
- Tablas y columnas en snake_case: `barbero`.

## Detalle de este módulo

- `BarberoRepository extends JpaRepository<Barbero, UUID>` y resuelve perfiles por estado o usuario.
- `BarberoServicioRepository extends JpaRepository<BarberoServicio, BarberoServicioId>` consulta y modifica la asignación compuesta entre barberos y servicios.
- La relación con `usuario` se resuelve en el service de `barbero` o en el mapper, no con una
  consulta cruzada desde otro módulo.

## Referencias

- Requisitos relacionados del SRS: RF-10, RN-01, RN-12.
- Los repositorios de este módulo no consultan tablas de `servicio` ni de `cita`; cada dato se obtiene mediante el service propietario.
