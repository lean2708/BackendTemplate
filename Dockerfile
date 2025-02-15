FROM openjdk:17

ARG FILE_JAR=target/tayjava-sample-code-0.0.1-SNAPSHOT.jar

ADD ${FILE_JAR} api-server.jar

ENTRYPOINT ["java", "-jar", "api-server.jar"]

EXPOSE 8080