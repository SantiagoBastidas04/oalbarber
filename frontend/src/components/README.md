# Componentes (`components`)

## Qué debe ir aquí

- Componentes de interfaz reutilizables por varias páginas, agrupados por tema en subcarpetas.
- Piezas sin conocimiento de la API: reciben datos ya resueltos por props.
- Componentes accesibles por defecto (foco visible, texto alternativo, roles ARIA correctos).

## Qué NO debe ir aquí

- Peticiones a la API, reglas de negocio ni cálculos de disponibilidad o precios.
- Componentes usados por una sola página: se quedan junto a esa página.
- Estado global: vive en `context/` o en React Query.
- Textos que dependan de datos inventados o de configuración del negocio.
- Una librería de componentes propia: se usan clases de Tailwind y los tokens del tema.

## Convenciones

- Carpeta en minúsculas y en español (`comunes/`, `catalogo/`, `reserva/`).
- Archivo y componente en PascalCase: `ImagenServicio.tsx` exporta `ImagenServicio`.
- Props tipadas con una interfaz `Props` local; sin `any`.
- Los colores salen del tema (`bg-superficie`, `text-texto-suave`, `border-borde`), nunca de
  valores hexadecimales sueltos.
