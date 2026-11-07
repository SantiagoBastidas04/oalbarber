# Repositorios — módulo `horario`

Paquete: `com.oalbarber.api.modules.horario.repository`.

## Contexto del módulo

Horario de atención por barbero, almuerzo, bloqueos de franjas o fechas completas y parámetros configurables (anticipación mínima, límite de cancelación, máximo de días adelante, intervalo entre turnos y margen entre citas).

## Qué debe ir en esta carpeta

- Interfaces Spring Data JPA del módulo `horario`: `HorarioAtencionRepository, BloqueoRepository y ConfiguracionRepository`.
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

- Paquete `com.oalbarber.api.modules.horario.repository`.
- Interfaces en PascalCase terminadas en `Repository`: `HorarioAtencionRepository, BloqueoRepository y ConfiguracionRepository`.
- Métodos `findBy...`, `existsBy...`, `countBy...`, `deleteBy...`, en inglés técnico estándar.
- Tablas y columnas en snake_case: `horario_atencion, bloqueo y configuracion`.

## Detalle de este módulo

- `HorarioAtencionRepository`: `findByBarberoIdAndDiaSemana`, `findByBarberoIdOrderByDiaSemana`.
- `BloqueoRepository`: consultas de solapamiento por barbero, por ejemplo
  `findByBarberoIdAndInicioLessThanAndFinGreaterThan`.
- `ConfiguracionRepository`: `findByClave`, `findAllByClaveIn`.
- Las consultas de solapamiento usan intervalos semiabiertos (`inicio < finAjeno` y
  `fin > inicioAjeno`) para no tratar dos citas contiguas como solapadas.

## Referencias

- Requisitos relacionados del SRS: RN-01, RN-02, RN-12, RN-18, RF-11 a RF-14.
- Relación con otros módulos: Aporta las reglas de tiempo que `cita` usa para calcular disponibilidad. La disponibilidad no se guarda aquí: se calcula al consultar. No cancela citas al crear un bloqueo; solo informa si hay citas afectadas para pedir confirmación.
