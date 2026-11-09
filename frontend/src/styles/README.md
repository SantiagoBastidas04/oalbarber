# Estilos (`styles`)

## Qué debe ir aquí

- `index.css`: punto único del tema de Tailwind v4.
- Los tokens de `@theme`: la paleta monocroma (`marca-50` a `marca-950`), las superficies
  (`fondo`, `superficie`), el texto (`texto`, `texto-suave`), los bordes, el color funcional de
  error y las familias tipográficas.
- Los estilos base: color de fondo, tipografía, selección, foco visible y desplazamiento táctil.

## Tipografía

Cuatro variables, disponibles como utilidades `font-marca`, `font-display`, `font-sans` y `font-mono`:

| Variable         | Utilidad       | Uso                                                       |
| ---------------- | -------------- | --------------------------------------------------------- |
| `--font-marca`   | `font-marca`   | Wordmark «OalBarber» en mayúsculas (Bebas Neue)            |
| `--font-display` | `font-display` | Títulos y cifras grandes (Playfair Display)                |
| `--font-sans`    | `font-sans`    | Cuerpo, interfaz y formularios (Inter); es la base         |
| `--font-mono`    | `font-mono`    | Etiquetas, códigos, cifras y microtexto (JetBrains Mono)   |

La combinación sigue la recomendación *Classic Elegant* de `ui-ux-pro-max` (Playfair Display +
Inter) con JetBrains Mono para las etiquetas. Inter se eligió como base porque es más legible que
una serif en pantallas pequeñas y evita el zoom automático de iOS en los campos (16 px).

La marca se separó de los títulos con **Bebas Neue**, tomada de la pareja *Bold Statement* de la
misma skill: condensada, en mayúsculas y con fuerza de barbería. Se usa solo en el wordmark
(`components/comunes/Marca.tsx`), nunca en párrafos, para no perder legibilidad.

Existe además la variante `corto`, definida con `@custom-variant`: se activa en escritorio con
ventanas de hasta 780 px de alto (`corto:py-4`, `corto:space-y-3`). Sirve para que el registro
entre completo en portátiles de 1366×768 sin apretar el diseño móvil.

Las cuatro familias se cargan desde Google Fonts en `index.html`, con una pila de respaldo del
sistema por si no hay red. Para cambiar la tipografía se editan las variables en `index.css` y el
enlace de `index.html`; ningún componente menciona el nombre de una fuente.

`body` usa `font-sans`; los encabezados `h1`, `h2` y `h3` usan `font-display` desde la capa base.
Las etiquetas de formulario y los botones usan `font-mono` en mayúsculas con `tracking` amplio.
La marca usa `font-marca` con `tracking` amplio, y su punto pasa de círculo a rombo al pasar el
cursor.

## Paleta y bordes

Monocroma pura: fondo `#fafafa`, superficie `#ffffff`, negro `#0a0a0a` y grises neutros sin tinte
cálido. El texto secundario (`texto-suave`, `#525252`) mantiene un contraste de 7.4:1 sobre blanco.
El único color funcional es `error` (`#b91c1c`), reservado para mensajes de validación.

Los bordes son visibles a propósito: las tarjetas usan `border-marca-300` con un remate superior
negro (`border-t-2 border-t-marca-900`) y los campos `border-marca-300` que se oscurecen al pasar
el cursor y al enfocar.

## Qué NO debe ir aquí

- Colores hexadecimales dentro de componentes o páginas: se usan las clases del tema
  (`bg-superficie`, `text-texto-suave`, `border-borde`, `text-error`).
- Estilos de una sola página: se resuelven con utilidades de Tailwind en el componente.
- Reglas de negocio o clases generadas por JavaScript.

## Cómo cambiar el branding

1. Cambiar los valores de `--color-marca-*` en `index.css`.
2. Ajustar `--color-fondo`, `--color-superficie` y `--color-texto` si cambia el modo claro.
3. Revisar el contraste AA de los textos sobre las nuevas superficies.
4. Reemplazar `public/favicon.svg` y el `theme-color` de `index.html`.

No hace falta tocar ningún componente: todos usan los tokens del tema.
