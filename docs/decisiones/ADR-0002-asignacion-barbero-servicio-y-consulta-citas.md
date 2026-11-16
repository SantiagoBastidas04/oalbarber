# ADR-0002: Asignación barbero-servicio y consulta de citas futuras

- **Fecha:** 2026-11-15
- **Estado:** aceptado parcialmente; la relación barbero-servicio está implementada y la integración con citas sigue pendiente
- **Última revisión:** 2026-11-15

## Contexto

La aplicación necesita expresar qué servicios ofrece cada perfil y, al quitar una asignación,
consultar si hay citas futuras que puedan verse afectadas. El catálogo ya implementa servicios con
UUID y el módulo `barbero` puede administrar sus asignaciones. El módulo `cita` y el cálculo de
disponibilidad todavía no están disponibles en esta rama.

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

- **Criterio 3 de H-13:** integrar las operaciones de `BarberoService` con la selección de barbero
  y la validación de disponibilidad dentro del módulo `cita`.
- **Criterio 4 de H-13:** antes de quitar una asignación, consultar las citas futuras afectadas y
  devolver al panel la información necesaria para pedir confirmación. No cancelar ni mover citas
  automáticamente.

Cuando exista `cita`, se propone declarar en `barbero` un puerto como
`ConsultaCitasFuturas` y hacer que `cita` lo implemente. Su firma deberá usar los tipos reales del
módulo y comunicar las citas confirmadas futuras afectadas por `(barberoId, servicioId)`. No se
implementan todavía el puerto, la confirmación ni los errores asociados.

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
- H-13 no se puede cerrar mientras los criterios 3 y 4 sigan pendientes; ver la
  [matriz de trazabilidad](../seguimiento/trazabilidad-H-13.md).
