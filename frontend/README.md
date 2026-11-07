# Frontend — OalBarber

Aplicación React + TypeScript + Vite + Tailwind CSS. Es la web de reservas de la fase 1: los
clientes eligen servicio y barbero, reservan y gestionan sus citas; el administrador y el barbero
trabajan desde el panel.

## Estado actual

**Esqueleto navegable, sin lógica de negocio.** Todas las rutas de la fase 1 existen y se pueden
recorrer, pero las páginas son marcadores con su título, los requisitos del SRS que
implementarán y el texto "Pendiente de implementar".

- No hay llamadas a la API al cargar la aplicación.
- No se muestran servicios, precios ni horarios inventados.
- La autenticación está preparada (cliente HTTP, token en memoria, contexto y guardas de rol),
  pero los formularios de registro e ingreso todavía no envían datos.
- El backend actual responde `401` en todo `/api/**` salvo `/actuator/health`; es lo esperado
  hasta que exista el módulo `auth`.

## Alcance de la fase 1

- Página de inicio con la presentación del negocio, el horario de atención y el acceso al
  catálogo y a la reserva.
- Catálogo de servicios con nombre, duración, precio y foto.
- Flujo de reserva: elegir servicio, barbero (o "cualquiera disponible", por confirmar), fecha y
  horario; confirmar la cita.
- Registro e inicio de sesión de clientes con nombre, celular y contraseña.
- "Mis citas": próximas e historial, con cancelación hasta 2 horas antes.
- Paneles de administración y de barbero según el rol.

WhatsApp es fase 2: no se implementa aquí.

## Estructura

```text
frontend/
├── Dockerfile · nginx.conf · .dockerignore · .env.example
├── index.html · package.json · package-lock.json · vite.config.ts
├── tsconfig.json · tsconfig.app.json · tsconfig.node.json · .oxlintrc.json
├── public/
│   ├── favicon.svg
│   └── servicios/            fotos estáticas del catálogo (solo .gitkeep y README.md)
└── src/
    ├── main.tsx              QueryClient + AuthProvider + router
    ├── config/               env.ts, roles.ts, rutas.ts
    ├── routes/               router.tsx, RutaProtegida.tsx
    ├── layouts/              LayoutPublico.tsx, LayoutPanel.tsx
    ├── context/              AuthContext.tsx
    ├── services/             apiClient.ts, tokenStore.ts, authService.ts
    ├── hooks/                useAuth.ts
    ├── types/                auth, servicio, barbero, cita, disponibilidad, error
    ├── utils/                fechas.ts, moneda.ts, celular.ts (+ pruebas)
    ├── components/comunes/   ImagenServicio.tsx, EstadoVacio.tsx
    ├── pages/                inicio, catalogo, reserva, cuenta, mis-citas, panel, errores
    ├── styles/               index.css (tema de Tailwind)
    └── test/                 setup.ts
```

Cada carpeta de `src/` y cada subcarpeta de `pages/` tiene su propio `README.md` con lo que va y
lo que no. Léelo antes de agregar código.

## Scripts

| Comando | Qué hace |
|---|---|
| `npm install` | Instala dependencias (la primera vez). |
| `npm run dev` | Servidor de desarrollo en `http://localhost:5173` con proxy de `/api`. |
| `npm run build` | Verifica tipos y genera `dist/`. |
| `npm run preview` | Sirve `dist/` en local para revisar el build. |
| `npm run lint` | Oxlint (el linter de la plantilla; no hay ESLint ni Prettier). |
| `npm run typecheck` | `tsc -b` sin emitir JavaScript. |
| `npm test` | Vitest una vez (entorno `jsdom`). |
| `npm run test:watch` | Vitest en modo interactivo. |

Antes de un Pull Request de frontend: `npm run lint && npm run typecheck && npm test && npm run build`.

## Cómo ejecutarlo en local

La API corre en Docker; el frontend, con Vite, que hace proxy de `/api` para evitar CORS:

```bash
# desde la raíz del monorepo
docker compose up -d postgres api

# en otra terminal
cd frontend
npm install
npm run dev
```

- Frontend: `http://localhost:5173`
- API a través del proxy: `http://localhost:5173/api/...`
- El proxy apunta a `http://localhost:8080` por defecto. Si la API usa otro puerto:

```powershell
$env:VITE_DEV_API_TARGET="http://localhost:8081"; npm run dev
```

```bash
VITE_DEV_API_TARGET=http://localhost:8081 npm run dev
```

