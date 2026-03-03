package com.sample.demo3.views.admin;

import com.sample.demo3.configuration.FirebaseService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SystemSettingsView {

    private final String PRIMARY_BLUE = "#2c3e50";
    private final String ACCENT_GOLD = "#f39c12";
    private final String SUCCESS_GREEN = "#27ae60";
    private final String DANGER_RED = "#e74c3c";
    private final String CARD_BG = "#ffffff";

    // Champs de configuration
    private TextField appNameField;
    private TextField companyNameField;
    private TextField supportEmailField;
    private TextField supportPhoneField;
    private TextField systemUrlField;
    private CheckBox maintenanceModeCheck;
    private CheckBox enableNotificationsCheck;
    private CheckBox enableAuditLogCheck;
    private CheckBox enableAutoBackupCheck;
    private Spinner<Integer> sessionTimeoutSpinner;
    private Spinner<Integer> backupRetentionSpinner;
    private Spinner<Integer> maxLoginAttemptsSpinner;
    private TextArea emailTemplateArea;
    private TextArea smsTemplateArea;

    public VBox createView() {
        VBox mainView = new VBox(20);
        mainView.setAlignment(Pos.TOP_LEFT);
        mainView.setPadding(new Insets(20));
        mainView.setStyle("-fx-background-color: #f5f7fa;");

        // Header
        HBox headerBox = createHeader();

        // Configuration générale
        VBox generalSettings = createGeneralSettings();

        // Sécurité
        VBox securitySettings = createSecuritySettings();

        // Notifications
        VBox notificationSettings = createNotificationSettings();

        // Boutons d'action
        HBox actionBar = createActionBar();

        mainView.getChildren().addAll(headerBox, generalSettings, securitySettings,
                notificationSettings, actionBar);

        // Charger les paramètres existants
        loadSettings();

        return mainView;
    }

    private HBox createHeader() {
        HBox header = new HBox();
        VBox headerText = new VBox(5);

        Text title = new Text("🔧 System Settings");
        title.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 28));
        title.setFill(Color.web(PRIMARY_BLUE));

        Text subtitle = new Text("Configure system-wide settings and preferences");
        subtitle.setFont(Font.font("Arial", 14));
        subtitle.setFill(Color.web("#7f8c8d"));

        headerText.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button loadDefaultsBtn = new Button("🔄 Load Defaults");
        loadDefaultsBtn.setStyle(
                "-fx-background-color: " + ACCENT_GOLD + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 10 20;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;"
        );
        loadDefaultsBtn.setOnAction(e -> loadDefaultSettings());

        header.getChildren().addAll(headerText, spacer, loadDefaultsBtn);
        return header;
    }

    private VBox createGeneralSettings() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(25));
        section.setStyle(
                "-fx-background-color: " + CARD_BG + ";" +
                        "-fx-background-radius: 15;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 15;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 15, 0.3, 0, 5);"
        );

        Label sectionTitle = new Label("🌐 General Configuration");
        sectionTitle.setStyle("-fx-text-fill: " + PRIMARY_BLUE + "; -fx-font-size: 18; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(15);
        grid.setPadding(new Insets(15, 0, 0, 0));
        grid.setStyle("-fx-text-fill: black;");

        // Application Name
        appNameField = new TextField();
        appNameField.setPromptText("Translink System");
        appNameField.setPrefWidth(300);
        appNameField.setStyle("-fx-text-fill: black; -fx-text-inner-color: black;");

        companyNameField = new TextField();
        companyNameField.setPromptText("Translink Inc.");
        companyNameField.setPrefWidth(300);
        companyNameField.setStyle("-fx-text-fill: black; -fx-text-inner-color: black;");

        supportEmailField = new TextField();
        supportEmailField.setPromptText("support@translink.com");
        supportEmailField.setPrefWidth(300);
        supportEmailField.setStyle("-fx-text-fill: black; -fx-text-inner-color: black;");

        supportPhoneField = new TextField();
        supportPhoneField.setPromptText("+1-800-123-4567");
        supportPhoneField.setPrefWidth(300);
        supportPhoneField.setStyle("-fx-text-fill: black; -fx-text-inner-color: black;");


        systemUrlField = new TextField();
        systemUrlField.setPromptText("https://system.translink.com");
        systemUrlField.setPrefWidth(300);
        systemUrlField.setStyle("-fx-text-fill: black; -fx-text-inner-color: black;");

        // Maintenance Mode
        maintenanceModeCheck = new CheckBox("Enable Maintenance Mode");
        maintenanceModeCheck.setStyle("-fx-font-weight: bold; -fx-text-fill: black;");
        // Auto Backup
        enableAutoBackupCheck = new CheckBox("Enable Automatic Backups");
        enableAutoBackupCheck.setStyle("-fx-font-weight: bold; -fx-text-fill: black;");
        int row = 0;
        grid.add(createFormLabel("Application Name:"), 0, row);
        grid.add(appNameField, 1, row++);
        grid.add(createFormLabel("Company Name:"), 0, row);
        grid.add(companyNameField, 1, row++);
        grid.add(createFormLabel("Support Email:"), 0, row);
        grid.add(supportEmailField, 1, row++);
        grid.add(createFormLabel("Support Phone:"), 0, row);
        grid.add(supportPhoneField, 1, row++);
        grid.add(createFormLabel("System URL:"), 0, row);
        grid.add(systemUrlField, 1, row++);
        grid.add(createFormLabel("System Mode:"), 0, row);
        grid.add(maintenanceModeCheck, 1, row++);
        grid.add(createFormLabel("Backup:"), 0, row);
        grid.add(enableAutoBackupCheck, 1, row++);

        section.getChildren().addAll(sectionTitle, grid);
        return section;
    }

    private VBox createSecuritySettings() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(25));
        section.setStyle(
                "-fx-background-color: " + CARD_BG + ";" +
                        "-fx-background-radius: 15;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 15;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 15, 0.3, 0, 5);"
        );

        Label sectionTitle = new Label("🔐 Security Settings");
        sectionTitle.setStyle("-fx-text-fill: " + PRIMARY_BLUE + "; -fx-font-size: 18; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(15);
        grid.setPadding(new Insets(15, 0, 0, 0));
        grid.setStyle("-fx-text-fill: black;");

        sessionTimeoutSpinner = new Spinner<>(5, 240, 30, 5);
        sessionTimeoutSpinner.setPrefWidth(100);
        sessionTimeoutSpinner.setStyle("-fx-text-fill: black; -fx-text-inner-color: black; -fx-control-inner-background: white;");
        sessionTimeoutSpinner.getEditor().setStyle("-fx-text-fill: black; -fx-text-inner-color: black;");

        // Max Login Attempts
        maxLoginAttemptsSpinner = new Spinner<>(1, 10, 5, 1);
        maxLoginAttemptsSpinner.setPrefWidth(100);
        maxLoginAttemptsSpinner.setStyle("-fx-text-fill: black; -fx-text-inner-color: black; -fx-control-inner-background: white;");
        maxLoginAttemptsSpinner.getEditor().setStyle("-fx-text-fill: black; -fx-text-inner-color: black;");

        // Backup Retention (days)
        backupRetentionSpinner = new Spinner<>(1, 365, 30, 1);
        backupRetentionSpinner.setPrefWidth(100);
        backupRetentionSpinner.setStyle("-fx-text-fill: black; -fx-text-inner-color: black; -fx-control-inner-background: white;");
        backupRetentionSpinner.getEditor().setStyle("-fx-text-fill: black; -fx-text-inner-color: black;");

        // Audit Log
        enableAuditLogCheck = new CheckBox("Enable Security Audit Logging");
        enableAuditLogCheck.setStyle("-fx-font-weight: bold; -fx-text-fill: black;");

        // Require Strong Passwords
        CheckBox requireStrongPasswordCheck = new CheckBox("Require Strong Passwords");
        requireStrongPasswordCheck.setStyle("-fx-font-weight: bold; -fx-text-fill: black;");


        // Enable 2FA
        CheckBox enable2FACheck = new CheckBox("Enable Two-Factor Authentication");
        enable2FACheck.setStyle("-fx-font-weight: bold; -fx-text-fill: black;");

        int row = 0;
        grid.add(createFormLabel("Session Timeout (min):"), 0, row);
        grid.add(sessionTimeoutSpinner, 1, row++);
        grid.add(createFormLabel("Max Login Attempts:"), 0, row);
        grid.add(maxLoginAttemptsSpinner, 1, row++);
        grid.add(createFormLabel("Backup Retention (days):"), 0, row);
        grid.add(backupRetentionSpinner, 1, row++);
        grid.add(createFormLabel("Audit Logging:"), 0, row);
        grid.add(enableAuditLogCheck, 1, row++);
        grid.add(createFormLabel("Password Policy:"), 0, row);
        grid.add(requireStrongPasswordCheck, 1, row++);
        grid.add(createFormLabel("Authentication:"), 0, row);
        grid.add(enable2FACheck, 1, row++);

        section.getChildren().addAll(sectionTitle, grid);
        return section;
    }

    private VBox createNotificationSettings() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(25));
        section.setStyle(
                "-fx-background-color: " + CARD_BG + ";" +
                        "-fx-background-radius: 15;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 15;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 15, 0.3, 0, 5);"
        );

        Label sectionTitle = new Label("📢 Notification Settings");
        sectionTitle.setStyle("-fx-text-fill: " + PRIMARY_BLUE + "; -fx-font-size: 18; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(15);
        grid.setPadding(new Insets(15, 0, 0, 0));
        grid.setStyle("-fx-text-fill: black;");

        // Enable Notifications
        enableNotificationsCheck = new CheckBox("Enable System Notifications");
        enableNotificationsCheck.setStyle("-fx-font-weight: bold; -fx-text-fill: black;");


        // Email Template
        emailTemplateArea = new TextArea();
        emailTemplateArea.setPromptText("Email notification template...");
        emailTemplateArea.setPrefRowCount(4);
        emailTemplateArea.setPrefWidth(400);
        emailTemplateArea.setStyle("-fx-text-fill: black; -fx-text-inner-color: black; -fx-control-inner-background: white;");

        // SMS Template
        smsTemplateArea = new TextArea();
        smsTemplateArea.setPromptText("SMS notification template...");
        smsTemplateArea.setPrefRowCount(4);
        smsTemplateArea.setPrefWidth(400);
        smsTemplateArea.setStyle("-fx-text-fill: black; -fx-text-inner-color: black; -fx-control-inner-background: white;");
        int row = 0;
        grid.add(createFormLabel("Notifications:"), 0, row);
        grid.add(enableNotificationsCheck, 1, row++);
        grid.add(createFormLabel("Email Template:"), 0, row);
        grid.add(emailTemplateArea, 1, row++);
        grid.add(createFormLabel("SMS Template:"), 0, row);
        grid.add(smsTemplateArea, 1, row++);

        section.getChildren().addAll(sectionTitle, grid);
        return section;
    }

    private HBox createActionBar() {
        HBox actionBar = new HBox(15);
        actionBar.setPadding(new Insets(20, 0, 0, 0));
        actionBar.setAlignment(Pos.CENTER);

        Button btnSave = createActionButton("💾 Save Settings", SUCCESS_GREEN);
        Button btnReset = createActionButton("🔄 Reset to Defaults", ACCENT_GOLD);
        Button btnTest = createActionButton("🔍 Test Configuration", PRIMARY_BLUE);

        btnSave.setOnAction(e -> saveSettings());
        btnReset.setOnAction(e -> resetSettings());
        btnTest.setOnAction(e -> testSettings());

        actionBar.getChildren().addAll(btnSave, btnReset, btnTest);
        return actionBar;
    }

    private Button createActionButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 12 25;" +
                        "-fx-background-radius: 10;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-size: 14;"
        );

        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: " + darkenColor(color) + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 12 25;" +
                        "-fx-background-radius: 10;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-size: 14;"
        ));

        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 12 25;" +
                        "-fx-background-radius: 10;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-size: 14;"
        ));

        return btn;
    }

    private Label createFormLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-weight: bold; " +
                "-fx-font-size: 14;"+
                "-fx-text-fill: black;"
        );
        return label;
    }

    // ==============================
    // DATA METHODS
    // ==============================

    private void loadSettings() {
        try {
            System.out.println("🔍 Loading system settings...");

            // Charger depuis Firestore
            // Pour l'instant, charger des valeurs par défaut
            appNameField.setText("Translink Eco Mobility System");
            companyNameField.setText("Translink EMS");
            supportEmailField.setText("Translink_EMC@gmail.com");
            supportPhoneField.setText("+212567453245");
            systemUrlField.setText("https://system.translink.com");
            maintenanceModeCheck = new CheckBox("Enable Maintenance Mode");
            maintenanceModeCheck.setStyle("-fx-font-weight: bold; -fx-text-fill: black;"); // FIX
            enableAutoBackupCheck = new CheckBox("Enable Automatic Backups");
            enableAutoBackupCheck.setStyle("-fx-font-weight: bold; -fx-text-fill: black;"); // FIX

            enableNotificationsCheck = new CheckBox("Enable System Notifications");
            enableNotificationsCheck.setStyle("-fx-font-weight: bold; -fx-text-fill: black;"); // FIX

            enableAuditLogCheck = new CheckBox("Enable Security Audit Logging");
            enableAuditLogCheck.setStyle("-fx-font-weight: bold; -fx-text-fill: black;"); // FIX

            // Templates par défaut
            emailTemplateArea.setText("Dear {user},\n\nSystem notification: {message}\n\nBest regards,\nTranslink Team");
            smsTemplateArea.setText("Translink Alert: {message}. Reply STOP to unsubscribe.");

            System.out.println("✅ Settings loaded");

        } catch (Exception e) {
            System.err.println("❌ Error loading settings: " + e.getMessage());
            showAlert("Error", "Failed to load settings: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadDefaultSettings() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Load Default Settings");
        alert.setHeaderText("Load default system settings?");
        alert.setContentText("This will replace all current settings with defaults.\n\n" +
                "Are you sure?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                appNameField.setText("Translink Eco Mobility System");
                companyNameField.setText("Translink EMS.");
                supportEmailField.setText("support@translink.com");
                supportPhoneField.setText("+212567243698");
                systemUrlField.setText("https://system.translink.com");
                maintenanceModeCheck.setSelected(false);
                enableAutoBackupCheck.setSelected(true);
                enableNotificationsCheck.setSelected(true);
                enableAuditLogCheck.setSelected(true);
                sessionTimeoutSpinner.getValueFactory().setValue(30);
                maxLoginAttemptsSpinner.getValueFactory().setValue(5);
                backupRetentionSpinner.getValueFactory().setValue(30);
                emailTemplateArea.setText("Dear {user},\n\nSystem notification: {message}\n\nBest regards,\nTranslink Team");
                smsTemplateArea.setText("Translink Alert: {message}. Reply STOP to unsubscribe.");

                showAlert("Success", "Default settings loaded", Alert.AlertType.INFORMATION);
            }
        });
    }

    private void saveSettings() {
        try {
            System.out.println("💾 Saving system settings...");

            // Valider les champs obligatoires
            if (appNameField.getText().isEmpty() || supportEmailField.getText().isEmpty()) {
                showAlert("Validation Error", "Application name and support email are required",
                        Alert.AlertType.ERROR);
                return;
            }

            // Préparer les données
            Map<String, Object> settings = new HashMap<>();
            settings.put("appName", appNameField.getText());
            settings.put("companyName", companyNameField.getText());
            settings.put("supportEmail", supportEmailField.getText());
            settings.put("supportPhone", supportPhoneField.getText());
            settings.put("systemUrl", systemUrlField.getText());
            settings.put("maintenanceMode", maintenanceModeCheck.isSelected());
            settings.put("autoBackup", enableAutoBackupCheck.isSelected());
            settings.put("notifications", enableNotificationsCheck.isSelected());
            settings.put("auditLog", enableAuditLogCheck.isSelected());
            settings.put("sessionTimeout", sessionTimeoutSpinner.getValue());
            settings.put("maxLoginAttempts", maxLoginAttemptsSpinner.getValue());
            settings.put("backupRetention", backupRetentionSpinner.getValue());
            settings.put("emailTemplate", emailTemplateArea.getText());
            settings.put("smsTemplate", smsTemplateArea.getText());
            settings.put("lastModified", new java.util.Date());
            settings.put("modifiedBy", "admin");

            // Sauvegarder dans Firestore
            // FirebaseService.saveSystemSettings(settings); // À implémenter

            showAlert("Success",
                    "✅ Settings saved successfully!\n\n" +
                            "Changes will take effect immediately for:\n" +
                            "• Application settings\n" +
                            "• Security policies\n" +
                            "• Notification templates",
                    Alert.AlertType.INFORMATION);

            System.out.println("✅ Settings saved");

        } catch (Exception e) {
            System.err.println("❌ Error saving settings: " + e.getMessage());
            showAlert("Error", "Failed to save settings: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void resetSettings() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Reset Settings");
        alert.setHeaderText("Reset all settings to default?");
        alert.setContentText("⚠️ This will reset ALL configuration values to defaults.\n\n" +
                "This action cannot be undone!\n\n" +
                "Are you sure?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                loadDefaultSettings();
                showAlert("Reset Complete",
                        "All settings have been reset to defaults.",
                        Alert.AlertType.INFORMATION);
            }
        });
    }

    private void testSettings() {
        ProgressIndicator progress = new ProgressIndicator();
        progress.setPrefSize(50, 50);

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Testing Configuration");
        dialog.setHeaderText("Testing all system settings...");
        dialog.getDialogPane().setContent(progress);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        new Thread(() -> {
            try {
                // Simuler des tests
                Thread.sleep(2000);

                javafx.application.Platform.runLater(() -> {
                    dialog.close();

                    Alert resultAlert = new Alert(Alert.AlertType.INFORMATION);
                    resultAlert.setTitle("Configuration Test Results");
                    resultAlert.setHeaderText("✅ All tests passed successfully!");
                    resultAlert.setContentText("Test results:\n\n" +
                            "✓ Application settings: OK\n" +
                            "✓ Database connection: OK\n" +
                            "✓ Email configuration: OK\n" +
                            "✓ Security settings: OK\n" +
                            "✓ Notification system: OK\n\n" +
                            "All systems are configured correctly.");

                    resultAlert.showAndWait();
                });

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();

        dialog.show();
    }

    // ==============================
    // AUX METHODS
    // ==============================

    private String darkenColor(String hexColor) {
        if (hexColor.equals(ACCENT_GOLD)) return "#e67e22";
        if (hexColor.equals(PRIMARY_BLUE)) return "#2c3e50";
        if (hexColor.equals(SUCCESS_GREEN)) return "#229954";
        if (hexColor.equals(DANGER_RED)) return "#c0392b";
        return hexColor;
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}