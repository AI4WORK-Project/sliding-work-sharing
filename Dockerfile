# syntax=docker/dockerfile:1

# Version of the published Sliding Work Sharing release
ARG SWS_VERSION=0.1.1

# Download the published Sliding Work Sharing JAR from GitHub
FROM curlimages/curl:8.21.0 AS sws-release-downloader

ARG SWS_VERSION

RUN curl --fail --location --silent --show-error \
    "https://github.com/AI4WORK-Project/sliding-work-sharing/releases/download/v${SWS_VERSION}/sliding-work-sharing-${SWS_VERSION}.jar" \
    --output /tmp/sliding-work-sharing.jar

# todo: change with the java 25
# Create the application image
FROM eclipse-temurin:23-jre
ARG SWS_VERSION

LABEL org.opencontainers.image.title="Sliding Work Sharing"
LABEL org.opencontainers.image.description="Sliding Work Sharing Management Component of the AI4Work project"
LABEL org.opencontainers.image.source="https://github.com/AI4WORK-Project/sliding-work-sharing"
LABEL org.opencontainers.image.version="${SWS_VERSION}"

WORKDIR /app

COPY --from=sws-release-downloader /tmp/sliding-work-sharing.jar /app/sliding-work-sharing.jar

# Create a standard directory for custom YAML configuration and FCL rule files
# when starting the container, custom files can be mount into this directory
RUN mkdir -p /config

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/sliding-work-sharing.jar"]
