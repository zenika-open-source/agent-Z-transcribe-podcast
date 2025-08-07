FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .
COPY src ./src
COPY Dockerfile .

RUN mvn dependency:go-offline -B
RUN mvn compile -B

EXPOSE 8080

ENTRYPOINT ["mvn", "exec:java", \
    "-Dexec.mainClass=com.google.adk.web.AdkWebServer", \
    "-Dexec.classpathScope=compile", \
    "-Dexec.args=--server.port=${PORT} --adk.agents.source-dir=src/main/java" \
]