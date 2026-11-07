# Repositorios — módulo `servicio`

Paquete: `com.oalbarber.api.modules.servicio.repository`.

## Contexto del módulo

Catálogo único de servicios para todo el negocio (nombre, descripción, duración en minutos, precio y nombre del archivo de imagen estático). Servicios iniciales: corte normal 40 min, premium 90 min, mechas 180 min y ondulación 180 min. El precio es igual para ambos barberos.

## Qué debe ir en esta carpeta

- Interfaces Spring Data JPA del módulo `servicio`: `ServicioRepository`.
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

- Paquete `com.oalbarber.api.modules.servicio.repository`.
- Interfaces en PascalCase terminadas en `Repository`: `ServicioRepository`.
- Métodos `findBy...`, `existsBy...`, `countBy...`, `deleteBy...`, en inglés técnico estándar.
- Tablas y columnas en snake_case: `servicio`.

## Detalle de este módulo

- `ServicioRepository extends JpaRepository<Servicio, Long>`.
- Consultas previstas: `findByActivoTrueOrderByNombreAsc`, `findByIdAndActivoTrue`,
  `existsByNombreIgnoreCase`.
- El catálogo público solo lee servicios activos; el panel de administración puede leer todos.
- No se borran servicios con citas históricas: se desactivan.

## Referencias

- Requisitos relacionados del SRS: RF-07, RF-08, RN-03, RN-04, RN-16.
- Relación con otros módulos: `cita` copia de aquí la duración y el precio vigentes al reservar. La imagen es solo el nombre de un archivo que vive en el frontend (`public/servicios/`); este módulo no sube ni sirve imágenes.
