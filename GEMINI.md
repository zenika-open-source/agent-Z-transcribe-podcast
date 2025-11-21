# Gemini CLI Project Context: agent-Z-transcribe-podcast

## Project Overview

This project is a Java-based AI agent designed to transcribe podcasts. It leverages the Google Agent Development Kit (ADK) to create an LLM-powered agent. The core of the project is the `AgentZTranscribePodcast` class, which configures and initializes an `LlmAgent` using the `gemini-2.5-pro` model. The agent is specifically instructed to transcribe French-language podcasts about tech topics, cleaning up the text to be more readable.

The project is built using Apache Maven, and its dependencies include the `google-adk` and `google-adk-dev` libraries.

## Building and Running

### Local Development

To run the agent locally, you can use the following Maven commands:

**Run a specific agent:**
```sh
mvn compile exec:java -Dexec.mainClass="agents.AgentZTranscribePodcast"
```

**Run the agent with the ADK web server and UI:**
```sh
mvn exec:java -Dexec.mainClass=com.google.adk.web.AdkWebServer -Dexec.classpathScope=compile -Dexec.args="--server.port=8080 --adk.agents.source-dir=src/main/java/agents/"
```
After running this command, the development UI is available at `http://localhost:8080/dev-ui`.

### Docker

The project includes a `Dockerfile` that can be used to build and run the application in a container.

**Build the Docker image:**
```sh
docker build -t agent-z-transcribe-podcast .
```

**Run the Docker container:**
```sh
docker run -p 8080:8080 -e PORT=8080 agent-z-transcribe-podcast
```

## Development Conventions

*   **Dependency Management:** The project uses Maven for managing dependencies. Dependencies are declared in the `pom.xml` file.
*   **Testing:** The project is set up to use JUnit 5 for testing. Tests are located in the `src/test/java` directory.
*   **Agent Logic:** The core agent logic and configuration are located in `src/main/java/agents/AgentZTranscribePodcast.java`. The `initAgent()` method in this class is the primary place where the agent's behavior is defined.
*   **Configuration:** The application requires API keys and other configuration to be set as environment variables or in a `.env` file, as documented in the `README.md`.
