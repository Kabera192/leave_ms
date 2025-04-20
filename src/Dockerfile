# Specify the JAVA jdk base image with a specific version for better security
FROM eclipse-temurin:17-jdk-alpine

# Create a new user who belongs to a new group so as to not use the root user
RUN addgroup -S app && adduser -S app -G app

# Set working directory
WORKDIR /app

# Copy jar file into docker's file system
COPY target/*.jar app.jar

# Switch to non-root user
USER app

EXPOSE 8080

# Specify and run the command to execute the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]