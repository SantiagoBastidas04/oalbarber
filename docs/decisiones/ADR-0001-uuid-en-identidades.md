# ADR-0001: UUID en identidades y perfiles

- **Fecha:** 2026-11-15
- **Estado:** aceptado para los módulos implementados

## Contexto

`usuario` ya es la identidad común para clientes, barberos y administradores. El perfil
profesional `barbero` referencia esa identidad y necesita un identificador estable en API y
persistencia. El esquema actual usa UUID para `usuario`, `sesion_refresco` y `barbero`.

## Decisión

Usar UUID como identificador de `usuario`, `sesion_refresco` y `barbero`. Los contratos HTTP
serializan los UUID como cadenas. Esta decisión confirma el esquema existente de esos módulos;
no determina el tipo de ID de `servicio` ni de entidades aún no implementadas.

## Alternativas consideradas

- **Enteros secuenciales:** simples y compactos, pero no corresponden al esquema ya aplicado y
  exponen secuencias previsibles en los recursos de API.
- **UUID solo en la API:** manteniendo claves numéricas internas, requeriría una traducción que no
  existe ni aporta una necesidad actual.

## Consecuencias

- Los repositorios Java usan `UUID` y TypeScript modela los IDs como `string`.
- Las FK de las tablas implementadas usan UUID.
- Antes de relacionar `barbero` con `servicio`, se debe confirmar el tipo de ID de la entidad y la
  migración del catálogo; este ADR no presupone su tipo.
