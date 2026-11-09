# Componentes comunes

Piezas pequeñas que comparten varias páginas.

## Qué debe ir aquí

- `EstadoVacio.tsx`: marcador de sección no implementada. Muestra el título
  "Pendiente de implementar" y el detalle con los RF que la cubrirán.
- `ImagenServicio.tsx`: muestra `/servicios/<archivo>` y, si falta el archivo o falla la carga,
  un marcador de posición con `role="img"` y texto alternativo.
- `Marca.tsx`: wordmark de OalBarber con Bebas Neue. El punto y el espaciado reaccionan al pasar el
  cursor cuando el enlace que lo contiene lleva `group`. Se usa en el encabezado, el pie y el panel
  oscuro del registro.
- `Bandera.tsx`: bandera SVG de un país por código ISO (`CO`, `EC`, …), tomada de
  `country-flag-icons`. No usa emojis porque Windows no dibuja banderas con emojis.
- `SelectorPais.tsx`: desplegable de prefijo telefónico con bandera y código. Implementa el
  patrón WAI-ARIA `listbox` (flechas, Home/End, Enter, Escape, clic fuera) porque un `<select>`
  nativo no puede mostrar SVG. Se usa en el formulario de registro.

## Qué NO debe ir aquí

- Lógica para obtener servicios, citas o disponibilidad.
- Estilos de una página concreta.
- Imágenes embebidas o rutas absolutas del sistema de archivos.
- Datos de ejemplo de servicios (la foto real se agrega en `public/servicios/`).

## Convenciones

- Componentes sin estado global; como máximo estado local de presentación.
- `ImagenServicio` recibe `archivo`, `nombre` y `className`; el nombre del archivo viene de la
  base de datos y solo se concatena con `/servicios/`.
- `SelectorPais` recibe la lista de países (`utils/celularPais.ts`), el prefijo actual y un
  `onCambio`; no conoce el formulario ni hace peticiones.
- Un componente nuevo se usa en al menos dos páginas antes de mudarse aquí (las banderas y el
  selector son la excepción prevista para reutilizarse en el ingreso y el panel).
