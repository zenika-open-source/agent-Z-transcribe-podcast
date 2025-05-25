FROM maven:3.8-openjdk-17 AS builder

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src

EXPOSE 8080

ENTRYPOINT ["mvn", "exec:java", \
    "-Dexec.mainClass=com.google.adk.web.AdkWebServer", \
    "-Dexec.classpathScope=compile", \
    "-Dexec.args=--server.port=${PORT} --adk.agents.source-dir=src/main/java" \
]