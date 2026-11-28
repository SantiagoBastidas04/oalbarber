# ADR-0004: Cálculo de disponibilidad de citas

- **Fecha:** 2026-11-25
- **Estado:** aceptado para PB-22

## Contexto

PB-22 necesita ofrecer horarios según el servicio, el perfil de barbero, el horario semanal,
almuerzo, bloqueos y citas. La misma regla debe poder usarse desde la web y futuros canales, sin
guardar una disponibilidad que pueda quedar desactualizada. El SRS deja algunos detalles sujetos a
confirmación; el cálculo necesita una interpretación coherente para poder implementarse y probarse.

## Decisión

- Calcular los turnos al consultar desde `DisponibilidadService` en `cita`, usando `CalculadorHuecos`
  como algoritmo puro. No persistir horarios disponibles ni modificar citas existentes.
- Cargar horario, bloqueos, parámetros y citas por lote mediante los services de sus módulos. Solo
  cuentan barberos activos que ofrecen el servicio y citas cuyo estado no es `CANCELADA`.
- Usar intervalos semiabiertos `[inicio, fin)`. Una cita termina exactamente según la duración del
  servicio; bloquear almuerzo, bloqueos, citas y el margen configurado. El turno siguiente puede
  comenzar justo cuando termina una cita.
- Generar turnos cada `intervalo_turnos_minutos` desde el inicio de cada hueco libre. El valor
  inicial es 40 minutos (RN-07). No se ancla la secuencia a una hora fija del día.
- Aceptar un turno cuyo fin coincida con el cierre (`fin <= hora_fin`). Por ello, un servicio de 180
  minutos puede iniciar a las 16:00 y terminar a las 19:00, conforme a CA-03.
- Interpretar el máximo de siete días como una duración exacta: el inicio del turno debe ser menor
  o igual a `ahora + 7 días`; la anticipación mínima se calcula desde el mismo instante de referencia.
- En “cualquiera disponible”, unir los inicios de los barberos elegibles y devolver un solo turno
  por inicio. Asignarlo al perfil con menos citas vigentes que cruzan la fecha local consultada; en
  empate, elegir el UUID menor en orden natural.
- Si un horario no tiene almuerzo definido, no descontar ninguna franja. P-01 sigue pendiente de
  confirmación y no se inventan hora ni duración de almuerzo.
- Implementar P-02 con turnos exactos desde cada hueco, según los criterios CA-02 y CA-03. La
  confirmación del cliente sobre la regla general continúa pendiente.
- Mantener UUID en las entidades nuevas, en línea con `Usuario` y los repositorios existentes. El
  enum continúa usando `ADMINISTRADOR`; no se renombra a `ADMIN` en esta HU.
- Exponer el GET público `/api/disponibilidad`; las demás rutas mantienen la autenticación actual.

## Alternativas consideradas

- **Guardar disponibilidad en la base:** descartado porque cada cita, bloqueo o cambio de horario
  exigiría invalidar y reconstruir datos derivados.
- **Alinear los turnos a horas fijas:** descartado porque CA-02 pide que el siguiente turno pueda
  empezar al terminar la cita anterior.
- **Interpretar siete días como hasta el final del séptimo día calendario:** descartado para mantener
  un límite uniforme de 168 horas desde el instante actual.
- **Elegir un barbero por orden de consulta:** descartado porque la asignación podría cambiar según
  el orden de resultados. Menos citas y UUID menor producen un desempate determinista.

## Consecuencias y pendientes

- Horarios, cierres y almuerzo son datos del módulo `horario`; la configuración inicial guarda 24
  horas de anticipación, 7 días, intervalo de 40 minutos y margen cero.
- V9 carga los cuatro servicios con COP 1.00 como marcador de precio provisional por P-05; estos
  valores deben reemplazarse antes de publicar el catálogo real.
- La interpretación inclusiva del cierre cumple el ejemplo de 16:00–19:00 de CA-03, aunque el
  SRS use la frase “antes de las 19:00”.
- La definición exacta de P-01 y la confirmación de P-02 siguen abiertas con el cliente. El SRS se
  conserva sin cambios.
- El código usa UUID para `barberoId`, mientras el frontend existente lo declara `number`. Ese
  cambio queda pendiente para una HU de frontend; esta HU no modifica el frontend.
- El código usa `RolUsuario.ADMINISTRADOR`; la discrepancia del SRS y frontend con `ADMIN` queda
  documentada y no se cambia aquí.
- PB-22 no implementa creación/cancelación de citas ni CRUD de horarios, bloqueos o servicios.
