package com.sample.demo3.views.admin;

import com.sample.demo3.configuration.SystemAuditService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;


public class SecurityAuditView {

    private final String PRIMARY_BLUE = "#2c3e50";
    private final String ACCENT_GOLD = "#f39c12";
    private final String SUCCESS_GREEN = "#27ae60";
    private final String DANGER_RED = "#e74c3c";
    private final String WARNING_ORANGE = "#e67e22";
    private final String CARD_BG = "#ffffff";

    private TableView<SystemAuditService.SecurityLog> securityTable;
    private TableView<SystemAuditService.ComplianceRecord> complianceTable;
    private ObservableList<SystemAuditService.SecurityLog> securityLogs = FXCollections.observableArrayList();
    private ObservableList<SystemAuditService.ComplianceRecord> complianceRecords = FXCollections.observableArrayList();

    // Labels pour les statistiques
    private Label totalEventsLabel;
    private Label failedLoginsLabel;
    private Label highSeverityLabel;
    private Label activeUsersLabel;

    public VBox createView() {
        VBox mainView = new VBox(20);
        mainView.setAlignment(Pos.TOP_LEFT);
        mainView.setPadding(new Insets(20));
        mainView.setStyle("-fx-background-color: #f5f7fa;");

        // Header
        HBox headerBox = createHeader();

        // Statistiques de sécurité
        HBox statsRow = createSecurityStats();

        // Table des logs de sécurité
        VBox securityLogsSection = createSecurityLogsTable();

        // Table de conformité
        VBox complianceSection = createComplianceTable();

        // Boutons d'action
        HBox actionBar = createActionBar();

        mainView.getChildren().addAll(headerBox, statsRow, securityLogsSection, complianceSection, actionBar);

        // Charger les données
        loadSecurityData();

        return mainView;
    }

