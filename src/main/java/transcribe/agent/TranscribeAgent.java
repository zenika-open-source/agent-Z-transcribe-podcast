package transcribe.agent;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;

import java.util.logging.Logger;

public class TranscribeAgent {

    private static final Logger logger = Logger.getLogger(TranscribeAgent.class.getName());

    public static BaseAgent build() {

        logger.info("🤖 Initializing agent Z Transcribe Podcast initialized");

        return LlmAgent.builder()
                .name("agent-z-transcribe-podcast")
                .description("Transcribe Podcast")
                .model("gemini-3-pro-preview")
                .instruction("""
                        Zenikast is the new podcast of Zenika, in French, so transcription must to be in French.
                        Episodes talk about development, agility, devops, Cloud infrastructure
                        Before giving a response, integrate these rules:
                        - Remove language tics like "uh", "Euh", "du coup",
                        - Remove word repetitions,
                        - Clean up the text to get a transcription readable as a book.
                        Identify the person speaking and start the paragraph with his name, for example:
                            **Martin Jean**
                            <text>
                        """)
                .build();
    }
}