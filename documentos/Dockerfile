FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src src
RUN mvn -B -q package -DskipTests
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/documentos-1.0.0.jar app.jar
ENTRYPOINT ["java","-Xmx256m","-jar","app.jar"]
