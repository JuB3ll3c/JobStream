FROM maven:3.9-eclipse-temurin-25-alpine AS build
WORKDIR /app

COPY codebase/dashboard-service/pom.xml ./dashboard-service/
RUN mvn -f dashboard-service/pom.xml dependency:go-offline -B

COPY openapi/openapi.yml ./openapi/
COPY codebase/dashboard-service/src ./dashboard-service/src

RUN mvn -f dashboard-service/pom.xml clean package -DskipTests


FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /app/dashboard-service/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]