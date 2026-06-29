FROM maven:3.9.9-eclipse-temurin-21-alpine AS build

WORKDIR /workspace

COPY portal-logging/pom.xml portal-logging/pom.xml
COPY portal-logging/src/ portal-logging/src/
RUN mvn -B -f portal-logging/pom.xml clean install -DskipTests

COPY core-backend/.mvn/ .mvn/
COPY core-backend/mvnw core-backend/pom.xml ./
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline

COPY core-backend/src/ src/
RUN ./mvnw -B clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S app && adduser -S app -G app

WORKDIR /app

COPY --from=build /workspace/target/*.jar app.jar

USER app

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
