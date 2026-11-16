# Mappers (entidad ↔ DTO) — módulo `barbero`

Paquete: `com.oalbarber.api.modules.barbero.mapper`.

## Contexto del módulo

Perfil profesional enlazado a una cuenta con rol `BARBERO` o `ADMINISTRADOR`.

## Qué debe ir en esta carpeta

- La conversión entre `Barbero` y `BarberoPublicoResponse` / `BarberoAdminResponse` mediante `BarberoMapper`.
- `aPublico` no expone datos de cuenta. `aAdmin` recibe también el `UsuarioResponse` preparado por el service para incluir nombre y celular.
- `aEntidad` usa la fábrica de dominio para crear un perfil desde el DTO de entrada y el UUID de usuario; nunca asigna la contraseña al perfil.
- Conversión de enums, fechas y valores monetarios en un solo lugar, para que el formato sea consistente.
- Mapeo seguro frente a nulos y a relaciones no cargadas (solo se accede a lo que el service ya preparó).

## Qué NO debe ir en esta carpeta

- Consultas a repositorios, transacciones o acceso a la base.
- Reglas de negocio: no decide estados, disponibilidad, precios ni permisos.
- Serialización JSON manual ni anotaciones de controladores.
- Exposición de campos sensibles (`password_hash`) aunque existan en la entidad.
- Lógica de presentación específica del frontend (colores, textos de interfaz).

## Convenciones de nombres

- Paquete `com.oalbarber.api.modules.barbero.mapper`.
- Clase en PascalCase terminada en `Mapper`: `BarberoMapper`.
- Métodos en español: `aEntidad`, `aResponse`, `aListaResponse`.
- Implementación MapStruct con `componentModel = "spring"` para inyección por constructor.
- Si el mapeo se vuelve complejo, se justifica y se documenta, pero no se delega en la entidad.

## Referencias

- Requisitos relacionados del SRS: RF-10, RN-01, RN-12.
- Relación con otros módulos: el service prepara el `UsuarioResponse` para la salida administrativa; el mapper no crea cuentas ni contraseñas.
