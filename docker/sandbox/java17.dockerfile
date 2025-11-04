FROM eclipse-temurin:17-jdk-jammy

RUN useradd -ms /bin/bash sandbox
USER sandbox
WORKDIR /workspace
