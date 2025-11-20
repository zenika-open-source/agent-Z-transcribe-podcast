package agents;

import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import io.javelit.core.Jt;
import io.javelit.core.Server;
import io.reactivex.rxjava3.core.Flowable;

import java.util.ArrayList;
import java.util.List;

public class AgentJavelitServer {

    private static final String APP_NAME = "agent-z-transcribe-podcast";
    private static InMemoryRunner runner;
    private static Session session;
    private static List<Message> chatHistory = new ArrayList<>();

    record Message(String sender, String text) {
    }

    public static void main(String[] args) {
        // Initialize the agent runner
        runner = new InMemoryRunner(AgentZTranscribePodcast.ROOT_AGENT, APP_NAME);
        session = runner
                .sessionService()
                .createSession(APP_NAME, "agent-transcribe")
                .blockingGet();

        // Start Javelit server
        Server.builder(AgentJavelitServer::app, 8888).build().start();
    }

    public static void app() {
        Jt.markdown("# 🎙️ Agent Z Transcribe Podcast").key("title").use();
        Jt.markdown("---").key("separator-1").use();

        // Display chat history
        if (!chatHistory.isEmpty()) {
            Jt.markdown("## 📝 Historique des transcriptions").key("history-title").use();
            int index = 0;
            for (Message msg : chatHistory) {
                if ("user".equals(msg.sender)) {
                    Jt.markdown("### " + msg.text).key("user-msg-" + index).use();
                } else {
                    Jt.markdown("**Transcription :**").key("transcription-label-" + index).use();
                    Jt.textArea(msg.text).key("transcription-" + index).use();
                }
                index++;
            }
        }

        // File upload
        Jt.markdown("## 🎤 Uploader un fichier audio").key("upload-title").use();
        var uploadedFiles = Jt.fileUploader("Sélectionnez un fichier audio (mp3, wav, m4a)").key("file-uploader").use();
        if (uploadedFiles != null && !uploadedFiles.isEmpty()) {
            var uploadedFile = uploadedFiles.get(0);
            String fileName = uploadedFile.filename().toLowerCase();
            if (fileName.endsWith(".mp3") || fileName.endsWith(".wav") || fileName.endsWith(".m4a")) {
                Jt.markdown("✅ **Fichier uploadé :** " + uploadedFile.filename()).key("file-uploaded").use();

                // Determine MIME type
                String mimeType;
                if (fileName.endsWith(".mp3")) {
                    mimeType = "audio/mpeg";
                } else if (fileName.endsWith(".wav")) {
                    mimeType = "audio/wav";
                } else {
                    mimeType = "audio/mp4"; // m4a
                }

                // Process the audio file
                byte[] audioBytes = uploadedFile.content();
                Content audioMsg = Content.fromParts(
                        Part.fromBytes(audioBytes, mimeType),
                        Part.fromText("Donne la transcription de épisode de podcast"));

                // Process transcription (without showing "in progress" messages)
                Flowable<Event> events = runner.runAsync(session.userId(), session.id(), audioMsg);

                StringBuilder transcription = new StringBuilder();
                events.blockingForEach(event -> {
                    String responseText = event.stringifyContent();
                    transcription.append(responseText);
                });

                // Add to history
                chatHistory.add(new Message("user", "📎 " + uploadedFile.filename()));
                chatHistory.add(new Message("agent", transcription.toString()));

                // Display the completed transcription immediately
                Jt.markdown("---").key("separator-result").use();
                Jt.markdown("### ✅ Transcription terminée !").key("completed-title").use();
                Jt.markdown("**Fichier :** " + uploadedFile.filename()).key("completed-filename").use();
                Jt.markdown("**Transcription :**").key("completed-label").use();
                Jt.textArea(transcription.toString()).key("completed-transcription").use();
            } else {
                Jt.markdown("❌ **Type de fichier invalide.** Veuillez uploader un fichier MP3, WAV ou M4A.")
                        .key("error-invalid-type").use();
            }
        }
    }
}
