# syntax=docker/dockerfile:1.7

FROM eclipse-temurin:17-jdk AS builder
WORKDIR /workspace

COPY gradlew gradlew.bat settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x ./gradlew

COPY src ./src
RUN ./gradlew --no-daemon clean bootJar -x test

FROM eclipse-temurin:17-jre
ARG PROFILE=customer
ENV SPRING_PROFILES_ACTIVE=${PROFILE}
WORKDIR /app

COPY --from=builder /workspace/build/libs/*.jar /app/app.jar
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]