FROM bellsoft/liberica-openjdk-alpine:21
VOLUME /tmp
ENV SERVER_PORT=8080
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
EXPOSE ${SERVER_PORT}
ENTRYPOINT ["java","-jar","/app.jar"]
