FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/kafkastream-0.0.1-SNAPSHOT.jar app.jar

CMD ["java", "-jar", "app.jar"]
