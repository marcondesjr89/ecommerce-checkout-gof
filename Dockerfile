# Stage 1: Application build
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace

# Copy Maven descriptors and wrapper first to leverage Docker layer caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source code and package application (tests already run in CI gate)
COPY src/ src/
RUN ./mvnw clean package -DskipTests -B

# Stage 2: Minimal hardened runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create non-root user for secure container execution
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy compiled artifact from builder stage
COPY --from=builder /workspace/target/*.jar app.jar
RUN chown -R appuser:appgroup /app

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
