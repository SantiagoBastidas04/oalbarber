# Fotos del catálogo

Carpeta de imágenes **estáticas** de los servicios. No hay subida ni almacenamiento: el
administrador no puede cargar archivos desde el panel en la fase 1.

## Cómo se usan

- La base de datos guarda solo el **nombre del archivo** en el campo `imagen` del servicio.
- El frontend arma la ruta `/servicios/<archivo>` con `components/comunes/ImagenServicio.tsx`.
- Si el archivo no existe o falla la carga, se muestra un marcador de posición accesible.

## Nombres

- Minúsculas, sin tildes, sin espacios y con guiones: `corte-normal.jpg`, `corte-premium.jpg`,
  `mechas.jpg`, `ondulacion.webp`.
- La extensión debe coincidir exactamente con la guardada en la base.
- Formatos recomendados: JPG o WebP optimizados para móvil (evitar archivos de varios MB).

## Qué NO debe ir aquí

- Imágenes de personas reales sin consentimiento ni fotos de clientes.
- Logos o contenido provisional que parezca definitivo.
- Un `index.html` o código: esta carpeta solo contiene archivos servidos tal cual.

## Pendiente

El cliente todavía no entregó las fotos. Cuando las envíe, se copian aquí con los nombres de
arriba y se actualiza el campo `imagen` de cada servicio en la base.
