# Módulos de negocio (`modules`)

Paquete `com.oalbarber.api.modules`.

## Qué debe ir en esta carpeta

Un subpaquete por dominio, cada uno con sus capas `controller/`, `service/`, `repository/`,
`domain/`, `dto/` y `mapper/`:

| Módulo | Responsabilidad |
|---|---|
| `auth` | Registro, inicio de sesión, JWT, refresh token y restablecimiento de contraseña. |
| `usuario` | Datos de identidad: nombre, celular único, hash de contraseña, rol y estado. |
| `barbero` | Perfil de los dos barberos y su vínculo con un usuario. |
| `servicio` | Catálogo único de servicios, duraciones, precios e imagen estática. |
| `horario` | Horario de atención, almuerzo, bloqueos y parámetros configurables. |
| `cita` | Disponibilidad, creación, cancelación y estados de las citas de cualquier canal. |

`auth` es la excepción: usa `controller/`, `service/`, `dto/` y `security/`.

## Qué NO debe ir en esta carpeta

- Código compartido entre módulos (va en `common/`) ni configuración (va en `config/`).
- Un módulo nuevo sin requisito en el SRS y sin ADR que lo justifique.
- Dependencias circulares entre módulos.

## Convenciones y reglas de dependencia

- Un módulo solo puede usar el **service** de otro módulo, nunca su repositorio, su entidad ni su controlador.
- El flujo permitido es `controller → service → repository → domain`, y `mapper` entre `domain` y `dto`.
- La lógica de reservas no depende de HTTP: se puede invocar desde la web, desde una tarea
  programada o desde el futuro módulo de WhatsApp.
- Nombres de paquetes y clases en español sin tildes ni eñes, salvo términos técnicos estándar.
- Cada módulo y cada capa tiene su propio `README.md`; léelo antes de agregar código.
