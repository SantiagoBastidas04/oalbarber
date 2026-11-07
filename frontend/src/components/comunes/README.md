# Componentes comunes

Piezas pequeñas que comparten varias páginas.

## Qué debe ir aquí

- `EstadoVacio.tsx`: marcador de sección no implementada. Muestra el título
  "Pendiente de implementar" y el detalle con los RF que la cubrirán.
- `ImagenServicio.tsx`: muestra `/servicios/<archivo>` y, si falta el archivo o falla la carga,
  un marcador de posición con `role="img"` y texto alternativo.

## Qué NO debe ir aquí

- Lógica para obtener servicios, citas o disponibilidad.
- Estilos de una página concreta.
- Imágenes embebidas o rutas absolutas del sistema de archivos.
- Datos de ejemplo de servicios (la foto real se agrega en `public/servicios/`).

## Convenciones

- Componentes sin estado global; como máximo estado local de presentación.
- `ImagenServicio` recibe `archivo`, `nombre` y `className`; el nombre del archivo viene de la
  base de datos y solo se concatena con `/servicios/`.
- Un componente nuevo se usa en al menos dos páginas antes de mudarse aquí.
