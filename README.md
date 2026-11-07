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
    ├── public/
    │   └── servicios/
    └── src/
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

En `frontend/` viven además `index.html`, `package.json`, `vite.config.ts`, `tsconfig*.json`,
`Dockerfile`, `nginx.conf`, `.dockerignore` y `.env.example`, y cada carpeta de `src/` tiene su
propio `README.md`.

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

### Requisitos

- Docker.
- Docker Compose (incluido en Docker Desktop).

### Puesta en marcha

```bash
# 1. Crear el archivo de entorno a partir del ejemplo
cp .env.example .env          # Linux/macOS
Copy-Item .env.example .env   # PowerShell

# 2. Ajustar las contraseñas y el correo de pgAdmin en .env

# 3. Levantar todo
docker compose up --build
```

### URLs

| Servicio | URL |
|---|---|
| Frontend (SPA) | `http://localhost:3000` |
| API | `http://localhost:8080` |
| Salud de la API | `http://localhost:8080/actuator/health` |
| pgAdmin | `http://localhost:5050` |

El puerto del frontend se cambia con `FRONTEND_PORT` en `.env` (por ejemplo `FRONTEND_PORT=3001`);
el contenedor siempre escucha en 8080. Para reconstruir solo la SPA después de un cambio:
`docker compose build frontend`.

El servicio `api` espera a que PostgreSQL esté saludable (`condition: service_healthy`) y su
`HEALTHCHECK` consulta `/actuator/health`, que queda público por `SecurityConfig`. El resto de
rutas exige autenticación.

El servicio `frontend` es nginx sin privilegios: sirve `index.html` para cualquier ruta de la SPA
y hace proxy de `/api/` al servicio `api` por la red interna, así que el navegador no necesita
CORS. Depende de `api` con `condition: service_started`, de modo que arranca aunque la API tarde;
mientras no esté lista, `/api/**` responde `502`.

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

### Cambié las credenciales en `.env` y la API ya no conecta

PostgreSQL solo aplica `POSTGRES_USER` y `POSTGRES_PASSWORD` al crear la base por primera vez. Si
el volumen `postgres_data` ya existe con las credenciales anteriores, hay que recrearlo:

```bash
docker compose down -v
docker compose up --build
```

`down -v` borra la base de datos y los datos de pgAdmin; úsalo solo en desarrollo.

### La API responde 401 en todo

Es el comportamiento esperado mientras no exista el módulo `auth`: `SecurityConfig` solo deja
público `/actuator/health/**` y exige autenticación en el resto. El TODO de esa clase indica
dónde se reemplaza al implementar JWT. Por eso el esqueleto del frontend no llama a la API al
cargar, y una petición manual a `http://localhost:3000/api/servicios` devuelve `401` (no es un
fallo de nginx ni del proxy).

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
