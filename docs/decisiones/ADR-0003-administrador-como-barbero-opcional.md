# ADR-0003: El perfil de barbero del administrador es opcional

- **Estado:** Aceptado
- **Fecha:** 2026-11-19
- **Decisores:** Equipo OalBarber

## Contexto

La aplicación crea o localiza una cuenta inicial con rol `ADMINISTRADOR`. Un administrador también
puede prestar servicios de barbería, pero no todos los administradores tienen que aparecer en el
catálogo público ni en la disponibilidad. La relación entre identidad y perfil es opcional y está
restringida a un perfil por usuario.

## Decisión

El arranque solo crea o localiza la cuenta administrativa. No crea un perfil `barbero`. Un usuario
con rol `ADMINISTRADOR` puede agregarse explícitamente desde `POST /api/barberos/admin`, protegido
por rol. El controlador obtiene el UUID del sujeto autenticado; el cuerpo solo acepta datos
opcionales de presentación. El service valida que la cuenta exista, esté activa y conserve el rol
real `ADMINISTRADOR`, y escribe únicamente en `barbero`. Si ya existe un perfil, la API responde
`409 BARBERO_DUPLICADO`; para un perfil inactivo indica que debe reactivarse.

La migración V6 incorpora `barbero.fecha_creacion` para mantener el modelo persistente. No siembra
perfiles ni modifica `usuario`.

## Alternativas consideradas

1. **Sembrar el perfil del administrador al iniciar o migrar.** Rechazada porque incluiría a todos
   los administradores automáticamente en listados y disponibilidad.
2. **Crear una segunda cuenta `BARBERO`.** Rechazada porque duplica identidad, teléfono y
   credenciales, y separa sesiones de una misma persona.

## Consecuencias

- El administrador no aparece en el catálogo antes del alta explícita.
- `usuario` permanece intacta durante el alta; el vínculo se mantiene con `barbero.usuario_id`.
- La cuenta conserva su rol, estado y sesiones al crear o desactivar el perfil.
- Un perfil inactivo se reactiva con la operación de estado existente en vez de crear un duplicado.
- La API pública solo devuelve perfiles activos.

## Referencias

- RF-10 en [SRS-OalBarber](../srs/SRS-OalBarber.md).
- [Modelo de datos](../arquitectura/modelo-datos.md).
