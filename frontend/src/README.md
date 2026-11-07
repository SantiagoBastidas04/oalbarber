# Código fuente del frontend

Aplicación React + TypeScript + Vite. Hoy es un **esqueleto navegable**: las páginas son
marcadores con su título, los requisitos del SRS que implementarán y el texto "Pendiente de
implementar". No hay llamadas a la API al cargar ni lógica de negocio.

## Capas y dirección de las dependencias

```text
pages → layouts → components
  ↓        ↓
routes → context → services → config
  ↓
types · utils
```

| Carpeta | Responsabilidad |
|---|---|
| `config/` | Variables de entorno públicas, rutas y etiquetas de rol. |
| `routes/` | Definición de rutas y `RutaProtegida` (sesión y rol). |
| `layouts/` | Encabezado, navegación, pie y estructura de las páginas. |
| `context/` | Estado de sesión (`usuario`, `rol`, `estaAutenticado`). |
| `services/` | Cliente HTTP, token en memoria y llamadas de autenticación. |
| `hooks/` | Hooks reutilizables (`useAuth`). |
| `types/` | Tipos que reflejan los DTO del backend (propuesta). |
| `utils/` | Funciones puras de formato (fechas, moneda, celular). |
| `components/` | Piezas de interfaz reutilizables. |
| `pages/` | Una carpeta por ruta, con su `README.md` y sus RF. |
| `styles/` | Tema de Tailwind (paleta provisional). |
| `test/` | Configuración común de Vitest. |

## Qué NO debe ir en esta carpeta

- Reglas de negocio: disponibilidad, anticipación de 24 h, cancelación de 2 h, solapamientos
  y precios se calculan en el backend; el frontend solo los consume.
- Tokens en `localStorage` o `sessionStorage` (RNF-02): el acceso vive en memoria.
- Datos de ejemplo inventados (servicios, precios, horarios) en el código o en las pruebas.
- Secretos o variables sin prefijo `VITE_`.
- Lógica de la fase 2 (WhatsApp) ni una segunda implementación de la agenda.

## Convenciones

- Carpetas en minúsculas y en español sin tildes ni eñes; componentes en PascalCase y hooks o
  utilidades en camelCase.
- Los tipos de la API se llaman igual que los DTO del backend (`CitaResponse`, `ServicioResponse`).
- Fechas siempre como ISO-8601 con offset y formateadas en `America/Bogota`.
- Textos visibles en español; nombres técnicos estándar (`pages`, `routes`, `hooks`) en inglés.
- Sin `any`: si el tipo no se conoce, se declara como propuesta en `types/` y se anota.
