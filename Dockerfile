FROM maven:3.9-eclipse-temurin-25 AS builder

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn dependency:go-offline -B
RUN mvn compile -B

# Set default port
ENV PORT=8080

EXPOSE ${PORT}

# Use shell form to support command chaining with &&
ENTRYPOINT ["sh", "-c", "mvn dependency:build-classpath -Dmdep.outputFile=cp.txt && java -cp target/classes:$(cat cp.txt) ZPodcastTranscribe"]

