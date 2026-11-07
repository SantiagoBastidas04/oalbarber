# Código común (`common`)

Paquete `com.oalbarber.api.common`.

## Qué debe ir en esta carpeta

- `exception/`: manejo global de errores y excepciones propias del negocio.
- `dto/`: DTOs realmente compartidos por varios módulos (paginación, respuestas genéricas).
- `util/`: utilidades stateless sin dependencia de un módulo (fechas, zona horaria, formato de celular).

## Qué NO debe ir en esta carpeta

- Código con conocimiento de un dominio concreto: si menciona citas, barberos o servicios, va en su módulo.
- Un cajón de sastre con clases copiadas de Internet.
- Entidades JPA o repositorios.
- Configuración de Spring: va en `config/`.

## Convenciones

- Paquetes en minúsculas y en español (`excepcion` no: se usa `exception` por ser término técnico estándar).
- Clases PascalCase; utilidades terminadas en `Util` y con constructor privado.
- Sin estado mutable ni campos estáticos que guarden datos de negocio.
- Todo método público con Javadoc en español.
