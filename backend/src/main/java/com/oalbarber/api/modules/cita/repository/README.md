# Repositorios — módulo `cita`

Paquete: `com.oalbarber.api.modules.cita.repository`.

## Contexto del módulo

Cálculo de disponibilidad, creación y cancelación de citas, estados (CONFIRMADA, CANCELADA, COMPLETADA, NO_ASISTIO) y campo `canal` (WEB, WHATSAPP, ADMIN). Cada cita guarda inicio y fin exactos, duración y precio vigentes al reservar. El módulo debe poder usarse desde cualquier canal sin depender de HTTP.

## Qué debe ir en esta carpeta

- Interfaces Spring Data JPA del módulo `cita`: `CitaRepository`.
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

- Paquete `com.oalbarber.api.modules.cita.repository`.
- Interfaces en PascalCase terminadas en `Repository`: `CitaRepository`.
- Métodos `findBy...`, `existsBy...`, `countBy...`, `deleteBy...`, en inglés técnico estándar.
- Tablas y columnas en snake_case: `cita`.

## Detalle de este módulo

- `CitaRepository extends JpaRepository<Cita, Long>`.
- Consultas previstas: citas de un cliente ordenadas por `inicio`, citas de un barbero en un
  rango, y detección de solapamiento:
  `inicio < :fin and fin > :inicio and barbero.id = :barberoId and estado <> 'CANCELADA'`.
- La validación de solapamiento en el service es una comprobación previa; la garantía última es
  la restricción de exclusión de la base (RNF-06).
- Al capturar el error de la restricción se traduce a `HorarioNoDisponibleException` en
  `common/exception/`, sin filtrar el mensaje interno de PostgreSQL.

## Referencias

- Requisitos relacionados del SRS: RN-05 a RN-13, RN-16, RN-17, RF-15 a RF-26, RNF-06, RNF-15.
- Relación con otros módulos: Es el núcleo de la agenda. Usa `servicio` (duración y precio), `barbero` y `horario` (atención, almuerzo, bloqueos y parámetros). La web y, en fase 2, WhatsApp llaman a los mismos services; ningún canal accede a la base directamente ni duplica el cálculo.
