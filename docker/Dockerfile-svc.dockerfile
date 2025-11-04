FROM maven:3.9.8-eclipse-temurin-21 AS build

ARG MODULE

WORKDIR /workspace

COPY pom.xml ./pom.xml
COPY contracts/pom.xml contracts/pom.xml
COPY submission-api/pom.xml submission-api/pom.xml
COPY evaluation-orchestrator/pom.xml evaluation-orchestrator/pom.xml
COPY executor-service/pom.xml executor-service/pom.xml
COPY ai-feedback/pom.xml ai-feedback/pom.xml
COPY user-service/pom.xml user-service/pom.xml

COPY contracts contracts
COPY submission-api submission-api
COPY evaluation-orchestrator evaluation-orchestrator
COPY executor-service executor-service
COPY ai-feedback ai-feedback
COPY user-service user-service

RUN mvn -B -DskipTests -pl ${MODULE} -am clean package

FROM eclipse-temurin:21-jre AS runtime
ARG MODULE
WORKDIR /app

COPY --from=build /workspace/${MODULE}/target/${MODULE}-*.jar /app/app.jar

ENV JAVA_OPTS=""
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
