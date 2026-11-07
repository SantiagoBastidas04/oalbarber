# Estilos (`styles`)

## Qué debe ir aquí

- `index.css`: punto único del tema de Tailwind v4.
- Los tokens de `@theme`: la paleta de marca (`marca-50` a `marca-900`), las superficies
  (`fondo`, `superficie`), el texto (`texto`, `texto-suave`), los bordes y la tipografía del
  sistema.
- Los estilos base: color de fondo, tipografía y anillo de foco visible.

## Qué NO debe ir aquí

- Colores hexadecimales dentro de componentes o páginas: se usan las clases del tema
  (`bg-superficie`, `text-texto-suave`, `border-borde`).
- Fuentes externas o archivos de imágenes.
- Estilos de una sola página: se resuelven con utilidades de Tailwind en el componente.
- Reglas de negocio o clases generadas por JavaScript.

## Cómo cambiar el branding

El cliente todavía no tiene logo ni colores. Cuando los entregue:

1. Cambiar los valores de `--color-marca-*` en `index.css`.
2. Ajustar `--color-fondo`, `--color-superficie` y `--color-texto` si cambia el modo claro.
3. Revisar el contraste AA de los textos sobre las nuevas superficies.
4. Reemplazar `public/favicon.svg`.

No hace falta tocar ningún componente: todos usan los tokens del tema.
