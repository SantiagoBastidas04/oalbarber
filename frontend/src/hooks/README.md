# Hooks (`hooks`)

## Qué debe ir aquí

- `useAuth.ts`: acceso tipado a `AuthContext`; lanza un error claro si se usa fuera de
  `<AuthProvider>`.
- En el futuro, hooks de React Query por recurso (`useServicios`, `useDisponibilidad`,
  `useMisCitas`), siempre envolviendo un servicio de `services/`.

## Qué NO debe ir aquí

- Reglas de negocio ni cálculos de disponibilidad, precios o plazos.
- Llamadas HTTP escritas con `axios` a mano: se usan las funciones de `services/`.
- Estado global de la sesión: vive en `context/AuthContext.tsx`.
- Componentes o JSX.
- Hooks que guarden tokens fuera de memoria.

## Convenciones

- Nombre en camelCase con prefijo `use` (`useAuth`, `useServicios`).
- Un archivo por hook.
- Las claves de caché de React Query se definen junto al hook y son estables
  (`['servicios']`, `['citas', 'mias']`).
- Sin `useEffect` para sincronizar estado derivado: se calcula en el render.
