# ADR-0002: Asignación barbero-servicio y consulta de citas futuras

- **Fecha:** 2026-11-15
- **Estado:** propuesto; requiere confirmar los contratos del catálogo y de citas

## Contexto

La aplicación necesita expresar qué servicios ofrece cada perfil y consultar citas futuras que
podrían verse afectadas por cambios de disponibilidad o asignaciones. El esquema y la interfaz
del catálogo, así como el módulo de citas, deben revisarse antes de concretar estas relaciones.

## Propuesta

- Modelar la asignación como relación muchos a muchos mediante `barbero_servicio`, con clave
  compuesta por `barbero_id` y `servicio_id`.
- Mantener las consultas de citas detrás del puerto `ConsultaCitasFuturas`, consumido por el
  módulo `barbero`, con la operación propuesta
  `contarCitasFuturasConfirmadas(UUID barberoId, <tipoIdServicio> servicioId)`. No depender de
  controladores ni repositorios internos de `cita`.
- Determinar tipo de `servicio_id`, nombre real de tabla y firma del servicio/puerto después de
  inspeccionar el catálogo y el diseño real de citas.

Esta propuesta aún no define migraciones, endpoints ni selección de servicios en la interfaz.

## Alternativas consideradas

- Guardar IDs de servicios como una lista serializada en `barbero`: dificulta integridad
  referencial y consultas inversas.
- Hacer que `barbero` acceda directamente al repositorio de `cita`: acopla módulos y cruza
  propiedad de datos.
- Decidir ahora el tipo/FK de servicio: arriesga incompatibilidad con el esquema del catálogo, que
  aún debe revisarse.

## Consecuencias y trabajo pendiente

- Si se acepta, la tabla puente requerirá dos FKs y una migración coordinada con el catálogo.
- Revisar el tipo de ID, nombre de tabla y firma de `ServicioService` antes de integrar el catálogo.
- Acordar con citas el contrato de consulta futura antes de implementar la lógica dependiente.
- Reemplazar este ADR por otro aceptado cuando se confirme el diseño; hasta entonces no se
  considera una decisión de implementación.
