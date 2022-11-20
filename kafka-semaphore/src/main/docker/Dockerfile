FROM amazoncorretto:17-alpine
VOLUME /app
COPY ./kafka-semaphore*.jar ./app.jar
ENTRYPOINT ["java", "-jar", "./app.jar"]
