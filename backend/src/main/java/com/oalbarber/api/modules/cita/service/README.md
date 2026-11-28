# Servicios (lógica de negocio) — módulo `cita`

Paquete: `com.oalbarber.api.modules.cita.service`.

## Contexto del módulo

Cálculo de disponibilidad, creación y cancelación de citas, estados (CONFIRMADA, CANCELADA, COMPLETADA, NO_ASISTIO) y campo `canal` (WEB, WHATSAPP, ADMIN). Cada cita guarda inicio y fin exactos, duración y precio vigentes al reservar. El módulo debe poder usarse desde cualquier canal sin depender de HTTP.

## Qué debe ir en esta carpeta

- La lógica del módulo `cita`: `CitaService y DisponibilidadService` y las clases de apoyo que hagan falta (por ejemplo, cálculo de disponibilidad, validación de reglas o generación de tokens).
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

- Paquete `com.oalbarber.api.modules.cita.service`.
- Clases en PascalCase terminadas en `Service`: `CitaService y DisponibilidadService`. Solo se extrae una interfaz si hay varias implementaciones.
- Métodos en camelCase y en español: `crear`, `actualizar`, `cancelar`, `calcularDisponibilidad`.
- Nada de estado mutable compartido: los services son stateless.
- Los mensajes de las excepciones se escriben en español y son aptos para el cliente final.

## Detalle de este módulo

- `DisponibilidadService` consulta `ServicioService`, `BarberoService` y `HorarioService`, y usa
  `CitaService` para leer ocupaciones en lote. Devuelve turnos únicos por inicio para un barbero o
  para “cualquiera”.
- `CalculadorHuecos` es una clase pura: resta almuerzo, bloqueos, citas y margen con intervalos
  semiabiertos; cuenta turnos cada 40 minutos desde cada hueco y permite terminar exactamente al
  cierre. La anticipación y el máximo de días se leen de `configuracion`.
- `CitaService` implementa actualmente la lectura por lote de citas vigentes para disponibilidad.
  La creación, cancelación y transiciones de estado quedan para otras historias.
- La lógica de disponibilidad no depende de HTTP ni del `SecurityContext`; el endpoint público
  `GET /api/disponibilidad` solo adapta parámetros y devuelve `DisponibilidadResponse`.
- La disponibilidad se calcula en cada consulta, no mueve citas existentes y consulta a través de
  los services propietarios de cada módulo. El detalle de las decisiones está en
  [ADR-0004](../../../../../../../../../../docs/decisiones/ADR-0004-calculo-de-disponibilidad.md).

## Referencias

- Requisitos relacionados del SRS: RN-05 a RN-13, RN-16, RN-17, RF-15 a RF-26, RNF-06, RNF-15.
- Relación con otros módulos: Es el núcleo de la agenda. Usa `servicio` (duración y precio), `barbero` y `horario` (atención, almuerzo, bloqueos y parámetros). La web y, en fase 2, WhatsApp llaman a los mismos services; ningún canal accede a la base directamente ni duplica el cálculo.
