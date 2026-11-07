# Pruebas del backend

Paquete base de pruebas: `com.oalbarber.api`.

## Qué debe ir en esta carpeta

- Pruebas unitarias (JUnit 5 + Mockito) de la lógica de disponibilidad y de las reglas de
  cancelación, sin levantar Spring ni una base de datos.
- Pruebas de integración con Testcontainers y PostgreSQL real (no H2), reutilizando
  `TestcontainersConfiguration`. Conviene fijar en esa configuración la misma versión mayor que
  producción (`postgres:17-alpine`) en lugar de `postgres:latest`.
- Pruebas de los controladores con `MockMvc` y de la seguridad (401/403) cuando exista el módulo
  `auth`.
- Datos de prueba explícitos y aislados; cada prueba debe poder ejecutarse sola y en cualquier orden.

## Qué NO debe ir en esta carpeta

- Pruebas contra la base de datos de desarrollo o de producción.
- Credenciales reales, celulares reales o datos personales.
- `Thread.sleep` para esperar condiciones; se usan aserciones o *awaitility*.
- Pruebas que dependan del orden de ejecución o de datos dejados por otra prueba.
- Código de producción: el código de apoyo de pruebas se queda en `src/test`.

## Convenciones

- Clases `...Test` para pruebas unitarias y `...IT` para integración si se separan con Failsafe.
- Métodos `@DisplayName` en español y nombres en camelCase: `noOfreceHorarioSiSeSolapaConOtraCita`.
- Un archivo por clase bajo prueba, en el mismo paquete que la clase de producción.
- `TestcontainersConfiguration` y `TestApiApplication` son apoyo del proyecto generado; no se
  convierten en pruebas por sí mismos.
- Las pruebas de la regla RN-11 deben verificar que la base rechaza el solapamiento, no solo el
  service.
