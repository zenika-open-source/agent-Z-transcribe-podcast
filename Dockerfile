FROM maven:3.9-eclipse-temurin-25 AS builder

WORKDIR /app

COPY pom.xml .
COPY src ./src

# Build classpath at build time (not runtime) for faster startup
RUN mvn dependency:go-offline -B
RUN mvn compile -B
RUN mvn dependency:build-classpath -Dmdep.outputFile=cp.txt

# Set default port
ENV PORT=8080

EXPOSE ${PORT}

# Use the pre-computed classpath for faster startup
ENTRYPOINT ["sh", "-c", "java -cp target/classes:$(cat cp.txt) transcribe.ZPodcastTranscribe"]

