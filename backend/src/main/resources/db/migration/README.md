# Migraciones Flyway

Carpeta de migraciones versionadas de PostgreSQL. El esquema de la base **solo** cambia aquí:
`spring.jpa.hibernate.ddl-auto=validate` impide que Hibernate lo modifique.

## Qué debe ir en esta carpeta

- Archivos SQL con el formato `V<n>__<descripcion>.sql`, por ejemplo:
  - `V1__creacion_inicial.sql`
  - `V2__restriccion_exclusion_citas.sql`
  - `V4__crear_tabla_servicios.sql`
- Migraciones repetibles con `R__<descripcion>.sql` solo para datos que deban recalcularse.
- La extensión `btree_gist` y la restricción de exclusión de la tabla `cita` (RNF-06), que impide
  solapar citas del mismo barbero incluso con reservas simultáneas.
- Los parámetros de agenda de RN-18 se inicializan en V7. Los perfiles de barbero se crean desde
  el flujo administrativo; no se insertan cuentas ni perfiles ficticios mediante migración.
- V9 crea los cuatro servicios del catálogo. Sus precios de COP 1.00 son marcadores técnicos
  provisionales por P-05, identificados en el comentario de la columna y en los datos iniciales;
  deben reemplazarse por los precios confirmados antes de publicar el catálogo en producción.
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
