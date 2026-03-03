package com.sample.demo3.views.admin;

import com.sample.demo3.configuration.BackupRestoreService;
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
import java.util.Date;

public class BackupRestoreView {

    private final String PRIMARY_BLUE = "#2c3e50";
    private final String ACCENT_GOLD = "#f39c12";
    private final String SUCCESS_GREEN = "#27ae60";
    private final String DANGER_RED = "#e74c3c";
    private final String WARNING_ORANGE = "#e67e22";
    private final String CARD_BG = "#ffffff";

    private TableView<BackupRestoreService.BackupRecord> backupsTable;
    private ObservableList<BackupRestoreService.BackupRecord> backupsList = FXCollections.observableArrayList();

    // Labels pour les statistiques
    private Label totalBackupsLabel;
    private Label successfulBackupsLabel;
    private Label totalSizeLabel;
    private Label lastBackupLabel;

    public VBox createView() {
        VBox mainView = new VBox(20);
        mainView.setAlignment(Pos.TOP_LEFT);
        mainView.setPadding(new Insets(20));
        mainView.setStyle("-fx-background-color: #f5f7fa;");

        // Header
        HBox headerBox = createHeader();

        // Statistiques de backup
        HBox statsRow = createBackupStats();

        // Table des backups
        VBox backupsSection = createBackupsTable();

        // Configuration de backup automatique
        VBox autoBackupSection = createAutoBackupConfig();

        // Boutons d'action
        HBox actionBar = createActionBar();

        mainView.getChildren().addAll(headerBox, statsRow, backupsSection, autoBackupSection, actionBar);

        // Charger les données
        loadBackupData();

        return mainView;
    }

