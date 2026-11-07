# Mappers (entidad ↔ DTO) — módulo `cita`

Paquete: `com.oalbarber.api.modules.cita.mapper`.

## Contexto del módulo

Cálculo de disponibilidad, creación y cancelación de citas, estados (CONFIRMADA, CANCELADA, COMPLETADA, NO_ASISTIO) y campo `canal` (WEB, WHATSAPP, ADMIN). Cada cita guarda inicio y fin exactos, duración y precio vigentes al reservar. El módulo debe poder usarse desde cualquier canal sin depender de HTTP.

## Qué debe ir en esta carpeta

- La conversión entre entidades del módulo `cita` y sus DTOs: `CitaMapper`.
- Métodos explícitos `aEntidad`, `aResponse`, `aListaResponse` y los que hagan falta, con correspondencia campo a campo.
- Conversión de enums, fechas y valores monetarios en un solo lugar, para que el formato sea consistente.
- Mapeo seguro frente a nulos y a relaciones no cargadas (solo se accede a lo que el service ya preparó).

## Qué NO debe ir en esta carpeta

- Consultas a repositorios, transacciones o acceso a la base.
- Reglas de negocio: no decide estados, disponibilidad, precios ni permisos.
- Serialización JSON manual ni anotaciones de controladores.
- Exposición de campos sensibles (`password_hash`) aunque existan en la entidad.
- Lógica de presentación específica del frontend (colores, textos de interfaz).

## Convenciones de nombres

- Paquete `com.oalbarber.api.modules.cita.mapper`.
- Clase en PascalCase terminada en `Mapper`: `CitaMapper`.
- Métodos en español: `aEntidad`, `aResponse`, `aListaResponse`.
- Implementación con métodos estáticos en una clase final o como `@Component`; no se mezclan ambos estilos en el mismo módulo.
- Si el mapeo se vuelve complejo, se justifica y se documenta, pero no se delega en la entidad.

## Referencias

- Requisitos relacionados del SRS: RN-05 a RN-13, RN-16, RN-17, RF-15 a RF-26, RNF-06, RNF-15.
- Relación con otros módulos: Es el núcleo de la agenda. Usa `servicio` (duración y precio), `barbero` y `horario` (atención, almuerzo, bloqueos y parámetros). La web y, en fase 2, WhatsApp llaman a los mismos services; ningún canal accede a la base directamente ni duplica el cálculo.
