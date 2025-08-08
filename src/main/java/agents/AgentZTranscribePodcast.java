package agents;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.adk.tools.Annotations.Schema;
import com.google.adk.tools.BaseTool;
import com.google.adk.tools.FunctionTool;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import io.reactivex.rxjava3.core.Flowable;
import org.springframework.stereotype.Component;
import services.GoogleDocService;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Component
public class AgentZTranscribePodcast {

    private static final Logger logger = Logger.getLogger(AgentZTranscribePodcast.class.getName());

    private static final String APP_NAME = "agent-z-transcribe-podcast";

    private static final String DEFAULT_INPUT = "Donne moi la transcription de cet épisode";

    public static BaseAgent ROOT_AGENT = initAgent();

    public static BaseAgent initAgent() {

        var createDocTranscription = FunctionTool.create(AgentZTranscribePodcast.class, "createTranscriptionGoogleDoc");
        List<BaseTool> allTools = new ArrayList<>();
        allTools.add(createDocTranscription);
        logger.info("🌈 Tools: " + allTools.toString());

        return LlmAgent.builder()
                .name("agent-z-transcribe-podcast")
                .description("Transcribe Podcast")
                .model("gemini-2.5-pro")
                .instruction("""
                Zenikast is the new podcast of Zenika, in French, so transcription must to be in French.
                Episodes talk about development, agility, devops, Cloud infrastructure
                Before giving a response, integrate these rules:
                - Remove language tics like "uh", "Euh", "du coup",
                - Remove word repetitions,
                - Clean up the text to get a transcription readable as a book.
                Identify the person speaking and start the paragraph with his name, for example:
                    Martin Jean
                    <text>
                """)
                .tools(allTools)
                .build();
    }

    @Schema(description = "Create a Google Doc to save the transcription.")
    public static void createTranscriptionGoogleDoc(@Schema(description = "The transcription", name = "transcription") String transcription) {
        logger.info("🌈 Creating transcription Google Doc...");

        try {
            GoogleDocService googleDocService = new GoogleDocService();
            googleDocService.createDoc(transcription, "12PdIadqBxkPdW3rxgLO-8lVCk8JbIPIk");
        } catch (GeneralSecurityException | IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static void main(String[] args) {
        InMemoryRunner runner = new InMemoryRunner(ROOT_AGENT, APP_NAME);

        Session session = runner
                .sessionService()
                .createSession(APP_NAME, "agent-transcribe")
                .blockingGet();

        Content userMsg = Content.fromParts(Part.fromText(DEFAULT_INPUT));
        Flowable<Event> events =
                runner.runAsync(session.userId(), session.id(), userMsg);

        logger.info("🌈 Agent > ");
        events.blockingForEach(event -> System.out.println(event.stringifyContent()));
    }
}