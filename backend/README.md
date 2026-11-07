# Backend — OalBarber API

API REST en Java 21 + Spring Boot, con Spring Security, Spring Data JPA, Flyway y PostgreSQL.
La estructura está preparada para implementar la fase 1 (reservas web) y, más adelante, el módulo
de WhatsApp sin rehacer la arquitectura.

## Estado actual

Solo hay **estructura, configuración y documentación**. No hay entidades, controladores, services
ni repositorios implementados: cada carpeta tiene un `README.md` con lo que debe ir y lo que no.
La única clase de código es `config/SecurityConfig`, necesaria para que el health check del
contenedor funcione.

## Requisitos

- JDK 21.
- Docker y Docker Compose (para la base de datos y para la imagen final).
- No hace falta Maven instalado: se usa el wrapper `./mvnw` (`mvnw.cmd` en Windows).

## Cómo ejecutarlo

La aplicación necesita PostgreSQL y las variables de entorno de la raíz del repositorio:

```bash
cp .env.example .env      # desde la raíz del monorepo
docker compose up --build
```

Para desarrollo local de la API contra el PostgreSQL de Docker, publica el puerto 5432
(descomentando el mapeo en `docker-compose.yml`) y ejecuta:

```bash
# Linux/macOS
DB_HOST=localhost DB_PORT=5432 DB_NAME=oalbarber \
DB_USER=oalbarber DB_PASSWORD=... ./mvnw spring-boot:run

# PowerShell
$env:DB_HOST="localhost"; $env:DB_PORT="5432"; $env:DB_NAME="oalbarber"
$env:DB_USER="oalbarber"; $env:DB_PASSWORD="..."
.\mvnw.cmd spring-boot:run
```

## Variables de entorno

| Variable | Uso | Ejemplo |
|---|---|---|
| `DB_HOST` | Host de PostgreSQL. | `localhost` (local) / `postgres` (Compose) |
| `DB_PORT` | Puerto de PostgreSQL. | `5432` |
| `DB_NAME` | Nombre de la base. | `oalbarber` |
| `DB_USER` | Usuario de la base. | `oalbarber` |
| `DB_PASSWORD` | Contraseña de la base. | (definida en `.env`) |
| `JAVA_OPTS` | Opciones de JVM de la imagen. | `-XX:MaxRAMPercentage=75` |

`application.properties` deja `ddl-auto=validate` y Flyway activo: el esquema solo cambia con
migraciones en `src/main/resources/db/migration/`.

## Pruebas

```bash
./mvnw test
```

Las pruebas de integración usan Testcontainers con PostgreSQL real; requieren Docker en marcha.
Ver `src/test/java/com/oalbarber/api/README.md`.

## Docker

```bash
docker build -t oalbarber-api ./backend
docker run --rm -p 8080:8080 \
  -e DB_HOST=host.docker.internal -e DB_NAME=oalbarber \
  -e DB_USER=oalbarber -e DB_PASSWORD=... oalbarber-api
```

La imagen usa un build multi-stage (Maven + JDK 21 → JRE 21 alpine), corre con un usuario no root,
expone el puerto 8080 y define un `HEALTHCHECK` contra `/actuator/health`.

## Estructura de paquetes

```text
com.oalbarber.api
├── config/       configuración transversal (seguridad, CORS, OpenAPI, zona horaria)
├── common/       exception/, dto/, util/
└── modules/      auth, usuario, barbero, servicio, horario, cita
```

Cada módulo tiene `controller/`, `service/`, `repository/`, `domain/`, `dto/` y `mapper/`; `auth`
tiene `controller/`, `service/`, `dto/` y `security/`.

## Convenciones

- Nombres en español sin tildes ni eñes, salvo términos técnicos (`controller`, `service`,
  `repository`, `dto`).
- Los controladores solo usan DTOs; las entidades no salen del service.
- Errores uniformes con `ProblemDetail` (RFC 7807) desde `common/exception/`.
- Los secretos solo en variables de entorno.
- Documentación y decisiones en `docs/`; el SRS en `docs/srs/SRS-OalBarber.md`.
