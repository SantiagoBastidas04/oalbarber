# Tipos (`types`)

## Qué debe ir aquí

- Tipos que reflejan los DTO reales del backend, con el mismo nombre: `UsuarioResponse`,
  `LoginRequest`, `RegistroRequest`, `AuthResponse`, `ServicioResponse`,
  `BarberoPublicoResponse`, `BarberoAdminResponse`, `BarberoRequest`,
  `CambiarEstadoBarberoRequest`, `HorarioDisponibleResponse`, `CitaResponse` y
  `CrearCitaRequest`.
- Los enums del dominio como uniones de literales: `Rol`, `EstadoCita`, `Canal`.
- `ApiError`, la forma normalizada del error que consume la interfaz.

## Contrato de barberos

`barbero.ts` refleja las respuestas reales del backend. Los UUID `id` y `usuarioId` viajan como
`string` en TypeScript. `BarberoPublicoResponse` no contiene nombre ni celular de cuenta y solo
se obtiene para perfiles activos; `BarberoAdminResponse` incluye esos datos y puede representar
perfiles activos o inactivos. El detalle HTTP está disponible en Swagger UI en `/swagger-ui.html`.

La solicitud de alta contiene `nombre`, `celular`, `contrasena` y campos opcionales de presentación;
el backend asigna el rol. El cambio de estado afecta solo al perfil y conserva la cuenta y sus
sesiones. La asignación barbero-servicio y sus DTO aún no están implementados.

## Qué NO debe ir aquí

- Lógica, validaciones en tiempo de ejecución ni funciones: para validar formularios se usa `zod`
  en la página correspondiente.
- Entidades de la base ni nombres de tablas: el frontend solo ve DTO.
- `any` o tipos "comodín" con todos los campos posibles.
- Fechas como `Date`: viajan como cadenas ISO-8601 con offset y se formatean con `utils/fechas.ts`.

## Convenciones

- Un archivo por recurso: `auth.ts`, `servicio.ts`, `barbero.ts`, `cita.ts`, `disponibilidad.ts`,
  `error.ts`.
- Interfaces en PascalCase; campos en camelCase, igual que el JSON de la API.
- Los tipos de operaciones implementadas coinciden con los DTO del backend; los contratos aún
  pendientes se etiquetan como tales en su documentación.
- Si el backend renombra un campo, se actualiza el tipo y el componente que lo usa en el mismo
  Pull Request.
