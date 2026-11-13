# Dominio (entidades y enums) — módulo `barbero`

Paquete: `com.oalbarber.api.modules.barbero.domain`.

## Contexto del módulo

Perfil profesional enlazado a un usuario con rol `BARBERO` o `ADMINISTRADOR`.

## Qué debe ir en esta carpeta

- El modelo persistente del módulo `barbero`: `Barbero`.
- Mapeo JPA explícito con `@Entity`, `@Table(name = "...")`, `@Column(name = "...")`, claves foráneas y restricciones únicas.
- Relación con `Usuario` mediante `usuario_id`; el perfil no duplica nombre, celular ni contraseña.
- `activo` como `boolean` permite desactivar el perfil sin borrar su historial de citas.
- Constructores protegidos vacíos para JPA y constructores de negocio cuando aporten claridad; `equals` y `hashCode` basados en el identificador.

## Qué NO debe ir en esta carpeta

- DTOs, controladores, servicios, repositorios ni mappers.
- Anotaciones de validación de entrada (`@NotBlank`, `@Size`): esas van en los DTOs.
- `@Data` de Lombok en entidades: genera `equals`/`hashCode`/`toString` peligrosos con relaciones perezosas.
- Lógica de presentación, formato de fechas para la web o textos de interfaz.
- Reglas de negocio complejas: si una operación cruza varias entidades, vive en el service.

## Convenciones de nombres

- Paquete `com.oalbarber.api.modules.barbero.domain`.
- Clases en PascalCase, en singular y en español sin tildes: `Barbero`.
- Campos en camelCase; columnas en snake_case mediante `@Column`.
- Tabla en snake_case y singular: `barbero`.
- Sin sufijos `Entity` ni `Model`: el nombre del concepto es suficiente (`Cita`, no `CitaEntity`).

## Detalle de este módulo

- Entidad `Barbero` → tabla `barbero`.
- Campos: `id`, `usuarioId` (columna `usuario_id`, referencia única a `usuario`), `activo`.
- El usuario referenciado debe tener rol `BARBERO` o `ADMIN`; la validación vive en el service.
- El perfil no guarda credenciales ni datos personales: el nombre y el celular están en `usuario`.
- Los perfiles no se insertan desde una migración. `AdminInicializador` asegura el perfil del
  administrador inicial al arrancar; las cuentas `BARBERO` y sus perfiles se crean desde el flujo
  administrativo del módulo.

## Referencias

- Requisitos relacionados del SRS: RF-10, RN-01, RN-12.
- Relación con otros módulos: guarda el UUID de `usuario`; el nombre y celular de cuenta se consultan mediante `UsuarioService`.
