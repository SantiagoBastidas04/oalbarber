# Configuración transversal (`config`)

Paquete `com.oalbarber.api.config`.

## Qué debe ir en esta carpeta

- `SecurityConfig`: cadena JWT de Spring Security. Mantiene las rutas expresamente públicas y
  exige autenticación en el resto; el acceso administrativo se limita por rol en los controladores.
- `AdminInicializador`: crea el administrador inicial solo cuando están configurados celular y
  contraseña; si la cuenta ya existe, no cambia sus credenciales ni su rol. No crea perfiles de
  barbero: el administrador se agrega explícitamente desde el endpoint protegido del módulo.
- La configuración de CORS limitada al dominio del frontend (por definir en despliegue).
- La configuración de OpenAPI/springdoc, cuando se agregue la dependencia.
- La configuración de zona horaria (`America/Bogota`) y del reloj (`Clock`) usado por los services.
- Configuración de Jackson (fechas con offset, zona horaria) si no basta con `application.properties`.

## Qué NO debe ir en esta carpeta

- Reglas de negocio, cálculo de disponibilidad ni validaciones de citas.
- Entidades, repositorios, controladores o DTOs de un módulo.
- Contraseñas, claves JWT ni secretos: van en variables de entorno.
- Un `SecurityConfig` por módulo: hay una sola configuración de seguridad para toda la API.

## Convenciones

- Clases PascalCase terminadas en `Config` (`SecurityConfig`, `CorsConfig`, `OpenApiConfig`).
- Métodos `@Bean` con el nombre del bean en camelCase.
- Cada decisión de seguridad relevante se registra como ADR en `docs/decisiones/`.
- El orden de los `requestMatchers` importa: primero las rutas públicas, después `anyRequest().authenticated()`.
