FROM eclipse-temurin:25
ENTRYPOINT ["java","-jar","target/*.jar", "--spring.profiles.active=prod"]