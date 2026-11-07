# OalBarber

Sistema web de reservas de citas para una barbería con dos barberos. API REST en Java 21 +
Spring Boot, frontend React y PostgreSQL, orquestados con Docker Compose.

## Tabla de contenido

1. [Descripción](#1-descripción)
2. [Alcance](#2-alcance)
3. [Roles](#3-roles)
4. [Reglas de negocio](#4-reglas-de-negocio)
5. [Stack](#5-stack)
6. [Arquitectura](#6-arquitectura)
7. [Estructura del repositorio](#7-estructura-del-repositorio)
8. [Reglas y convenciones de desarrollo](#8-reglas-y-convenciones-de-desarrollo)
9. [Flujo de trabajo con Git](#9-flujo-de-trabajo-con-git)
10. [Cómo clonar el proyecto](#10-cómo-clonar-el-proyecto)
11. [Cómo ejecutarlo con Docker](#11-cómo-ejecutarlo-con-docker)
12. [Cómo contribuir](#12-cómo-contribuir)
13. [Solución de problemas](#13-solución-de-problemas)

---

## 1. Descripción

OalBarber es una web de reservas para una barbería con dos barberos: el administrador, que también
corta, y uno contratado. Los clientes reservan citas eligiendo servicio y barbero. El pago se hace
en el local; no hay pasarela de pagos.

La web es el primer canal de reservas. El diseño deja preparado un segundo canal (WhatsApp) para
la fase 2, reutilizando la misma lógica de agenda.

## 2. Alcance

### Fase 1 (actual)

- Catálogo de servicios con nombre, duración, precio y foto.
- Reserva de citas con elección de servicio, barbero y horario.
- Registro e inicio de sesión de clientes.
- "Mis citas" con las citas próximas, el historial y la cancelación.
- Panel de administración y panel de barbero.
- Aviso al administrador por cada nueva reserva.

El detalle completo, con requisitos, reglas y criterios de aceptación, está en
[`docs/srs/SRS-OalBarber.md`](docs/srs/SRS-OalBarber.md).

**Estado del código:** el backend tiene la estructura y la configuración base (todavía sin
controladores ni módulo `auth`), y el frontend ya cuenta con un **esqueleto navegable** de todas
las rutas de la fase 1, sin lógica de negocio. Ver [§7](#7-estructura-del-repositorio) y
[§11](#11-cómo-ejecutarlo-con-docker).

### Fase 2 (futuro, no implementada)

- Módulo de WhatsApp con un bot para reservar y para que el administrador consulte la agenda del
  día.

La fase 2 no se implementa ahora; solo se deja la arquitectura preparada (lógica de reservas sin
dependencia de HTTP y campo `canal` en cada cita).

## 3. Roles

| Rol | Permisos |
|---|---|
| `CLIENTE` | Reserva y cancela sus propias citas; consulta el catálogo y su historial. |
| `BARBERO` | Ve y gestiona solo su propia agenda. |
| `ADMIN` | Todo: servicios, precios, horarios, bloqueos, citas de ambos barberos, creación manual de citas para clientes sin reserva y gestión de cuentas. |

## 4. Reglas de negocio

### Horario

- Lunes a sábado, de 10:00 a 19:00; domingo cerrado. Es el mismo horario para ambos barberos.
- Un descanso de almuerzo, con hora y duración por definir **(por confirmar)**. En esa franja no
  se ofrece agenda.

### Servicios

- Duraciones: corte normal 40 min, premium 90 min, mechas 180 min y ondulación 180 min.
- Cada servicio tiene un precio distinto; el precio es igual para ambos barberos.
- La lista final de servicios con precios la entrega el cliente **(por confirmar)**.
- El catálogo es único para todo el negocio. La foto es un archivo estático del frontend; no hay
  subida de imágenes.

### Citas y disponibilidad

- Cada cita ocupa exactamente la duración de su servicio y debe terminar antes del cierre
  (19:00), sin solaparse con el almuerzo, bloqueos ni otras citas **(por confirmar)**.
- Los horarios se ofrecen cada 40 minutos (la duración del servicio más corto), contados desde el
  inicio de cada hueco libre de la agenda del barbero. Al terminar una cita, el siguiente turno
  libre empieza en ese momento **(por confirmar)**.
- La disponibilidad no se almacena: se calcula al consultar, a partir de horarios, almuerzo,
  bloqueos y citas.
- Una cita confirmada nunca se mueve automáticamente. La disponibilidad se recalcula solo para
  ofrecer horarios a nuevas reservas. Cambiar la duración o el precio de un servicio no afecta a
  las citas ya creadas.
- El cliente elige barbero; además se propone la opción "cualquiera disponible" **(por confirmar)**.
- Un cliente reserva con al menos 24 horas de anticipación y hasta con una semana de anticipación
  como máximo.
- Un cliente puede cancelar hasta 2 horas antes de la cita.
- El administrador puede crear citas manualmente (clientes sin reserva, llamadas), a hora exacta y
  sin las restricciones de anticipación.
- El administrador puede bloquear franjas horarias o fechas completas por barbero. Un bloqueo no
  cancela citas existentes y, si hay citas en el rango, exige confirmación explícita.
- Dos citas del mismo barbero nunca pueden solaparse. Además de validarlo el servicio, lo garantiza
  una restricción de exclusión de PostgreSQL.
- La anticipación mínima, el límite de cancelación, el máximo de días, el intervalo entre turnos y
  el margen entre citas son configurables desde el panel; no están fijos en el código.

### Fechas, autenticación e imágenes

- Zona horaria `America/Bogota`. Las fechas se guardan como `timestamptz`.
- El cliente se registra con nombre, celular (formato internacional `+57...`) y contraseña; con
  JWT ve y cancela sus citas.
- No se guarda correo electrónico. Si el cliente olvida su contraseña, el administrador la
  restablece **(por confirmar)**.
- Las fotos del catálogo son archivos estáticos del frontend (por ejemplo
  `frontend/public/servicios/`). La base de datos solo guarda el nombre del archivo.

## 5. Stack

| Capa | Tecnología |
|---|---|
| Backend | Java 21 + Spring Boot 4, Spring Security con JWT, Spring Data JPA, Flyway, Bean Validation |
| Base de datos | PostgreSQL 17 (contenedor `postgres:17-alpine`) |
| Frontend | React 19 + TypeScript + Vite 8 + Tailwind CSS 4; React Router, TanStack Query, Axios, React Hook Form + Zod y lucide-react |
| Contenedores | Docker + Docker Compose (SPA con nginx, API, PostgreSQL y pgAdmin) |
| CI/CD | GitHub Actions con workflows separados por carpeta (propuesto) |
| Documentación | OpenAPI, Markdown y ADR |

## 6. Arquitectura

Monolito modular organizado por capas:

```text
controller → service → repository → domain
                  ↕
                mapper
```

- Paquete base: `com.oalbarber.api`.
- `config/`: seguridad, CORS, OpenAPI y zona horaria.
- `common/`: excepciones, DTOs compartidos y utilidades.
- `modules/`: un submódulo por dominio (`auth`, `usuario`, `barbero`, `servicio`, `horario`,
  `cita`), cada uno con `controller/`, `service/`, `repository/`, `domain/`, `dto/` y `mapper/`
  (`auth` usa `controller/`, `service/`, `dto/` y `security/`).
- La lógica de reservas vive en los services y **no depende de HTTP**: recibe datos y devuelve
  resultados, sin `HttpServletRequest`, `ResponseEntity` ni `SecurityContextHolder`. Así la web y,
  en el futuro, WhatsApp usan la misma agenda.
- El campo `canal` de la cita registra el origen (`WEB`, `WHATSAPP`, `ADMIN`).
- La API REST se documenta con OpenAPI.
- La integridad de la agenda se refuerza en PostgreSQL con una restricción de exclusión sobre el
  rango de cada barbero, que ignora las citas canceladas.

El frontend es una SPA de React servida por nginx. El navegador habla siempre con el mismo origen:
nginx sirve `index.html` y hace proxy de `/api/` hacia la API, de modo que **no hay CORS** en
Docker. En desarrollo hace lo mismo el proxy de Vite. La SPA solo conoce `VITE_API_BASE_URL`
(por defecto `/api`) y no guarda tokens en `localStorage`.

Más detalle en [`docs/arquitectura/`](docs/arquitectura/README.md), en el
[modelo de datos](docs/arquitectura/modelo-datos.md) y en los
[ADR](docs/decisiones/README.md).

## 7. Estructura del repositorio

```text
oalbarber/
├── .github/
│   └── workflows/
├── backend/
│   ├── .mvn/
│   │   └── wrapper/
│   └── src/
│       ├── main/
│       │   ├── java/com/oalbarber/api/
│       │   │   ├── common/
│       │   │   │   ├── dto/
│       │   │   │   ├── exception/
│       │   │   │   └── util/
│       │   │   ├── config/
│       │   │   └── modules/
│       │   │       ├── auth/
│       │   │       │   ├── controller/
│       │   │       │   ├── dto/
│       │   │       │   ├── security/
│       │   │       │   └── service/
│       │   │       ├── barbero/
│       │   │       │   ├── controller/
│       │   │       │   ├── domain/
│       │   │       │   ├── dto/
│       │   │       │   ├── mapper/
│       │   │       │   ├── repository/
│       │   │       │   └── service/
│       │   │       ├── cita/
│       │   │       │   ├── controller/
│       │   │       │   ├── domain/
│       │   │       │   ├── dto/
│       │   │       │   ├── mapper/
│       │   │       │   ├── repository/
│       │   │       │   └── service/
│       │   │       ├── horario/
│       │   │       │   ├── controller/
│       │   │       │   ├── domain/
│       │   │       │   ├── dto/
│       │   │       │   ├── mapper/
│       │   │       │   ├── repository/
│       │   │       │   └── service/
│       │   │       ├── servicio/
│       │   │       │   ├── controller/
│       │   │       │   ├── domain/
│       │   │       │   ├── dto/
│       │   │       │   ├── mapper/
│       │   │       │   ├── repository/
│       │   │       │   └── service/
│       │   │       └── usuario/
│       │   │           ├── controller/
│       │   │           ├── domain/
│       │   │           ├── dto/
│       │   │           ├── mapper/
│       │   │           ├── repository/
│       │   │           └── service/
│       │   └── resources/
│       │       └── db/
│       │           └── migration/
│       └── test/
│           └── java/com/oalbarber/api/
├── docs/
│   ├── api/
│   ├── arquitectura/
│   ├── decisiones/
│   └── srs/
└── frontend/
    ├── Dockerfile              build multi-stage: Node + nginx
    ├── nginx.conf              SPA, proxy /api, cachés y cabeceras
    ├── .dockerignore
    ├── .env.example
    ├── index.html
    ├── package.json
    ├── package-lock.json
    ├── vite.config.ts
    ├── public/
    │   ├── favicon.svg
    │   └── servicios/          fotos estáticas del catálogo
    └── src/
        ├── main.tsx
        ├── components/
        │   └── comunes/
        ├── config/
        ├── context/
        ├── hooks/
        ├── layouts/
        ├── pages/
        │   ├── catalogo/
        │   ├── cuenta/
        │   ├── errores/
        │   ├── inicio/
        │   ├── mis-citas/
        │   ├── panel/
        │   └── reserva/
        ├── routes/
        ├── services/
        ├── styles/
        ├── test/
        ├── types/
        └── utils/
```

En `frontend/` viven además los `tsconfig*.json`, `.oxlintrc.json` y `.gitignore`, y cada carpeta
de `src/` tiene su propio `README.md` con lo que debe ir y lo que no.

En la raíz viven además `docker-compose.yml`, `.env.example`, `.gitignore` y este `README.md`.
Cada carpeta de código y documentación tiene su propio `README.md` con lo que debe ir y lo que no.

## 8. Reglas y convenciones de desarrollo

### DTOs

- Los controladores nunca reciben ni devuelven entidades JPA, solo DTOs.
- Las entidades no salen de la capa de servicio.
- Los DTOs de entrada se validan con Bean Validation (`@Valid`).
- El mapeo entre entidad y DTO se hace en `mapper/`.

### Excepciones

- Un `@RestControllerAdvice` global en `common/exception/` convierte las excepciones en respuestas
  JSON uniformes con formato `ProblemDetail` (RFC 7807).
- Se usan excepciones propias del negocio, por ejemplo `RecursoNoEncontradoException` y
  `HorarioNoDisponibleException`.
- Nunca se devuelve un stack trace al cliente.
- Nunca se captura una excepción para ignorarla.

### Documentación

- Cada endpoint se documenta con OpenAPI.
- Javadoc en los métodos públicos de los services.
- Las decisiones importantes se registran en `docs/decisiones/` (ADR).
- El SRS vive en `docs/srs/`.
- Todo cambio funcional actualiza la documentación en el mismo Pull Request.

### Base de datos

- El esquema solo cambia con migraciones Flyway (`V1__descripcion.sql`).
- Nunca se edita una migración ya fusionada a `main`: se crea una nueva.
- `spring.jpa.hibernate.ddl-auto=validate`; Hibernate no modifica el esquema.
- Fechas con zona (`timestamptz`) y zona horaria `America/Bogota`.

### Seguridad

- Contraseñas con BCrypt.
- Secretos solo en variables de entorno (`.env`, nunca en el repositorio).
- Permisos validados en el backend con `@PreAuthorize`.
- CORS limitado al dominio del frontend.
- Límite de intentos de login y bloqueo temporal.

### Estilo

- Nombres de código en español sin tildes ni eñes (`cita`, `servicio`, `horario`).
- Clases en PascalCase; métodos y variables en camelCase.
- Tablas y columnas en snake_case.
- Términos técnicos estándar en inglés: `controller`, `service`, `repository`, `dto`, `mapper`.

### Pruebas

- Pruebas unitarias para la lógica de disponibilidad y de cancelación.
- Pruebas de integración con Testcontainers y PostgreSQL real.
- La restricción de solapamiento se prueba contra la base, no solo en el service.

## 9. Flujo de trabajo con Git

### Ramas fijas

- `main`: versión final y estable, la que se despliega a producción.
- `develop`: integración.

Ambas están protegidas: solo se fusionan por Pull Request, con al menos una revisión y la CI en
verde.

### Ramas de trabajo

Cada tarea se trabaja en una rama creada desde `develop`:

- `feature/<id-requisito>-<descripcion>` (por ejemplo `feature/rf-15-reserva-citas`).
- `fix/<descripcion>`.
- `docs/<descripcion>`.
- `chore/<descripcion>`.

Se integra con un Pull Request hacia `develop` usando fusión *squash* y la rama se borra después.

### Paso a producción

- Cuando `develop` está estable, se abre un Pull Request `develop` → `main` con *merge commit*.
- A `main` solo entran `develop` y los hotfix.
- Hotfix: rama creada desde `main`, Pull Request a `main` y después integración también en
  `develop`.

### Commits y Pull Requests

- Commits con Conventional Commits: `feat:`, `fix:`, `docs:`, `chore:`.
- Los Pull Requests describen qué cambia y cómo probarlo.

## 10. Cómo clonar el proyecto

```bash
git clone https://github.com/SantiagoBastidas04/oalbarber.git
cd oalbarber
git checkout develop
```

La rama de trabajo es `develop`; `main` es la rama estable.

## 11. Cómo ejecutarlo con Docker

### Guía rápida

Estos comandos, desde cero, dejan la aplicación arriba:

```bash
git clone https://github.com/SantiagoBastidas04/oalbarber.git
cd oalbarber
git checkout develop
cp .env.example .env            # PowerShell: Copy-Item .env.example .env
docker compose up -d --build    # la primera vez tarda varios minutos
```

Luego abre **<http://localhost:3000>**. Para detener todo: `docker compose down`.

> Si ya tenías el proyecto clonado: `git checkout develop`, `git pull`, y después
> `docker compose up -d --build`. El `.env` solo se crea la primera vez.

### Requisitos

- Docker Desktop (incluye Docker Compose).
- Node.js 24 LTS y npm, solo si vas a trabajar en el frontend con `npm run dev`.

### Qué levanta Docker Compose

| Servicio | Imagen | Puerto en tu equipo | Para qué sirve |
|---|---|---|---|
| `postgres` | `postgres:17-alpine` | `5432` (opcional) | Base de datos. |
| `api` | se construye desde `backend/` | `8080` | API REST de Spring Boot. |
| `pgadmin` | `dpage/pgadmin4` | `5050` | Administrar la base desde el navegador. |
| `frontend` | se construye desde `frontend/` (Node + nginx) | `3000` | SPA de React servida por nginx, que además hace proxy de `/api` a `api`. |

Todo queda en la red interna `oalbarber_net`. El navegador **nunca** habla directo con la API: pide
`/api/...` al mismo origen (3000) y nginx lo reenvía al servicio `api`. Por eso no hay CORS.

### El contenedor del frontend

El servicio `frontend` se construye con `frontend/Dockerfile` en dos etapas:

1. **Compilación** (`node:24-alpine`): copia `package*.json`, ejecuta `npm ci` y luego
   `npm run build`, que verifica los tipos y genera `dist/`. La URL de la API entra como *build
   arg* `VITE_API_BASE_URL` (por defecto `/api`) y queda incrustada en el JavaScript.
2. **Servicio** (`nginxinc/nginx-unprivileged:1.29-alpine`): copia `dist/` a
   `/usr/share/nginx/html` y `nginx.conf` a `/etc/nginx/conf.d/default.conf`. Escucha en **8080** y
   corre **sin root**; el host lo publica en `${FRONTEND_PORT:-3000}`.

`frontend/nginx.conf` hace cuatro cosas:

- **SPA**: `try_files $uri /index.html`, así recargar en `/mis-citas` o `/panel/agenda` funciona.
- **Proxy de la API**: `location /api/` reenvía a `http://api:8080` conservando el prefijo. Usa una
  variable y el `resolver` interno de Docker, por lo que nginx arranca aunque la API aún no exista
  (responde `502` hasta que esté lista).
- **Estáticos**: `/assets/` con caché de un año e `immutable`, `index.html` con `no-cache`, y
  `/servicios/` con las fotos del catálogo (404 si el archivo no existe).
- **Salud y seguridad**: `200` en `/healthz` (lo usa el `HEALTHCHECK`), gzip y cabeceras
  `X-Content-Type-Options`, `X-Frame-Options` y `Referrer-Policy`.

Por eso `docker compose ps` muestra `frontend` como `healthy`, y **no hace falta** publicar la API
ni configurar CORS.

### Paso a paso (primera vez)

```bash
# 1. Clonar y entrar a la rama de trabajo
git clone https://github.com/SantiagoBastidas04/oalbarber.git
cd oalbarber
git checkout develop

# 2. Crear tu archivo de entorno a partir del ejemplo
cp .env.example .env          # Linux/macOS
Copy-Item .env.example .env   # PowerShell

# 3. Editar .env: cambia POSTGRES_PASSWORD y PGADMIN_DEFAULT_PASSWORD
#    (opcional) FRONTEND_PORT=3000

# 4. Construir y levantar todo
docker compose up --build
```

> **Ojo con `POSTGRES_PASSWORD`:** se aplica solo al crear la base por primera vez. Si la cambias
> después, la API falla con `28P01` hasta que actualices la clave en la base
> (ver [Solución de problemas](#13-solución-de-problemas)).

La primera vez tarda varios minutos: descarga imágenes, compila el backend y compila la SPA.

### Paso a paso (día a día)

```bash
docker compose up -d          # levantar en segundo plano
docker compose ps             # ver estado y salud
docker compose down           # detener todo
```

Si cambiaste código del **backend** o del **frontend**, hay que reconstruir esa imagen:

```bash
docker compose build api frontend
docker compose up -d
```

### Comprobar que quedó bien

```bash
docker compose ps
```

`postgres`, `api` y `frontend` deben aparecer como `healthy`. Luego abre
`http://localhost:3000`: debe cargar la página de inicio de OalBarber y navegar a `/servicios`,
`/reservar`, `/mis-citas`, etc. sin errores.

Un `401` al pedir `http://localhost:3000/api/servicios` es **correcto** por ahora: la API exige
autenticación mientras no exista el módulo `auth`. Un `502` significaría que nginx no encuentra la
API.

### Desarrollar el frontend con recarga en caliente

Docker sirve la versión compilada; para programar usa Vite con la API en Docker:

```bash
docker compose up -d postgres api     # la API en Docker

cd frontend
npm install                           # solo la primera vez
npm run dev                           # http://localhost:5173
```

Vite reenvía `/api` a `http://localhost:8080`. Si la API usa otro puerto:

```powershell
$env:VITE_DEV_API_TARGET="http://localhost:8081"; npm run dev
```

```bash
VITE_DEV_API_TARGET=http://localhost:8081 npm run dev
```

`frontend/.env` es **opcional**: solo si necesitas cambiar `VITE_API_BASE_URL`. La plantilla está en
`frontend/.env.example`.

### URLs

| Servicio | URL |
|---|---|
| Frontend (SPA) | `http://localhost:3000` |
| API | `http://localhost:8080` |
| Salud de la API | `http://localhost:8080/actuator/health` |
| pgAdmin | `http://localhost:5050` |

El puerto publicado del frontend se cambia con `FRONTEND_PORT` en `.env` (por ejemplo
`FRONTEND_PORT=3001`); el contenedor siempre escucha en 8080.

Cómo se encadenan los servicios:

- `api` espera a PostgreSQL (`condition: service_healthy`) y su `HEALTHCHECK` consulta
  `/actuator/health`, que es público en `SecurityConfig`.
- `frontend` espera a `api` con `condition: service_started`: nginx arranca aunque la API tarde y
  responde `502` en `/api/**` hasta que el backend esté listo. El resto del comportamiento de nginx
  está en [El contenedor del frontend](#el-contenedor-del-frontend).

### Variables de entorno

| Variable | Dónde | Uso |
|---|---|---|
| `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` | `.env` | Base de datos; la API los recibe como `DB_*`. |
| `PGADMIN_DEFAULT_EMAIL`, `PGADMIN_DEFAULT_PASSWORD` | `.env` | Acceso a pgAdmin. |
| `FRONTEND_PORT` | `.env` | Puerto del host para la SPA (`3000` por defecto). |
| `VITE_API_BASE_URL` | `.env` del frontend / build arg | Base de la API que usa el navegador (`/api`). |
| `VITE_DEV_API_TARGET` | entorno del proceso | Destino del proxy de Vite en desarrollo. |

`.env` no se versiona (está en `.gitignore`); `.env.example` sí, y no contiene secretos. Todos los
servicios leen el mismo `POSTGRES_PASSWORD`, que **solo se aplica al crear la base por primera
vez**. Si lo cambias con el volumen ya creado, la API falla con `28P01` (ver
[Solución de problemas](#13-solución-de-problemas)).

### Registrar el servidor en pgAdmin

1. Abrir `http://localhost:5050` e iniciar sesión con `PGADMIN_DEFAULT_EMAIL` y
   `PGADMIN_DEFAULT_PASSWORD` de `.env`.
2. *Add New Server* → pestaña *General*: nombre `OalBarber`.
3. Pestaña *Connection*:
   - Host: `postgres` (nombre del servicio en la red de Compose, **no** `localhost`).
   - Port: `5432`.
   - Maintenance database: el valor de `POSTGRES_DB`.
   - Username: el valor de `POSTGRES_USER`.
   - Password: el valor de `POSTGRES_PASSWORD`.
4. Guardar. La base `oalbarber` debe aparecer en el árbol.

### Comandos útiles

```bash
docker compose logs -f api        # ver logs de la API en vivo
docker compose down               # detener y eliminar contenedores
docker compose down -v            # detener y borrar también los volúmenes (se pierde la base)
docker compose build --no-cache api
docker compose build frontend     # reconstruir solo la SPA
docker compose up -d --build      # levantar en segundo plano
docker compose ps                 # estado y salud de los servicios
```

## 12. Cómo contribuir

1. Leer este README y el `README.md` de la carpeta donde se va a trabajar.
2. Crear la rama de la tarea desde `develop` con el prefijo correspondiente.
3. Implementar el cambio sin salirse del alcance del SRS.
4. Escribir o actualizar las pruebas (unitarias y de integración cuando aplique).
5. Actualizar la documentación afectada (`docs/`, OpenAPI, README de la carpeta).
6. Abrir un Pull Request hacia `develop` describiendo qué cambia y cómo probarlo.
7. Pedir revisión: se necesita al menos una aprobación y la CI en verde para fusionar.

Si el cambio toca el frontend, antes de abrir el Pull Request ejecutar desde `frontend/`:

```bash
npm ci
npm run lint
npm run typecheck
npm run test
npm run build
```

### Subir un cambio (resumen)

```bash
git status                      # ver qué cambió
git add -A                      # preparar todo (respeta .gitignore)
git status --short              # revisar la lista antes de confirmar
git commit -m "feat: descripcion corta del cambio"
git push -u origin <tu-rama>
```

Después se abre el Pull Request hacia `develop` en GitHub. `.env`, `node_modules/`, `dist/` y
`target/` no se suben porque están en `.gitignore`; `.env.example` sí se versiona y no contiene
secretos.

## 13. Solución de problemas

### El puerto 8080 (o 5050) está ocupado

Síntoma: `Bind for 0.0.0.0:8080 failed: port is already allocated`.

- Identificar el proceso que usa el puerto:
  - Linux/macOS: `lsof -i :8080`
  - Windows: `Get-NetTCPConnection -LocalPort 8080`
- Detener ese proceso o cambiar el mapeo en `docker-compose.yml` (por ejemplo `8081:8080`).

### El contenedor `api` no arranca porque PostgreSQL no está listo

`api` tiene `depends_on` con `condition: service_healthy`, así que primero revisa la salud de la
base:

```bash
docker compose ps
docker compose logs postgres
docker compose logs api
```

Si PostgreSQL reinicia, suele ser por credenciales inválidas en `.env` o por un volumen viejo
(ver el punto siguiente). También puede tardar la primera vez mientras descarga la imagen.

### Cambié las credenciales en `.env` y la API no conecta (`28P01`)

Síntoma en los logs de `api`:

```text
FATAL: password authentication failed for user "oalbarber"
SQL State : 28P01
```

PostgreSQL solo aplica `POSTGRES_USER` y `POSTGRES_PASSWORD` la **primera vez** que crea la base.
El volumen `oalbarber_postgres_data` conserva la contraseña anterior, aunque el `.env` ya diga otra.
Hay dos salidas:

**A. Conservar los datos** (cambiar la clave dentro de la base y reiniciar la API):

```bash
docker compose exec -T postgres psql -U oalbarber -d postgres
# dentro de psql:
ALTER USER oalbarber WITH PASSWORD 'la_clave_del_env';
\q
docker compose restart api
```

En PowerShell, sin entrar a la consola:

```powershell
"ALTER USER oalbarber WITH PASSWORD 'la_clave_del_env';" | docker compose exec -T postgres psql -U oalbarber -d postgres
docker compose restart api
```

**B. Empezar de cero** (borra la base y los datos de pgAdmin):

```bash
docker compose down -v
docker compose up --build
```

`down -v` borra los datos; úsalo solo en desarrollo.

### La API responde 401 en todo

Es el comportamiento esperado mientras no exista el módulo `auth`: `SecurityConfig` solo deja
público `/actuator/health/**` y exige autenticación en el resto. El TODO de esa clase indica
dónde se reemplaza al implementar JWT. Por eso el esqueleto del frontend no llama a la API al
cargar, y una petición manual a `http://localhost:3000/api/servicios` devuelve `401` (no es un
fallo de nginx ni del proxy).

Al abrir esa URL en el navegador puede aparecer un diálogo pidiendo usuario y contraseña: es la
autenticación básica de Spring Security. No hay credenciales válidas todavía; basta con cancelarlo.
Ese diálogo desaparece cuando exista el módulo `auth` con JWT.

### El puerto 3000 está ocupado

Síntoma: `Bind for 0.0.0.0:3000 failed: port is already allocated` al levantar `frontend`.

- Identificar el proceso que usa el puerto:
  - Linux/macOS: `lsof -i :3000`
  - Windows: `Get-NetTCPConnection -LocalPort 3000`
- Detener ese proceso o cambiar el puerto del host en `.env`:
  `FRONTEND_PORT=3001` y volver a ejecutar `docker compose up -d frontend`. El contenedor sigue
  escuchando en 8080; solo cambia el puerto publicado.

### `/api` responde 502 desde el frontend

`frontend` depende de `api` con `condition: service_started`, así que nginx arranca aunque la API
todavía no esté disponible y devuelve `502` hasta que el backend termina de subir.

```bash
docker compose ps            # comprobar que api está healthy
docker compose logs -f api   # ver por qué no arranca
```

Si `api` está sana y el `502` continúa, revisar que el servicio se llame `api` en
`docker-compose.yml` (nginx resuelve ese nombre por la red `oalbarber_net`).

### Los cambios del frontend no aparecen

La SPA se compila **dentro de la imagen**: editar `frontend/src` no recarga el contenedor. Hay que
reconstruir el servicio:

```bash
docker compose build frontend
docker compose up -d frontend
```

Para desarrollo con recarga en caliente, usar `npm run dev` en `frontend/` (puerto 5173) y dejar
la API en Docker.

### Cambios que no aparecen en la imagen

Reconstruir sin caché:

```bash
docker compose build --no-cache api
docker compose up -d
```