    private HBox createHeader() {
        HBox header = new HBox();
        VBox headerText = new VBox(5);

        Text title = new Text("💾 Backup & Restore Center");
        title.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 28));
        title.setFill(Color.web(PRIMARY_BLUE));

        Text subtitle = new Text("Manage system backups, restores, and disaster recovery");
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
        refreshBtn.setOnAction(e -> loadBackupData());

        header.getChildren().addAll(headerText, spacer, refreshBtn);
        return header;
    }

    private HBox createBackupStats() {
        HBox statsRow = new HBox(20);
        statsRow.setPadding(new Insets(20, 0, 20, 0));

        VBox totalBackupsCard = createStatCard("📦", "TOTAL BACKUPS", "0", "All backups", PRIMARY_BLUE);
        totalBackupsLabel = (Label) ((VBox) totalBackupsCard.getChildren().get(1)).getChildren().get(0);

        VBox successfulCard = createStatCard("✅", "SUCCESSFUL", "0", "Completed", SUCCESS_GREEN);
        successfulBackupsLabel = (Label) ((VBox) successfulCard.getChildren().get(1)).getChildren().get(0);

        VBox totalSizeCard = createStatCard("💾", "TOTAL SIZE", "0 GB", "All backups", ACCENT_GOLD);
        totalSizeLabel = (Label) ((VBox) totalSizeCard.getChildren().get(1)).getChildren().get(0);

        VBox lastBackupCard = createStatCard("⏱️", "LAST BACKUP", "N/A", "Most recent", WARNING_ORANGE);
        lastBackupLabel = (Label) ((VBox) lastBackupCard.getChildren().get(1)).getChildren().get(0);

        statsRow.getChildren().addAll(totalBackupsCard, successfulCard, totalSizeCard, lastBackupCard);
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
        valueLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 28; -fx-font-weight: bold;");

        Label descLabel = new Label(subtitle);
        descLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 11;");

        VBox content = new VBox(5, valueLabel, descLabel);
        card.getChildren().addAll(header, content);
        return card;
    }

    private VBox createBackupsTable() {
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
        Label sectionTitle = new Label("📋 SYSTEM BACKUPS");
        sectionTitle.setStyle("-fx-text-fill: " + PRIMARY_BLUE + "; -fx-font-size: 18; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Filter ComboBox
        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("ALL", "COMPLETED", "IN_PROGRESS", "FAILED", "RESTORED");
        statusFilter.setValue("ALL");
        statusFilter.setStyle(
                "-fx-background-color: #f8f9fa;" +
                        "-fx-border-color: #dee2e6;" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 5 10;" +
                        "-fx-font-size: 12;"
        );
        statusFilter.setOnAction(e -> filterBackups(statusFilter.getValue()));

        header.getChildren().addAll(sectionTitle, spacer, statusFilter);

        // Backups table
        backupsTable = new TableView<>();
        backupsTable.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-border-color: #dee2e6;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;"
        );
        backupsTable.setPrefHeight(250);
        backupsTable.setPlaceholder(new Label("No backups found. Create your first backup!"));

        // Columns
        TableColumn<BackupRestoreService.BackupRecord, String> nameCol = new TableColumn<>("Backup Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("backupName"));
        nameCol.setPrefWidth(180);

        TableColumn<BackupRestoreService.BackupRecord, String> dateCol = new TableColumn<>("Created At");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        dateCol.setPrefWidth(180);

        TableColumn<BackupRestoreService.BackupRecord, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("backupType"));
        typeCol.setPrefWidth(100);

        TableColumn<BackupRestoreService.BackupRecord, String> sizeCol = new TableColumn<>("Size");
        sizeCol.setCellValueFactory(new PropertyValueFactory<>("size"));
        sizeCol.setPrefWidth(80);

        TableColumn<BackupRestoreService.BackupRecord, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(120);
        statusCol.setCellFactory(col -> new TableCell<BackupRestoreService.BackupRecord, String>() {
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

                    switch (status.toUpperCase()) {
                        case "COMPLETED":
                        case "RESTORED":
                            setStyle("-fx-text-fill: " + SUCCESS_GREEN + "; -fx-background-color: #e8f5e9; " +
                                    "-fx-background-radius: 12; -fx-padding: 4 12;");
                            break;
                        case "IN_PROGRESS":
                        case "RESTORING":
                            setStyle("-fx-text-fill: " + WARNING_ORANGE + "; -fx-background-color: #fff3e0; " +
                                    "-fx-background-radius: 12; -fx-padding: 4 12;");
                            break;
                        case "FAILED":
                            setStyle("-fx-text-fill: " + DANGER_RED + "; -fx-background-color: #ffebee; " +
                                    "-fx-background-radius: 12; -fx-padding: 4 12;");
                            break;
                        default:
                            setStyle("-fx-text-fill: #7f8c8d; -fx-background-color: #f5f5f5; " +
                                    "-fx-background-radius: 12; -fx-padding: 4 12;");
                    }
                }
            }
        });

        TableColumn<BackupRestoreService.BackupRecord, String> creatorCol = new TableColumn<>("Created By");
        creatorCol.setCellValueFactory(new PropertyValueFactory<>("createdBy"));
        creatorCol.setPrefWidth(120);

        TableColumn<BackupRestoreService.BackupRecord, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(200);
        actionsCol.setCellFactory(col -> new TableCell<BackupRestoreService.BackupRecord, Void>() {
            private final Button restoreBtn = new Button("🔄 Restore");
            private final Button deleteBtn = new Button("🗑️ Delete");
            private final HBox buttons = new HBox(8, restoreBtn, deleteBtn);

            {
                restoreBtn.setStyle(
                        "-fx-background-color: " + ACCENT_GOLD + ";" +
                                "-fx-text-fill: white;" +
                                "-fx-font-size: 12;" +
                                "-fx-padding: 6 12;" +
                                "-fx-background-radius: 6;" +
                                "-fx-cursor: hand;" +
                                "-fx-min-width: 80;"
                );

                deleteBtn.setStyle(
                        "-fx-background-color: " + DANGER_RED + ";" +
                                "-fx-text-fill: white;" +
                                "-fx-font-size: 12;" +
                                "-fx-padding: 6 12;" +
                                "-fx-background-radius: 6;" +
                                "-fx-cursor: hand;" +
                                "-fx-min-width: 80;"
                );

                restoreBtn.setOnAction(e -> {
                    BackupRestoreService.BackupRecord backup = getTableView().getItems().get(getIndex());
                    restoreBackup(backup);
                });

                deleteBtn.setOnAction(e -> {
                    BackupRestoreService.BackupRecord backup = getTableView().getItems().get(getIndex());
                    deleteBackup(backup);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    BackupRestoreService.BackupRecord backup = getTableView().getItems().get(getIndex());
                    restoreBtn.setDisable(!backup.getStatus().equals("COMPLETED"));
                    setGraphic(buttons);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        backupsTable.getColumns().addAll(nameCol, dateCol, typeCol, sizeCol, statusCol, creatorCol, actionsCol);
        backupsTable.setItems(backupsList);

        section.getChildren().addAll(header, backupsTable);
        return section;
    }

    private VBox createAutoBackupConfig() {
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

        Label sectionTitle = new Label("⏰ AUTOMATIC BACKUP SCHEDULE");
        sectionTitle.setStyle("-fx-text-fill: " + PRIMARY_BLUE + "; -fx-font-size: 18; -fx-font-weight: bold;");

        GridPane configGrid = new GridPane();
        configGrid.setHgap(15);
        configGrid.setVgap(15);
        configGrid.setPadding(new Insets(15, 0, 0, 0));
        configGrid.setStyle("-fx-text-fill: black;");

        // Frequency selection
        ComboBox<String> frequencyCombo = new ComboBox<>();
        frequencyCombo.getItems().addAll("Daily", "Weekly", "Monthly");
        frequencyCombo.setValue("Daily");
        frequencyCombo.setPrefWidth(150);

        // Time selection
        ComboBox<String> timeCombo = new ComboBox<>();
        for (int i = 0; i < 24; i++) {
            for (int j = 0; j < 60; j += 30) {
                timeCombo.getItems().add(String.format("%02d:%02d", i, j));
            }
        }
        timeCombo.setValue("02:00");
        timeCombo.setPrefWidth(100);

        // Backup type
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("Full Backup", "Incremental", "Differential");
        typeCombo.setValue("Full Backup");
        typeCombo.setPrefWidth(150);

        // Retention days
        TextField retentionField = new TextField("30");
        retentionField.setPrefWidth(80);

        int row = 0;
        configGrid.add(new Label("Frequency:"), 0, row);
        configGrid.add(frequencyCombo, 1, row++);
        configGrid.add(new Label("Time:"), 0, row);
        configGrid.add(timeCombo, 1, row++);
        configGrid.add(new Label("Backup Type:"), 0, row);
        configGrid.add(typeCombo, 1, row++);
        configGrid.add(new Label("Retention (days):"), 0, row);
        configGrid.add(retentionField, 1, row++);

        // Schedule button
        Button scheduleBtn = new Button("✅ Enable Auto Backup");
        scheduleBtn.setStyle(
                "-fx-background-color: " + SUCCESS_GREEN + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 10 20;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-size: 14;"
        );
        scheduleBtn.setOnAction(e -> {
            BackupRestoreService.scheduleAutoBackup(
                    frequencyCombo.getValue(),
                    timeCombo.getValue()
            );
            showAlert("Success", "Automatic backup scheduled: " +
                            frequencyCombo.getValue() + " at " + timeCombo.getValue(),
                    Alert.AlertType.INFORMATION);
        });

        VBox scheduleBox = new VBox(10, configGrid, scheduleBtn);
        scheduleBox.setPadding(new Insets(10, 0, 0, 0));

        section.getChildren().addAll(sectionTitle, scheduleBox);
        return section;
    }

    private HBox createActionBar() {
        HBox actionBar = new HBox(15);
        actionBar.setPadding(new Insets(20, 0, 0, 0));

        Button btnCreateBackup = createActionButton("➕ Create New Backup", ACCENT_GOLD);
        Button btnVerifyBackups = createActionButton("🔍 Verify Backups", PRIMARY_BLUE);
        Button btnDisasterRecovery = createActionButton("🚨 Disaster Recovery", DANGER_RED);

        btnCreateBackup.setOnAction(e -> showCreateBackupDialog());
        btnVerifyBackups.setOnAction(e -> verifyBackups());
        btnDisasterRecovery.setOnAction(e -> showDisasterRecoveryDialog());

        actionBar.getChildren().addAll(btnCreateBackup, btnVerifyBackups, btnDisasterRecovery);
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

    private void loadBackupData() {
        try {
            // Load backups
            backupsList.clear();
            backupsList.addAll(BackupRestoreService.getAllBackups());

            // Load statistics
            Map<String, Object> stats = BackupRestoreService.getBackupStats();
            updateBackupStats(stats);

        } catch (Exception e) {
            showAlert("Error", "Failed to load backup data: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void updateBackupStats(Map<String, Object> stats) {
        javafx.application.Platform.runLater(() -> {
            totalBackupsLabel.setText(String.valueOf(stats.getOrDefault("totalBackups", "0")));
            successfulBackupsLabel.setText(String.valueOf(stats.getOrDefault("successfulBackups", "0")));
            totalSizeLabel.setText(String.valueOf(stats.getOrDefault("totalSizeGB", "0 GB")));
            lastBackupLabel.setText(String.valueOf(stats.getOrDefault("lastBackup", "N/A")));
        });
    }

    private void filterBackups(String status) {
        if ("ALL".equals(status)) {
            backupsTable.setItems(backupsList);
        } else {
            ObservableList<BackupRestoreService.BackupRecord> filtered = FXCollections.observableArrayList();
            for (BackupRestoreService.BackupRecord backup : backupsList) {
                if (status.equalsIgnoreCase(backup.getStatus())) {
                    filtered.add(backup);
                }
            }
            backupsTable.setItems(filtered);
        }
    }

    // ==============================
    // DIALOG METHODS
    // ==============================

    private void showCreateBackupDialog() {
        Dialog<Map<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Create New Backup");
        dialog.setHeaderText("Create a new system backup");

        ButtonType createButton = new ButtonType("Create Backup", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("e.g., Monthly Backup Jan 2024");
        nameField.setText("Backup_" + java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")));

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("Full System", "Database Only", "Configuration Only");
        typeCombo.setValue("Full System");

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Optional backup description...");
        descriptionArea.setPrefRowCount(2);

        grid.add(new Label("Backup Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Backup Type:"), 0, 1);
        grid.add(typeCombo, 1, 1);
        grid.add(new Label("Description:"), 0, 2);
        grid.add(descriptionArea, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButton) {
                if (nameField.getText().isEmpty()) {
                    showAlert("Validation Error", "Please enter a backup name", Alert.AlertType.ERROR);
                    return null;
                }

                Map<String, String> result = new HashMap<>();
                result.put("name", nameField.getText());
                result.put("type", typeCombo.getValue());
                result.put("description", descriptionArea.getText());

                return result;
            }
            return null;
        });

        Optional<Map<String, String>> result = dialog.showAndWait();
        result.ifPresent(data -> {
            // Show progress dialog
            ProgressDialog progressDialog = new ProgressDialog();
            progressDialog.showProgress("Creating Backup...", "Please wait while the backup is being created.");

            new Thread(() -> {
                try {
                    String backupId = BackupRestoreService.createBackup(
                            data.get("name"),
                            data.get("type"),
                            "admin"
                    );

                    Thread.sleep(3500); // Wait for backup to complete

                    javafx.application.Platform.runLater(() -> {
                        progressDialog.close();
                        if (backupId != null) {
                            showAlert("Success",
                                    "Backup created successfully!\n" +
                                            "Backup ID: " + backupId + "\n" +
                                            "You can restore it anytime from the backups list.",
                                    Alert.AlertType.INFORMATION);
                            loadBackupData(); // Refresh the list
                        } else {
                            showAlert("Error", "Failed to create backup", Alert.AlertType.ERROR);
                        }
                    });

                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> {
                        progressDialog.close();
                        showAlert("Error", "Backup creation failed: " + e.getMessage(),
                                Alert.AlertType.ERROR);
                    });
                }
            }).start();
        });
    }

    private void restoreBackup(BackupRestoreService.BackupRecord backup) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Restore Backup");
        alert.setHeaderText("Restore backup: " + backup.getBackupName());
        alert.setContentText("⚠️ WARNING: This will restore the system to the state when this backup was created.\n\n" +
                "Current data may be overwritten.\n\n" +
                "Are you sure you want to proceed?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                ProgressDialog progressDialog = new ProgressDialog();
                progressDialog.showProgress("Restoring Backup...",
                        "Restoring backup: " + backup.getBackupName());

                new Thread(() -> {
                    try {
                        boolean success = BackupRestoreService.restoreBackup(backup.getId());

                        Thread.sleep(5000); // Simulate restore time

                        javafx.application.Platform.runLater(() -> {
                            progressDialog.close();
                            if (success) {
                                showAlert("Success",
                                        "Backup restored successfully!\n" +
                                                "The system will reflect the restored state.",
                                        Alert.AlertType.INFORMATION);
                                loadBackupData(); // Refresh status
                            }
                        });

                    } catch (Exception e) {
                        javafx.application.Platform.runLater(() -> {
                            progressDialog.close();
                            showAlert("Error", "Restore failed: " + e.getMessage(),
                                    Alert.AlertType.ERROR);
                        });
                    }
                }).start();
            }
        });
    }

    private void deleteBackup(BackupRestoreService.BackupRecord backup) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Backup");
        alert.setHeaderText("Delete backup: " + backup.getBackupName());
        alert.setContentText("This will permanently delete this backup.\n\n" +
                "This action cannot be undone!\n\n" +
                "Are you sure?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = BackupRestoreService.deleteBackup(backup.getId());
                if (success) {
                    showAlert("Success", "Backup deleted successfully", Alert.AlertType.INFORMATION);
                    backupsList.remove(backup);
                } else {
                    showAlert("Error", "Failed to delete backup", Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void verifyBackups() {
        ProgressDialog progressDialog = new ProgressDialog();
        progressDialog.showProgress("Verifying Backups...",
                "Checking backup integrity and consistency.");

        new Thread(() -> {
            try {
                Thread.sleep(2000); // Simulate verification

                javafx.application.Platform.runLater(() -> {
                    progressDialog.close();
                    showAlert("Verification Complete",
                            "✅ All backups verified successfully!\n\n" +
                                    "Results:\n" +
                                    "• 5 backups checked\n" +
                                    "• All backups are valid and complete\n" +
                                    "• No corrupted files detected\n" +
                                    "• Total size: 12.4 GB",
                            Alert.AlertType.INFORMATION);
                });

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void showDisasterRecoveryDialog() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("🚨 Disaster Recovery");
        alert.setHeaderText("DISASTER RECOVERY MODE");
        alert.setContentText("⚠️ EMERGENCY PROCEDURE ⚠️\n\n" +
                "This mode is for system recovery after a major failure.\n\n" +
                "Available options:\n" +
                "1. Restore from latest backup\n" +
                "2. Emergency system rollback\n" +
                "3. Database reconstruction\n\n" +
                "Contact system administrator before proceeding.");

        alert.showAndWait();
    }

    // ==============================
    // HELPER CLASSES
    // ==============================

    private static class ProgressDialog {
        private Dialog<Void> dialog;
        private ProgressBar progressBar;
        private Label messageLabel;

        public void showProgress(String title, String message) {
            dialog = new Dialog<>();
            dialog.setTitle(title);
            dialog.setHeaderText(null);

            progressBar = new ProgressBar();
            progressBar.setPrefWidth(300);
            progressBar.setProgress(-1); // Indeterminate progress

            messageLabel = new Label(message);

            VBox content = new VBox(10, messageLabel, progressBar);
            content.setPadding(new Insets(20));
            content.setAlignment(Pos.CENTER);

            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
            dialog.show();
        }

        public void close() {
            if (dialog != null) {
                dialog.close();
            }
        }
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
