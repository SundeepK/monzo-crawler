FROM gradle:7.6.1-jdk17-alpine AS builder

COPY . /home/gradle/src

# Set the working directory
WORKDIR /home/gradle/src

# Build the application using Gradle
RUN gradle build --no-daemon


# Start with a base Java 17 image
FROM eclipse-temurin:17-alpine

# Set the working directory
WORKDIR /app

# Copy the jar file built using Gradle to the container
COPY --from=builder /home/gradle/src/build/libs/*.jar .

# Use the below to keep the container running for debugging
# CMD ["sh", "-c", "tail -f /dev/null"]

## Set the entrypoint to start the Java application
ENTRYPOINT ["java", "-jar", "monzo-crawler.jar"]
