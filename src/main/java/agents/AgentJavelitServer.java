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
    private static boolean showHistory = false; // Track history visibility
    private static boolean readingMode = true; // Track transcription mode (true = reading mode, false = strict)

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

        // Options section with toggles
        Jt.markdown("## ⚙️ Options").key("options-title").use();
        showHistory = Jt.toggle("Show Transcription History")
                .value(showHistory)
                .key("toggle-show-history")
                .use();
        readingMode = Jt.toggle("Reading Mode (vs Strict Transcription)")
                .value(readingMode)
                .key("toggle-reading-mode")
                .use();
        Jt.markdown("---").key("separator-2").use();

        // Display chat history
        if (!chatHistory.isEmpty() && showHistory) {
            Jt.markdown("## 📝 Transcription History").key("history-title").use();
            boolean resetClicked = Jt.button("🗑️ Reset History").key("reset-button").use();
            if (resetClicked) {
                chatHistory.clear();
            }

            int index = 0;
            for (Message msg : chatHistory) {
                if ("user".equals(msg.sender)) {
                    Jt.markdown("### " + msg.text).key("user-msg-" + index).use();
                } else {
                    // Compact transcription display with limited height
                    Jt.markdown("**Transcription:**").key("transcription-label-" + index).use();
                    Jt.markdown(msg.text).key("transcription-" + index).use();
                }
                index++;
            }
            Jt.markdown("---").key("separator-history").use();
        }

        // Upload section
        Jt.markdown("## 🎶 Upload Audio File").key("upload-title").use();
        var uploadedFiles = Jt.fileUploader("Select an audio file (mp3, wav, m4a)").key("file-uploader").use();

        // Context input section
        Jt.markdown("## 📝 Context (Optional)").key("context-title").use();
        String context = Jt.textArea(
                "Add any context about this podcast episode to improve transcription quality: e.g., Topic, speakers names, technical terms...")
                .key("context-input").use();

        // Generate button
        boolean generateClicked = Jt.button("🚀 Generate Transcription").key("generate-button").use();

        // Process transcription when Generate button is clicked
        if (generateClicked && uploadedFiles != null && !uploadedFiles.isEmpty()) {
            var uploadedFile = uploadedFiles.get(0);
            String fileName = uploadedFile.filename().toLowerCase();

            if (fileName.endsWith(".mp3") || fileName.endsWith(".wav") || fileName.endsWith(".m4a")) {
                // Determine MIME type
                String mimeType;
                if (fileName.endsWith(".mp3")) {
                    mimeType = "audio/mpeg";
                } else if (fileName.endsWith(".wav")) {
                    mimeType = "audio/wav";
                } else {
                    mimeType = "audio/mp4"; // m4a
                }

                // Build prompt based on mode and context
                String prompt;
                if (readingMode) {
                    prompt = "Donne la transcription de cet épisode de podcast. " +
                            "La transcription est arrangée pour être lisible comme un livre.";
                } else {
                    prompt = "Donne la transcription de cet épisode de podcast. " +
                            "La transcription doit être équivalente au son.";
                }

                if (context != null && !context.trim().isEmpty()) {
                    prompt += " Context: " + context.trim();
                }

                // Process the audio file
                byte[] audioBytes = uploadedFile.content();
                Content audioMsg = Content.fromParts(
                        Part.fromBytes(audioBytes, mimeType),
                        Part.fromText(prompt));

                // Process transcription
                Flowable<Event> events = runner.runAsync(session.userId(), session.id(), audioMsg);

                StringBuilder transcription = new StringBuilder();
                events.blockingForEach(event -> {
                    String responseText = event.stringifyContent();
                    transcription.append(responseText);
                });

                // Add to history
                String userMessage = "🎙️ " + uploadedFile.filename();
                if (context != null && !context.trim().isEmpty()) {
                    userMessage += " (Context: " + context.trim() + ")";
                }
                chatHistory.add(new Message("user", userMessage));
                chatHistory.add(new Message("agent", transcription.toString()));

                // Display the completed transcription immediately
                Jt.markdown("---").key("separator-result").use();
                Jt.markdown("### ✅ Transcription Complete!").key("completed-title").use();
                Jt.markdown("**File:** " + uploadedFile.filename()).key("completed-filename").use();
                if (context != null && !context.trim().isEmpty()) {
                    Jt.markdown("**Context:** " + context.trim()).key("completed-context").use();
                }
                Jt.markdown("**Transcription:**").key("completed-label").use();
                Jt.markdown(transcription.toString()).key("completed-transcription").use();
                Jt.markdown("---").key("separator-result-").use();
            } else {
                Jt.markdown("❌ **Invalid file type.** Please upload an MP3, WAV, or M4A file.")
                        .key("error-invalid-type").use();
            }
        } else if (generateClicked) {
            Jt.markdown("⚠️ **Please upload an audio file first.**").key("error-no-file").use();
        }
    }
}
