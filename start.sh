#!/bin/bash
set -e

# Charger et exporter les variables d'environnement depuis le fichier .env
if [ -f .env ]; then
  echo "Chargement et export des variables d'environnement depuis .env..."
  set -a
  # shellcheck disable=SC1091
  source .env
  set +a
else
  echo "⚠️ Fichier .env non trouvé."
fi

# Vérifier la présence du classpath et de la compilation
if [ ! -f "cp.txt" ] || [ ! -d "target/classes" ]; then
  echo "🔨 Compilation et génération du classpath..."
  mvn compile dependency:build-classpath -Dmdep.outputFile=cp.txt
fi

# Exécuter l'application
echo "🚀 Démarrage de l'application..."
java -cp "target/classes:$(cat cp.txt)" transcribe.ZPodcastTranscribe "$@"
