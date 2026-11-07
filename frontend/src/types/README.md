# Tipos (`types`)

## Qué debe ir aquí

- Los tipos que reflejan los DTO del backend, con el mismo nombre: `UsuarioResponse`,
  `LoginRequest`, `RegistroRequest`, `AuthResponse`, `ServicioResponse`, `BarberoResponse`,
  `HorarioDisponibleResponse`, `CitaResponse`, `CrearCitaRequest`.
- Los enums del dominio como uniones de literales: `Rol`, `EstadoCita`, `Canal`.
- `ApiError`, la forma normalizada del error que consume la interfaz.

## Qué NO debe ir aquí

- Lógica, validaciones en tiempo de ejecución ni funciones: para validar formularios se usa `zod`
  en la página correspondiente.
- Entidades de la base ni nombres de tablas: el frontend solo ve DTO.
- `any` o tipos "comodín" con todos los campos posibles.
- Fechas como `Date`: viajan como cadenas ISO-8601 con offset
  (`2026-11-10T10:00:00-05:00`) y se formatean con `utils/fechas.ts`.

## Convenciones

- Un archivo por recurso: `auth.ts`, `servicio.ts`, `barbero.ts`, `cita.ts`, `disponibilidad.ts`,
  `error.ts`.
- Interfaces en PascalCase; campos en camelCase, igual que el JSON de la API.
- Este contrato es una **propuesta del frontend**: el responsable del backend lo confirma y luego
  se publica en `docs/api/openapi.yaml`.
- Si el backend renombra un campo, se actualiza el tipo y el componente que lo usa en el mismo
  Pull Request.
