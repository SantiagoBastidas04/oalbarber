# Migraciones Flyway

Carpeta de migraciones versionadas de PostgreSQL. El esquema de la base **solo** cambia aquí:
`spring.jpa.hibernate.ddl-auto=validate` impide que Hibernate lo modifique.

## Qué debe ir en esta carpeta

- Archivos SQL con el formato `V<n>__<descripcion>.sql`, por ejemplo:
  - `V1__creacion_inicial.sql`
  - `V2__restriccion_exclusion_citas.sql`
  - `V3__datos_iniciales_servicios.sql`
- Migraciones repetibles con `R__<descripcion>.sql` solo para datos que deban recalcularse.
- La extensión `btree_gist` y la restricción de exclusión de la tabla `cita` (RNF-06), que impide
  solapar citas del mismo barbero incluso con reservas simultáneas.
- Los datos iniciales del SRS: los dos barberos, los cuatro servicios (corte normal 40 min,
  premium 90 min, mechas 180 min, ondulación 180 min) y los parámetros de la RN-18.
- Columnas de fecha con zona (`timestamptz`) y nombres de tabla/columna en snake_case.

## Qué NO debe ir en esta carpeta

- Cambios de esquema hechos a mano en pgAdmin o con `ddl-auto=update`.
- Modificar una migración que ya se fusionó a `main`: se crea una migración nueva.
- Credenciales, contraseñas reales o datos personales de clientes.
- Lógica de negocio en PL/pgSQL salvo la restricción de integridad justificada.

## Convenciones

- `V` mayúscula, número consecutivo sin ceros a la izquierda, doble guion bajo y descripción en
  minúsculas con guiones bajos: `V4__agrega_indice_citas_por_barbero.sql`.
- Una migración = un cambio coherente y revisable.
- Los scripts deben poder aplicarse sobre una base vacía en orden.
- Toda migración se prueba con Testcontainers antes del Pull Request.