`VITE_DEV_API_TARGET` se lee del entorno del proceso al arrancar Vite, no de `frontend/.env`.

## Cómo ejecutarlo con Docker

```bash
# desde la raíz del monorepo
docker compose build frontend
docker compose up -d
```

- SPA: `http://localhost:3000`
- El contenedor escucha en `8080` y nginx hace proxy de `/api/` al servicio `api` de la red
  `oalbarber_net`, conservando el prefijo.
- `frontend` depende de `api` con `condition: service_started`, así que arranca aunque la API tarde;
  mientras no esté lista, `/api/**` responde `502`.
- Si la API ya está construida, `docker compose up --build` levanta todo.

## Variables de entorno

| Variable | Dónde | Uso |
|---|---|---|
| `VITE_API_BASE_URL` | `frontend/.env` o build arg | Base de la API que usa el navegador. Por defecto `/api`. |
| `VITE_DEV_API_TARGET` | entorno del proceso | Destino del proxy de Vite en desarrollo. Por defecto `http://localhost:8080`. |
| `FRONTEND_PORT` | `.env` de la raíz | Puerto del host que publica la SPA en Docker. Por defecto `3000`. |

Solo se usan variables públicas con prefijo `VITE_`: todo lo que empieza por `VITE_` se incrusta
en el JavaScript del navegador. **Nunca** se ponen secretos. El token de acceso vive solo en
memoria (`services/tokenStore.ts`); el refresh token va en una cookie `HttpOnly` (RNF-02).

## Rutas y requisitos

| Ruta | Página | Rol | RF |
|---|---|---|---|
| `/` | `InicioPage` | Público | RF-09 |
| `/servicios` | `CatalogoPage` | Público | RF-07 |
| `/reservar` | `ReservaPage` | Público | RF-15 a RF-18 |
| `/registro` | `RegistroPage` | Público | RF-01 |
| `/ingresar` | `IngresarPage` | Público | RF-02, RF-04 |
| `/mis-citas` | `MisCitasPage` | CLIENTE | RF-19 a RF-21 |
| `/panel/agenda` | `AgendaPage` | ADMIN, BARBERO | RF-22 a RF-25 |
| `/panel/citas/nueva` | `NuevaCitaPage` | ADMIN | RF-26 |
| `/panel/servicios` | `ServiciosAdminPage` | ADMIN | RF-08 |
| `/panel/horarios` | `HorariosPage` | ADMIN | RF-11 a RF-14 |
| `/panel/barberos` | `BarberosPage` | ADMIN | RF-10 |
| `/panel/clientes` | `ClientesPage` | ADMIN | RF-05, RF-27 |
| `/no-autorizado` | `NoAutorizadoPage` | Público | — |
| `*` | `NoEncontradoPage` | Público | — |

`RutaProtegida` redirige a `/ingresar` (guardando el origen) cuando no hay sesión y a
`/no-autorizado` cuando el rol no está permitido. Es una ayuda de navegación, no seguridad: el
backend valida permisos en cada endpoint (RNF-03).

## API que el frontend espera (propuesta)

Este contrato lo propone el frontend a partir del SRS y de las convenciones de
[`docs/api/README.md`](../docs/api/README.md). **El responsable del backend lo valida** y después
se publica en `docs/api/openapi.yaml`; hasta entonces puede cambiar.

| Método y ruta | Acceso | RF |
|---|---|---|
| `POST /api/auth/registro` | Público | RF-01 |
| `POST /api/auth/login` | Público | RF-02 |
| `POST /api/auth/refresh` | Cookie de refresh | RF-03 |
| `POST /api/auth/logout` | Autenticado | RF-04 |
| `GET /api/servicios` | Público | RF-07 |
| `POST /api/servicios` · `PUT\|PATCH /api/servicios/{id}` | ADMIN | RF-08 |
| `GET /api/barberos` | Público | RF-15 |
| `GET /api/disponibilidad?servicioId&fecha&barberoId?` | Público | RF-16 |
| `POST /api/citas` | CLIENTE | RF-17, RF-18 |
| `GET /api/citas/mias` | CLIENTE | RF-19 |
| `POST /api/citas/{id}/cancelar` | CLIENTE (la propia) | RF-20, RF-21 |
| `GET /api/citas?fecha&barberoId` | ADMIN; BARBERO solo las suyas | RF-22, RF-23 |
| `PATCH /api/citas/{id}/estado` | ADMIN; BARBERO solo las suyas | RF-25 |
| `POST /api/citas/manual` | ADMIN | RF-26 |
| `/api/horarios` | ADMIN | RF-11 |
| `/api/bloqueos` | ADMIN | RF-12, RF-13 |
| `/api/configuracion` | ADMIN | RF-14 |
| `GET /api/usuarios?rol=CLIENTE` | ADMIN | RF-27 |
| `PATCH /api/usuarios/{id}/estado` | ADMIN | RF-27 |
| `POST /api/usuarios/{id}/clave-temporal` | ADMIN | RF-05 |

