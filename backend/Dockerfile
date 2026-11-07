# syntax=docker/dockerfile:1

# =============================================================================
# Etapa 1: compilación con JDK 21 y Maven
# Se copia primero el pom.xml para reutilizar la caché de dependencias.
# =============================================================================
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml ./
RUN mvn -B -ntp -q dependency:go-offline

COPY src ./src
RUN mvn -B -ntp -q clean package -DskipTests

# =============================================================================
# Etapa 2: imagen final ligera (JRE 21 alpine), usuario no root
# =============================================================================
FROM eclipse-temurin:21-jre-alpine

ENV TZ=America/Bogota \
    JAVA_OPTS="-XX:MaxRAMPercentage=75"

# tzdata asegura que la JVM resuelva America/Bogota dentro del contenedor.
RUN apk add --no-cache tzdata \
    && addgroup -S oalbarber \
    && adduser -S oalbarber -G oalbarber

WORKDIR /app

COPY --from=build /workspace/target/*.jar /app/app.jar
RUN chown -R oalbarber:oalbarber /app

USER oalbarber

EXPOSE 8080

# El endpoint /actuator/health queda público gracias a SecurityConfig.
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD wget -qO- http://127.0.0.1:8080/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
