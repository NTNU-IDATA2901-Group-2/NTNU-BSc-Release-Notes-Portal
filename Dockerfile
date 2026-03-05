FROM eclipse-temurin:25

EXPOSE 8080

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java","-jar","/app.jar", "--spring.profiles.active=prod"]