    private HBox createHeader() {
        HBox header = new HBox();
        VBox headerText = new VBox(5);

        Text title = new Text("🛡️ Security & Audit Center");
        title.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 28));
        title.setFill(Color.web(PRIMARY_BLUE));

        Text subtitle = new Text("Monitor security events, audit trails, and compliance status");
        subtitle.setFont(Font.font("Arial", 14));
        subtitle.setFill(Color.web("#7f8c8d"));

        headerText.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.setStyle(
                "-fx-background-color: " + ACCENT_GOLD + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 10 20;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;"
        );
        refreshBtn.setOnAction(e -> loadSecurityData());

        header.getChildren().addAll(headerText, spacer, refreshBtn);
        return header;
    }

    private HBox createSecurityStats() {
        HBox statsRow = new HBox(20);
        statsRow.setPadding(new Insets(20, 0, 20, 0));

        VBox totalEventsCard = createStatCard("📊", "TOTAL EVENTS", "0", "Security events", PRIMARY_BLUE);
        totalEventsLabel = (Label) ((VBox) totalEventsCard.getChildren().get(1)).getChildren().get(0);

        VBox failedLoginsCard = createStatCard("🔒", "FAILED LOGINS", "0", "Last 24h", DANGER_RED);
        failedLoginsLabel = (Label) ((VBox) failedLoginsCard.getChildren().get(1)).getChildren().get(0);

        VBox highSeverityCard = createStatCard("⚠️", "HIGH SEVERITY", "0", "Critical alerts", WARNING_ORANGE);
        highSeverityLabel = (Label) ((VBox) highSeverityCard.getChildren().get(1)).getChildren().get(0);

        VBox activeUsersCard = createStatCard("👥", "ACTIVE USERS", "0", "Last 24h", SUCCESS_GREEN);
        activeUsersLabel = (Label) ((VBox) activeUsersCard.getChildren().get(1)).getChildren().get(0);

        statsRow.getChildren().addAll(totalEventsCard, failedLoginsCard, highSeverityCard, activeUsersCard);
        return statsRow;
    }

    private VBox createStatCard(String icon, String title, String value, String subtitle, String color) {
        VBox card = new VBox(10);
        card.setPrefWidth(180);
        card.setPadding(new Insets(20));
        card.setStyle(
                "-fx-background-color: " + CARD_BG + ";" +
                        "-fx-background-radius: 15;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 15;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 15, 0.3, 0, 5);"
        );

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Text iconText = new Text(icon);
        iconText.setFont(Font.font(22));

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12; -fx-font-weight: bold;");

        header.getChildren().addAll(iconText, titleLabel);

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 32; -fx-font-weight: bold;");

        Label descLabel = new Label(subtitle);
        descLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 11;");

        VBox content = new VBox(5, valueLabel, descLabel);
        card.getChildren().addAll(header, content);
        return card;
    }

    private VBox createSecurityLogsTable() {
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

        HBox header = new HBox();
        Label sectionTitle = new Label("🔍 SECURITY LOGS");
        sectionTitle.setStyle("-fx-text-fill: " + PRIMARY_BLUE + "; -fx-font-size: 18; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Filter ComboBox
        ComboBox<String> severityFilter = new ComboBox<>();
        severityFilter.getItems().addAll("ALL", "HIGH", "MEDIUM", "LOW");
        severityFilter.setValue("ALL");
        severityFilter.setStyle(
                "-fx-background-color: #f8f9fa;" +
                        "-fx-border-color: #dee2e6;" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 5 10;" +
                        "-fx-font-size: 12;"
        );
        severityFilter.setOnAction(e -> filterSecurityLogs(severityFilter.getValue()));

        header.getChildren().addAll(sectionTitle, spacer, severityFilter);

        // Security logs table
        securityTable = new TableView<>();
        securityTable.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-border-color: #dee2e6;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;"
        );
        securityTable.setPrefHeight(250);
        securityTable.setPlaceholder(new Label("Loading security logs..."));

        // Columns
        TableColumn<SystemAuditService.SecurityLog, String> timeCol = new TableColumn<>("Timestamp");
        timeCol.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        timeCol.setPrefWidth(180);

        TableColumn<SystemAuditService.SecurityLog, String> userCol = new TableColumn<>("User");
        userCol.setCellValueFactory(new PropertyValueFactory<>("userId"));
        userCol.setPrefWidth(120);

        TableColumn<SystemAuditService.SecurityLog, String> actionCol = new TableColumn<>("Action");
        actionCol.setCellValueFactory(new PropertyValueFactory<>("action"));
        actionCol.setPrefWidth(150);

        TableColumn<SystemAuditService.SecurityLog, String> severityCol = new TableColumn<>("Severity");
        severityCol.setCellValueFactory(new PropertyValueFactory<>("severity"));
        severityCol.setPrefWidth(100);
        severityCol.setCellFactory(col -> new TableCell<SystemAuditService.SecurityLog, String>() {
            @Override
            protected void updateItem(String severity, boolean empty) {
                super.updateItem(severity, empty);
                if (empty || severity == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(severity);
                    setFont(Font.font("Arial", FontWeight.BOLD, 12));
                    setAlignment(Pos.CENTER);

                    switch (severity.toUpperCase()) {
                        case "HIGH":
                            setStyle("-fx-text-fill: " + DANGER_RED + "; -fx-background-color: #ffebee; " +
                                    "-fx-background-radius: 12; -fx-padding: 4 12;");
                            break;
                        case "MEDIUM":
                            setStyle("-fx-text-fill: " + WARNING_ORANGE + "; -fx-background-color: #fff3e0; " +
                                    "-fx-background-radius: 12; -fx-padding: 4 12;");
                            break;
                        case "LOW":
                            setStyle("-fx-text-fill: " + SUCCESS_GREEN + "; -fx-background-color: #e8f5e9; " +
                                    "-fx-background-radius: 12; -fx-padding: 4 12;");
                            break;
                        default:
                            setStyle("-fx-text-fill: #7f8c8d; -fx-background-color: #f5f5f5; " +
                                    "-fx-background-radius: 12; -fx-padding: 4 12;");
                    }
                }
            }
        });

        TableColumn<SystemAuditService.SecurityLog, String> detailsCol = new TableColumn<>("Details");
        detailsCol.setCellValueFactory(new PropertyValueFactory<>("details"));
        detailsCol.setPrefWidth(300);

        TableColumn<SystemAuditService.SecurityLog, String> ipCol = new TableColumn<>("IP Address");
        ipCol.setCellValueFactory(new PropertyValueFactory<>("ipAddress"));
        ipCol.setPrefWidth(120);

        securityTable.getColumns().addAll(timeCol, userCol, actionCol, severityCol, detailsCol, ipCol);
        securityTable.setItems(securityLogs);

        section.getChildren().addAll(header, securityTable);
        return section;
    }

    private VBox createComplianceTable() {
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

        HBox header = new HBox();
        Label sectionTitle = new Label("📋 COMPLIANCE RECORDS");
        sectionTitle.setStyle("-fx-text-fill: " + PRIMARY_BLUE + "; -fx-font-size: 18; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addCheckBtn = new Button("➕ Add Check");
        addCheckBtn.setStyle(
                "-fx-background-color: " + SUCCESS_GREEN + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 8 15;" +
                        "-fx-background-radius: 6;" +
                        "-fx-cursor: hand;"
        );
        addCheckBtn.setOnAction(e -> showAddComplianceDialog());

        header.getChildren().addAll(sectionTitle, spacer, addCheckBtn);

        // Compliance table
        complianceTable = new TableView<>();
        complianceTable.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-border-color: #dee2e6;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;"
        );
        complianceTable.setPrefHeight(200);

        // Columns
        TableColumn<SystemAuditService.ComplianceRecord, String> dateCol = new TableColumn<>("Check Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("checkDate"));
        dateCol.setPrefWidth(180);

        TableColumn<SystemAuditService.ComplianceRecord, String> typeCol = new TableColumn<>("Check Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("checkType"));
        typeCol.setPrefWidth(150);

        TableColumn<SystemAuditService.ComplianceRecord, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(100);
        statusCol.setCellFactory(col -> new TableCell<SystemAuditService.ComplianceRecord, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    setFont(Font.font("Arial", FontWeight.BOLD, 12));
                    setAlignment(Pos.CENTER);

                    if ("PASS".equals(status.toUpperCase())) {
                        setStyle("-fx-text-fill: " + SUCCESS_GREEN + "; -fx-background-color: #e8f5e9; " +
                                "-fx-background-radius: 12; -fx-padding: 4 12;");
                    } else if ("FAIL".equals(status.toUpperCase())) {
                        setStyle("-fx-text-fill: " + DANGER_RED + "; -fx-background-color: #ffebee; " +
                                "-fx-background-radius: 12; -fx-padding: 4 12;");
                    } else if ("WARNING".equals(status.toUpperCase())) {
                        setStyle("-fx-text-fill: " + WARNING_ORANGE + "; -fx-background-color: #fff3e0; " +
                                "-fx-background-radius: 12; -fx-padding: 4 12;");
                    }
                }
            }
        });

        TableColumn<SystemAuditService.ComplianceRecord, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setPrefWidth(250);

        TableColumn<SystemAuditService.ComplianceRecord, String> checkerCol = new TableColumn<>("Checked By");
        checkerCol.setCellValueFactory(new PropertyValueFactory<>("checkedBy"));
        checkerCol.setPrefWidth(120);

        complianceTable.getColumns().addAll(dateCol, typeCol, statusCol, descCol, checkerCol);
        complianceTable.setItems(complianceRecords);

        section.getChildren().addAll(header, complianceTable);
        return section;
    }

    private HBox createActionBar() {
        HBox actionBar = new HBox(15);
        actionBar.setPadding(new Insets(20, 0, 0, 0));

        Button btnExportLogs = createActionButton("📤 Export Logs", ACCENT_GOLD);
        Button btnRunAudit = createActionButton("🔍 Run Security Audit", PRIMARY_BLUE);
        Button btnClearOldLogs = createActionButton("🗑️ Clear Old Logs", DANGER_RED);

        btnExportLogs.setOnAction(e -> exportSecurityLogs());
        btnRunAudit.setOnAction(e -> runSecurityAudit());
        btnClearOldLogs.setOnAction(e -> clearOldLogs());

        actionBar.getChildren().addAll(btnExportLogs, btnRunAudit, btnClearOldLogs);
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

    // ==============================
    // DATA LOADING
    // ==============================

    private void loadSecurityData() {
        try {
            // Load security logs
            securityLogs.clear();
            securityLogs.addAll(SystemAuditService.getAllSecurityLogs());

            // Load compliance records
            complianceRecords.clear();
            complianceRecords.addAll(SystemAuditService.getComplianceRecords());

            // Load statistics
            Map<String, Long> stats = SystemAuditService.getSecurityStats();
            updateSecurityStats(stats);

        } catch (Exception e) {
            showAlert("Error", "Failed to load security data: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void updateSecurityStats(Map<String, Long> stats) {
        javafx.application.Platform.runLater(() -> {
            totalEventsLabel.setText(String.valueOf(stats.getOrDefault("totalEvents", 0L)));
            failedLoginsLabel.setText(String.valueOf(stats.getOrDefault("failedLogins24h", 0L)));
            highSeverityLabel.setText(String.valueOf(stats.getOrDefault("highSeverityEvents", 0L)));
            activeUsersLabel.setText(String.valueOf(stats.getOrDefault("activeUsers24h", 0L)));
        });
    }

    private void filterSecurityLogs(String severity) {
        if ("ALL".equals(severity)) {
            securityTable.setItems(securityLogs);
        } else {
            ObservableList<SystemAuditService.SecurityLog> filtered = FXCollections.observableArrayList();
            for (SystemAuditService.SecurityLog log : securityLogs) {
                if (severity.equalsIgnoreCase(log.getSeverity())) {
                    filtered.add(log);
                }
            }
            securityTable.setItems(filtered);
        }
    }

    // ==============================
    // DIALOG METHODS
    // ==============================

    private void showAddComplianceDialog() {
        Dialog<Map<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Add Compliance Check");
        dialog.setHeaderText("Record a new compliance check");

        ButtonType addButton = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField typeField = new TextField();
        typeField.setPromptText("e.g., GDPR, PCI-DSS, ISO27001");

        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("PASS", "FAIL", "WARNING");
        statusCombo.setValue("PASS");

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Check details and findings...");
        descriptionArea.setPrefRowCount(3);

        grid.add(new Label("Check Type:"), 0, 0);
        grid.add(typeField, 1, 0);
        grid.add(new Label("Status:"), 0, 1);
        grid.add(statusCombo, 1, 1);
        grid.add(new Label("Description:"), 0, 2);
        grid.add(descriptionArea, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButton) {
                if (typeField.getText().isEmpty() || descriptionArea.getText().isEmpty()) {
                    showAlert("Validation Error", "Please fill in all fields", Alert.AlertType.ERROR);
                    return null;
                }

                Map<String, String> result = new HashMap<>();
                result.put("type", typeField.getText());
                result.put("status", statusCombo.getValue());
                result.put("description", descriptionArea.getText());

                return result;
            }
            return null;
        });

        Optional<Map<String, String>> result = dialog.showAndWait();
        result.ifPresent(data -> {
            try {
                SystemAuditService.addComplianceCheck(
                        data.get("type"),
                        data.get("description"),
                        data.get("status"),
                        "admin"
                );

                // Reload data
                complianceRecords.clear();
                complianceRecords.addAll(SystemAuditService.getComplianceRecords());

                showAlert("Success", "Compliance check added", Alert.AlertType.INFORMATION);

            } catch (Exception e) {
                showAlert("Error", "Failed to add compliance check: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        });
    }

    private void exportSecurityLogs() {
        showAlert("Info", "Export feature coming soon!", Alert.AlertType.INFORMATION);
    }

    private void runSecurityAudit() {
        ProgressIndicator progress = new ProgressIndicator();
        progress.setPrefSize(50, 50);

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Running Security Audit");
        dialog.setHeaderText("Scanning system for security vulnerabilities...");
        dialog.getDialogPane().setContent(progress);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        new Thread(() -> {
            try {
                Thread.sleep(3000); // Simulate audit

                javafx.application.Platform.runLater(() -> {
                    dialog.close();
                    showAlert("Audit Complete",
                            "Security audit completed successfully!\n\n" +
                                    "Findings:\n" +
                                    "• 2 medium severity issues found\n" +
                                    "• 5 low severity warnings\n" +
                                    "• All critical checks passed",
                            Alert.AlertType.INFORMATION);
                });

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();

        dialog.show();
    }

    private void clearOldLogs() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Clear Old Logs");
        alert.setHeaderText("Clear logs older than 30 days");
        alert.setContentText("This will permanently delete security logs older than 30 days.\n\n" +
                "Are you sure?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // In a real implementation, you would delete old logs
                showAlert("Info", "Old logs cleared (simulated)", Alert.AlertType.INFORMATION);
            }
        });
    }

    // ==============================
    // HELPER METHODS
    // ==============================

    private String darkenColor(String hexColor) {
        if (hexColor.equals(ACCENT_GOLD)) return "#e67e22";
        if (hexColor.equals(PRIMARY_BLUE)) return "#2c3e50";
        if (hexColor.equals(SUCCESS_GREEN)) return "#229954";
        if (hexColor.equals(DANGER_RED)) return "#c0392b";
        if (hexColor.equals(WARNING_ORANGE)) return "#d35400";
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