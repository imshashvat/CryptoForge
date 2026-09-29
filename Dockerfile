# syntax=docker/dockerfile:1

# Stage 1: Build the application with Maven
FROM maven:3.9.6-eclipse-temurin-21-jammy AS build
WORKDIR /workspace

# Pre-fetch dependencies to leverage Docker layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy sources and build WAR
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Minimal runtime image
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Create a non-privileged system user for security
RUN groupadd -r cryptoforge && useradd -r -g cryptoforge cryptoforge

COPY --from=build --chown=cryptoforge:cryptoforge /workspace/target/cryptoforge-1.0.0.war app.war

USER cryptoforge

EXPOSE 8080

ENV PORT=8080
ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT} -Djava.security.egd=file:/dev/./urandom -jar app.war"]
