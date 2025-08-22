# 🤖 Agent Z transcribe podcast

This is an IA Agent to transcribe podcasts to be easily readable as a book.

## 🛠️ Configuration

1️⃣ Export these 2 variables:
```sh
export GOOGLE_GENAI_USE_VERTEXAI=FALSE
export GOOGLE_API_KEY=<your gemini API>
```

2️⃣ Create a `src/main/resources/credentials.json` containing the credential for the Google Docs API (cf [here](https://console.cloud.google.com/apis/credentials))

## ✨ Development

To run your agent (if you have only one specific agent):

```sh
mvn compile exec:java -Dexec.mainClass="agents.AgentZTranscribePodcast"
```

or to your project with some agents (where `--adk.agents.source-dir` contains the directory where agents are located):

```sh
mvn exec:java -Dexec.mainClass=com.google.adk.web.AdkWebServer -Dexec.classpathScope=compile  -Dexec.args="--server.port=8080 --adk.agents.source-dir=src/main/java/agents/"
```

To run the UI, run this command and go to ```http://localhost:8080/dev-ui?```


## 🚀 Deployment

To deploy on Cloud Run:

- authentification on GCP with `gcloud auth login`

- configure settings exporting variables:

```sh 
export GOOGLE_CLOUD_PROJECT=<your projet>
export GOOGLE_CLOUD_LOCATION=<location like europe-west1>
export GOOGLE_GENAI_USE_VERTEXAI=<true if you deploy on Google Cloud, else false>
export GOOGLE_API_KEY=<your GOOGLE API KEY>
```

or adding them into a `.env` file (cf `env-template` file)
```
export GCLOUD_PROJECT=
export GCLOUD_LOCATION=europe-west1
export GCLOUD_GENAI_USE_VERTEXAI=FALSE
export GOOGLE_API_KEY=
```

- and run the `gcloud` command.

```sh
gcloud run deploy agent-z-transcribe-podcast \
--source . \
--region $GCLOUD_LOCATION \
--project $GCLOUD_PROJECT \
--allow-unauthenticated \
--memory 1Gi \
--max-instances 1 \
--set-env-vars="GOOGLE_CLOUD_PROJECT=$GCLOUD_PROJECT,GOOGLE_CLOUD_LOCATION=$GCLOUD_LOCATION,GOOGLE_GENAI_USE_VERTEXAI=$GCLOUD_GENAI_USE_VERTEXAI,GOOGLE_API_KEY=$GOOGLE_API_KEY,GOOGLE_CLOUD_CREDENTIAL=$GOOGLE_CLOUD_CREDENTIAL"

```


## ✨Contribute

Anyone can contribute to this project. For the moment, please add your question or purpose something in [a new issue](https://github.com/zenika-open-source/opensource-statistics/issues).

![with love by zenika](https://img.shields.io/badge/With%20%E2%9D%A4%EF%B8%8F%20by-Zenika-b51432.svg?link=https://oss.zenika.com)

