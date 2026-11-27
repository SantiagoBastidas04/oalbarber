# Documentación de la API

La documentación interactiva de la API está disponible en Swagger UI en `/swagger-ui.html`.
Cada operación indica su método, ruta, parámetros, formato de entrada y respuesta, seguridad,
roles permitidos y errores conocidos.

## Convenciones

- Prefijo `/api`, recursos en plural y en español, verbos HTTP estándar y códigos coherentes.
- Los ejemplos usan datos ficticios y fechas ISO-8601 con offset de Bogotá, por ejemplo
  `2026-11-10T10:00:00-05:00`.
- Los errores usan `ProblemDetail` (RFC 7807), con detalles de validación y estados HTTP
  apropiados.
- El acceso usa `Authorization: Bearer <token>`; el refresh token se envía en cookie `HttpOnly`
  según RNF-02.
- La documentación se mantiene junto con el código de cada endpoint.

## Perfiles de barbero

La API expone consulta pública de perfiles activos, listado administrativo de todos los perfiles,
alta y cambio de estado. La lista pública no incluye datos personales de la cuenta; las operaciones
administrativas requieren rol `ADMINISTRADOR`. El cambio de estado conserva activa la cuenta y sus
sesiones.

| Método y ruta | Acceso | Comportamiento |
|---|---|---|
| `POST /api/barberos/admin` | `ADMINISTRADOR` | Agrega al administrador autenticado como barbero. El cuerpo acepta `nombrePublico`, `descripcion` y `foto` opcionales; la identidad se toma del token. Crea solo el perfil y responde `201`. Si ya existe un perfil responde `409` con `BARBERO_DUPLICADO`. |

El administrador inicial no aparece como barbero automáticamente. El perfil se crea solo al usar
este endpoint; la cuenta de usuario permanece intacta.

### Servicios ofrecidos

| Método y ruta | Acceso | Comportamiento |
|---|---|---|
| `GET /api/barberos/{id}/servicios` | Público | Servicios activos de un perfil activo. Devuelve `404` si el perfil no existe o está inactivo. |
| `PUT /api/barberos/{id}/servicios` | `ADMINISTRADOR` | Reemplaza la asignación completa. Recibe `{ "servicioIds": ["uuid", ...] }` y devuelve los servicios activos asignados. Una lista vacía quita todas las asignaciones. |

El PUT devuelve `400` para una lista ausente, nula o con IDs duplicados, `404` si no existe el
perfil y `409` si algún ID no identifica un servicio activo. `GET /api/barberos/admin` incluye
`servicioIds` por perfil para que el panel admin lea las asignaciones en el listado administrativo.

La consulta de citas futuras afectadas al quitar una asignación todavía no está implementada:
depende del módulo `cita`. El endpoint no muestra ni simula ese aviso. Ver la
[matriz de trazabilidad H-13](../seguimiento/trazabilidad-H-13.md).

## Disponibilidad

### Consultar horarios disponibles

`GET /api/disponibilidad` es público y corresponde a la consulta de disponibilidad de H-01 (endpoint
19), RF-15 y RF-16. Calcula los turnos al momento de la consulta; no almacena ni mueve citas.

| Parámetro | Obligatorio | Formato | Descripción |
|---|---|---|---|
| `servicioId` | Sí | UUID | Servicio activo del catálogo. |
| `fecha` | Sí | `AAAA-MM-DD` | Fecha local en `America/Bogota`. |
| `barberoId` | No | UUID | Barbero solicitado. Si se omite, busca cualquier barbero activo que ofrezca el servicio. |

Ejemplo sin barbero específico:

```bash
curl "http://localhost:8080/api/disponibilidad?servicioId=9a7c1e2d-4b3f-4a6e-8c1d-2f5a7b9c0d1e&fecha=2026-11-28"
```

Ejemplo de respuesta `200`:

```json
[
  {
    "inicio": "2026-11-28T10:00:00-05:00",
    "fin": "2026-11-28T10:40:00-05:00",
    "barberoId": "aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa"
  },
  {
    "inicio": "2026-11-28T10:40:00-05:00",
    "fin": "2026-11-28T11:20:00-05:00",
    "barberoId": "bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb"
  }
]
```

El `fin` refleja la duración exacta del servicio. Los turnos se generan cada 40 minutos desde el
inicio de cada hueco libre; se permite que un turno termine exactamente al cierre. “Cualquiera”
devuelve inicios únicos y asigna los repetidos al barbero con menos citas vigentes ese día, con UUID
menor como desempate. Los límites se calculan desde el instante actual: al menos 24 horas y hasta
168 horas adelante. Las fechas fuera de ese rango o sin huecos responden `200` con `[]`.

| Estado | Causa | Respuesta |
|---|---|---|
| `400` | Falta `servicioId` o `fecha`, o UUID/fecha tiene formato inválido. | `ProblemDetail`, con campo en `errors`. |
| `404` | El servicio no existe o está inactivo. | `ProblemDetail`. |
| `409` | El `barberoId` indicado no existe, está inactivo o no ofrece el servicio. | `ProblemDetail`. |

La franja de almuerzo solo se descuenta cuando el horario la tiene configurada. La hora y duración
siguen pendientes de confirmación (P-01). La interpretación del cierre, el límite de siete días y
la asignación de “cualquiera” se documentan en el
[ADR-0004](../decisiones/ADR-0004-calculo-de-disponibilidad.md).
