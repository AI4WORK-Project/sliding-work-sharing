# syntax=docker/dockerfile:1

# Create the application image
FROM eclipse-temurin:25-jre

ARG SWS_VERSION

LABEL org.opencontainers.image.title="Sliding Work Sharing"
LABEL org.opencontainers.image.description="Sliding Work Sharing Management Component of the AI4Work project"
LABEL org.opencontainers.image.source="https://github.com/AI4WORK-Project/sliding-work-sharing"
LABEL org.opencontainers.image.version=${SWS_VERSION}

WORKDIR /app

# Copy the JAR produced by Maven
COPY target/sliding-work-sharing-${SWS_VERSION}.jar /app/

# Create a standard directory for custom YAML configuration and FCL rule files
# when starting the container, custom files can be mount into this directory
RUN mkdir -p /config

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -jar /app/sliding-work-sharing-${SWS_VERSION}.jar"]
