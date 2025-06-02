package agents;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import io.reactivex.rxjava3.core.Flowable;

import java.util.Scanner;

import static java.nio.charset.StandardCharsets.UTF_8;

public class AgentZTranscribePodcast {

    public static BaseAgent ROOT_AGENT = initAgent();

    public static BaseAgent initAgent() {
        return LlmAgent.builder()
                .name("agent-z-transcribe-podcast")
                .description("Transcribe Podcast")
                .model("gemini-2.5-flash-preview-05-20")
                //.model("gemini-2.0-flash")
                .instruction("""
                Zenikast is the new podcast of Zenika, in French, so transcription must to be in French.
                Episodes talk about development, agility, devops, Cloud infrastructure
                Before giving a response, integrate these rules:
                - Remove language tics like "uh", "Euh", "du coup",
                - Remove word repetitions,
                - Clean up the text to get a transcription readable as a book.
                """)
                .build();
    }

    public static void main(String[] args) {
        InMemoryRunner runner = new InMemoryRunner(ROOT_AGENT);

        Session session = runner
                .sessionService()
                .createSession(runner.appName(), "episode")
                .blockingGet();

        try (Scanner scanner = new Scanner(System.in, UTF_8)) {
            while (true) {
                System.out.print("\nYou > ");
                String userInput = scanner.nextLine();
                if ("quit".equalsIgnoreCase(userInput)) {
                    break;
                }

                Content userMsg = Content.fromParts(Part.fromText(userInput));
                Flowable<Event> events =
                        runner.runAsync(session.userId(), session.id(), userMsg);

                System.out.print("\nAgent > ");
                events.blockingForEach(event -> {
                    System.out.println(event.stringifyContent());
                });
            }
        }
    }
}