# Layouts (`layouts`)

## Qué debe ir aquí

- `LayoutPublico.tsx`: encabezado con navegación (Inicio, Servicios, Reservar y, según la sesión,
  Ingresar, Mis citas o Panel), menú móvil, enlace "Saltar al contenido", `main` y pie con los
  datos del negocio marcados como `[POR DEFINIR]`.
- `LayoutPanel.tsx`: estructura del panel con navegación lateral en escritorio y una fila
  desplazable en móvil, filtrada por el rol de la sesión.

## Qué NO debe ir aquí

- Reglas de negocio ni llamadas a la API.
- Contenido de cada página: el layout solo aporta el marco y el `Outlet`.
- Listas de enlaces duplicadas para cada rol escritas a mano: se filtran desde `ITEMS_PANEL` con
  el arreglo `roles`.
- Colores literales: se usan los tokens del tema (`marca-*`, `fondo`, `texto`, `borde`).

## Convenciones y accesibilidad

- Landmarks semánticos: `header`, `nav` con `aria-label`, `main` con `id` y `tabIndex={-1}`,
  `footer`.
- Diseño móvil primero; los enlaces y botones tienen al menos 44 px de alto (`min-h-11`).
- El menú móvil usa `aria-expanded` y `aria-controls`; se cierra al navegar.
- Foco visible global definido en `styles/index.css`; contraste AA con los tonos `marca-700` y
  `texto` sobre `superficie`.
- Un layout nuevo debe renderizar `<Outlet />` y su propio enlace "Saltar al contenido".
