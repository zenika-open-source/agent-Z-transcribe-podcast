import com.google.adk.runner.InMemoryRunner;
import io.javelit.core.Server;
import transcribe.agent.TranscribeAgent;
import transcribe.app.TranscribeApp;

private static final String APP_NAME = "agent-z-transcribe-podcast";

void main(String[] args) {
    // Initialize the agent runner
    var runner = new InMemoryRunner(TranscribeAgent.build(), APP_NAME);
    var session = runner
            .sessionService()
            .createSession(APP_NAME, "agent-transcribe")
            .blockingGet();

    // Read port from environment variable (for Cloud Run) or default to 8888
    int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8888"));

    var app = new TranscribeApp(runner, session);

    // Start Javelit server
    Server.builder(app::run, port).build().start();
}