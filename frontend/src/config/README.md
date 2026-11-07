# Configuración (`config`)

Paquete de constantes de la aplicación. No contiene estado ni componentes.

## Qué debe ir aquí

- `env.ts`: única puerta a `import.meta.env`. Expone `env.apiBaseUrl` (por defecto `/api`),
  normalizado sin barra final.
- `rutas.ts`: todas las rutas de la fase 1 en el objeto `RUTAS`. Ningún componente escribe una
  ruta a mano: se importa de aquí.
- `roles.ts`: `ROLES` y `ETIQUETAS_ROL` para los tres roles del SRS.

## Qué NO debe ir aquí

- Secretos, tokens o valores privados: todo lo que se lea con `import.meta.env` y empiece por
  `VITE_` queda visible en el navegador.
- Reglas de negocio: no se decide aquí quién puede reservar ni qué horarios son válidos.
- Textos largos de interfaz ni contenido de las páginas.
- Configuración de Vite o de Docker: viven en `vite.config.ts`, `nginx.conf` y `Dockerfile`.

## Convenciones

- Un archivo por tema; nombres en camelCase (`env.ts`, `rutas.ts`).
- Constantes en MAYÚSCULAS y `as const` para que TypeScript conserve los literales.
- Al agregar una ruta: se añade a `RUTAS`, se registra en `routes/router.tsx`, se crea la página
  con su `README.md` y se actualiza la tabla del `frontend/README.md`.
- `VITE_API_BASE_URL` se cambia solo si la API deja de estar bajo `/api`; el valor por defecto
  funciona en desarrollo (proxy de Vite) y en Docker (proxy de nginx).
