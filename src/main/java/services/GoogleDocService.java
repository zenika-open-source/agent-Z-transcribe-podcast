package services;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.docs.v1.Docs;
import com.google.api.services.docs.v1.DocsScopes;
import com.google.api.services.docs.v1.model.*;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import com.google.protobuf.ByteString;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.logging.Logger;

@Service
public class GoogleDocService {

    private static final Logger logger = Logger.getLogger(GoogleDocService.class.getName());

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private static final List<String> SCOPES = List.of(DocsScopes.DOCUMENTS, DriveScopes.DRIVE_FILE);
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    private static Drive driveService;
    private static Docs docsService;
    private static final String SECRET_VERSION_ID = "projects/zenikast/secrets/zenikast-transcription-agent-secret/versions/latest";


    public GoogleDocService() throws GeneralSecurityException, IOException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        Credential credentials = getCredentialsFromSecretManager(HTTP_TRANSPORT);

        driveService = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credentials)
                .setApplicationName("agent-z-transcribe-podcast")
                .build();

        docsService = new Docs.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName("agent-z-transcribe-podcast")
                .build();
    }

    private static Credential getCredentialsFromSecretManager(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
            SecretVersionName secretVersionName = SecretVersionName.parse(SECRET_VERSION_ID);

            ByteString payload = client.accessSecretVersion(secretVersionName).getPayload().getData();

            GoogleClientSecrets secrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(payload.newInput()));

            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    HTTP_TRANSPORT, JSON_FACTORY, secrets, SCOPES)
                    .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                    .setAccessType("offline")
                    .build();
            LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
            Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");

            return credential;
        }
    }

    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
            throws IOException {

        String base64Json = System.getenv("GOOGLE_CLOUD_CREDENTIAL");
        String clientSecretsJson = new String(Base64.getDecoder().decode(base64Json));

        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new StringReader(clientSecretsJson));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("jean-philippe.baconnais@zenika.com");

        return credential;
    }

    public static void createDoc(String text, String folderId) throws IOException {
        logger.info("🌈 Create doc in this folder: " + folderId);

        List<Request> requests = new ArrayList<>();
        requests.add(new Request().setInsertText(new InsertTextRequest()
                .setText(text)
                .setLocation(new Location().setIndex(1))));
        BatchUpdateDocumentRequest body = new BatchUpdateDocumentRequest().setRequests(requests);

        Document doc = new Document().setTitle("Zenikast-Transcription " + LocalDateTime.now().toString());
        doc = docsService.documents().create(doc).execute();

        driveService.files().update(doc.getDocumentId(), null).setAddParents(folderId).execute();

        logger.info("✅ Created document with title: " + doc.getTitle());

    }
}
