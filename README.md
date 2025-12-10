# 🤖 Agent Z transcribe podcast

<!-- ALL-CONTRIBUTORS-BADGE:START - Do not remove or modify this section -->
[![All Contributors](https://img.shields.io/badge/all_contributors-4-orange.svg?style=flat-square)](#contributors)
<!-- ALL-CONTRIBUTORS-BADGE:END -->
[![Release](https://github.com/zenika-open-source/agent-Z-transcribe-podcast/actions/workflows/release.yml/badge.svg)](https://github.com/zenika-open-source/agent-Z-transcribe-podcast/actions/workflows/release.yml)
[![GitHub Release](https://img.shields.io/github/v/release/zenika-open-source/agent-Z-transcribe-podcast)](https://github.com/zenika-open-source/agent-Z-transcribe-podcast/releases)

This is an AI Agent to transcribe podcasts to be easily readable as a book 🤘

## ✨ Features

- **Podcast Transcription**: Transcribes French tech podcasts into readable text.
- **Timestamps**: Option to include timestamps in the transcription.
- **Download**: Save transcription as a Markdown file directly from the browser (compatible with Cloud Run).
- **Modern AI**: Powered by Gemini 1.5 Pro.

## 🛠️ Configuration

**Prerequisites:**
- Java 25

1️⃣ Export these 2 variables:
```sh
export GOOGLE_GENAI_USE_VERTEXAI=FALSE
export GOOGLE_API_KEY=<your gemini API>
```

2️⃣ Create a `.env` file. The template `.env-template` is available.

## 🧑‍💻 Development

To run your agent (if you have only one specific agent):

```sh
mvn dependency:build-classpath -Dmdep.outputFile=cp.txt && java -cp target/classes:$(cat cp.txt) transcribe.ZPodcastTranscribe
```

Then go to [http://localhost:8080/](http://localhost:8080/).

## 🐳 Docker

This app can be run with Docker:

```sh
docker build -t agent-z-transcribe-podcast .
docker run -p 8080:8080 -e PORT=8080 agent-z-transcribe-podcast
```

## 🚀 Deployment

To deploy on Cloud Run:

1. Authenticate on GCP:
   ```sh
   gcloud auth login
   ```

2. Configure settings by exporting variables:
   ```sh 
   export GOOGLE_CLOUD_PROJECT=<your project>
   export GOOGLE_CLOUD_LOCATION=<location like europe-west1>
   export GOOGLE_GENAI_USE_VERTEXAI=<true if you deploy on Google Cloud, else false>
   export GOOGLE_API_KEY=<your GOOGLE API KEY>
   ```

3. Deploy using `gcloud`:
   ```sh
   gcloud run deploy agent-z-transcribe-podcast \
   --source . \
   --region $GOOGLE_CLOUD_LOCATION \
   --project $GOOGLE_CLOUD_PROJECT \
   --allow-unauthenticated \
   --memory 1Gi \
   --max-instances 1 \
   --set-env-vars="GOOGLE_CLOUD_PROJECT=$GOOGLE_CLOUD_PROJECT,GOOGLE_CLOUD_LOCATION=$GOOGLE_CLOUD_LOCATION,GOOGLE_GENAI_USE_VERTEXAI=$GOOGLE_GENAI_USE_VERTEXAI,GOOGLE_API_KEY=$GOOGLE_API_KEY"
   ```

## ✨ Contribute

Anyone can contribute to this project. For the moment, please add your question or purpose something in [a new issue](https://github.com/zenika-open-source/agent-Z-transcribe-podcast/issues).

## 🙏 Contributors

<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->
<table>
  <tbody>
    <tr>
      <td align="center" valign="top" width="14.28%"><a href="https://jeanphi-baconnais.gitlab.io/"><img src="https://avatars.githubusercontent.com/u/32639372?v=4" width="100px;" alt=""/><br /><sub><b>Jean-Phi Baconnais</b></sub></a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://www.bbourgeois.dev"><img src="https://avatars.githubusercontent.com/u/20949060?v=4" width="100px;" alt=""/><br /><sub><b>Benjamin</b></sub></a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/glefloch"><img src="https://avatars.githubusercontent.com/u/1827790?v=4" width="100px;" alt=""/><br /><sub><b>Guillaume Le Floch</b></sub></a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://ctftime.org/user/30785"><img src="https://avatars.githubusercontent.com/u/32420956?v=4" width="100px;" alt=""/><br /><sub><b>Gudsfile</b></sub></a></td>
    </tr>
  </tbody>
</table>

<!-- markdownlint-restore -->
<!-- prettier-ignore-end -->

<!-- ALL-CONTRIBUTORS-LIST:END -->

![with love by zenika](https://img.shields.io/badge/With%20%E2%9D%A4%EF%B8%8F%20by-Zenika-b51432.svg?link=https://oss.zenika.com)
