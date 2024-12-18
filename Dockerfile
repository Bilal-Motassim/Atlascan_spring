FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

ENV MAVEN_OPTS="-Dfile.encoding=UTF-8"

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests \
    -Dproject.build.sourceEncoding=UTF-8 \
    -Dproject.reporting.outputEncoding=UTF-8

FROM openjdk:21-slim
WORKDIR /app

COPY --from=build /app/target/Atlascan_spring-0.0.1-SNAPSHOT.jar app.jar

CMD ["java", "-jar", "app.jar"]
