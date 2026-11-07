# Mappers (entidad ↔ DTO) — módulo `usuario`

Paquete: `com.oalbarber.api.modules.usuario.mapper`.

## Contexto del módulo

Datos de las personas que usan el sistema: nombre, celular único en formato internacional (`+57...`), contraseña con hash BCrypt, rol y estado activo. Es la base de identidad que consumen auth, barbero y cita.

## Qué debe ir en esta carpeta

- La conversión entre entidades del módulo `usuario` y sus DTOs: `UsuarioMapper`.
- Métodos explícitos `aEntidad`, `aResponse`, `aListaResponse` y los que hagan falta, con correspondencia campo a campo.
- Conversión de enums, fechas y valores monetarios en un solo lugar, para que el formato sea consistente.
- Mapeo seguro frente a nulos y a relaciones no cargadas (solo se accede a lo que el service ya preparó).

## Qué NO debe ir en esta carpeta

- Consultas a repositorios, transacciones o acceso a la base.
- Reglas de negocio: no decide estados, disponibilidad, precios ni permisos.
- Serialización JSON manual ni anotaciones de controladores.
- Exposición de campos sensibles (`password_hash`) aunque existan en la entidad.
- Lógica de presentación específica del frontend (colores, textos de interfaz).

## Convenciones de nombres

- Paquete `com.oalbarber.api.modules.usuario.mapper`.
- Clase en PascalCase terminada en `Mapper`: `UsuarioMapper`.
- Métodos en español: `aEntidad`, `aResponse`, `aListaResponse`.
- Implementación con métodos estáticos en una clase final o como `@Component`; no se mezclan ambos estilos en el mismo módulo.
- Si el mapeo se vuelve complejo, se justifica y se documenta, pero no se delega en la entidad.

## Referencias

- Requisitos relacionados del SRS: RN-15, RF-01, RF-27, RNF-01, RNF-10.
- Relación con otros módulos: Es la tabla base de identidad. `barbero` referencia a un usuario con rol BARBERO o ADMIN; `cita` referencia al usuario con rol CLIENTE. Ningún módulo debe leer `password_hash` fuera de `auth`.
