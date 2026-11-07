# Arquitectura

## Contenido

| Archivo | Qué contiene |
|---|---|
| [`modelo-datos.md`](modelo-datos.md) | Diagrama entidad-relación, reglas de integridad, flujo de una reserva y pendientes del modelo. |

## Qué va aquí

- La descripción del **monolito modular**: módulos `auth`, `usuario`, `barbero`, `servicio`,
  `horario` y `cita`, y la regla de dependencia entre ellos.
- Diagramas de contexto, de módulos y de despliegue (Docker Compose con API, PostgreSQL y
  pgAdmin).
- El flujo de una reserva: cliente → API → controlador → service de `cita` → services de
  `servicio`, `barbero` y `horario` → repositorios → PostgreSQL.
- La preparación para la fase 2 (WhatsApp): la lógica de reservas no depende de HTTP y cada cita
  guarda su `canal`.
- La estrategia de integridad de la agenda (restricción de exclusión de PostgreSQL) y la
  separación entre disponibilidad calculada y datos almacenados.

## Qué NO va aquí

- Código, configuraciones completas ni credenciales.
- El detalle de cada regla de negocio: eso vive en el SRS.
- Documentación endpoint por endpoint: eso vive en `docs/api/`.
- Diagramas sin fecha ni versión: todo diagrama debe poder rastrearse a un cambio.

## Convenciones

- Diagramas en Mermaid dentro del Markdown, para que se rendericen en GitHub y se versionen como
  texto.
- Un archivo por tema (`vision-general.md`, `modulos.md`, `flujo-reserva.md`,
  `despliegue.md`, `modelo-datos.md`).
- Los nombres de módulos y clases coinciden exactamente con el código.
- Toda decisión estructural se registra como ADR en `docs/decisiones/` y se enlaza aquí.
