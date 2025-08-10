FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /producer-api

COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn
COPY producer-api/pom.xml .
COPY producer-api/src ./src

RUN chmod +x mvnw
RUN ./mvnw clean package

FROM eclipse-temurin:21-jre-alpine

WORKDIR /producer-api
COPY --from=builder /producer-api/target/*.jar producer-api.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "producer-api.jar"]