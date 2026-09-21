# --- Stage 1: Build the jar with Maven ---
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml first and download dependencies separately, so Docker can
# cache this layer - dependencies only re-download when pom.xml changes,
# not on every source code edit. Speeds up rebuilds a lot.
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

# --- Stage 2: Run the jar on a lean JRE-only image ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/target/text-to-speech-backend-1.0.0.jar app.jar

# Render sets $PORT at runtime and expects the app to bind to it.
# server.port isn't hardcoded in application.properties, so this env var
# drives it via Spring Boot's standard SERVER_PORT convention.
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=${PORT}"]