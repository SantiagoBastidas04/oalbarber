# Documentación

Repositorio de documentación viva de OalBarber. Todo cambio funcional debe actualizar la
documentación en el mismo Pull Request.

## Contenido

| Carpeta | Qué contiene |
|---|---|
| `srs/` | Especificación de requisitos de software (fuente de verdad del alcance). |
| `arquitectura/` | Diagramas y descripción del monolito modular, módulos, flujos e infraestructura. |
| `api/` | Contrato de la API REST: OpenAPI, convenciones de endpoints y errores. |
| `decisiones/` | Registros de decisiones de arquitectura (ADR). |

## Qué NO va aquí

- Código fuente, scripts de despliegue ni credenciales.
- Actas de reunión sin decisiones: si no cambia el sistema, no es documentación del proyecto.
- Copias duplicadas del README raíz.

## Convenciones

- Markdown con títulos en español y una sola idea por sección.
- Los documentos usan los identificadores del SRS (`RN-xx`, `RF-xx`, `RNF-xx`, `CA-xx`) para
  poder rastrear requisitos, código, pruebas y Pull Requests.
- Los enlaces entre documentos son relativos, para que funcionen en GitHub.
- Cada documento indica su estado (borrador, aprobado, obsoleto) y la fecha de la última revisión.
