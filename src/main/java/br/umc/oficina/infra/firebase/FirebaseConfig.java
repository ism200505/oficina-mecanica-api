package br.umc.oficina.infra.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Conexão com o Firebase (Cloud Firestore - banco NoSQL orientado a documentos).
 * As credenciais vêm de arquivo local OU da variável de ambiente FIREBASE_CREDENTIALS_JSON
 * (usada no deploy), nunca versionadas no GitHub.
 */
@Configuration
@Profile("!memoria")
public class FirebaseConfig {

    @Bean
    public Firestore firestore(@Value("${firebase.credenciais}") String caminhoCredenciais,
                               @Value("${firebase.credenciais-json:}") String credenciaisJson,
                               @Value("${firebase.project-id}") String projectId) throws IOException {
        try (InputStream credenciais = abrirCredenciais(caminhoCredenciais, credenciaisJson)) {
            FirebaseOptions opcoes = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(credenciais))
                    .setProjectId(projectId)
                    .build();
            FirebaseApp app = FirebaseApp.getApps().isEmpty()
                    ? FirebaseApp.initializeApp(opcoes)
                    : FirebaseApp.getInstance();
            return FirestoreClient.getFirestore(app);
        }
    }

    private InputStream abrirCredenciais(String caminho, String json) throws IOException {
        if (json != null && !json.isBlank()) {
            return new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        }
        return new FileInputStream(caminho);
    }
}
