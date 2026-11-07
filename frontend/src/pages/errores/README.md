# Errores (`pages/errores`)

## Qué debe ir aquí

- `NoAutorizadoPage` (`/no-autorizado`): la sesión existe pero el rol no permite la sección.
  Ofrece volver al inicio.
- `NoEncontradoPage` (`*`): cualquier ruta inexistente. Ofrece volver al inicio.

## Qué NO debe ir aquí

- Detalles técnicos, stack traces o mensajes internos de la API.
- Peticiones a la API: son páginas estáticas.
- Lógica de redirección automática que pueda crear bucles.
- Un mensaje que revele si un recurso existe pero pertenece a otra persona.

## Convenciones

- Título `h1` claro, una explicación breve y un enlace de salida.
- El código HTTP se muestra como texto (`403`, `404`) sin jerga.
- Si se agrega un error nuevo (por ejemplo, sesión expirada), se crea su página y se registra en
  `routes/router.tsx`.
