# Páginas (`pages`)

Una carpeta por ruta, con su `README.md` y los RF del SRS que implementa. Hoy todas son
marcadores: título, requisitos y el texto "Pendiente de implementar".

| Carpeta | Página | Ruta | Rol |
|---|---|---|---|
| `inicio/` | `InicioPage` | `/` | Público |
| `catalogo/` | `CatalogoPage` | `/servicios` | Público |
| `reserva/` | `ReservaPage` | `/reservar` | Público |
| `cuenta/` | `RegistroPage`, `IngresarPage` | `/registro`, `/ingresar` | Público |
| `mis-citas/` | `MisCitasPage` | `/mis-citas` | CLIENTE |
| `panel/` | `AgendaPage` y las páginas de administración | `/panel/**` | ADMIN, BARBERO |
| `errores/` | `NoAutorizadoPage`, `NoEncontradoPage` | `/no-autorizado`, `*` | Público |

## Qué NO debe ir aquí

- Reglas de negocio: el frontend no calcula disponibilidad, no valida las 24 h de anticipación ni
  las 2 h de cancelación, y no decide precios.
- Datos de ejemplo o "mock" que parezcan reales: si falta la API, se muestra el estado vacío.
- Llamadas a la API al cargar la aplicación: cada página consulta cuando corresponde, con React
  Query, y maneja el error.
- Acceso a `localStorage` para sesión o tokens.
- Lógica de la fase 2.

## Convenciones

- Un archivo por página con el sufijo `Page`: `CatalogoPage.tsx` exporta `CatalogoPage`.
- La página compone layout y componentes; no implementa HTTP ni formato de fechas o moneda.
- Los textos visibles van en español y las fechas se muestran en `America/Bogota`.
- Cada página lleva un `<h1>` único y una estructura de landmarks coherente.
- Al implementar una página se actualiza su `README.md` y se marcan los RF cubiertos.
