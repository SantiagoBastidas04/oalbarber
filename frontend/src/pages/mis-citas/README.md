# Mis citas (`pages/mis-citas`)

Página privada de cliente en `/mis-citas` (`RutaProtegida roles={['CLIENTE']}`).

## Qué implementa

- **RF-19**: listar las citas próximas y el historial del cliente.
- **RF-20**: cancelar una cita propia hasta 2 horas antes.
- **RF-21**: fuera de plazo, impedir la cancelación e indicar que contacte al local.

## Qué debe ir aquí

- Dos listas o pestañas: próximas e historial.
- El botón de cancelar con confirmación explícita y el estado del enum `EstadoCita`.
- La fecha y la hora formateadas con `utils/fechas.ts` en `America/Bogota`.
- El mensaje de error que devuelve la API cuando la cancelación no está permitida.

## Qué NO debe ir aquí

- El cálculo del plazo de 2 horas: la API decide y responde `409` o `422` con el motivo.
- Cambiar el estado de una cita en el cliente antes de que la API confirme.
- Reprogramar: está fuera de alcance en la fase 1 (lo hace el administrador desde el panel).
- Datos de otras personas: la API solo devuelve las citas del usuario autenticado.