Convenciones: prefijo `/api`, recursos en plural y en español, fechas ISO-8601 con offset de
Bogotá (`2026-11-10T10:00:00-05:00`) y errores `ProblemDetail` (RFC 7807).

## Imágenes del catálogo: archivos estáticos

**No hay subida ni almacenamiento de imágenes.** Las fotos son archivos estáticos en
`public/servicios/`:

```text
public/servicios/
├── corte-normal.jpg
├── corte-premium.jpg
├── mechas.jpg
└── ondulacion.jpg
```

La base de datos solo guarda el **nombre del archivo** (campo `imagen` del servicio). El frontend
construye la ruta `/servicios/<archivo>` y muestra un marcador de posición si falta el archivo o
falla la carga (`components/comunes/ImagenServicio.tsx`). Los nombres van en minúsculas, sin
tildes ni espacios, con guiones. Ver [`public/servicios/README.md`](public/servicios/README.md).

En nginx, `/servicios/` tiene su propio `location` que sirve solo archivos de esa carpeta (una
foto inexistente responde `404`), mientras que `/servicios` sin barra final la atiende el router
de React. Por eso el `try_files` del fallback de la SPA no usa `$uri/`: las carpetas no deben
provocar un `301` que saque al usuario de la aplicación.

## Cómo agregar una página

1. Crear `src/pages/<carpeta>/<Nombre>Page.tsx` con su `README.md` (RF que cubre).
2. Añadir la ruta a `RUTAS` en `src/config/rutas.ts`.
3. Registrarla en `src/routes/router.tsx`; si exige sesión o rol, envolverla en `RutaProtegida`.
4. Si aparece en la navegación, agregarla en `LayoutPublico` o en `ITEMS_PANEL`.
5. Actualizar la tabla de rutas de este README.

## Cómo agregar un servicio de API

1. Añadir o completar los tipos en `src/types/`.
2. Crear `src/services/<recurso>Service.ts` y usar `apiClient` (nunca `axios` directo) para
   heredar el token y el manejo de `401`.
3. Envolver la llamada en un hook de `src/hooks/` con su clave de React Query.
4. Documentar el endpoint en la sección "API que el frontend espera" y avisar al responsable del
   backend para que lo valide.

## Cómo agregar un tipo

1. Comprobar el DTO real del backend; el nombre debe coincidir (`CitaResponse`, no `Cita`).
2. Declararlo en `src/types/<recurso>.ts` con campos en camelCase e importarlo con `import type`.
3. Fechas como `string` ISO-8601, nunca `Date`; precios como `number`.
4. Si el contrato aún no existe, anotarlo como propuesta en el `README.md` de `src/types/`.

## Qué NO debe ir en esta carpeta

- Lógica de negocio de reservas, disponibilidad o precios: vive en el backend y el frontend solo
  la consume.
- Credenciales, secretos o claves de API en el código o en el repositorio.
- Subida de imágenes ni un servicio de almacenamiento de archivos.
- `node_modules/` ni `dist/`: están en `.gitignore`.
- Datos de clientes reales en archivos de ejemplo o pruebas.
- Tokens en `localStorage` o `sessionStorage`.

## Convenciones

- Componentes en PascalCase (`BotonReserva.tsx`); utilidades y hooks en camelCase
  (`useDisponibilidad.ts`).
- Carpetas en minúsculas y en español sin tildes ni eñes.
- Los tipos de la API se nombran como los DTOs del backend (`CitaResponse`, `ServicioResponse`).
- Los textos visibles van en español; las fechas se muestran en `America/Bogota`.
- Variables de entorno del frontend con prefijo `VITE_` y solo valores públicos.
- Diseño móvil primero: objetivos táctiles de al menos 44 px, foco visible y contraste AA.
- El branding vive en `src/styles/index.css` (tokens de Tailwind); los componentes no usan
  colores literales.
