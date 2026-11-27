# Decisiones de arquitectura (ADR)

## Qué va aquí

Registros breves de decisiones que afectan a más de una persona o que son costosas de revertir.
Formato de nombre: `ADR-0001-titulo-en-minusculas.md`, numeración consecutiva y sin reutilizar.

Cada ADR debe incluir:

1. **Título y fecha.**
2. **Estado:** propuesto, aceptado, rechazado, obsoleto o reemplazado por `ADR-xxxx`.
3. **Contexto:** el problema y las restricciones (por ejemplo, equipo de tres personas, plazo de
   un mes, un solo local).
4. **Decisión:** qué se eligió.
5. **Alternativas consideradas:** y por qué se descartaron.
6. **Consecuencias:** positivas, negativas y trabajo pendiente.

Decisiones que ya se conocen y merecen un ADR al implementarse:

- Monolito modular en lugar de microservicios.
- JWT de acceso corto con refresh token revocable en cookie `HttpOnly`.
- Cálculo de disponibilidad en el service de `cita`, sin almacenar horarios disponibles.
- Restricción de exclusión de PostgreSQL para impedir solapamientos.
- Imágenes del catálogo como archivos estáticos del frontend, sin subida.
- Uso de `ProblemDetail` (RFC 7807) como formato único de error.

## ADR del proyecto

- [ADR-0001: UUID en identidades y perfiles](ADR-0001-uuid-en-identidades.md) — aceptado para
  `usuario`, `sesion_refresco` y `barbero`; no define el ID del catálogo de servicios.
- [ADR-0002: asignación barbero-servicio y consulta de citas futuras](ADR-0002-asignacion-barbero-servicio-y-consulta-citas.md)
  — disponibilidad integrada en PB-22; sigue pendiente el aviso de citas futuras al quitar una asignación.
- [ADR-0003: el perfil de barbero del administrador es opcional](ADR-0003-administrador-como-barbero-opcional.md)
  — el administrador se agrega explícitamente y se reutiliza su cuenta existente.
- [ADR-0004: cálculo de disponibilidad de citas](ADR-0004-calculo-de-disponibilidad.md)
  — reglas de cálculo, desempate de “cualquiera” y decisiones de PB-22.

## Qué NO va aquí

- Decisiones triviales o reversibles sin coste (nombre de una variable, orden de imports).
- Documentación de requisitos: eso es el SRS.
- Actas de reunión: el ADR resume la decisión, no la conversación.
- Credenciales, secretos o datos de clientes.

## Convenciones

- Un archivo por decisión, en Markdown, con el título `# ADR-0001: ...`.
- Los ADR aceptados no se reescriben: si la decisión cambia, se crea otro ADR que reemplaza al
  anterior y se actualiza el estado del primero.
- Se enlazan desde el Pull Request que implementa la decisión y desde `docs/arquitectura/`.
