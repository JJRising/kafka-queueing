FROM amazoncorretto:17-alpine
VOLUME /app
COPY ./testing-interface*.jar ./app.jar
ENTRYPOINT ["java", "-jar", "./app.jar"]
