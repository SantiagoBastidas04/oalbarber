# Módulo `barbero` — Barbero

Paquete raíz: `com.oalbarber.api.modules.barbero`.

## Responsabilidad

Perfil profesional enlazado a una cuenta de `usuario` con rol `BARBERO` o `ADMINISTRADOR`. El sistema no presupone una cantidad fija de barberos. Cada perfil puede tener cero o más servicios activos asignados.

## Subcarpetas

| Carpeta | Contenido |
|---|---|
| `controller/` | Controladores REST del módulo barbero. |
| `service/` | Servicios (lógica de negocio) del módulo barbero. |
| `repository/` | Repositorios del módulo barbero. |
| `domain/` | Dominio (entidades y enums) del módulo barbero. |
| `dto/` | DTOs (entrada y salida) del módulo barbero. |
| `mapper/` | Mappers (entidad ↔ DTO) del módulo barbero. |

## Qué NO debe ir en este módulo

- Entidades o tablas que pertenezcan a otro módulo: se accede por el service del módulo dueño.
- Controladores de otros dominios, utilidades genéricas (van en `common/`) ni configuración transversal (va en `config/`).
- Lógica de disponibilidad duplicada: el cálculo vive en `cita` y todos los canales lo reutilizan.
- Reglas de negocio dentro de los controladores, los mappers o las entidades.
- Dependencias directas del módulo `cita` hacia controladores de `auth` o `usuario`.

## Convenciones

- Paquete `com.oalbarber.api.modules.barbero` y subpaquetes por capa.
- Clases en PascalCase con el dominio en español sin tildes (`BarberoController`, `BarberoService`).
- DTOs con sufijo `Request`/`Response`; entidades sin sufijo.
- Rutas bajo `/api/barberos`.
- Cada decisión relevante se documenta en `docs/decisiones/` y se enlaza desde el Pull Request.

## Relación con otros módulos

`BarberoService` crea la cuenta `BARBERO` mediante `UsuarioService` y luego su perfil en la misma transacción. `AdminInicializador` crea o localiza la cuenta `ADMINISTRADOR`, pero nunca crea el perfil profesional. El administrador puede agregarse explícitamente mediante `POST /api/barberos/admin`: se reutiliza el UUID de su identidad autenticada y se inserta solo una fila en `barbero`, sin alterar la cuenta. El DTO público omite el nombre y celular de la cuenta. Desactivar un perfil no desactiva la cuenta de usuario.

La tabla puente `barbero_servicio` se administra desde este módulo mediante clave compuesta y
FKs con `ON DELETE RESTRICT`. El catálogo se consulta solo a través de `ServicioService`; no se
usa el repositorio de `servicio`. Un barbero nuevo empieza sin asignaciones. El endpoint público
`GET /api/barberos/{id}/servicios` devuelve servicios activos solo para perfiles activos. El
endpoint administrativo `PUT /api/barberos/{id}/servicios` reemplaza la lista completa y permite
quitar todos los servicios con una lista vacía. `GET /api/barberos/admin` devuelve `servicioIds`
para todos los perfiles.

`listarActivosQueOfrecenServicio` devuelve DTOs públicos y `ofreceServicio` valida la asignación.
`DisponibilidadService` del módulo `cita` consume esta consulta para PB-22. Sigue pendiente avisar
de las citas futuras afectadas antes de quitar una asignación; no se cancelan ni reubican
automáticamente. Ver [ADR-0002](../../../../../../../../../docs/decisiones/ADR-0002-asignacion-barbero-servicio-y-consulta-citas.md)
y [ADR-0004](../../../../../../../../../docs/decisiones/ADR-0004-calculo-de-disponibilidad.md).

## Requisitos del SRS

RF-10, H-13, RN-01, RN-12
