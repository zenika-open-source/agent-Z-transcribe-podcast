# agent-Z-transcribe-podcast
An IA Agent to transcibe podcasts

## 🛠️ Configuration

Export these 2 variables:
```sh
export GOOGLE_GENAI_USE_VERTEXAI=FALSE
export GOOGLE_API_KEY=<your gemini API>
```

## ✨ Development

To run your agent :

```sh
mvn compile exec:java -Dexec.mainClass="agents.AgentZTranscribePodcast"
```

To run the UI, run this command and got to ```http://localhost:8080/dev-ui?```

```
mvn exec:java \
    -Dexec.mainClass="com.google.adk.web.AdkWebServer" \
    -Dexec.classpathScope="compile"
```

## 🚀 Deployment

To deploy on Cloud Run:

- configure settings exporting variables:

```sh 
export GOOGLE_CLOUD_PROJECT=<your projet>
export GOOGLE_CLOUD_LOCATION=<location like europe-west1>
```

```sh
gcloud run deploy my-cook-agent \
--source . \
--region $GOOGLE_CLOUD_LOCATION \
--project $GOOGLE_CLOUD_PROJECT \
--allow-unauthenticated \
--set-env-vars="GOOGLE_CLOUD_PROJECT=$GOOGLE_CLOUD_PROJECT,GOOGLE_CLOUD_LOCATION=$GOOGLE_CLOUD_LOCATION,GOOGLE_GENAI_USE_VERTEXAI=$GOOGLE_GENAI_USE_VERTEXAI"
``