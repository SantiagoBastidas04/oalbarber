# Trazabilidad de H-13: barberos y servicios que ofrece cada uno

**Estado:** implementación parcial; H-13 no está lista para cierre.  
**Última revisión:** 2026-11-15.

| Criterio | Estado | Evidencia y trabajo restante |
|---|---|---|
| 1. Administrar qué servicios ofrece cada barbero | ✅ Cubierto | Tabla `barbero_servicio` (V5), lógica transaccional de reemplazo, endpoint administrativo `PUT /api/barberos/{id}/servicios` y editor del panel con servicios activos. |
| 2. Consultar los servicios asignados a un barbero | ✅ Cubierto | `GET /api/barberos/{id}/servicios` devuelve servicios activos solo para perfiles activos; `GET /api/barberos/admin` incluye `servicioIds` para el panel. |
| 3. Usar los servicios ofrecidos al seleccionar barbero y validar disponibilidad | 🟡 Parcial | El módulo `barbero` implementa `listarActivosQueOfrecenServicio` y `ofreceServicio`. Falta integrarlos al flujo de selección y disponibilidad en el módulo `cita`, que aún no existe. |
| 4. Avisar de citas futuras afectadas antes de quitar un servicio | ⛔ Bloqueado | Requiere consultar citas futuras confirmadas. El módulo `cita` no existe; no se muestra ni simula el aviso y no se cancelan ni mueven citas. |

**H-13 no puede cerrarse en Jira hasta resolver los criterios 3 y 4.** El criterio 3 necesita la
integración del flujo de reserva con `barbero`; el criterio 4 necesita el puerto de consulta futura
definido en [ADR-0002](../decisiones/ADR-0002-asignacion-barbero-servicio-y-consulta-citas.md) e
implementado por el módulo `cita`.
