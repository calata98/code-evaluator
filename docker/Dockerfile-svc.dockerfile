# ---- build ----
FROM maven:3.9.8-eclipse-temurin-21 AS build

ARG MODULE

WORKDIR /workspace

COPY pom.xml ./pom.xml
COPY contracts/pom.xml contracts/pom.xml
COPY producer-api/pom.xml producer-api/pom.xml
COPY consumer-service/pom.xml consumer-service/pom.xml

COPY contracts contracts
COPY producer-api producer-api
COPY consumer-service consumer-service

RUN mvn -B -DskipTests -pl ${MODULE} -am clean package

# ---- runtime ----
FROM eclipse-temurin:21-jre AS runtime
ARG MODULE
WORKDIR /app

COPY --from=build /workspace/${MODULE}/target/${MODULE}-*.jar /app/app.jar

ENV JAVA_OPTS=""
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
