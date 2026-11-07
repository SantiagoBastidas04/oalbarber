# Panel (`pages/panel`)

Páginas privadas en `/panel/**`. El layout y la guarda de rol están en `routes/router.tsx`.

| Archivo | Ruta | Rol | RF |
|---|---|---|---|
| `AgendaPage` | `/panel/agenda` | ADMIN, BARBERO | RF-22 a RF-25 |
| `NuevaCitaPage` | `/panel/citas/nueva` | ADMIN | RF-26 |
| `ServiciosAdminPage` | `/panel/servicios` | ADMIN | RF-08 |
| `HorariosPage` | `/panel/horarios` | ADMIN | RF-11 a RF-14 |
| `BarberosPage` | `/panel/barberos` | ADMIN | RF-10 |
| `ClientesPage` | `/panel/clientes` | ADMIN | RF-05, RF-27 |

## Qué debe ir aquí

- La vista de agenda por día y semana; el barbero solo ve la suya y el administrador, ambas.
- Acciones de estado de una cita (`COMPLETADA`, `NO_ASISTIO`) y la cancelación por el panel.
- Formularios del catálogo, horarios, bloqueos, parámetros y barberos.
- El aviso de bloqueo con citas afectadas: se pide confirmación y **no** se cancelan solas.

## Qué NO debe ir aquí

- Reglas de disponibilidad, precios o plazos: las valida la API.
- Dejar que el barbero consulte o modifique la agenda del otro: lo impide el backend.
- Enviar un `rol` desde el formulario de creación de barbero: el service fuerza `BARBERO`.
- Inventar reportes, estadísticas u opiniones: están fuera de la fase 1.

## Convenciones

- Las páginas del panel no repiten la navegación: la aporta `LayoutPanel`.
- Los formularios usan `react-hook-form` + `zod` y muestran los errores de validación del backend
  (`erroresCampo` de `ApiError`).
- Las acciones destructivas piden confirmación y explican la consecuencia.
