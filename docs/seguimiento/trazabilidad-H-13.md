# Trazabilidad de H-13: barberos y servicios que ofrece cada uno

**Estado:** implementación parcial; H-13 no está lista para cierre.  
**Última revisión:** 2026-11-25.

| Criterio | Estado | Evidencia y trabajo restante |
|---|---|---|
| 1. Administrar qué servicios ofrece cada barbero | ✅ Cubierto | Tabla `barbero_servicio` (V5), lógica transaccional de reemplazo, endpoint administrativo `PUT /api/barberos/{id}/servicios` y editor del panel con servicios activos. |
| 2. Consultar los servicios asignados a un barbero | ✅ Cubierto | `GET /api/barberos/{id}/servicios` devuelve servicios activos solo para perfiles activos; `GET /api/barberos/admin` incluye `servicioIds` para el panel. |
| 3. Usar los servicios ofrecidos al seleccionar barbero y validar disponibilidad | ✅ Cubierto para la consulta de disponibilidad | `DisponibilidadService` consulta `BarberoService`, que devuelve perfiles activos asignados; el motor excluye perfiles que no ofrecen el servicio. La creación de citas aún queda fuera de PB-22. |
| 4. Avisar de citas futuras afectadas antes de quitar un servicio | ⛔ Pendiente | Requiere consultar citas futuras confirmadas antes de retirar una asignación. No se muestra el aviso y no se cancelan ni mueven citas. |

**H-13 no puede cerrarse en Jira hasta resolver el criterio 4.** La consulta y el cálculo de
disponibilidad se implementaron en PB-22; el aviso por citas futuras necesita el contrato de
consulta definido en [ADR-0002](../decisiones/ADR-0002-asignacion-barbero-servicio-y-consulta-citas.md).
