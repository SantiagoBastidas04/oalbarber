# DTOs (entrada y salida) — módulo `barbero`

Paquete: `com.oalbarber.api.modules.barbero.dto`.

## Contexto del módulo

Perfil profesional enlazado a una cuenta con rol `BARBERO` o `ADMINISTRADOR`. El DTO público y el DTO administrativo son contratos distintos.

## Qué debe ir en esta carpeta

- Los objetos de transferencia del módulo `barbero`: `BarberoRequest`, `CambiarEstadoBarberoRequest`, `BarberoPublicoResponse` y `BarberoAdminResponse`.
- DTOs de entrada con Bean Validation (`@NotBlank`, `@NotNull`, `@Pattern` para celular internacional) y documentación Swagger con `@Schema`.
- `BarberoPublicoResponse` no contiene datos personales de la cuenta; `BarberoAdminResponse` es para el panel y sí contiene nombre y celular. Ninguna respuesta incluye contraseñas ni hashes.
- `BarberoRequest` limita `nombrePublico` a 100 caracteres y `descripcion` a 500. `foto` solo admite un nombre de archivo estático en minúsculas con extensión `.jpg`, `.jpeg`, `.png` o `.webp`, sin rutas.
- Registros (`record`) inmutables cuando no se necesite mutabilidad; clases solo si el framework lo exige.
- Los identificadores de perfil y usuario son UUID en Java y se serializan como cadenas UUID en JSON.

## Qué NO debe ir en esta carpeta

- Entidades JPA ni anotaciones de persistencia (`@Entity`, `@Column`, `@Id`).
- Lógica de negocio, cálculos de disponibilidad o validaciones que dependan de la base.
- Contraseñas en claro en respuestas, hashes, secretos o rutas absolutas del servidor.
- Campos que el frontend no necesita solo porque existen en la entidad.
- DTOs compartidos entre módulos sin justificación: lo común va en `common/dto/`.

## Convenciones de nombres

- Paquete `com.oalbarber.api.modules.barbero.dto`.
- Sufijos `Request` para entradas y `Response` para salidas.
- Nombres en español sin tildes; los términos técnicos estándar (`Request`, `Response`, `DTO`) se mantienen en inglés.
- Campos en camelCase; el JSON conserva camelCase (no se traduce a snake_case).
- La validación de formato y obligatoriedad vive aquí; la validación contra la base, en el service.

## Referencias

- Requisitos relacionados del SRS: RF-10, RN-01, RN-12.
- Relación con otros módulos: depende de `usuario` para el nombre y los datos de cuenta. La integración con servicios y citas todavía no está implementada.
