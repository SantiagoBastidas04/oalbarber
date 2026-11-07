# Rutas (`routes`)

## Qué debe ir aquí

- `router.tsx`: el árbol de rutas con `createBrowserRouter`. Las rutas públicas cuelgan de
  `LayoutPublico`; las del panel, de `LayoutPanel`.
- `RutaProtegida.tsx`: guarda de sesión y rol. Sin sesión redirige a `/ingresar` con el origen en
  `location.state.origen`; con sesión y rol no permitido, a `/no-autorizado`.

| Ruta | Página | Rol |
|---|---|---|
| `/` | `InicioPage` | Público |
| `/servicios` | `CatalogoPage` | Público |
| `/reservar` | `ReservaPage` | Público |
| `/registro` | `RegistroPage` | Público |
| `/ingresar` | `IngresarPage` | Público |
| `/mis-citas` | `MisCitasPage` | CLIENTE |
| `/panel/agenda` | `AgendaPage` | ADMIN, BARBERO |
| `/panel/citas/nueva` | `NuevaCitaPage` | ADMIN |
| `/panel/servicios` | `ServiciosAdminPage` | ADMIN |
| `/panel/horarios` | `HorariosPage` | ADMIN |
| `/panel/barberos` | `BarberosPage` | ADMIN |
| `/panel/clientes` | `ClientesPage` | ADMIN |
| `/no-autorizado` | `NoAutorizadoPage` | Público |
| `*` | `NoEncontradoPage` | Público |

## Qué NO debe ir aquí

- Peticiones a la API, cálculos de disponibilidad ni validaciones de negocio.
- Comprobaciones de rol hechas a mano dentro de una página: se usa `RutaProtegida`.
- Rutas duplicadas o escritas como cadenas sueltas: siempre desde `config/rutas.ts`.
- Protección real de datos: el backend valida permisos en cada endpoint (RNF-03). Estas guardas
  solo evitan mostrar pantallas que el usuario no puede usar.

## Convenciones

- Los hijos del panel usan rutas relativas (`agenda`, `citas/nueva`).
- Una ruta nueva se registra con `path` en minúsculas y sin verbos; la acción la expresa el
  componente, no la URL.
- El index de `/panel` redirige a `agenda`.
- Las pruebas de las guardas viven en `RutaProtegida.test.tsx` y `router.test.tsx`.
