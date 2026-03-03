package com.sample.demo3;

import com.sample.demo3.configuration.FirebaseConfig;
import com.google.cloud.firestore.DocumentSnapshot;
import com.sample.demo3.configuration.FirebaseService;
import com.sample.demo3.views.AdminDashboard;
import com.sample.demo3.views.ManagerDashboard;
import com.sample.demo3.views.DriverDashboard;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.application.Platform;
import java.net.URL;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private ImageView backgroundImageView;
    @FXML private ImageView logoImageView;

    @FXML
    public void initialize() {
        try {
            URL backgroundUrl = getClass().getResource("/com/sample/demo3/Slice_1.png");
            URL logoUrl = getClass().getResource("/com/sample/demo3/logo.png");

            if (backgroundUrl != null) {
                backgroundImageView.setImage(new Image(backgroundUrl.toString()));
            }
            if (logoUrl != null) {
                logoImageView.setImage(new Image(logoUrl.toString()));
            }

            // Mettre à jour l'errorLabel avec le mode on ligne
            errorLabel.setText("Mode on ligne - Serveur accessible");
            errorLabel.setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Erreur de chargement des images");
            errorLabel.setVisible(true);
        }
    }

    private void showForgotPasswordDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Forgot Password");
        dialog.setHeaderText("Reset your password");

        ButtonType resetButton = new ButtonType("Reset Password", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(resetButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField dialogUsernameField = new TextField();
        dialogUsernameField.setPromptText("Enter your username");

        TextField emailField = new TextField();
        emailField.setPromptText("Enter your email");

        grid.add(new Label("Username:"), 0, 0);
        grid.add(dialogUsernameField, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == resetButton) {
                return dialogUsernameField.getText();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(username -> {
            try {
                // Vérifier si l'utilisateur existe
                DocumentSnapshot userDoc = FirebaseService.findUser(username);
                if (userDoc != null) {
                    String userEmail = userDoc.getString("email");
                    if (userEmail != null && userEmail.equalsIgnoreCase(emailField.getText())) {
                        // Générer un nouveau mot de passe temporaire
                        String tempPassword = generateTempPassword();

                        // Mettre à jour dans Firebase
                        FirebaseService.updateUserPassword(username, tempPassword);

                        // Envoyer un email
                        showSuccessAlert("Password Reset",
                                "A temporary password has been sent to your email.\n" +
                                        "Temporary password: " + tempPassword + "\n" +
                                        "Please change it after login.");
                    } else {
                        showErrorAlert("Email Mismatch", "Email does not match our records.");
                    }
                } else {
                    showErrorAlert("User Not Found", "Username not found.");
                }
            } catch (Exception e) {
                showErrorAlert("Error", "Failed to reset password: " + e.getMessage());
            }
        });
    }

    private String generateTempPassword() {
        // Générer un mot de passe temporaire aléatoire
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            int index = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }

    private void showSuccessAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText(null);
        a.setTitle(title);
        a.setContentText(msg);
        a.show();
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        System.out.println("=== TENTATIVE DE CONNEXION ===");
        System.out.println("Username:" + username);

        // Mettre à jour l'errorLabel
        errorLabel.setText("Connexion en cours...");
        errorLabel.setStyle("-fx-text-fill: blue;");

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Veuillez remplir les deux champs.");
            errorLabel.setStyle("-fx-text-fill: red;");
            showAlert("Erreur", "Veuillez remplir les deux champs.");
            return;
        }

        try {
            System.out.println("1. Initialisation Firebase...");
            FirebaseConfig.init();
            System.out.println("✅ Firebase initialisé");

            System.out.println("2. Recherche utilisateur: " + username);
            DocumentSnapshot userDoc = FirebaseService.findUser(username);

            if (userDoc == null) {
                System.out.println("❌ Utilisateur non trouvé");
                errorLabel.setText("Utilisateur introuvable.");
                errorLabel.setStyle("-fx-text-fill: red;");
                showAlert("Erreur", "Utilisateur introuvable.");
                return;
            }

            System.out.println("✅ Document trouvé: " + userDoc.getId());
            errorLabel.setText("Utilisateur trouvé, vérification du mot de passe...");
            errorLabel.setStyle("-fx-text-fill: blue;");

            // DEBUG: Afficher tous les champs
            System.out.println("=== DONNÉES UTILISATEUR ===");
            userDoc.getData().forEach((key, value) -> {
                System.out.println("  " + key + " = " + value);
            });

            String realPassword = userDoc.getString("password");
            if (realPassword == null) {
                System.out.println("❌ Champ 'password' non trouvé");
                errorLabel.setText("Configuration utilisateur incorrecte.");
                errorLabel.setStyle("-fx-text-fill: red;");
                showAlert("Erreur", "Configuration utilisateur incorrecte.");
                return;
            }

            System.out.println("Password attendu:" + realPassword);
            System.out.println("Password saisi:" + password);

            if (!password.equals(realPassword)) {
                System.out.println("❌ Mot de passe incorrect");
                errorLabel.setText("Mot de passe incorrect.");
                errorLabel.setStyle("-fx-text-fill: red;");
                showAlert("Erreur", "Mot de passe incorrect.");
                return;
            }

            String role = userDoc.getString("role");
            if (role != null) {
                role = role.trim();  // ← CORRECTION IMPORTANTE
                System.out.println("✅ Rôle détecté (après trim): " + role);
            } else {
                role = "driver";
                System.out.println("⚠️ Rôle non défini, utilisation par défaut: driver");
            }

            // Retirer les espaces des noms de champs
            String fullName = userDoc.getString("fullname");
            if (fullName != null) {
                fullName = fullName.trim();
            }
            if (fullName == null || fullName.isEmpty()) {
                fullName = userDoc.getString("username");
                if (fullName != null) {
                    fullName = fullName.trim();
                }
            }

            // Succès de connexion
            errorLabel.setText("Connexion réussie ! Redirection en cours...");
            errorLabel.setStyle("-fx-text-fill: green;");

            if ("manager".equalsIgnoreCase(role)) {
                System.out.println("🎯 Lancement ManagerDashboard...");
                launchManagerDashboard(username, fullName);
            } else if ("driver".equalsIgnoreCase(role)) {
                System.out.println("🎯 Lancement DriverDashboard...");
                String driverId = userDoc.getString("driverId");
                if (driverId == null || driverId.trim().isEmpty()) {
                    driverId = "D" + username.toUpperCase();
                }
                launchDriverDashboard(username, fullName, driverId);
            } else if ("admin".equalsIgnoreCase(role)) {
                System.out.println("🎯 Lancement AdminDashboard...");
                launchAdminDashboard(username, fullName);
            } else {
                System.out.println("⚠️ Rôle inconnu: '" + role + "'");
                errorLabel.setText("Interface pour rôle '" + role + "' à venir.");
                errorLabel.setStyle("-fx-text-fill: orange;");
                showAlert("Info", "Interface pour rôle '" + role + "' à venir.");
            }

        } catch (Exception e) {
            System.err.println("💥 ERREUR FATALE:");
            e.printStackTrace();
            errorLabel.setText("Connexion impossible: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: red;");
            showAlert("Erreur", "Connexion impossible: " + e.getMessage());
        }
    }

    @FXML
    private void handleForgotPassword() {
        showForgotPasswordDialog();
    }

    private void launchAdminDashboard(String username, String fullName) {
        Stage loginStage = (Stage) usernameField.getScene().getWindow();
        loginStage.close();

        Platform.runLater(() -> {
            try {
                Stage adminStage = new Stage();
                AdminDashboard adminApp = new AdminDashboard();
                adminApp.start(adminStage);
                System.out.println("✅ AdminDashboard lancé avec succès");
                adminApp.setAdminInfo(username, fullName);
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Erreur", "Impossible de lancer l'interface Admin.");
            }
        });
    }


    private void launchManagerDashboard(String username, String fullName) {
        Stage loginStage = (Stage) usernameField.getScene().getWindow();
        loginStage.close();

        Platform.runLater(() -> {
            try {
                Stage managerStage = new Stage();
                ManagerDashboard managerApp = new ManagerDashboard();

                // Passer les informations SANS ESPACES
                managerApp.setManagerInfo(username, fullName);

                managerApp.start(managerStage);
                System.out.println("✅ ManagerDashboard lancé avec succès");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Erreur", "Impossible de lancer l'interface Manager.");
            }
        });
    }

    private void launchDriverDashboard(String username, String fullName, String driverId) {
        Stage loginStage = (Stage) usernameField.getScene().getWindow();
        loginStage.close();

        Platform.runLater(() -> {
            try {
                Stage driverStage = new Stage();
                DriverDashboard driverApp = new DriverDashboard();

                // Passer les informations SANS ESPACES
                driverApp.setDriverInfo(username, fullName, driverId);

                driverApp.start(driverStage);
                System.out.println("✅ DriverDashboard lancé avec succès");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Erreur", "Impossible de lancer l'interface Driver.");
            }
        });

    }
}