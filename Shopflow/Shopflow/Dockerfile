# Build stage
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/shopflow-0.0.1-SNAPSHOT.jar app.jar
# We expect the serviceAccountKey.json to be provided via environment variable or secret in production,
# but for the container build we might need a placeholder or handle it via Cloud Run secrets.
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=${PORT:-8080}"]
