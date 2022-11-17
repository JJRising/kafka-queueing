FROM amazoncorretto:17-alpine
VOLUME /app
COPY ./kafka-queueing*.jar ./app.jar
ENTRYPOINT ["java", "-jar", "./app.jar"]
