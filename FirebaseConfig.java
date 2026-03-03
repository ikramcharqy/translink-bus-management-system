package com.sample.demo3.configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import java.io.InputStream;

public class FirebaseConfig {

    private static boolean isInitialized = false;

    public static void init() throws Exception {
        if (isInitialized) {
            System.out.println(" Firebase déjà initialisé");
            return;
        }

        if (!FirebaseApp.getApps().isEmpty()) {
            System.out.println(" Firebase Apps déjà initialisés");
            isInitialized = true;
            return;
        }

        System.out.println(" Chargement du fichier firebaseAccountKey.json...");

        InputStream serviceAccount =
                FirebaseConfig.class.getClassLoader().getResourceAsStream("firebaseAccountKey.json");

        if (serviceAccount == null) {
            System.err.println(" ERREUR: firebaseAccountKey.json introuvable");
            System.err.println(" Vérifiez que le fichier est dans: src/main/resources/com/sample/demo3/");
            throw new Exception("firebaseAccountKey.json introuvable");
        }

        System.out.println(" Fichier firebaseAccountKey.json trouvé");

        try {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://translink-e065b.firebaseio.com") // ← VOTRE URL
                    .build();

            System.out.println(" Initialisation de FirebaseApp...");
            FirebaseApp.initializeApp(options);
            isInitialized = true;
            System.out.println(" Firebase connecté avec succès !");

        } catch (Exception e) {
            System.err.println(" ERREUR lors de l'initialisation Firebase: " + e.getMessage());
            throw e;
        } finally {
            if (serviceAccount != null) {
                serviceAccount.close();
            }
        }
    }

    public static boolean isInitialized() {
        return isInitialized;
    }
}