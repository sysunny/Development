# Use Maven to build the application
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml ./
COPY src ./src
RUN mvn -e -B -DskipTests package

# Run stage
FROM eclipse-temurin:17-jdk-jammy
ARG JAR_FILE=/workspace/target/payment-service-0.0.1-SNAPSHOT.jar
COPY --from=build ${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
