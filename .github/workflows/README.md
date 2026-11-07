# Workflows de GitHub Actions

Esta carpeta contiene la definición de la CI/CD. Por ahora solo este README: los archivos de
workflow se agregan cuando el backend y el frontend tengan algo ejecutable.

## Workflows esperados

| Archivo | Alcance | Se dispara con |
|---|---|---|
| `backend-ci.yml` | API Spring Boot (`backend/**`) | Pull Request hacia `develop` y `main`; push a `main` para desplegar. |
| `frontend-ci.yml` | Aplicación React (`frontend/**`) | Pull Request hacia `develop` y `main`; push a `main` para desplegar. |

No se crea un tercer workflow general: cada carpeta se valida y despliega con el suyo, filtrado
por ruta, para no ejecutar trabajos innecesarios.

## Disparadores

```yaml
on:
  pull_request:
    branches: [develop, main]
    paths:
      - "backend/**"
      - ".github/workflows/backend-ci.yml"
  push:
    branches: [main]
    paths:
      - "backend/**"
      - ".github/workflows/backend-ci.yml"
```

Reglas:

- La **CI** corre en Pull Requests hacia `develop` y hacia `main`, con filtro de ruta para que el
  workflow de backend no se ejecute si solo cambió el frontend (y viceversa).
- El **despliegue a producción** solo se dispara con cambios en `main`; un push a `develop` o una
  rama `feature/*` nunca despliega.
- `develop` es la rama de integración; `main` es la rama estable y desplegable.
- Los workflows se agregan a la protección de rama como checks obligatorios.

## Backend (`backend-ci.yml`)

1. Checkout del repositorio.
2. Configurar JDK 21 (Temurin) con caché de Maven.
3. `./mvnw -B verify` dentro de `backend/`: compila, ejecuta pruebas unitarias y de integración
   (Testcontainers usa PostgreSQL en el runner `ubuntu-latest`).
4. Validar `docker compose config` en la raíz.
5. En `push` a `main`: construir la imagen (`docker build`) y desplegar con el mecanismo que se
   defina en el ADR de despliegue. Las credenciales van en GitHub Secrets, nunca en el
   repositorio.

## Frontend (`frontend-ci.yml`)

1. Checkout del repositorio.
2. Configurar Node.js LTS con caché de npm.
3. `npm ci` dentro de `frontend/`.
4. Lint, verificación de tipos (`tsc --noEmit`) y build (`npm run build`).
5. En `push` a `main`: publicar el contenido de `frontend/dist/` en el hosting estático definido.

## Qué NO va aquí

- Credenciales, tokens ni claves: se configuran como GitHub Secrets.
- Scripts de despliegue con rutas locales de una máquina.
- Workflows que desplieguen desde `develop` o desde ramas de tarea.
- `npm install` en CI: se usa `npm ci` para builds reproducibles.

## Convenciones

- Nombres de archivo en minúsculas y con guiones: `backend-ci.yml`, `frontend-ci.yml`.
- Un workflow = una carpeta del monorepo.
- Los pasos tienen `name` en español para que el resumen sea legible.
- Toda acción de terceros se fija a una versión mayor estable (`actions/checkout@v4`).
