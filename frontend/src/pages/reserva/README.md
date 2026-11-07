# Reserva (`pages/reserva`)

Página pública en `/reservar`.

## Qué implementa

- **RF-15**: elegir servicio, barbero (o "cualquiera disponible") y fecha.
- **RF-16**: mostrar solo los horarios válidos según duración, huecos, anticipación y máximo de
  días.
- **RF-17**: al confirmar, guardar inicio, fin, precio, duración y canal (lo hace el backend).
- **RF-18**: si el horario se ocupó, rechazar y mostrar horarios actualizados.

## Qué debe ir aquí

- El flujo por pasos y el resumen antes de confirmar.
- La consulta de disponibilidad a `GET /api/disponibilidad` con `servicioId`, `fecha` y
  `barberoId` opcional.
- El manejo del `409` de la API: mensaje claro y recarga de horarios.
- El `400` de validación con los campos señalados.

## Qué NO debe ir aquí

- El cálculo de huecos, la resta de almuerzo y bloqueos, ni el intervalo de 40 minutos.
- Las validaciones de 24 h de anticipación, una semana de máximo o cierre a las 19:00.
- La decisión de qué barbero asignar cuando se elige "cualquiera": la resuelve el backend.
- La creación de la cita en dos pasos: el backend guarda en una sola transacción.
