FROM maven:3.9.8-eclipse-temurin-21 AS build

ARG MODULE

WORKDIR /workspace

COPY pom.xml ./pom.xml
COPY contracts/pom.xml contracts/pom.xml
COPY kafka-config/pom.xml kafka-config/pom.xml
COPY submission-api/pom.xml submission-api/pom.xml
COPY evaluation-orchestrator/pom.xml evaluation-orchestrator/pom.xml
COPY executor-service/pom.xml executor-service/pom.xml
COPY ai-feedback/pom.xml ai-feedback/pom.xml
COPY user-service/pom.xml user-service/pom.xml
COPY similarity-service/pom.xml similarity-service/pom.xml
COPY authorship-service/pom.xml authorship-service/pom.xml

COPY contracts contracts
COPY kafka-config kafka-config
COPY submission-api submission-api
COPY evaluation-orchestrator evaluation-orchestrator
COPY executor-service executor-service
COPY ai-feedback ai-feedback
COPY user-service user-service
COPY similarity-service similarity-service
COPY authorship-service authorship-service

RUN mvn -B -DskipTests -pl ${MODULE} -am clean package

FROM eclipse-temurin:21-jre AS runtime
ARG MODULE
WORKDIR /app

COPY --from=build /workspace/${MODULE}/target/${MODULE}-*.jar /app/app.jar

ENV JAVA_TOOL_OPTIONS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"

ENV JAVA_OPTS=""
EXPOSE 8080
EXPOSE 5005
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
