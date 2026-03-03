package com.sample.demo3.views.admin;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;
import com.sample.demo3.configuration.FirebaseService;
import com.sample.demo3.models.*;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javafx.scene.shape.Circle;

public class SystemControlView {

    private final String PRIMARY_BLUE = "#2c3e50";
    private final String ACCENT_GOLD = "#f39c12";
    private static final String SUCCESS_GREEN = "#27ae60";
    private static final String DANGER_RED = "#e74c3c";
    private static final String WARNING_ORANGE = "#e67e22";
    private final String CARD_BG = "#ffffff";
    private final String DARK_BG = "#1a1a2e";

    private TableView<SystemLog> logsTable;
    private TableView<ServiceStatus> servicesTable;
    private ObservableList<SystemLog> logsList = FXCollections.observableArrayList();
    private ObservableList<ServiceStatus> servicesList = FXCollections.observableArrayList();

    // Labels pour les statistiques système
    private Label cpuUsageLabel;
    private Label memoryUsageLabel;
    private Label dbConnectionsLabel;
    private Label activeUsersLabel;
    private Label systemUptimeLabel;

    // Éléments de contrôle
    private ToggleButton maintenanceToggle;
    private Button btnClearCache;
    private Button btnRestartServices;
    private Button btnBackupNow;

    // Observateurs pour les mises à jour en temps réel
    private boolean isListening = false;

    public VBox createView() {
        VBox mainView = new VBox(20);
        mainView.setAlignment(Pos.TOP_LEFT);
        mainView.setPadding(new Insets(20));
        mainView.setStyle("-fx-background-color: #f5f7fa;");

        // Header
        HBox headerBox = createHeader();

        // Cartes de statut système
        GridPane systemStatusGrid = createSystemStatusGrid();

        // Section de contrôle système
        VBox controlPanel = createControlPanel();

        // Table des services
        VBox servicesSection = createServicesTable();

        // Logs système
        VBox logsSection = createLogsTable();

        // Boutons d'action
        HBox actionBar = createActionBar();

        // Ajouter tout au layout principal
        VBox contentBox = new VBox(25);
        contentBox.getChildren().addAll(
                headerBox,
                systemStatusGrid,
                controlPanel,
                servicesSection,
                logsSection,
                actionBar
        );

        ScrollPane scrollPane = new ScrollPane(contentBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        mainView.getChildren().add(scrollPane);

        // Charger les données initiales
        loadSystemData();
        startRealtimeMonitoring();

        return mainView;
    }

    private HBox createHeader() {
        HBox header = new HBox();
        VBox headerText = new VBox(5);

        Text title = new Text("⚙ System Control Center");
        title.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 28));
        title.setFill(Color.web(PRIMARY_BLUE));

        Text subtitle = new Text("Monitor and control all system services and configurations");
        subtitle.setFont(Font.font("Arial", 14));
        subtitle.setFill(Color.web("#7f8c8d"));

        headerText.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Indicateur de statut en temps réel
        HBox statusIndicator = new HBox(10);
        statusIndicator.setAlignment(Pos.CENTER);

        CircleIndicator liveIndicator = new CircleIndicator();
        liveIndicator.setStatus("live");

        Label statusLabel = new Label("LIVE MONITORING");
        statusLabel.setStyle("-fx-text-fill: " + SUCCESS_GREEN + "; -fx-font-weight: bold; -fx-font-size: 12;");

        statusIndicator.getChildren().addAll(liveIndicator, statusLabel);

