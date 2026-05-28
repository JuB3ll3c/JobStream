# Étape 1 : Build de l'application
FROM maven:3.9-eclipse-temurin-25-alpine AS build
WORKDIR /app

COPY dashboard-service/pom.xml ./dashboard-service/pom.xml
WORKDIR /app/dashboard-service
RUN mvn dependency:go-offline -B

WORKDIR /app
COPY openapi/openapi.yml ./openapi/openapi.yml
COPY dashboard-service/src ./dashboard-service/src

WORKDIR /app/dashboard-service
RUN mvn clean package -DskipTests

# Étape 2 : Image de runtime JRE Java 25
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /app/dashboard-service/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]