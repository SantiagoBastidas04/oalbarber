# Fotos de barberos

Carpeta de imágenes estáticas referidas por el campo `foto` del perfil. El formulario del panel
solo guarda el nombre; no sube archivos ni permite explorar carpetas.

Los nombres deben ser simples, en minúsculas y con extensión `.jpg`, `.jpeg`, `.png` o `.webp`,
por ejemplo `barbero-01.webp`. No se agregan fotos reales ni provisionales sin autorización.

El servidor devuelve `404` si una imagen solicitada no existe. El cliente todavía no ha entregado
fotos; esta carpeta conserva únicamente `.gitkeep` hasta que se proporcionen.
