FROM maven:3.9.8-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml ./pom.xml
COPY user-service/pom.xml user-service/pom.xml

COPY user-service user-service

RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -DskipTests -f user-service/pom.xml clean package

FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

COPY --from=build /workspace/user-service/target/user-service-*.jar /app/app.jar

COPY user-service/jwt_private.pem /app/jwt_private.pem
COPY user-service/jwt_public.pem /app/jwt_public.pem

ENV JAVA_OPTS=""
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
