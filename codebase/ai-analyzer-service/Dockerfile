# Étape 1 : Build de l'application
FROM maven:3.9-eclipse-temurin-25-alpine AS build
WORKDIR /app

COPY ai-analyzer-service/pom.xml ./ai-analyzer-service/pom.xml
WORKDIR /app/ai-analyzer-service
RUN mvn dependency:go-offline -B

WORKDIR /app
COPY openapi/openapi.yml ./openapi/openapi.yml
COPY ai-analyzer-service/src ./ai-analyzer-service/src

WORKDIR /app/ai-analyzer-service
RUN mvn clean package -DskipTests

# Étape 2 : Image de runtime JRE Java 25
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

RUN addgroup -S springia && adduser -S springia -G springia
USER springia:springia

COPY --from=build /app/ai-analyzer-service/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]