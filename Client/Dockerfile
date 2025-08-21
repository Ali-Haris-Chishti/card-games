# Multi-stage build for Client
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Set working directory
WORKDIR /app

# Copy parent POM first
COPY pom.xml .

# Copy ALL module POMs (Maven needs to see all modules)
COPY Common/pom.xml ./Common/
COPY Client/pom.xml ./Client/
COPY Server/pom.xml ./Server/

# Copy ALL source code
COPY Common/src ./Common/src
COPY Client/src ./Client/src
COPY Server/src ./Server/src

# Build only Client and its dependencies
RUN mvn clean package -pl Client -am -DskipTests

# Runtime stage with X11 support for JavaFX
FROM eclipse-temurin:21-jre-jammy

# Install necessary packages for JavaFX GUI
RUN apt-get update && apt-get install -y \
    libxrender1 \
    libxtst6 \
    libxi6 \
    libgl1-mesa-glx \
    libgtk-3-0 \
    xvfb \
    && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/Client/target/cards-client.jar ./cards-client.jar

# Set display for X11 forwarding
ENV DISPLAY=:0

# Create a script to run with virtual display if needed
RUN echo '#!/bin/bash\n\
if [ -z "$DISPLAY" ]; then\n\
    echo "Starting virtual display..."\n\
    Xvfb :1 -screen 0 1024x768x24 &\n\
    export DISPLAY=:1\n\
fi\n\
java -jar cards-client.jar' > /app/start-client.sh && \
chmod +x /app/start-client.sh

# Default command
ENTRYPOINT ["/app/start-client.sh"]