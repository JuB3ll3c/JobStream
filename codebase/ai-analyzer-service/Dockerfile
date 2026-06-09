# Étape 1 : Build de l'application
FROM maven:3.9-eclipse-temurin-25-alpine AS build
WORKDIR /app

COPY codebase/ai-analyzer-service/pom.xml ./ai-analyzer-service/pom.xml
RUN mvn dependency:go-offline -B

COPY openapi/openapi.yml ./openapi/openapi.yml
COPY codebase/ai-analyzer-service/src ./ai-analyzer-service/src

RUN mvn -f ai-analyzer-service/pom.xml clean package -DskipTests


FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

RUN addgroup -S springia && adduser -S springia -G springia
USER springia:springia

COPY --from=build /app/ai-analyzer-service/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]