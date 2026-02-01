# =========================
# Build stage (Java 21 + Maven)
# =========================
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy pom first for better layer caching
COPY pom.xml .

# Download dependencies offline
RUN mvn -B dependency:go-offline

# Copy source code
COPY src ./src

# Build the application and create shaded jar
RUN mvn clean package -DskipTests

# =========================
# Runtime stage (lightweight JRE)
# =========================
FROM eclipse-temurin:21-jre

WORKDIR /app

# 1. Copy the shaded jar
COPY --from=build /app/target/airline-booking-1.0.0-SNAPSHOT-shaded.jar app.jar

# 2. ADD THIS LINE: Copy the resources so the hardcoded path works
COPY --from=build /app/src/main/resources ./src/main/resources

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
