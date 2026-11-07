# Catálogo (`pages/catalogo`)

Página pública en `/servicios`.

## Qué implementa

- **RF-07**: mostrar los servicios con nombre, duración, precio y foto.

## Qué debe ir aquí

- Una tarjeta por servicio con `ImagenServicio`, nombre, duración y precio formateado con
  `utils/moneda.ts`.
- El estado de carga, el error y el estado vacío ("todavía no hay servicios").
- El enlace a `/reservar` con el servicio elegido.

## Qué NO debe ir aquí

- Precios, duraciones o nombres de servicios escritos en el código.
- Cálculos de descuentos, promociones o disponibilidad.
- Subida de imágenes: la foto es un archivo de `public/servicios/` y la base solo guarda el nombre.
- Datos de ejemplo mientras no exista la API: se muestra un estado vacío explícito.
