# syntax=docker/dockerfile:1

# =============================================================================
# Etapa 1: compilación de la SPA
# Node 24 LTS alpine cumple el requisito de Vite 8 (Node ^20.19 || >=22.12).
# =============================================================================
FROM node:24-alpine AS build

WORKDIR /app

# Primero los manifiestos para reutilizar la caché de npm.
COPY package*.json ./
RUN npm ci

# La URL de la API se resuelve en tiempo de compilación (Vite la incrusta).
ARG VITE_API_BASE_URL=/api
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL

COPY . .
RUN npm run build

# =============================================================================
# Etapa 2: nginx sin privilegios (escucha en 8080, igual que la API)
# =============================================================================
FROM nginxinc/nginx-unprivileged:1.29-alpine

COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
    CMD wget -qO- http://127.0.0.1:8080/healthz || exit 1
