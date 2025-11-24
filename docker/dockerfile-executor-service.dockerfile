FROM maven:3.9.8-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml ./pom.xml
COPY executor-service/pom.xml executor-service/pom.xml
COPY contracts/pom.xml contracts/pom.xml
COPY kafka-config/pom.xml kafka-config/pom.xml

COPY executor-service executor-service
COPY contracts contracts
COPY kafka-config kafka-config

RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -N -f pom.xml install

RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -DskipTests -f contracts/pom.xml clean install

RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -DskipTests -f kafka-config/pom.xml clean install

RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -DskipTests -f executor-service/pom.xml clean package


FROM eclipse-temurin:21-jre-alpine AS runtime

RUN apk update && apk add --no-cache docker-cli

WORKDIR /app

COPY --from=build /workspace/executor-service/target/executor-service-*.jar /app/app.jar

ENV JAVA_TOOL_OPTIONS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"

ENV JAVA_OPTS=""
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
