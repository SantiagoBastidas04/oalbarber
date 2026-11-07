# Servicios (lógica de negocio) — módulo `horario`

Paquete: `com.oalbarber.api.modules.horario.service`.

## Contexto del módulo

Horario de atención por barbero, almuerzo, bloqueos de franjas o fechas completas y parámetros configurables (anticipación mínima, límite de cancelación, máximo de días adelante, intervalo entre turnos y margen entre citas).

## Qué debe ir en esta carpeta

- La lógica del módulo `horario`: `HorarioService` y las clases de apoyo que hagan falta (por ejemplo, cálculo de disponibilidad, validación de reglas o generación de tokens).
- Métodos públicos con Javadoc en español, indicando qué regla del SRS aplican y qué excepciones de negocio lanzan.
- Transacciones con `@Transactional` en los métodos que modifican datos; lecturas con `@Transactional(readOnly = true)` cuando corresponda.
- Uso de excepciones propias del dominio (`RecursoNoEncontradoException`, `HorarioNoDisponibleException`, etc.), nunca `null` ni códigos de error ambiguos.
- Dependencias por constructor (obligatorias, sin campos mutables) hacia repositorios y hacia services de otros módulos.

## Qué NO debe ir en esta carpeta

- Tipos HTTP: `HttpServletRequest`, `HttpServletResponse`, `ResponseEntity`, `@RequestHeader` o `SecurityContextHolder`. El service debe poder invocarse desde WhatsApp o desde una tarea programada.
- Anotaciones de autorización `@PreAuthorize`: la autorización se valida en el controlador o en el adaptador del canal.
- Entidades JPA como contrato público de vuelta al controlador: se devuelven DTOs o resultados de dominio internos.
- SQL manual, `EntityManager` o acceso a la base desde otro módulo.
- `try/catch` que ignoren errores o que devuelvan valores por defecto silenciosos.

## Convenciones de nombres

- Paquete `com.oalbarber.api.modules.horario.service`.
- Clases en PascalCase terminadas en `Service`: `HorarioService`. Solo se extrae una interfaz si hay varias implementaciones.
- Métodos en camelCase y en español: `crear`, `actualizar`, `cancelar`, `calcularDisponibilidad`.
- Nada de estado mutable compartido: los services son stateless.
- Los mensajes de las excepciones se escriben en español y son aptos para el cliente final.

## Detalle de este módulo

- `HorarioService` administra el horario semanal por barbero, el almuerzo, los bloqueos y los
  parámetros de `Configuracion`.
- Valida que las franjas estén dentro de la atención (10:00 a 19:00) y que el almuerzo no se
  solape con una cita existente.
- Al crear un bloqueo no cancela citas: pide al módulo `cita` la lista de citas afectadas para que
  el administrador confirme (RN-12/RF-13).
- Expone los parámetros vigentes a `cita` sin que este los lea de la base por su cuenta.
- No calcula horarios disponibles; entrega las reglas y los datos de tiempo.

## Referencias

- Requisitos relacionados del SRS: RN-01, RN-02, RN-12, RN-18, RF-11 a RF-14.
- Relación con otros módulos: Aporta las reglas de tiempo que `cita` usa para calcular disponibilidad. La disponibilidad no se guarda aquí: se calcula al consultar. No cancela citas al crear un bloqueo; solo informa si hay citas afectadas para pedir confirmación.
