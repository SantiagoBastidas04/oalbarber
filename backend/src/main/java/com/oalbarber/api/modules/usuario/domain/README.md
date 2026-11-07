# Dominio (entidades y enums) — módulo `usuario`

Paquete: `com.oalbarber.api.modules.usuario.domain`.

## Contexto del módulo

Datos de las personas que usan el sistema: nombre, celular único en formato internacional (`+57...`), contraseña con hash BCrypt, rol y estado activo. Es la base de identidad que consumen auth, barbero y cita.

## Qué debe ir en esta carpeta

- El modelo persistente del módulo `usuario`: `Usuario`, `RolUsuario`.
- Mapeo JPA explícito con `@Entity`, `@Table(name = "...")`, `@Column(name = "...")`, claves foráneas y restricciones únicas.
- Enums de dominio con `@Enumerated(EnumType.STRING)` para que la base guarde nombres legibles.
- Fechas con zona: `OffsetDateTime` o `Instant` para columnas `timestamptz` (America/Bogota).
- Constructores protegidos vacíos para JPA y constructores de negocio cuando aporten claridad; `equals` y `hashCode` basados en el identificador.

## Qué NO debe ir en esta carpeta

- DTOs, controladores, servicios, repositorios ni mappers.
- Anotaciones de validación de entrada (`@NotBlank`, `@Size`): esas van en los DTOs.
- `@Data` de Lombok en entidades: genera `equals`/`hashCode`/`toString` peligrosos con relaciones perezosas.
- Lógica de presentación, formato de fechas para la web o textos de interfaz.
- Reglas de negocio complejas: si una operación cruza varias entidades, vive en el service.

## Convenciones de nombres

- Paquete `com.oalbarber.api.modules.usuario.domain`.
- Clases en PascalCase, en singular y en español sin tildes: `Usuario`, `RolUsuario`.
- Campos en camelCase; columnas en snake_case mediante `@Column`.
- Tabla en snake_case y singular: `usuario`.
- Sin sufijos `Entity` ni `Model`: el nombre del concepto es suficiente (`Cita`, no `CitaEntity`).

## Detalle de este módulo

- Entidad `Usuario` → tabla `usuario`.
- Campos: `id`, `nombre`, `telefono` (único, formato `+57...`), `passwordHash` (columna
  `password_hash`), `rol`, `activo`, `creadoEn` (columna `creado_en`, `timestamptz`).
- `RolUsuario` es un enum con `ADMIN`, `BARBERO` y `CLIENTE`, persistido como texto.
- La contraseña nunca se guarda en claro: el hash se produce en el service con BCrypt.

## Referencias

- Requisitos relacionados del SRS: RN-15, RF-01, RF-27, RNF-01, RNF-10.
- Relación con otros módulos: Es la tabla base de identidad. `barbero` referencia a un usuario con rol BARBERO o ADMIN; `cita` referencia al usuario con rol CLIENTE. Ningún módulo debe leer `password_hash` fuera de `auth`.
