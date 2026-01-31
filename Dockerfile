<<<<<<< HEAD
# Dockerfile for Airline Booking System
# Candidate: Complete this Dockerfile to containerize the Vert.x application

FROM openjdk:21-jre-slim

# Set working directory


# Copy application files


# Expose port


# Run the application
=======
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/vertx-airline-booking-1.0.0-SNAPSHOT-shaded.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
>>>>>>> aa04445 (first Commit)
