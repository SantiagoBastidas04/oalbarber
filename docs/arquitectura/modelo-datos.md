# Modelo de datos OalBarber

**Estado:** modelo de cuentas, catálogo, perfiles, asignaciones y horarios implementado en V1–V8;
bloqueos y citas siguen pendientes. **Última revisión:** 2026-11-24. El SRS se conserva sin cambios.

## Tablas implementadas

Las migraciones crean `usuario` (V1), `sesion_refresco` (V2), `servicio` (V3), `barbero` (V4),
`barbero_servicio` (V5), `horario_atencion` (V7) y `horario_excepcion` (V8). V6 añade
`barbero.fecha_creacion`. Los identificadores de estas tablas son UUID. V4 crea únicamente
`barbero`, con `usuario_id` único, FK a `usuario(id)` y `ON DELETE RESTRICT`. V5 enlaza barbero y
servicio con una clave primaria compuesta, dos FKs con `ON DELETE RESTRICT`, `creado_en` y un
índice para `servicio_id`. V7 guarda una jornada continua por día y barbero, con almuerzo opcional;
V8 añade fechas especiales que abren, cierran o cambian el horario de una fecha concreta.

```mermaid
erDiagram
    usuario {
        uuid id PK
        varchar nombre
        varchar celular UK
        varchar contrasena_hash
        varchar rol "CLIENTE | BARBERO | ADMINISTRADOR"
        boolean activo
        timestamptz fecha_creacion
    }

    sesion_refresco {
        uuid id PK
        uuid usuario_id FK
        varchar hash_token UK
        varchar agente_usuario
        varchar ip
        timestamptz fecha_creacion
        timestamptz ultima_actividad
        timestamptz fecha_expiracion
        boolean revocado
        timestamptz fecha_revocacion
        uuid reemplazado_por_id FK
    }

    servicio {
        uuid id PK
        varchar nombre UK
        text descripcion
        integer duracion_min
        numeric precio "12,2"
        varchar imagen
        boolean activo
    }

    barbero {
        uuid id PK
        uuid usuario_id FK, UK
        varchar nombre_publico
        text descripcion
        varchar foto
        boolean activo
        timestamptz fecha_creacion
    }

    barbero_servicio {
        uuid barbero_id PK, FK
        uuid servicio_id PK, FK
        timestamptz creado_en
    }

    horario_atencion {
        uuid id PK
        uuid barbero_id FK
        smallint dia_semana "1 lunes ... 6 sábado"
        time hora_inicio
        time hora_fin
        time almuerzo_inicio
        time almuerzo_fin
    }

    horario_excepcion {
        uuid id PK
        uuid barbero_id FK
        date fecha
        boolean cerrado
        time hora_inicio
        time hora_fin
        time almuerzo_inicio
        time almuerzo_fin
    }

    usuario ||--o{ sesion_refresco : "sesiones"
    sesion_refresco o|--o| sesion_refresco : "reemplaza"
    usuario ||--o| barbero : "perfil profesional"
    barbero ||--o{ barbero_servicio : "ofrece"
    servicio ||--o{ barbero_servicio : "asignado a"
    barbero ||--o{ horario_atencion : "atiende"
    barbero ||--o{ horario_excepcion : "fechas especiales"
```

`barbero` es un perfil separado de la identidad. El nombre, celular y credenciales permanecen en
`usuario`; el perfil guarda presentación pública, estado profesional y fecha de creación. El
inicializador no crea perfil para el administrador: este se agrega explícitamente mediante
`POST /api/barberos/admin`, que solo inserta en `barbero`. Cambiar `barbero.activo` no cambia
`usuario.activo` ni revoca sus sesiones.

`barbero_servicio` representa las asignaciones y pertenece al módulo `barbero`. El módulo consulta
el catálogo a través de `ServicioService`, valida que los servicios estén activos y permite una
lista vacía. Un perfil nuevo comienza sin servicios asignados; el administrador los configura en
`/panel/barberos`.

## Entidades pendientes

El siguiente diagrama muestra entidades del SRS que aún no tienen migración ni implementación.
No representa capacidades disponibles.

```mermaid
erDiagram
    barbero ||--o{ bloqueo : "bloqueo futuro"
    usuario ||--o{ cita : "cliente futuro"
    barbero ||--o{ cita : "barbero futuro"
    servicio ||--o{ cita : "servicio futuro"

    bloqueo {
        uuid id PK
        uuid barbero_id FK
    }
    cita {
        uuid id PK
        uuid cliente_id FK
        uuid barbero_id FK
        uuid servicio_id FK
    }
```

La integración de las asignaciones con disponibilidad y la consulta de citas futuras está
pendiente del módulo `cita`. El diseño propuesto usa un puerto propiedad de `barbero` e
implementado por `cita`, para evitar dependencia circular. Detalle y estado de los criterios en la
[matriz H-13](../seguimiento/trazabilidad-H-13.md) y en el
[ADR-0002](../decisiones/ADR-0002-asignacion-barbero-servicio-y-consulta-citas.md).

## Reglas confirmadas

- `usuario.celular` es único y `usuario.contrasena_hash` almacena el hash, no la contraseña.
- `usuario.rol` admite `CLIENTE`, `BARBERO` y `ADMINISTRADOR`.
- `barbero.usuario_id` es único y su FK usa `ON DELETE RESTRICT`.
- Las cuentas y perfiles de barberos se crean desde el flujo administrativo. El administrador ya
  existente puede agregarse explícitamente como perfil sin cambiar su cuenta. Ningún perfil se
  inserta mediante una migración ni por el inicializador.
- `barbero_servicio` evita pares duplicados y restringe el borrado de los registros referenciados.
- `horario_atencion` guarda una jornada continua por día y barbero y evita pares duplicados con una
  clave única de barbero y día; los días sin fila están cerrados. `dia_semana` usa ISO 1–7 y el
  domingo no se configura en la semana (RN-01).
- `horario_excepcion` sobrescribe una fecha concreta y evita duplicados con una clave única de
  barbero y fecha: puede abrir un domingo, cerrar un festivo o cambiar el horario de ese día. Una
  fecha cerrada no lleva horas; una abierta respeta la jornada de 10:00 a 19:00 y el almuerzo
  dentro (RN-01 y RN-02).
- El DTO público de barbero solo lista perfiles activos y no incluye nombre ni celular de la
  cuenta. El DTO administrativo incluye esos campos y `servicioIds`, y está restringido a
  administradores.

## Conteo y SRS

El esquema actualmente implementado corresponde a V1–V8; las tablas restantes descritas por el SRS
se mantienen como trabajo pendiente hasta que existan los módulos respectivos. El detalle de
RF-10 en el SRS aclara que el perfil del administrador es explícito y opcional.
