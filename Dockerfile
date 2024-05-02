FROM bellsoft/liberica-openjdk-alpine:21
VOLUME /tmp
ARG JAR_FILE=build/libs/HKI-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]