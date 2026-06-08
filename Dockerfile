# Stage 1 - Build
# Image tag
FROM maven:4.0.0-rc-5-eclipse-temurin-21 AS builder

# Set working directory
WORKDIR /app

# Copy the pom.xml first
COPY pom.xml .

# Copy source code into /app/src
COPY src ./src

# Compile code and package it
RUN mvn package


# Stage 2 - Run
# Use Java 21 runtime
FROM eclipse-temurin:21-jdk-ubi10-minimal

# Set working directory
WORKDIR /app

# Copy jar file
COPY --from=builder /app/target/*.jar app.jar

# Expose application port
EXPOSE 8080

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]


# Stage 3 - Test
# Image tag
FROM maven:4.0.0-rc-5-eclipse-temurin-21 AS tester

# Set working directory
WORKDIR /app

COPY pom.xml .
COPY .mvn .mvn

# Copy source code into /app/src
COPY src ./src

# Run tests
CMD ["mvn", "-ntp", "test"]