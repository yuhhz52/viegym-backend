# Multi-stage build for optimized production image
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml first for better layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Production stage
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Create non-root user for security
RUN groupadd -r spring && useradd -r -g spring spring

# Copy the jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Change ownership to spring user
RUN chown spring:spring app.jar

# Switch to non-root user
USER spring:spring

# Expose port
EXPOSE 8080

# Health check - Use PORT env var or default to 8080
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:${PORT:-8080}/actuator/health || exit 1

# JVM tuning for container environments
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:InitialRAMPercentage=50.0", \
    "-XX:+ExitOnOutOfMemoryError", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", \
    "app.jar"]
