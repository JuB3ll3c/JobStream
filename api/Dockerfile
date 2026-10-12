#Build stage
FROM maven:3.9.12-eclipse-temurin-25 AS build
WORKDIR /build
COPY api/pom.xml ./api/
COPY api/src ./api/src
COPY openapi/openapi.yaml ./openapi/openapi.yaml

WORKDIR /build/api
RUN mvn clean package -DskipTests

#Run stage
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=build /build/api/target/*.jar app.jar
RUN addgroup -S app && adduser -S app -G app && chown -R app:app /app
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]