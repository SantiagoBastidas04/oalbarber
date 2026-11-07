# Mappers (entidad ↔ DTO) — módulo `horario`

Paquete: `com.oalbarber.api.modules.horario.mapper`.

## Contexto del módulo

Horario de atención por barbero, almuerzo, bloqueos de franjas o fechas completas y parámetros configurables (anticipación mínima, límite de cancelación, máximo de días adelante, intervalo entre turnos y margen entre citas).

## Qué debe ir en esta carpeta

- La conversión entre entidades del módulo `horario` y sus DTOs: `HorarioMapper, BloqueoMapper y ConfiguracionMapper`.
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

- Paquete `com.oalbarber.api.modules.horario.mapper`.
- Clase en PascalCase terminada en `Mapper`: `HorarioMapper, BloqueoMapper y ConfiguracionMapper`.
- Métodos en español: `aEntidad`, `aResponse`, `aListaResponse`.
- Implementación con métodos estáticos en una clase final o como `@Component`; no se mezclan ambos estilos en el mismo módulo.
- Si el mapeo se vuelve complejo, se justifica y se documenta, pero no se delega en la entidad.

## Referencias

- Requisitos relacionados del SRS: RN-01, RN-02, RN-12, RN-18, RF-11 a RF-14.
- Relación con otros módulos: Aporta las reglas de tiempo que `cita` usa para calcular disponibilidad. La disponibilidad no se guarda aquí: se calcula al consultar. No cancela citas al crear un bloqueo; solo informa si hay citas afectadas para pedir confirmación.
