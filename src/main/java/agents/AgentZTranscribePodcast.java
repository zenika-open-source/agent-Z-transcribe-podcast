package agents;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import io.reactivex.rxjava3.core.Flowable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.stereotype.Component;
import java.util.logging.Logger;

@Component
public class AgentZTranscribePodcast {

    private static final String APP_NAME = "agent-z-transcribe-podcast";
    private static final Logger logger = Logger.getLogger(AgentZTranscribePodcast.class.getName());

    private static final String DEFAULT_INPUT = "Donne moi la transcription de cet épisode";

    private static final Log log = LogFactory.getLog(AgentZTranscribePodcast.class);

    public static BaseAgent ROOT_AGENT = initAgent();

    public static BaseAgent initAgent() {

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

    // Create a method to transcribe an audio file
    public static void transcribeAudio() {
        InMemoryRunner runner = new InMemoryRunner(ROOT_AGENT, APP_NAME);

        Session session = runner
                .sessionService()
                .createSession(APP_NAME, "agent-transcribe")
                .blockingGet();

        Content userMsg = Content.fromParts(Part.fromText(DEFAULT_INPUT));
        Flowable<Event> events = runner.runAsync(session.userId(), session.id(), userMsg);

        System.out.print("🤖 Agent > ");
        events.blockingForEach(event -> System.out.println(event.stringifyContent()));

    }
}