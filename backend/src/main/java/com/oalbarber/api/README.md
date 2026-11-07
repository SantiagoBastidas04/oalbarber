# Código fuente de la API

Paquete base: `com.oalbarber.api`.

## Qué debe ir en esta carpeta

- `ApiApplication.java`: punto de arranque de Spring Boot. No debe contener lógica.
- `config/`: configuración transversal (seguridad, CORS, OpenAPI, zona horaria).
- `common/`: piezas reutilizables sin dominio propio (excepciones, DTOs comunes, utilidades).
- `modules/`: un paquete por dominio del negocio (`auth`, `usuario`, `barbero`, `servicio`, `horario`, `cita`).

## Qué NO debe ir en esta carpeta

- Clases de negocio sueltas en la raíz del paquete.
- Controladores o entidades fuera de su módulo.
- Scripts SQL (van en `src/main/resources/db/migration/`).
- Configuración sensible o credenciales.

## Convenciones

- Paquetes en minúsculas, en español sin tildes ni eñes (`cita`, `servicio`, `horario`).
- Clases PascalCase; métodos y variables camelCase.
- Dependencias entre módulos solo a través de sus services, nunca de sus repositorios o controladores.
- Cada carpeta tiene su propio `README.md` con las reglas específicas.
