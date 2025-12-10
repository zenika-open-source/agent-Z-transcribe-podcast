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
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;

public class TranscribeApp {

    private final InMemoryRunner runner;

    private final Session session;

    public TranscribeApp(final InMemoryRunner runner, Session session) {
        this.runner = runner;
        this.session = session;
    }

    record Message(String sender, String text) {
    }

    record TranscriptionResult(String filename, String text, String context) {
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

        boolean includeTimestamps = Jt.toggle("Include some Timestamps (HH:MM:SS)")
                .value((Boolean) Jt.sessionState().getOrDefault("includeTimestamps", false))
                .key("toggle-timestamps")
                .use(cols.col(1));
        Jt.sessionState().put("includeTimestamps", includeTimestamps);

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
                    boolean copyClicked = Jt.button("📋 Copy to Clipboard").key("copy-button-" + index).use();
                    if (copyClicked) {
                        copyToClipboard(msg.text);
                    }
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

        // Display the persisted result if available and file hasn't changed
        TranscriptionResult storedResult = (TranscriptionResult) Jt.sessionState().get("savedTranscription");
        if (storedResult != null) {
            if (uploadedFiles == null || uploadedFiles.isEmpty()
                    || !uploadedFiles.getFirst().filename().equalsIgnoreCase(storedResult.filename())) {
                Jt.sessionState().remove("savedTranscription");
            } else {
                displayResult(storedResult);
            }
        }
    }

    private void displayResult(TranscriptionResult result) {
        Jt.markdown("---").key("separator-result").use();
        Jt.markdown("### ✅ Transcription Complete!").key("completed-title").use();
        Jt.markdown("**File:** " + result.filename()).key("completed-filename").use();
        if (result.context() != null && !result.context().trim().isEmpty()) {
            Jt.markdown("**Context:** " + result.context().trim()).key("completed-context").use();
        }
        Jt.markdown("**Transcription:**").key("completed-label").use();
        Jt.markdown(result.text()).key("completed-transcription").use();

        boolean copyClicked = Jt.button("📋 Copy to Clipboard").key("copy-button-completed").use();
        if (copyClicked) {
            copyToClipboard(result.text());
        }
        Jt.markdown("---").key("separator-result-").use();
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
        boolean currentIncludeTimestamps = (Boolean) Jt.sessionState().getOrDefault("includeTimestamps", false);
        String prompt = "";
        if (currentReadingMode) {
            prompt += "The transcription is arranged to be readable like a book.";
        } else {
            prompt += "The transcription must be equivalent to the audio.";
        }

        if (currentIncludeTimestamps) {
            prompt += " Add timestamps in [HH:MM:SS] format at the beginning of each section or topic change " +
                    "to easily locate passages in the audio.";
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

        // Save result to session state to persist across reruns
        TranscriptionResult result = new TranscriptionResult(uploadedFile.filename(), transcription.toString(),
                context);
        Jt.sessionState().put("savedTranscription", result);

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

    private void copyToClipboard(String text) {
        if (text == null || text.isEmpty()) {
            return;
        }
        try {
            StringSelection selection = new StringSelection(text);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
        } catch (Exception e) {
            System.err.println("Failed to copy to clipboard: " + e.getMessage());
        }
    }

}
