# Multi-stage build for Server
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# Copy parent POM and assets
COPY pom.xml .
COPY assets ./assets/

# Copy module POMs
COPY Common/pom.xml ./Common/
COPY Server/pom.xml ./Server/
COPY Client/pom.xml ./Client/

# Copy source code
COPY Common/src ./Common/src
COPY Server/src ./Server/src
COPY Client/src ./Client/src

# Build Server module
RUN mvn clean package -pl Server -am -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copy the JAR
COPY --from=build /app/Server/target/cards-server.jar ./cards-server.jar

# Expose port
EXPOSE 5000

# Set default port via environment variable
ENV IN_PORT=5000

# Run the server with environment variable
CMD ["sh", "-c", "java -jar cards-server.jar ${IN_PORT}"]