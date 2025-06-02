# 🤖 Agent Z transcribe podcast

This is an IA Agent to transcribe podcasts to be easily readable as a book.

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
export GOOGLE_GENAI_USE_VERTEXAI=<true if you deploy on Google Cloud, else false>
export GOOGLE_API_KEY=<your GOOGLE API KEY>
```

- and run the `gcloud` command.

```sh
gcloud run deploy agent-z-transcribe-podcast \
--source . \
--region $GOOGLE_CLOUD_LOCATION \
--project $GOOGLE_CLOUD_PROJECT \
--allow-unauthenticated \
--memory 1Gi \
--max-instances 5 \
--set-env-vars="GOOGLE_CLOUD_PROJECT=$GOOGLE_CLOUD_PROJECT,GOOGLE_CLOUD_LOCATION=$GOOGLE_CLOUD_LOCATION,GOOGLE_GENAI_USE_VERTEXAI=$GOOGLE_GENAI_USE_VERTEXAI,GOOGLE_API_KEY=$GOOGLE_API_KEY"
```


## ✨Contribute

Anyone can contribute to this project. For the moment, please add your question or purpose something in [a new issue](https://github.com/zenika-open-source/opensource-statistics/issues).

![with love by zenika](https://img.shields.io/badge/With%20%E2%9D%A4%EF%B8%8F%20by-Zenika-b51432.svg?link=https://oss.zenika.com)

