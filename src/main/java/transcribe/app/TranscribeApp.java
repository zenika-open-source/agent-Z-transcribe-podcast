package transcribe.app;

import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import io.javelit.core.Jt;
import io.javelit.core.JtUploadedFile;

import io.reactivex.rxjava3.core.Flowable;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;

public class TranscribeApp {

    private final InMemoryRunner runner;

    private final Session session;

    public TranscribeApp(final InMemoryRunner runner, Session session) {
        this.runner = runner;
        this.session = session;
    }

    record Message(String sender, String text) {
    }

    public void run() {
        Jt.markdown("# 🎙️ Agent Z Transcribe Podcast").key("title").use();
        Jt.markdown("---").key("separator-1").use();

        // Get session state for this user
        @SuppressWarnings("unchecked")
        List<Message> chatHistory = (List<Message>) Jt.sessionState()
                .computeIfAbsent("chatHistory", k -> new ArrayList<Message>());

        // Two-column layout
        var cols = Jt.columns(2).key("main-columns").use();

        // Left column - Upload and Context section
        Jt.markdown("## 🎶 Upload Audio File").key("upload-title").use(cols.col(0));
        var uploadedFiles = Jt.fileUploader("Select an audio file (mp3, wav, m4a)").key("file-uploader")
                .use(cols.col(0));

        Jt.markdown("## 📝 Context (Optional)").key("context-title").use(cols.col(0));
        String context = Jt.textArea(
                "Add any context about this podcast episode to improve transcription quality: e.g., Topic, speakers names, technical terms...")
                .key("context-input").use(cols.col(0));

        boolean generateClicked = Jt.button("🚀 Generate Transcription").key("generate-button").use(cols.col(0));

        // Right column - Options section
        Jt.markdown("## ⚙️ Options").key("options-title").use(cols.col(1));

        boolean showHistory = Jt.toggle("Show Transcription History")
                .value((Boolean) Jt.sessionState().getOrDefault("showHistory", false))
                .key("toggle-show-history")
                .use(cols.col(1));
        Jt.sessionState().put("showHistory", showHistory);

        boolean readingMode = Jt.toggle("Reading Mode (vs Strict Transcription)")
                .value((Boolean) Jt.sessionState().getOrDefault("readingMode", true))
                .key("toggle-reading-mode")
                .use(cols.col(1));
        Jt.sessionState().put("readingMode", readingMode);

        Jt.markdown("---").key("separator-2").use();

        // Display chat history
        if (!chatHistory.isEmpty() && showHistory) {
            Jt.markdown("## 📝 Transcription History").key("history-title").use();
            boolean resetClicked = Jt.button("🗑️ Reset History").key("reset-button").use();
            if (resetClicked) {
                chatHistory.clear();
                Jt.sessionState().put("chatHistory", new ArrayList<Message>());
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

        // Process transcription when Generate button is clicked
        if (generateClicked && uploadedFiles != null && !uploadedFiles.isEmpty()) {
            transcribe(uploadedFiles, chatHistory, context);
        } else if (generateClicked) {
            Jt.markdown("⚠️ **Please upload an audio file first.**").key("error-no-file").use();
        }
    }

    private void transcribe(final List<JtUploadedFile> uploadedFiles, final List<Message> chatHistory,
            final String context) {
        var uploadedFile = uploadedFiles.getFirst();
        String fileName = uploadedFile.filename().toLowerCase();

        if (!isFileExtensionValid(fileName)) {
            Jt.markdown("❌ **Invalid file type.** Please upload an MP3, WAV, or M4A file.")
                    .key("error-invalid-type").use();
            return;
        }

        var mimeType = getMimeType(fileName);

        // Build prompt based on mode and context
        boolean currentReadingMode = (Boolean) Jt.sessionState().getOrDefault("readingMode", true);
        String prompt;
        if (currentReadingMode) {
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

    }

    private static @NotNull String getMimeType(String fileName) {
        String mimeType;
        if (fileName.endsWith(".mp3")) {
            mimeType = "audio/mpeg";
        } else if (fileName.endsWith(".wav")) {
            mimeType = "audio/wav";
        } else {
            mimeType = "audio/mp4"; // m4a
        }
        return mimeType;
    }

    private boolean isFileExtensionValid(final String filename) {
        return filename.endsWith(".mp3") || filename.endsWith(".wav") || filename.endsWith(".m4a");
    }

}
