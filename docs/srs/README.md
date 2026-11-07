# SRS — Especificación de requisitos

## Qué va aquí

- `SRS-OalBarber.md`: especificación de requisitos de la fase 1, con los requisitos funcionales
  (`RF-01` a `RF-29`), no funcionales (`RNF-01` a `RNF-16`), reglas de negocio (`RN-01` a
  `RN-18`), criterios de aceptación (`CA-01` a `CA-14`) y pendientes (`P-01` a `P-10`).
- Las versiones aprobadas del SRS y, si se necesita, anexos de requisitos por fase.

## Qué NO va aquí

- Diseño técnico detallado: va en `docs/arquitectura/`.
- Contrato de endpoints: va en `docs/api/`.
- Decisiones puntuales de implementación: van en `docs/decisiones/`.
- Apuntes de reuniones sin requisitos acordados.

## Convenciones

- El SRS es la fuente de verdad del alcance: si el código y el SRS no coinciden, se corrige uno
  de los dos en el mismo Pull Request.
- Los identificadores (`RN-`, `RF-`, `RNF-`, `CA-`, `P-`) no se renumeran ni se reutilizan; un
  requisito eliminado se marca como obsoleto.
- Los cambios de alcance los aprueba el cliente y se reflejan en la sección de pendientes.
- Cada versión aprobada indica fecha y autores al inicio del documento.
