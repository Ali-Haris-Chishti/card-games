# Stage 1: Build
FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime with JavaFX GUI support
FROM eclipse-temurin:21-jre

# Install JavaFX native dependencies
RUN apt-get update && apt-get install -y \
    libx11-6 libxext6 libxrender1 libxtst6 libxi6 libglib2.0-0 libgtk-3-0 libcanberra-gtk3-module \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
RUN mkdir -p /app/cards

COPY --from=builder /app/target/Cards-1.0-SNAPSHOT.jar app.jar
COPY --from=builder /app/target/classes/com/ahccode/cards/card /app/cards

# Non-root user
RUN useradd -ms /bin/bash appuser
USER appuser

EXPOSE 5000
ENTRYPOINT ["java", "-jar", "app.jar"]