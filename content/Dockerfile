FROM openjdk:21-jdk-slim AS builder
WORKDIR /opt/app
COPY . .
RUN ./gradlew clean build -x test --no-daemon
FROM openjdk:21-jdk-slim
WORKDIR /opt/app
COPY --from=builder /opt/app/build/libs/content-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8090
ENTRYPOINT ["java", "-jar", "app.jar"]