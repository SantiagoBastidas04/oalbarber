# ADR-0002: Asignación barbero-servicio y consulta de citas futuras

- **Fecha:** 2026-11-15
- **Estado:** aceptado parcialmente; relación y disponibilidad implementadas, aviso por citas futuras pendiente
- **Última revisión:** 2026-11-25

## Contexto

La aplicación necesita expresar qué servicios ofrece cada perfil y, al quitar una asignación,
consultar si hay citas futuras que puedan verse afectadas. El catálogo ya implementa servicios con
UUID y el módulo `barbero` puede administrar sus asignaciones. En la fecha de esta decisión, el
módulo `cita` y el cálculo de disponibilidad todavía no estaban disponibles.

## Decisión

- Representar la asignación mediante `barbero_servicio`, con clave compuesta
  `(barbero_id, servicio_id)`, UUID en ambas columnas, claves foráneas y borrado restringido.
- Validar y listar los servicios mediante `ServicioService`; el módulo `barbero` no accede al
  repositorio de `servicio`.
- Dejar la consulta de citas futuras detrás de un puerto propiedad de `barbero`. Cuando exista el
  módulo `cita`, este implementará ese puerto y consultará sus propios datos, evitando una
  dependencia circular entre módulos.

## Estado implementado

La relación está creada por V5. `BarberoService` lista servicios activos asignados, reemplaza la
lista de asignaciones en una transacción y ofrece operaciones para consultar perfiles activos que
ofrecen un servicio. La API y `/panel/barberos` permiten consultar y modificar esas asignaciones.

## Pendiente

- **Criterio 4 de H-13:** antes de quitar una asignación, consultar las citas futuras afectadas y
  devolver al panel la información necesaria para pedir confirmación. No cancelar ni mover citas
  automáticamente.

El puerto `ConsultaCitasFuturas` todavía no se implementa; su firma deberá comunicar las citas
confirmadas futuras afectadas por `(barberoId, servicioId)` sin hacer que `barbero` acceda a
repositorios de `cita`.

## Actualización 2026-11-25

PB-22 integró `BarberoService` con `DisponibilidadService`: la selección “cualquiera” solo considera
perfiles activos que ofrecen el servicio, y la consulta explícita valida esa asignación. El
criterio 3 de H-13 queda cubierto para disponibilidad; la creación de citas y el aviso del criterio
4 siguen pendientes. Ver [ADR-0004](ADR-0004-calculo-de-disponibilidad.md).

## Alternativas consideradas

- Guardar IDs como una lista serializada en `barbero`: se descarta porque elimina la integridad
  referencial y dificulta las consultas inversas.
- Hacer que `barbero` acceda directamente al repositorio de `cita`: se descarta porque acopla
  módulos y cruza la propiedad de los datos.
- Hacer que `cita` dependa de implementaciones internas de `barbero`: se descarta; `cita` debe
  depender de contratos y los módulos no deben formar una dependencia circular.

## Consecuencias

- La clave compuesta impide duplicar una asignación y las claves foráneas protegen ambos perfiles
  de datos.
- El catálogo debe seguir consultándose mediante su service.
- H-13 no se puede cerrar mientras el criterio 4 siga pendiente; ver la
  [matriz de trazabilidad](../seguimiento/trazabilidad-H-13.md).