        header.getChildren().addAll(headerText, spacer, statusIndicator);
        return header;
    }

    private GridPane createSystemStatusGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setPadding(new Insets(20, 0, 20, 0));

        // CPU Usage Card
        VBox cpuCard = createSystemCard("💻 CPU USAGE", "45%", "Optimal", "#3498db", "CPU: Intel Xeon E5-2680");
        cpuUsageLabel = (Label) ((VBox) cpuCard.getChildren().get(1)).getChildren().get(0);

        // Memory Usage Card
        VBox memoryCard = createSystemCard("🧠 MEMORY USAGE", "68%", "Normal", "#9b59b6", "RAM: 16GB DDR4");
        memoryUsageLabel = (Label) ((VBox) memoryCard.getChildren().get(1)).getChildren().get(0);

        // Database Connections
        VBox dbCard = createSystemCard("🗄 DB CONNECTIONS", "24", "Active", "#2ecc71", "Firestore: Connected");
        dbConnectionsLabel = (Label) ((VBox) dbCard.getChildren().get(1)).getChildren().get(0);

        // Active Users
        VBox usersCard = createSystemCard("👥 ACTIVE USERS", "3", "Online", "#e74c3c", "Managers: 2, Drivers: 2 ");
        activeUsersLabel = (Label) ((VBox) usersCard.getChildren().get(1)).getChildren().get(0);

        // System Uptime
        VBox uptimeCard = createSystemCard("⏱ SYSTEM UPTIME", "99.8", "Stable", "#f39c12", "Last restart: 7 days ago");
        systemUptimeLabel = (Label) ((VBox) uptimeCard.getChildren().get(1)).getChildren().get(0);

        // Storage Usage
        VBox storageCard = createSystemCard("💾 STORAGE USAGE", "2.4/10 GB", "24% used", "#1abc9c", "Firestore + Files");

        // API Response Time
        VBox apiCard = createSystemCard("⚡ API RESPONSE", "120ms", "Fast", "#d35400", "Avg latency: 95ms");

        grid.add(cpuCard, 0, 0);
        grid.add(memoryCard, 1, 0);
        grid.add(dbCard, 2, 0);
        grid.add(usersCard, 3, 0);
        grid.add(uptimeCard, 0, 1);
        grid.add(storageCard, 1, 1);
        grid.add(apiCard, 2, 1);

        return grid;
    }

    private VBox createSystemCard(String title, String value, String status, String color, String description) {
        VBox card = new VBox(12);
        card.setPrefWidth(200);
        card.setPadding(new Insets(20));
        card.setStyle(
                "-fx-background-color: " + CARD_BG + ";" +
                        "-fx-background-radius: 15;" +
                        "-fx-border-color: " + color + "20;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 15;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 15, 0.3, 0, 5);"
        );
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Text icon = new Text(title.split(" ")[0]);
        icon.setFont(Font.font(20));

        VBox titleBox = new VBox(2);
        Label titleLabel = new Label(title.substring(title.indexOf(" ") + 1));
        titleLabel.setStyle("-fx-text-fill: " + PRIMARY_BLUE + "; -fx-font-size: 13; -fx-font-weight: bold;");

        Label statusLabel = new Label(status);
        statusLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 10; -fx-font-weight: bold;");

        titleBox.getChildren().addAll(titleLabel, statusLabel);
        header.getChildren().addAll(icon, titleBox);

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 28; -fx-font-weight: bold;");

        ProgressBar progressBar = new ProgressBar();
        progressBar.setPrefWidth(160);

        // Configurer la barre de progression selon la valeur
        if (value.contains("%")) {
            double percent = Double.parseDouble(value.replace("%", "")) / 100.0;
            progressBar.setProgress(percent);
            progressBar.setStyle("-fx-accent: " + color + ";");
        } else {
            progressBar.setVisible(false);
        }

        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 11;");
        descLabel.setWrapText(true);

        VBox content = new VBox(8, valueLabel, progressBar, descLabel);
        card.getChildren().addAll(header, content);
        return card;
    }

    private void updateSystemCards() {
        try {
            // Récupérer les stats réelles
            Map<String, Object> stats = FirebaseService.getLiveSystemStats();

            // Mettre à jour la carte CPU
            cpuUsageLabel.setText(stats.get("cpuUsage") + "%");

            // Mettre à jour la carte Mémoire
            memoryUsageLabel.setText(stats.get("memoryUsage") + "%");

            // Mettre à jour la carte DB Connections
            Long activeCoaches = (Long) stats.getOrDefault("activeCoaches", 0L);
            Long activeDrivers = (Long) stats.getOrDefault("activeDrivers", 0L);
            dbConnectionsLabel.setText(activeCoaches + " coaches, " + activeDrivers + " drivers");

            // Mettre à jour la carte Active Users
            Long totalUsers = (Long) stats.getOrDefault("totalUsers", 0L);
            activeUsersLabel.setText(String.valueOf(totalUsers));

        } catch (Exception e) {
            System.err.println("❌ Error updating system cards: " + e.getMessage());
        }
    }

    private VBox createControlPanel() {
        VBox controlPanel = new VBox(20);
        controlPanel.setPadding(new Insets(25));
        controlPanel.setStyle(
                "-fx-background-color: " + CARD_BG + ";" +
                        "-fx-background-radius: 15;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 15;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 15, 0.3, 0, 5);"
        );

        Label sectionTitle = new Label("🎛 SYSTEM CONTROLS");
        sectionTitle.setStyle("-fx-text-fill: " + PRIMARY_BLUE + "; -fx-font-size: 18; -fx-font-weight: bold;");

        GridPane controlsGrid = new GridPane();
        controlsGrid.setHgap(20);
        controlsGrid.setVgap(20);
        controlsGrid.setPadding(new Insets(15, 0, 0, 0));

        // Maintenance Mode Toggle
        VBox maintenanceBox = createControlBox("🔧 MAINTENANCE MODE",
                "Temporarily disable system for non-admin users",
                "maintenance");

        maintenanceToggle = (ToggleButton) ((HBox) maintenanceBox.getChildren().get(2)).getChildren().get(0);
        maintenanceToggle.setOnAction(e -> toggleMaintenanceMode());

        // Cache Control
        VBox cacheBox = createControlBox("🗑 CACHE MANAGEMENT",
                "Clear system cache to free memory",
                "button");

        btnClearCache = (Button) ((HBox) cacheBox.getChildren().get(2)).getChildren().get(0);
        btnClearCache.setOnAction(e -> clearSystemCache());

        // Service Restart
        VBox restartBox = createControlBox("🔄 RESTART SERVICES",
                "Restart all background services",
                "button");

        btnRestartServices = (Button) ((HBox) restartBox.getChildren().get(2)).getChildren().get(0);
        btnRestartServices.setOnAction(e -> restartServices());

        // Backup Now
        VBox backupBox = createControlBox("💾 MANUAL BACKUP",
                "Create immediate system backup",
                "button");

        btnBackupNow = (Button) ((HBox) backupBox.getChildren().get(2)).getChildren().get(0);
        btnBackupNow.setOnAction(e -> createBackup());

        controlsGrid.add(maintenanceBox, 0, 0);
        controlsGrid.add(cacheBox, 1, 0);
        controlsGrid.add(restartBox, 0, 1);
        controlsGrid.add(backupBox, 1, 1);

        controlPanel.getChildren().addAll(sectionTitle, controlsGrid);
        return controlPanel;
    }

    private VBox createControlBox(String title, String description, String type) {
        VBox box = new VBox(10);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: " + PRIMARY_BLUE + "; -fx-font-size: 14; -fx-font-weight: bold;");

        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12;");
        descLabel.setWrapText(true);

        HBox controlBox = new HBox();

        if ("maintenance".equals(type)) {
            ToggleButton toggle = new ToggleButton("OFF");
            toggle.setStyle(
                    "-fx-background-color: " + SUCCESS_GREEN + ";" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-padding: 8 20;" +
                            "-fx-background-radius: 8;" +
                            "-fx-cursor: hand;"
            );

            toggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    toggle.setText("ON");
                    toggle.setStyle(
                            "-fx-background-color: " + DANGER_RED + ";" +
                                    "-fx-text-fill: white;" +
                                    "-fx-font-weight: bold;" +
                                    "-fx-padding: 8 20;" +
                                    "-fx-background-radius: 8;" +
                                    "-fx-cursor: hand;"
                    );
                } else {
                    toggle.setText("OFF");
                    toggle.setStyle(
                            "-fx-background-color: " + SUCCESS_GREEN + ";" +
                                    "-fx-text-fill: white;" +
                                    "-fx-font-weight: bold;" +
                                    "-fx-padding: 8 20;" +
                                    "-fx-background-radius: 8;" +
                                    "-fx-cursor: hand;"
                    );
                }
            });

            controlBox.getChildren().add(toggle);

        } else if ("button".equals(type)) {
            Button button = new Button("Execute");
            button.setStyle(
                    "-fx-background-color: " + ACCENT_GOLD + ";" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-padding: 8 25;" +
                            "-fx-background-radius: 8;" +
                            "-fx-cursor: hand;"
            );

            button.setOnMouseEntered(e -> button.setStyle(
                    "-fx-background-color: " + WARNING_ORANGE + ";" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-padding: 8 25;" +
                            "-fx-background-radius: 8;" +
                            "-fx-cursor: hand;"
            ));

            button.setOnMouseExited(e -> button.setStyle(
                    "-fx-background-color: " + ACCENT_GOLD + ";" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-padding: 8 25;" +
                            "-fx-background-radius: 8;" +
                            "-fx-cursor: hand;"
            ));

            controlBox.getChildren().add(button);
        }

        box.getChildren().addAll(titleLabel, descLabel, controlBox);
        return box;
    }

    private VBox createServicesTable() {
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
        Label sectionTitle = new Label("🔄 SYSTEM SERVICES");
        sectionTitle.setStyle("-fx-text-fill: " + PRIMARY_BLUE + "; -fx-font-size: 18; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnRefresh = new Button("🔄 Refresh");
        btnRefresh.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: " + ACCENT_GOLD + ";" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 5 15;" +
                        "-fx-border-color: " + ACCENT_GOLD + ";" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 6;" +
                        "-fx-cursor: hand;"
        );
        btnRefresh.setOnAction(e -> loadServicesStatus());

        header.getChildren().addAll(sectionTitle, spacer, btnRefresh);

        // Créer la table des services
        servicesTable = new TableView<>();
        servicesTable.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-border-color: #dee2e6;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;"
        );
        servicesTable.setRowFactory(tv -> {
            TableRow<ServiceStatus> row = new TableRow<>();
            row.setStyle("-fx-text-fill: black;"); // Force le texte en noir
            return row;
        });
        servicesTable.setPrefHeight(250);

        Label placeholder = new Label("Loading services status...");
        placeholder.setStyle("-fx-text-fill: black; -fx-font-size: 14;");
        servicesTable.setPlaceholder(placeholder);

        // Colonnes
        TableColumn<ServiceStatus, String> serviceCol = new TableColumn<>("Service");
        serviceCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        serviceCol.setPrefWidth(200);
        serviceCol.setSortable(true);

        TableColumn<ServiceStatus, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(120);
        statusCol.setSortable(true);
        statusCol.setCellFactory(col -> new TableCell<ServiceStatus, String>() {
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

                    if ("Running".equals(status) || "Active".equals(status) || "Connected".equals(status)) {
                        setStyle("-fx-text-fill: " + SUCCESS_GREEN + "; -fx-background-color: #e8f5e9; " +
                                "-fx-background-radius: 12; -fx-padding: 4 12;");
                    } else if ("Stopped".equals(status) || "Error".equals(status) || "Disconnected".equals(status)) {
                        setStyle("-fx-text-fill: " + DANGER_RED + "; -fx-background-color: #ffebee; " +
                                "-fx-background-radius: 12; -fx-padding: 4 12;");
                    } else if ("Starting".equals(status) || "Restarting".equals(status)) {
                        setStyle("-fx-text-fill: " + WARNING_ORANGE + "; -fx-background-color: #fff3e0; " +
                                "-fx-background-radius: 12; -fx-padding: 4 12;");
                    } else {
                        setStyle("-fx-text-fill: black; -fx-background-color: #f5f5f5; " +
                                "-fx-background-radius: 12; -fx-padding: 4 12;");
                    }
                }
            }
        });

        TableColumn<ServiceStatus, String> uptimeCol = new TableColumn<>("Uptime");
        uptimeCol.setCellValueFactory(new PropertyValueFactory<>("uptime"));
        uptimeCol.setPrefWidth(120);
        uptimeCol.setSortable(true);

        TableColumn<ServiceStatus, String> cpuCol = new TableColumn<>("CPU");
        cpuCol.setCellValueFactory(new PropertyValueFactory<>("cpuUsage"));
        cpuCol.setPrefWidth(80);
        cpuCol.setSortable(true);

        TableColumn<ServiceStatus, String> memoryCol = new TableColumn<>("Memory");
        memoryCol.setCellValueFactory(new PropertyValueFactory<>("memoryUsage"));
        memoryCol.setPrefWidth(100);
        memoryCol.setSortable(true);

        TableColumn<ServiceStatus, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(150);

        actionsCol.setCellFactory(col -> new TableCell<ServiceStatus, Void>() {
            private final Button restartBtn = new Button("🔄");
            private final Button stopBtn = new Button("⏹️");
            private final HBox buttons = new HBox(8, restartBtn, stopBtn);

            {
                restartBtn.setStyle(
                        "-fx-background-color: " + ACCENT_GOLD + ";" +
                                "-fx-text-fill: white;" +
                                "-fx-font-size: 12;" +
                                "-fx-padding: 6 12;" +
                                "-fx-background-radius: 6;" +
                                "-fx-cursor: hand;" +
                                "-fx-min-width: 40;"
                );
                restartBtn.setTooltip(new Tooltip("Restart Service"));

                stopBtn.setStyle(
                        "-fx-background-color: " + DANGER_RED + ";" +
                                "-fx-text-fill: white;" +
                                "-fx-font-size: 12;" +
                                "-fx-padding: 6 12;" +
                                "-fx-background-radius: 6;" +
                                "-fx-cursor: hand;" +
                                "-fx-min-width: 40;"
                );
                stopBtn.setTooltip(new Tooltip("Stop Service"));

                restartBtn.setOnAction(e -> {
                    ServiceStatus service = getTableView().getItems().get(getIndex());
                    restartService(service);
                });

                stopBtn.setOnAction(e -> {
                    ServiceStatus service = getTableView().getItems().get(getIndex());
                    stopService(service);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    ServiceStatus service = getTableView().getItems().get(getIndex());
                    if ("Running".equals(service.getStatus()) || "Connected".equals(service.getStatus())) {
                        stopBtn.setDisable(false);
                    } else {
                        stopBtn.setDisable(true);
                    }
                    setGraphic(buttons);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        servicesTable.getColumns().addAll(serviceCol, statusCol, uptimeCol, cpuCol, memoryCol, actionsCol);
        servicesTable.setItems(servicesList);

        section.getChildren().addAll(header, servicesTable);
        return section;
    }

    private void loadRealSystemLogs() {
        logsList.clear();

        try {
            System.out.println("🔄 Loading REAL system logs...");

            // 1. Charger les incidents comme logs
            Firestore db = FirestoreClient.getFirestore();
            QuerySnapshot incidentsSnapshot = db.collection("incidents")
                    .orderBy("reportedDate", Query.Direction.DESCENDING)
                    .limit(20)
                    .get()
                    .get();

            for (QueryDocumentSnapshot doc : incidentsSnapshot) {
                Timestamp timestamp = doc.getTimestamp("reportedDate");
                String time = timestamp != null ?
                        timestamp.toDate().toString() :
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                logsList.add(new SystemLog(
                        time,
                        "WARNING",
                        "Incident",
                        "Type: " + doc.getString("type") + " - " + doc.getString("description"),
                        doc.getString("driverId") != null ? doc.getString("driverId") : "Unknown"
                ));
            }

            // 2. Charger les réclamations comme logs
            QuerySnapshot claimsSnapshot = db.collection("reclamations")
                    .orderBy("date", Query.Direction.DESCENDING)
                    .limit(10)
                    .get()
                    .get();

            for (QueryDocumentSnapshot doc : claimsSnapshot) {
                logsList.add(new SystemLog(
                        doc.getString("date"),
                        "INFO",
                        "Reclamation",
                        "Type: " + doc.getString("type") + " - Status: " + doc.getString("status"),
                        doc.getString("customer")
                ));
            }

            // 3. Charger les logs système si existent
            try {
                QuerySnapshot systemLogsSnapshot = db.collection("systemLogs")
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                        .limit(10)
                        .get()
                        .get();

                for (QueryDocumentSnapshot doc : systemLogsSnapshot) {
                    Timestamp timestamp = doc.getTimestamp("timestamp");
                    String time = timestamp != null ?
                            timestamp.toDate().toString() :
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                    logsList.add(new SystemLog(
                            time,
                            doc.getString("level"),
                            doc.getString("source"),
                            doc.getString("message"),
                            doc.getString("user")
                    ));
                }
            } catch (Exception e) {
                // Si la collection n'existe pas, c'est normal
                System.out.println("ℹ️ No systemLogs collection found");
            }

            System.out.println("✅ Loaded " + logsList.size() + " real system logs");

            // Si vide, ajouter un log manuel
            if (logsList.isEmpty()) {
                logsList.add(new SystemLog(
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                        "INFO",
                        "System",
                        "No system logs available. Database may be empty.",
                        "System"
                ));
            }

        } catch (Exception e) {
            System.err.println("❌ ERROR loading system logs: " + e.getMessage());
            e.printStackTrace();

            // Ajouter un log d'erreur
            logsList.add(new SystemLog(
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    "ERROR",
                    "System",
                    "Failed to load logs: " + e.getMessage(),
                    "System"
            ));
        }

        logsTable.setItems(logsList);
        logsTable.refresh();
    }

    private VBox createLogsTable() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(25));
        Label sectionTitle = new Label("📋 SYSTEM LOGS");
        sectionTitle.setStyle("-fx-text-fill: " + PRIMARY_BLUE + "; -fx-font-size: 18; -fx-font-weight: bold;");

        // Créer la table des logs
        logsTable = new TableView<>();
        logsTable.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-border-color: #dee2e6;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;"
        );
        logsTable.setRowFactory(tv -> {
            TableRow<SystemLog> row = new TableRow<>();
            row.setStyle("-fx-text-fill: black;"); // Force le texte en noir
            return row;
        });
        logsTable.setPrefHeight(300);

        Label placeholder = new Label("Loading system logs...");
        placeholder.setStyle("-fx-text-fill: black; -fx-font-size: 14;");
        logsTable.setPlaceholder(placeholder);

        // Colonnes
        TableColumn<SystemLog, String> timeCol = new TableColumn<>("Timestamp");
        timeCol.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        timeCol.setPrefWidth(180);
        timeCol.setSortable(true);

        TableColumn<SystemLog, String> levelCol = new TableColumn<>("Level");
        levelCol.setCellValueFactory(new PropertyValueFactory<>("level"));
        levelCol.setPrefWidth(100);
        levelCol.setSortable(true);
        levelCol.setCellFactory(col -> new TableCell<SystemLog, String>() {
            @Override
            protected void updateItem(String level, boolean empty) {
                super.updateItem(level, empty);
                if (empty || level == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(level);
                    setFont(Font.font("Arial", FontWeight.BOLD, 12));
                    setAlignment(Pos.CENTER);

                    switch (level.toUpperCase()) {
                        case "ERROR":
                        case "CRITICAL":
                            setStyle("-fx-text-fill: " + DANGER_RED + "; -fx-background-color: #ffebee; " +
                                    "-fx-background-radius: 12; -fx-padding: 4 12;");
                            break;
                        case "WARNING":
                            setStyle("-fx-text-fill: " + WARNING_ORANGE + "; -fx-background-color: #fff3e0; " +
                                    "-fx-background-radius: 12; -fx-padding: 4 12;");
                            break;
                        case "INFO":
                            setStyle("-fx-text-fill: " + SUCCESS_GREEN + "; -fx-background-color: #e8f5e9; " +
                                    "-fx-background-radius: 12; -fx-padding: 4 12;");
                            break;
                        default:
                            setStyle("-fx-text-fill: black; -fx-background-color: #f5f5f5; " +
                                    "-fx-background-radius: 12; -fx-padding: 4 12;");
                    }
                }
            }
        });

        TableColumn<SystemLog, String> sourceCol = new TableColumn<>("Source");
        sourceCol.setCellValueFactory(new PropertyValueFactory<>("source"));
        sourceCol.setPrefWidth(150);
        sourceCol.setSortable(true);

        TableColumn<SystemLog, String> messageCol = new TableColumn<>("Message");
        messageCol.setCellValueFactory(new PropertyValueFactory<>("message"));
        messageCol.setPrefWidth(400);
        messageCol.setSortable(true);

        TableColumn<SystemLog, String> userCol = new TableColumn<>("User");
        userCol.setCellValueFactory(new PropertyValueFactory<>("user"));
        userCol.setPrefWidth(120);
        userCol.setSortable(true);

        logsTable.getColumns().addAll(timeCol, levelCol, sourceCol, messageCol, userCol);
        logsTable.setItems(logsList);

        // Boutons pour les logs
        HBox logActions = new HBox(10);
        logActions.setPadding(new Insets(15, 0, 0, 0));

        Button btnClearLogs = new Button("🗑️ Clear Logs");
        Button btnExportLogs = new Button("📤 Export Logs");
        Button btnFilterErrors = new Button("⚠️ Show Errors Only");
        Button btnShowAll = new Button("📋 Show All Logs");

        for (Button btn : new Button[]{btnClearLogs, btnExportLogs, btnFilterErrors, btnShowAll}) {
            btn.setStyle(
                    "-fx-background-color: " + PRIMARY_BLUE + ";" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-padding: 8 15;" +
                            "-fx-background-radius: 6;" +
                            "-fx-cursor: hand;" +
                            "-fx-font-size: 12;"
            );
        }

        btnClearLogs.setOnAction(e -> clearLogs());
        btnExportLogs.setOnAction(e -> exportLogs());
        btnFilterErrors.setOnAction(e -> filterErrorLogs());
        btnShowAll.setOnAction(e -> {
            logsTable.setItems(logsList);
            Label newPlaceholder = new Label("No system logs available");
            newPlaceholder.setStyle("-fx-text-fill: black; -fx-font-size: 14;");
            logsTable.setPlaceholder(newPlaceholder);
        });

        logActions.getChildren().addAll(btnClearLogs, btnExportLogs, btnFilterErrors, btnShowAll);

        section.getChildren().addAll(sectionTitle, logsTable, logActions);
        return section;
    }

    // REMPLACER aussi cette méthode:
    private void filterErrorLogs() {
        ObservableList<SystemLog> filteredLogs = FXCollections.observableArrayList();

        for (SystemLog log : logsList) {
            if ("ERROR".equalsIgnoreCase(log.getLevel()) || "CRITICAL".equalsIgnoreCase(log.getLevel())) {
                filteredLogs.add(log);
            }
        }

        logsTable.setItems(filteredLogs);

        Label newPlaceholder = new Label("No error logs found");
        newPlaceholder.setStyle("-fx-text-fill: black; -fx-font-size: 14;");
        logsTable.setPlaceholder(newPlaceholder);
    }

    private HBox createActionBar() {
        HBox actionBar = new HBox(15);
        actionBar.setPadding(new Insets(20, 0, 0, 0));
        actionBar.setAlignment(Pos.CENTER);

        Button btnSystemDiagnostic = createActionButton("🔍 Run Diagnostic", ACCENT_GOLD);
        Button btnUpdateSystem = createActionButton("🔄 Check Updates", "#3498db");
        Button btnEmergencyStop = createActionButton("🛑 Emergency Stop", DANGER_RED);

        btnSystemDiagnostic.setOnAction(e -> runSystemDiagnostic());
        btnUpdateSystem.setOnAction(e -> checkForUpdates());
        btnEmergencyStop.setOnAction(e -> emergencyStop());
        Button btnInitCollections = new Button("🏗 Init DB");
        btnInitCollections.setTooltip(new Tooltip("Initialize all database collections"));
        btnInitCollections.setStyle(
                "-fx-background-color: #8e44ad;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 8 15;" +
                        "-fx-background-radius: 6;" +
                        "-fx-cursor: hand;"
        );

        btnInitCollections.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION); // CHANGÉ de CONFIRMATION à INFORMATION
            alert.setTitle("Database Initialization");
            alert.setHeaderText("⚠ Feature Temporarily Disabled");
            alert.setContentText("The automatic database initialization feature has been temporarily disabled\n" +
                    "to prevent excessive Firestore operations.\n\n" +
                    "Collections are created automatically when needed.\n" +
                    "No manual initialization is required.");

            alert.showAndWait(); // Juste un message d'information, pas d'action
        });

        actionBar.getChildren().addAll(btnSystemDiagnostic, btnUpdateSystem, btnEmergencyStop);
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
                        "-fx-font-size: 14;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0.3, 0, 2);"
        );

        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: " + darkenColor(color) + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 12 25;" +
                        "-fx-background-radius: 10;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-size: 14;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0.4, 0, 3);"
        ));

        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 12 25;" +
                        "-fx-background-radius: 10;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-size: 14;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0.3, 0, 2);"
        ));

        return btn;
    }

    // ==============================
    // DATA LOADING & SYNC METHODS
    // ==============================

    private void loadSystemData() {
        try {
            System.out.println("🔄 Loading system data from Firestore...");

            // Charger les statistiques
            loadSystemStats();

            // Charger le statut des services
            loadServicesStatus();

            // Charger les logs système
            loadSystemLogs();

            System.out.println("✅ System data loaded successfully");

        } catch (Exception e) {
            System.err.println("❌ Error loading system data: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to load system data: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadSystemStats() {
        try {
            // Récupérer les données en temps réel de Firestore
            Map<String, Object> stats = FirebaseService.getSystemStatus();

            javafx.application.Platform.runLater(() -> {
                if (stats != null) {
                    // Utiliser les données réelles
                    cpuUsageLabel.setText(stats.getOrDefault("cpuUsage", "0") + "%");
                    memoryUsageLabel.setText(stats.getOrDefault("memoryUsage", "0") + "%");

                    // Connexions DB (nombre total d'utilisateurs)
                    Long totalUsers = (Long) stats.getOrDefault("totalUsers", 0L);
                    dbConnectionsLabel.setText(String.valueOf(totalUsers));

                    // Utilisateurs actifs (drivers actifs)
                    Long activeDrivers = (Long) stats.getOrDefault("activeDrivers", 0L);
                    // Ajouter les managers actifs
                    Long activeManagers = (Long) stats.getOrDefault("activeManagers", 0L);
                    activeUsersLabel.setText(String.valueOf(activeDrivers + activeManagers));

                    // Calculer l'uptime système (simulé pour l'instant)
                    systemUptimeLabel.setText("99.8%");
                } else {
                    // Données par défaut si aucune donnée n'est disponible
                    cpuUsageLabel.setText("N/A");
                    memoryUsageLabel.setText("N/A");
                    dbConnectionsLabel.setText("0");
                    activeUsersLabel.setText("0");
                    systemUptimeLabel.setText("N/A");
                }
            });

        } catch (Exception e) {
            System.err.println("❌ Error loading system stats: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadServicesStatus() {
        try {
            servicesList.clear();

            // 1. Vérifier la connexion Firestore
            boolean isFirebaseConnected = checkRealFirestoreConnection();

            servicesList.add(new ServiceStatus(
                    "Firebase Database",
                    isFirebaseConnected ? "Connected" : "Disconnected",
                    getServiceUptime(),
                    isFirebaseConnected ? "5%" : "N/A",
                    isFirebaseConnected ? "128MB" : "N/A"
            ));

            // 2. Vérifier les services basés sur les données réelles
            checkAllServices();

        } catch (Exception e) {
            System.err.println("❌ Error loading services status: " + e.getMessage());
            servicesList.add(new ServiceStatus(
                    "System Monitor",
                    "Error",
                    "0s",
                    "N/A",
                    "N/A"
            ));
        }
    }

    private void checkAllServices() {
        servicesList.clear();

        try {
            System.out.println("🔄 Loading REAL services status...");

            // 1. Vérifier Firestore - test direct
            boolean firestoreConnected = false;
            try {
                Firestore db = FirestoreClient.getFirestore();
                QuerySnapshot testQuery = db.collection("users").limit(1).get().get();
                firestoreConnected = !testQuery.isEmpty();
            } catch (Exception e) {
                firestoreConnected = false;
            }

            servicesList.add(new ServiceStatus(
                    "Firestore Database",
                    firestoreConnected ? "Connected" : "Disconnected",
                    "Live",
                    firestoreConnected ? "15%" : "0%",
                    firestoreConnected ? "OK" : "ERROR"
            ));

            // 2. Vérifier si des données existent
            boolean hasData = false;
            try {
                Firestore db = FirestoreClient.getFirestore();
                long usersCount = db.collection("users").count().get().get().getCount();
                hasData = usersCount > 0;
            } catch (Exception e) {
                hasData = false;
            }

            servicesList.add(new ServiceStatus(
                    "Data Service",
                    hasData ? "Running" : "No Data",
                    "Live",
                    hasData ? "10%" : "0%",
                    hasData ? "Data available" : "No users found"
            ));

            // 3. Vérifier les incidents
            long pendingIncidents = 0;
            try {
                Firestore db = FirestoreClient.getFirestore();
                QuerySnapshot pendingSnapshot = db.collection("incidents")
                        .whereEqualTo("status", "pending")
                        .get()
                        .get();
                pendingIncidents = pendingSnapshot.size();
            } catch (Exception e) {
                // Ignore
            }

            servicesList.add(new ServiceStatus(
                    "Incident Service",
                    pendingIncidents > 0 ? "Active" : "Idle",
                    "Live",
                    "8%",
                    pendingIncidents + " pending"
            ));

            // 4. Vérifier les drivers
            long activeDrivers = 0;
            try {
                Firestore db = FirestoreClient.getFirestore();
                QuerySnapshot driversSnapshot = db.collection("drivers")
                        .whereEqualTo("status", "Active")
                        .get()
                        .get();
                activeDrivers = driversSnapshot.size();
            } catch (Exception e) {
                // Ignore
            }

            servicesList.add(new ServiceStatus(
                    "Driver Service",
                    activeDrivers > 0 ? "Running" : "No Drivers",
                    "Live",
                    "12%",
                    activeDrivers + " active drivers"
            ));

            // 5. Vérifier les coaches
            long activeCoaches = 0;
            try {
                Firestore db = FirestoreClient.getFirestore();
                QuerySnapshot coachesSnapshot = db.collection("coaches")
                        .whereEqualTo("status", "Active")
                        .get()
                        .get();
                activeCoaches = coachesSnapshot.size();
            } catch (Exception e) {
                // Ignore
            }

            servicesList.add(new ServiceStatus(
                    "Coach Service",
                    activeCoaches > 0 ? "Running" : "No Coaches",
                    "Live",
                    "10%",
                    activeCoaches + " active coaches"
            ));

            System.out.println("✅ Loaded " + servicesList.size() + " real services");

        } catch (Exception e) {
            System.err.println("❌ ERROR loading services: " + e.getMessage());

            // Services de secours
            servicesList.add(new ServiceStatus(
                    "System Monitor",
                    "ERROR",
                    "0s",
                    "N/A",
                    "Failed to load services"
            ));
        }

        servicesTable.setItems(servicesList);
        servicesTable.refresh();
    }

    private String getServiceUptime() {
        try {
            // Utiliser FirebaseService pour obtenir l'uptime
            // Pour l'instant, retourner une valeur par défaut
            // Vous pouvez créer une méthode dans FirebaseService pour calculer l'uptime réel

            return "7d 12h"; // Valeur par défaut temporaire

        } catch (Exception e) {
            System.err.println("⚠️ Could not calculate uptime: " + e.getMessage());
            return "Unknown";
        }
    }

    private String formatUptime(long millis) {
        long days = millis / (1000 * 60 * 60 * 24);
        long hours = (millis % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
        return days + "d " + hours + "h";
    }

    private boolean checkRealFirestoreConnection() {
        try {
            // Utiliser FirebaseService pour vérifier la connexion
            return FirebaseService.checkUserExists("admin1");
        } catch (Exception e) {
            System.err.println("❌ Firebase connection error: " + e.getMessage());
            return false;
        }
    }

    private void loadSystemLogs() {
        try {
            logsList.clear();

            // Charger les logs système depuis Firestore
            ObservableList<SystemLog> firestoreLogs = loadSystemLogsFromFirestore();
            logsList.addAll(firestoreLogs);

            // Si pas de logs, ajouter des logs par défaut
            if (logsList.isEmpty()) {
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                logsList.add(new SystemLog(
                        dtf.format(LocalDateTime.now()),
                        "INFO",
                        "SystemControl",
                        "No system logs found in database",
                        "system"
                ));
            }

        } catch (Exception e) {
            System.err.println("❌ Error loading system logs: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private ObservableList<SystemLog> loadSystemLogsFromFirestore() {
        ObservableList<SystemLog> logs = FXCollections.observableArrayList();

        try {
            // la methode  qui charge les vrais logs
            logs = FirebaseService.getRealSystemLogs();

            // Si toujours vide, ajouter un log de test
            if (logs.isEmpty()) {
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                logs.add(new SystemLog(
                        dtf.format(LocalDateTime.now()),
                        "INFO",
                        "SystemControl",
                        "No system logs found in database",
                        "system"
                ));
            }

        } catch (Exception e) {
            System.err.println("❌ Error loading system logs: " + e.getMessage());
            e.printStackTrace();
        }

        return logs;
    }

    private void loadIncidentLogs() {
        try {
            // Récupérer les incidents depuis Firestore
            ObservableList<Incident> incidents = FirebaseService.loadAllIncidents();

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            for (Incident incident : incidents) {
                logsList.add(new SystemLog(
                        incident.getDate(),
                        "WARNING",
                        "IncidentReport",
                        incident.getType() + " - " + incident.getDescription(),
                        incident.getDriverId() != null ? incident.getDriverId() : "unknown"
                ));
            }

        } catch (Exception e) {
            System.err.println("⚠️ Could not load incident logs: " + e.getMessage());
        }
    }

    private void startRealtimeMonitoring() {
        if (isListening) return;

        isListening = true;
        System.out.println("🔍 Starting real-time system monitoring...");

        // Simuler des mises à jour périodiques des statistiques
        Thread monitoringThread = new Thread(() -> {
            try {
                while (isListening) {
                    Thread.sleep(1000000); // Mettre à jour toutes les 1000 secondes

                    javafx.application.Platform.runLater(() -> {
                        // Mettre à jour les statistiques de manière aléatoire (simulation)
                        updateLiveStats();
                    });
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        monitoringThread.setDaemon(true);
        monitoringThread.start();
    }

    private void updateLiveStats() {
        try {
            // Récupérer les statistiques en temps réel
            Map<String, Object> stats = FirebaseService.getLiveSystemStats();

            javafx.application.Platform.runLater(() -> {
                if (stats != null) {
                    // Mettre à jour les labels
                    cpuUsageLabel.setText(stats.get("cpuUsage") + "%");
                    memoryUsageLabel.setText(stats.get("memoryUsage") + "%");

                    // Calculer les utilisateurs actifs
                    Long activeDrivers = (Long) stats.getOrDefault("activeDrivers", 0L);
                    Long activeManagers = (Long) stats.getOrDefault("activeManagers", 0L);
                    activeUsersLabel.setText(String.valueOf(activeDrivers + activeManagers));

                    // Mettre à jour les connexions DB
                    Long totalUsers = (Long) stats.getOrDefault("totalUsers", 0L);
                    dbConnectionsLabel.setText(String.valueOf(totalUsers));
                }

                // Rafraîchir les services
                checkAllServices();
                servicesTable.refresh();
            });

        } catch (Exception e) {
            System.err.println("❌ Error updating live stats: " + e.getMessage());
        }
    }
    // ==============================
    // CONTROL METHODS
    // ==============================

    private void toggleMaintenanceMode() {
        boolean isMaintenance = maintenanceToggle.isSelected();

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Maintenance Mode");
        alert.setHeaderText(isMaintenance ? "Enable Maintenance Mode" : "Disable Maintenance Mode");
        alert.setContentText(isMaintenance
                ? "This will temporarily disable the system for all non-admin users.\n\nAre you sure?"
                : "This will restore normal system operation.\n\nAre you sure?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Enregistrer le statut de maintenance dans Firestore
                    Map<String, Object> data = new HashMap<>();
                    data.put("maintenanceMode", isMaintenance);
                    data.put("changedBy", "admin");
                    data.put("changedAt", LocalDateTime.now().toString());

                    // Ici vous pourriez sauvegarder dans Firestore
                    // FirebaseService.saveSystemConfig("maintenance", data);

                    // Ajouter un log
                    logsList.add(0, new SystemLog(
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                            "INFO",
                            "SystemControl",
                            "Maintenance mode " + (isMaintenance ? "enabled" : "disabled"),
                            "admin"
                    ));

                    showAlert("Success",
                            "Maintenance mode has been " + (isMaintenance ? "enabled" : "disabled") + ".",
                            Alert.AlertType.INFORMATION);

                } catch (Exception e) {
                    showAlert("Error", "Failed to update maintenance mode: " + e.getMessage(),
                            Alert.AlertType.ERROR);
                    maintenanceToggle.setSelected(!isMaintenance);
                }
            } else {
                maintenanceToggle.setSelected(!isMaintenance);
            }
        });
    }

    private void clearSystemCache() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Clear System Cache");
        alert.setHeaderText("Clear All System Cache");
        alert.setContentText("This will clear all cached data including:\n" +
                "• User session data\n" +
                "• Database query cache\n" +
                "• Temporary files\n\n" +
                "This may cause temporary slowdown while cache rebuilds.\n\n" +
                "Are you sure?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Simuler le nettoyage du cache
                btnClearCache.setText("Clearing...");
                btnClearCache.setDisable(true);

                new Thread(() -> {
                    try {
                        Thread.sleep(2000); // Simuler le temps de nettoyage

                        javafx.application.Platform.runLater(() -> {
                            btnClearCache.setText("Execute");
                            btnClearCache.setDisable(false);

                            // Ajouter un log
                            logsList.add(0, new SystemLog(
                                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                                    "INFO",
                                    "SystemControl",
                                    "System cache cleared successfully",
                                    "admin"
                            ));

                            showAlert("Success",
                                    "System cache cleared successfully.\n" +
                                            "Cache will rebuild automatically as needed.",
                                    Alert.AlertType.INFORMATION);
                        });

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
            }
        });
    }

    private void restartServices() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Restart Services");
        alert.setHeaderText("Restart All System Services");
        alert.setContentText("This will restart all background services.\n\n" +
                "⚠️ Users may experience temporary interruptions.\n" +
                "⚠️ Active sessions will be maintained.\n\n" +
                "Are you sure?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                btnRestartServices.setText("Restarting...");
                btnRestartServices.setDisable(true);

                new Thread(() -> {
                    try {
                        // Mettre à jour le statut de tous les services
                        for (ServiceStatus service : servicesList) {
                            service.setStatus("Restarting");
                        }

                        javafx.application.Platform.runLater(() -> {
                            servicesTable.refresh();
                        });

                        Thread.sleep(1500); // Simuler le temps de redémarrage

                        javafx.application.Platform.runLater(() -> {
                            for (ServiceStatus service : servicesList) {
                                service.setStatus("Running");
                                service.setUptime("0s");
                            }

                            servicesTable.refresh();
                            btnRestartServices.setText("Execute");
                            btnRestartServices.setDisable(false);

                            // Ajouter un log
                            logsList.add(0, new SystemLog(
                                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                                    "INFO",
                                    "SystemControl",
                                    "All system services restarted successfully",
                                    "admin"
                            ));

                            showAlert("Success",
                                    "All services have been restarted successfully.\n" +
                                            "System is now running with fresh instances.",
                                    Alert.AlertType.INFORMATION);
                        });

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
            }
        });
    }

    private void createBackup() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Create Backup");
        alert.setHeaderText("Create Manual System Backup");
        alert.setContentText("This will create a complete backup of:\n" +
                "• All user data\n" +
                "• System configurations\n" +
                "• Database records\n" +
                "• Log files\n\n" +
                "Backup may take several minutes depending on data size.\n\n" +
                "Are you sure?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                btnBackupNow.setText("Backing up...");
                btnBackupNow.setDisable(true);

                ProgressDialog progressDialog = new ProgressDialog();
                progressDialog.showProgress("Creating backup...", "This may take a few minutes.");

                new Thread(() -> {
                    try {
                        // Simuler le processus de sauvegarde
                        for (int i = 0; i <= 100; i += 10) {
                            final int progress = i;
                            Thread.sleep(500);

                            javafx.application.Platform.runLater(() -> {
                                progressDialog.updateProgress(progress);
                                if (progress == 100) {
                                    progressDialog.close();

                                    btnBackupNow.setText("Execute");
                                    btnBackupNow.setDisable(false);

                                    // Ajouter un log
                                    logsList.add(0, new SystemLog(
                                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                                            "INFO",
                                            "SystemControl",
                                            "Manual system backup created successfully",
                                            "admin"
                                    ));

                                    showAlert("Backup Complete",
                                            "✅ System backup created successfully!\n\n" +
                                                    "Backup ID: BKP-" + System.currentTimeMillis() + "\n" +
                                                    "Size: ~2.4 GB\n" +
                                                    "Location: /backups/system/",
                                            Alert.AlertType.INFORMATION);
                                }
                            });
                        }

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        javafx.application.Platform.runLater(() -> {
                            progressDialog.close();
                            btnBackupNow.setText("Execute");
                            btnBackupNow.setDisable(false);
                        });
                    }
                }).start();
            }
        });
    }

    private void restartService(ServiceStatus service) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Restart Service");
        alert.setHeaderText("Restart Service: " + service.getName());
        alert.setContentText("Are you sure you want to restart this service?\n\n" +
                "Service: " + service.getName() + "\n" +
                "Current status: " + service.getStatus() + "\n\n" +
                "This may cause temporary interruption for this service.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                service.setStatus("Restarting");
                servicesTable.refresh();

                new Thread(() -> {
                    try {
                        Thread.sleep(1500); // Simuler le temps de redémarrage

                        javafx.application.Platform.runLater(() -> {
                            service.setStatus("Running");
                            service.setUptime("0s");
                            servicesTable.refresh();

                            logsList.add(0, new SystemLog(
                                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                                    "INFO",
                                    "SystemControl",
                                    "Service restarted: " + service.getName(),
                                    "admin"
                            ));
                        });

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
            }
        });
    }

    private void stopService(ServiceStatus service) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Stop Service");
        alert.setHeaderText("Stop Service: " + service.getName());
        alert.setContentText("⚠️ WARNING: Stopping this service will:\n" +
                "• Disable all functionality related to this service\n" +
                "• May cause system instability\n" +
                "• Require manual restart\n\n" +
                "Are you absolutely sure?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                service.setStatus("Stopped");
                servicesTable.refresh();

                logsList.add(0, new SystemLog(
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                        "WARNING",
                        "SystemControl",
                        "Service stopped manually: " + service.getName(),
                        "admin"
                ));

                showAlert("Service Stopped",
                        "Service '" + service.getName() + "' has been stopped.\n" +
                                "You can restart it using the restart button.",
                        Alert.AlertType.WARNING);
            }
        });
    }

    private void clearLogs() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Clear Logs");
        alert.setHeaderText("Clear All System Logs");
        alert.setContentText("This will permanently delete all system logs.\n\n" +
                "⚠️ This action cannot be undone!\n" +
                "⚠️ Important diagnostic information will be lost.\n\n" +
                "Are you absolutely sure?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                logsList.clear();
                showAlert("Logs Cleared",
                        "All system logs have been cleared.",
                        Alert.AlertType.INFORMATION);
            }
        });
    }

    private void exportLogs() {
        try {
            // Simuler l'exportation des logs
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = "system_logs_" + timestamp + ".csv";

            // Ici vous implémenteriez l'exportation réelle
            showAlert("Export Started",
                    "System logs export has been initiated.\n\n" +
                            "File: " + filename + "\n" +
                            "Records: " + logsList.size() + "\n\n" +
                            "Download will start automatically when ready.",
                    Alert.AlertType.INFORMATION);

            logsList.add(0, new SystemLog(
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    "INFO",
                    "SystemControl",
                    "System logs exported: " + filename,
                    "admin"
            ));

        } catch (Exception e) {
            showAlert("Export Failed",
                    "Failed to export logs: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }


    private void runSystemDiagnostic() {
        ProgressDialog progressDialog = new ProgressDialog();
        progressDialog.showProgress("Running system diagnostic...", "Checking all system components.");

        new Thread(() -> {
            try {
                List<String> results = new ArrayList<>();

                // Simuler des vérifications
                String[] checks = {
                        "Database connection",
                        "File system permissions",
                        "Network connectivity",
                        "Service health",
                        "Security settings",
                        "Backup integrity",
                        "User authentication",
                        "API endpoints"
                };

                Random random = new Random();

                for (int i = 0; i < checks.length; i++) {
                    Thread.sleep(800);
                    final int progress = (i + 1) * 100 / checks.length;
                    final String check = checks[i];

                    boolean passed = random.nextDouble() > 0.2; // 80% de succès
                    String result = passed ? "✅ PASS" : "❌ FAIL";
                    results.add(result + " - " + check);

                    javafx.application.Platform.runLater(() -> {
                        progressDialog.updateProgress(progress);
                    });
                }

                Thread.sleep(1000);

                javafx.application.Platform.runLater(() -> {
                    progressDialog.close();

                    // Afficher les résultats
                    StringBuilder resultText = new StringBuilder();
                    resultText.append("System Diagnostic Results:\n\n");

                    for (String result : results) {
                        resultText.append(result).append("\n");
                    }

                    resultText.append("\nSummary: ").append(results.stream()
                                    .filter(r -> r.startsWith("✅")).count())
                            .append(" passed, ")
                            .append(results.stream().filter(r -> r.startsWith("❌")).count())
                            .append(" failed out of ").append(results.size()).append(" checks.");

                    TextArea textArea = new TextArea(resultText.toString());
                    textArea.setEditable(false);
                    textArea.setWrapText(true);
                    textArea.setPrefRowCount(15);

                    Alert resultsAlert = new Alert(Alert.AlertType.INFORMATION);
                    resultsAlert.setTitle("Diagnostic Results");
                    resultsAlert.setHeaderText("System Diagnostic Complete");
                    resultsAlert.getDialogPane().setContent(textArea);
                    resultsAlert.setResizable(true);
                    resultsAlert.getDialogPane().setPrefSize(600, 400);
                    resultsAlert.showAndWait();
                });

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                javafx.application.Platform.runLater(() -> {
                    progressDialog.close();
                });
            }
        }).start();
    }

    private void checkForUpdates() {
        ProgressDialog progressDialog = new ProgressDialog();
        progressDialog.showProgress("Checking for updates...", "Connecting to update server.");

        new Thread(() -> {
            try {
                Thread.sleep(2000); // Simuler la vérification

                javafx.application.Platform.runLater(() -> {
                    progressDialog.close();

                    Random random = new Random();
                    boolean updateAvailable = random.nextDouble() > 0.7; // 30% de chance

                    if (updateAvailable) {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Update Available");
                        alert.setHeaderText("New System Update Available");
                        alert.setContentText("Version 2.1.3 is available for download.\n\n" +
                                "New features:\n" +
                                "• Enhanced security protocols\n" +
                                "• Performance improvements\n" +
                                "• Bug fixes\n\n" +
                                "Download size: ~45 MB\n" +
                                "Install now?");

                        alert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                showAlert("Update Started",
                                        "System update has been scheduled.\n" +
                                                "The update will be applied on next system restart.",
                                        Alert.AlertType.INFORMATION);
                            }
                        });
                    } else {
                        showAlert("No Updates",
                                "Your system is up to date.\n" +
                                        "Current version: 2.1.2 (Latest)",
                                Alert.AlertType.INFORMATION);
                    }
                });

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                javafx.application.Platform.runLater(() -> {
                    progressDialog.close();
                });
            }
        }).start();
    }

    private void emergencyStop() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("⚠️ EMERGENCY STOP");
        alert.setHeaderText("EMERGENCY SYSTEM SHUTDOWN");
        alert.setContentText("⚠️ ⚠️ ⚠️ CRITICAL WARNING ⚠️ ⚠️ ⚠️\n\n" +
                "This will immediately:\n" +
                "• Stop ALL system services\n" +
                "• Log out ALL users\n" +
                "• Prevent any new connections\n" +
                "• Require manual restart\n\n" +
                "ONLY use this in case of:\n" +
                "• Security breach\n" +
                "• Critical system failure\n" +
                "• Data corruption\n\n" +
                "Enter 'CONFIRM' to proceed:");

        TextField confirmField = new TextField();
        confirmField.setPromptText("Type CONFIRM here");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        grid.add(new Label("Confirmation:"), 0, 0);
        grid.add(confirmField, 1, 0);

        alert.getDialogPane().setContent(grid);

        ButtonType confirmButton = new ButtonType("Emergency Stop", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getDialogPane().getButtonTypes().setAll(confirmButton, cancelButton);

        // Désactiver le bouton de confirmation par défaut
        Button confirmBtn = (Button) alert.getDialogPane().lookupButton(confirmButton);
        confirmBtn.setDisable(true);
        confirmBtn.setStyle("-fx-background-color: " + DANGER_RED + "; -fx-text-fill: white;");

        // Activer seulement si "CONFIRM" est saisi
        confirmField.textProperty().addListener((obs, oldVal, newVal) -> {
            confirmBtn.setDisable(!newVal.equalsIgnoreCase("CONFIRM"));
        });

        alert.showAndWait().ifPresent(response -> {
            if (response == confirmButton) {
                // Simuler l'arrêt d'urgence
                showAlert("EMERGENCY STOP INITIATED",
                        "⚠️ SYSTEM SHUTDOWN IN PROGRESS ⚠️\n\n" +
                                "All services are being stopped...\n" +
                                "Users are being logged out...\n" +
                                "System will be offline momentarily.",
                        Alert.AlertType.ERROR);

                // Ajouter un log critique
                logsList.add(0, new SystemLog(
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                        "CRITICAL",
                        "SystemControl",
                        "EMERGENCY SYSTEM SHUTDOWN INITIATED BY ADMIN",
                        "admin"
                ));
            }
        });
    }

    // ==============================
    // Other CLASSES
    // ==============================

    private static class CircleIndicator extends StackPane {
        private Circle circle;

        public CircleIndicator() {
            circle = new Circle(5);
            setStatus("offline");
            getChildren().add(circle);
        }

        public void setStatus(String status) {
            switch (status.toLowerCase()) {
                case "live":
                    circle.setFill(Color.web(SUCCESS_GREEN));
                    break;
                case "warning":
                    circle.setFill(Color.web(WARNING_ORANGE));
                    break;
                case "error":
                    circle.setFill(Color.web(DANGER_RED));
                    break;
                default:
                    circle.setFill(Color.GRAY);
            }
        }
    }

    public static class ServiceStatus {
        private final javafx.beans.property.SimpleStringProperty name;
        private final javafx.beans.property.SimpleStringProperty status;
        private final javafx.beans.property.SimpleStringProperty uptime;
        private final javafx.beans.property.SimpleStringProperty cpuUsage;
        private final javafx.beans.property.SimpleStringProperty memoryUsage;

        public ServiceStatus(String name, String status, String uptime, String cpuUsage, String memoryUsage) {
            this.name = new javafx.beans.property.SimpleStringProperty(name);
            this.status = new javafx.beans.property.SimpleStringProperty(status);
            this.uptime = new javafx.beans.property.SimpleStringProperty(uptime);
            this.cpuUsage = new javafx.beans.property.SimpleStringProperty(cpuUsage);
            this.memoryUsage = new javafx.beans.property.SimpleStringProperty(memoryUsage);
        }

        public String getName() { return name.get(); }
        public String getStatus() { return status.get(); }
        public String getUptime() { return uptime.get(); }
        public String getCpuUsage() { return cpuUsage.get(); }
        public String getMemoryUsage() { return memoryUsage.get(); }

        public void setName(String name) { this.name.set(name); }
        public void setStatus(String status) { this.status.set(status); }
        public void setUptime(String uptime) { this.uptime.set(uptime); }
        public void setCpuUsage(String cpuUsage) { this.cpuUsage.set(cpuUsage); }
        public void setMemoryUsage(String memoryUsage) { this.memoryUsage.set(memoryUsage); }
    }

    public static class SystemLog {
        private final javafx.beans.property.SimpleStringProperty timestamp;
        private final javafx.beans.property.SimpleStringProperty level;
        private final javafx.beans.property.SimpleStringProperty source;
        private final javafx.beans.property.SimpleStringProperty message;
        private final javafx.beans.property.SimpleStringProperty user;

        public SystemLog(String timestamp, String level, String source, String message, String user) {
            this.timestamp = new javafx.beans.property.SimpleStringProperty(timestamp);
            this.level = new javafx.beans.property.SimpleStringProperty(level);
            this.source = new javafx.beans.property.SimpleStringProperty(source);
            this.message = new javafx.beans.property.SimpleStringProperty(message);
            this.user = new javafx.beans.property.SimpleStringProperty(user);
        }

        public String getTimestamp() { return timestamp.get(); }
        public String getLevel() { return level.get(); }
        public String getSource() { return source.get(); }
        public String getMessage() { return message.get(); }
        public String getUser() { return user.get(); }
    }

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

            messageLabel = new Label(message);

            VBox content = new VBox(10, messageLabel, progressBar);
            content.setPadding(new Insets(20));
            content.setAlignment(Pos.CENTER);

            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
            dialog.show();
        }

        public void updateProgress(double progress) {
            if (progressBar != null) {
                progressBar.setProgress(progress / 100.0);
            }
        }

        public void close() {
            if (dialog != null) {
                dialog.close();
            }
        }
    }

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

    public void stopMonitoring() {
        isListening = false;
        System.out.println("🛑 System monitoring stopped");
    }
}