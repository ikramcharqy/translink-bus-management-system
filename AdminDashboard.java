package com.sample.demo3.views;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;
import com.sample.demo3.configuration.FirebaseService;
import com.sample.demo3.views.admin.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.animation.PauseTransition;

import java.util.Map;

import com.sample.demo3.configuration.SystemAuditService;
import com.sample.demo3.models.Incident;
import com.sample.demo3.views.admin.SystemSettingsView;
import javafx.util.Duration;


public class AdminDashboard extends Application {

    private BorderPane mainContainer;
    private VBox sidebarMenu;
    private StackPane contentArea;
    private String currentUser = "ADMIN";
    private String fullName = "SUPER ADMINISTRATOR";

    // Color scheme - Professionnel avec touches admin
    private final String PRIMARY_BLUE = "#2c3e50";
    private final String SECONDARY_BLUE = "#34495e";
    private final String ACCENT_GOLD = "#f39c12";
    private final String DANGER_RED = "#e74c3c";
    private final String SUCCESS_GREEN = "#27ae60";
    private final String WARNING_ORANGE = "#e67e22";
    private final String PURPLE = "#8e44ad";
    private final String DARK_BG = "#1a1a2e";
    private final String CARD_BG = "#ffffff";
    private final String TEXT_DARK = "#2c3e50";
    private final String TEXT_LIGHT = "#ecf0f1";

    // Data lists
    private ObservableList<User> usersList = FXCollections.observableArrayList();
    private ObservableList<Incident> allIncidentsList = FXCollections.observableArrayList();
    private ObservableList<SystemLog> systemLogsList = FXCollections.observableArrayList();
    private ObservableList<BackupRecord> backupsList = FXCollections.observableArrayList();

    // Table views
    private TableView<User> usersTable;
    private TableView<SystemLog> logsTable;
    private TableView<BackupRecord> backupsTable;

    // Statistics
    private int totalUsers = 0;
    private int activeSessions = 0;
    private int pendingActions = 0;
    private int systemHealth = 100;

    @Override
    public void start(Stage primaryStage) {
        try {
            mainContainer = new BorderPane();
            mainContainer.setStyle("-fx-background-color: " + DARK_BG + ";");

            // Create top header
            HBox header = createHeader();

            // Create sidebar navigation
            sidebarMenu = createSidebar();

            // Create content area
            contentArea = new StackPane();
            contentArea.setStyle("-fx-background-color: #f5f7fa;");
            contentArea.setPadding(new Insets(20));

            // Set default view (Admin Dashboard)
            showAdminDashboard();

            // Assemble layout
            mainContainer.setTop(header);
            mainContainer.setLeft(sidebarMenu);
            mainContainer.setCenter(contentArea);

            // 1. Obtenir les dimensions de l'écran (avec barre des tâches)
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            System.out.println("📏 Dimensions écran: " + screenBounds.getWidth() + "x" + screenBounds.getHeight());

            // 2. Calculer la taille optimale (95% de l'écran pour la barre des tâches)
            double windowWidth = screenBounds.getWidth() * 0.95;
            double windowHeight = screenBounds.getHeight() * 0.95;

            // 3. Créer la scène avec ces dimensions
            Scene scene = new Scene(mainContainer, windowWidth, windowHeight);

            // 4. Positionner la fenêtre
            primaryStage.setX(screenBounds.getMinX() + (screenBounds.getWidth() - windowWidth) / 2);
            primaryStage.setY(screenBounds.getMinY() + (screenBounds.getHeight() - windowHeight) / 2);
            primaryStage.setTitle("Translink - Admin Control Panel");
            primaryStage.setScene(scene);
            primaryStage.show();
            System.out.println("🚀 Démarrage de l'application...");

            // Load sample data
            loadSampleData();
            mainContainer = new BorderPane();

        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Application Error", "Failed to start admin panel: " + e.getMessage());
        }
    }
    private void checkAndGenerateMonitoringData() {
        System.out.println("ℹ️ Monitoring data check disabled to prevent excessive Firestore reads.");
    }

    private HBox createHeader() {
        HBox header = new HBox();
        header.setPadding(new Insets(15, 30, 15, 30));
        header.setStyle("-fx-background-color: " + PRIMARY_BLUE + "; -fx-background-radius: 0 0 15 15;");
        header.setAlignment(Pos.CENTER_LEFT);

        // Logo and title with admin icon
        HBox logoBox = new HBox(15);
        logoBox.setAlignment(Pos.CENTER_LEFT);

        // Admin shield icon
        Text adminIcon = new Text("🛡");
        adminIcon.setFont(Font.font(28));
        adminIcon.setFill(Color.web(ACCENT_GOLD));

        VBox titleBox = new VBox(2);
        Text titleText = new Text("TRANSLINK");
        titleText.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 26));
        titleText.setFill(Color.web(TEXT_LIGHT));

        Text subtitleText = new Text("ADMIN CONTROL PANEL");
        subtitleText.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        subtitleText.setFill(Color.web(ACCENT_GOLD));

        titleBox.getChildren().addAll(titleText, subtitleText);
        logoBox.getChildren().addAll(adminIcon, titleBox);

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // System status indicators
        HBox statusIndicators = createStatusIndicators();

        // User info section
        VBox userInfo = new VBox(2);
        userInfo.setAlignment(Pos.CENTER_RIGHT);

        Text userName = new Text(fullName);
        userName.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        userName.setFill(Color.web(TEXT_LIGHT));

        Text userRole = new Text("SUPER ADMINISTRATOR");
        userRole.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        userRole.setFill(Color.web(ACCENT_GOLD));

        userInfo.getChildren().addAll(userName, userRole);

        // Admin avatar with crown
        StackPane avatarStack = new StackPane();
        Circle avatarBg = new Circle(28);
        avatarBg.setFill(Color.web(SECONDARY_BLUE));
        avatarBg.setStroke(Color.web(ACCENT_GOLD));
        avatarBg.setStrokeWidth(2);

        Text avatarText = new Text("👑");
        avatarText.setFont(Font.font(20));

        avatarStack.getChildren().addAll(avatarBg, avatarText);

        HBox userSection = new HBox(15);
        userSection.setAlignment(Pos.CENTER_RIGHT);
        userSection.getChildren().addAll(userInfo, avatarStack);

        header.getChildren().addAll(logoBox, spacer, statusIndicators, userSection);

        return header;
    }

    private HBox createStatusIndicators() {
        HBox indicators = new HBox(20);
        indicators.setAlignment(Pos.CENTER);

        // System Health Indicator
        VBox healthBox = new VBox(3);
        healthBox.setAlignment(Pos.CENTER);

        StackPane healthIndicator = new StackPane();
        Circle healthCircle = new Circle(8);
        healthCircle.setFill(systemHealth > 80 ? Color.web(SUCCESS_GREEN) :
                systemHealth > 60 ? Color.web(WARNING_ORANGE) : Color.web(DANGER_RED));

        Text healthIcon = new Text(systemHealth > 80 ? "✓" : systemHealth > 60 ? "⚠" : "✗");
        healthIcon.setFont(Font.font(10));
        healthIcon.setFill(Color.WHITE);

        healthIndicator.getChildren().addAll(healthCircle, healthIcon);

        Label healthLabel = new Label("System Health");
        healthLabel.setStyle("-fx-text-fill: " + TEXT_LIGHT + "; -fx-font-size: 10;");

        healthBox.getChildren().addAll(healthIndicator, healthLabel);

        // Active Sessions Indicator
        VBox sessionsBox = new VBox(3);
        sessionsBox.setAlignment(Pos.CENTER);

        Label sessionsCount = new Label(activeSessions + " 🔗");
        sessionsCount.setStyle("-fx-text-fill: " + ACCENT_GOLD + "; -fx-font-weight: bold; -fx-font-size: 14;");

        Label sessionsLabel = new Label("Active Sessions");
        sessionsLabel.setStyle("-fx-text-fill: " + TEXT_LIGHT + "; -fx-font-size: 10;");

        sessionsBox.getChildren().addAll(sessionsCount, sessionsLabel);

        // Database Status
        VBox dbBox = new VBox(3);
        dbBox.setAlignment(Pos.CENTER);

        Text dbIcon = new Text("🗄️");
        dbIcon.setFont(Font.font(14));

        Label dbLabel = new Label("Firestore");
        dbLabel.setStyle("-fx-text-fill: " + TEXT_LIGHT + "; -fx-font-size: 10;");

        dbBox.getChildren().addAll(dbIcon, dbLabel);

        indicators.getChildren().addAll(healthBox, sessionsBox, dbBox);

        return indicators;
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.setPrefWidth(280);
        sidebar.setPadding(new Insets(25, 15, 25, 15));
        sidebar.setStyle("-fx-background-color: " + SECONDARY_BLUE + "; -fx-background-radius: 0 20 20 0;");

        // Admin welcome
        VBox welcomeBox = new VBox(5);
        welcomeBox.setPadding(new Insets(0, 0, 20, 15));

        Text welcomeText = new Text("Welcome Back");
        welcomeText.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        welcomeText.setFill(Color.web(TEXT_LIGHT));

        Text adminText = new Text("Super Admin");
        adminText.setFont(Font.font("Arial", 12));
        adminText.setFill(Color.web(ACCENT_GOLD));

        welcomeBox.getChildren().addAll(welcomeText, adminText);

        // Menu buttons with professional icons
        Button btnDashboard = createMenuButton("📊", "ADMIN DASHBOARD", true);
        Button btnUsers = createMenuButton("👥", "USER MANAGEMENT", false);
        Button btnSystem = createMenuButton("⚙", "SYSTEM CONTROL", false);
        Button btnMonitor = createMenuButton("👁", "REAL-TIME MONITOR", false);
        Button btnAnalytics = createMenuButton("📈", "ANALYTICS CENTER", false);
        Button btnSecurity = createMenuButton("🛡", "SECURITY & AUDIT", false);
        Button btnBackup = createMenuButton("💾", "BACKUP & RESTORE", false);
        Button btnSettings = createMenuButton("🔧", "SYSTEM SETTINGS", false);

        // Add actions
        btnDashboard.setOnAction(e -> {
            showAdminDashboard();
            updateSidebarButtons(btnDashboard);
        });
        btnUsers.setOnAction(e -> {
            showUserManagement();  // Appeler la nouvelle méthode
            updateSidebarButtons(btnUsers);
        });
        btnSystem.setOnAction(e -> {
            showSystemControl();
            updateSidebarButtons(btnSystem);
        });
        btnMonitor.setOnAction(e -> {
            showRealTimeMonitor();
            updateSidebarButtons(btnMonitor);
        });
        btnAnalytics.setOnAction(e -> {
            showAnalyticsCenter();
            updateSidebarButtons(btnAnalytics);
        });
        btnSecurity.setOnAction(e -> {
            showSecurityAudit();
            updateSidebarButtons(btnSecurity);
        });
        btnBackup.setOnAction(e -> {
            showBackupRestore();
            updateSidebarButtons(btnBackup);
        });
        btnSettings.setOnAction(e -> {
            showSystemSettings();
            updateSidebarButtons(btnSettings);
        });

        // Spacer to push logout to bottom
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Emergency controls
        VBox emergencyBox = new VBox(10);
        emergencyBox.setPadding(new Insets(20, 0, 0, 0));

        Button btnMaintenance = createEmergencyButton("🛑 MAINTENANCE MODE", DANGER_RED);
        btnMaintenance.setOnAction(e -> toggleMaintenanceMode());

        Button btnLogout = createMenuButton("🚪", "LOGOUT", false);
        btnLogout.setOnAction(e -> handleLogout());

        emergencyBox.getChildren().addAll(btnMaintenance, btnLogout);

        sidebar.getChildren().addAll(
                welcomeBox,
                btnDashboard,
                btnUsers,
                btnSystem,
                btnMonitor,
                btnAnalytics,
                btnSecurity,
                btnBackup,
                btnSettings,
                spacer,
                emergencyBox
        );

        return sidebar;
    }

    private Button createMenuButton(String icon, String text, boolean active) {
        Button btn = new Button();
        btn.setPrefWidth(250);
        btn.setPrefHeight(55);
        btn.setAlignment(Pos.CENTER_LEFT);

        HBox content = new HBox(15);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setPadding(new Insets(0, 20, 0, 20));

        Text iconText = new Text(icon);
        iconText.setFont(Font.font(20));

        Text labelText = new Text(text);
        labelText.setFont(Font.font("Arial", FontWeight.BOLD, 13));

        content.getChildren().addAll(iconText, labelText);
        btn.setGraphic(content);

        if (active) {
            btn.setStyle(
                    "-fx-background-color: " + ACCENT_GOLD + ";" +
                            "-fx-background-radius: 12;" +
                            "-fx-border-width: 0;" +
                            "-fx-cursor: hand;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0.3, 0, 3);"
            );
            labelText.setFill(Color.web(TEXT_DARK));
            iconText.setFill(Color.web(TEXT_DARK));
        } else {
            btn.setStyle(
                    "-fx-background-color: transparent;" +
                            "-fx-border-width: 0;" +
                            "-fx-cursor: hand;"
            );
            labelText.setFill(Color.web(TEXT_LIGHT));
            iconText.setFill(Color.web(TEXT_LIGHT));
        }

        // Hover effect
        btn.setOnMouseEntered(e -> {
            if (!active) {
                btn.setStyle(
                        "-fx-background-color: rgba(243, 156, 18, 0.2);" +
                                "-fx-background-radius: 12;" +
                                "-fx-border-width: 0;" +
                                "-fx-cursor: hand;" +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0.2, 0, 2);"
                );
            }
        });

        btn.setOnMouseExited(e -> {
            if (!active) {
                btn.setStyle(
                        "-fx-background-color: transparent;" +
                                "-fx-border-width: 0;" +
                                "-fx-cursor: hand;" +
                                "-fx-effect: none;"
                );
            }
        });

        return btn;
    }

    private Button createEmergencyButton(String text, String color) {
        Button btn = new Button(text);
        btn.setPrefWidth(250);
        btn.setPrefHeight(45);
        btn.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-width: 0;" +
                        "-fx-cursor: hand;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 12;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0.3, 0, 2);"
        );

        btn.setOnMouseEntered(e -> {
            btn.setStyle(
                    "-fx-background-color: " + color + ";" +
                            "-fx-background-radius: 10;" +
                            "-fx-border-width: 0;" +
                            "-fx-cursor: hand;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-font-size: 12;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 12, 0.4, 0, 3);"
            );
        });

        btn.setOnMouseExited(e -> {
            btn.setStyle(
                    "-fx-background-color: " + color + ";" +
                            "-fx-background-radius: 10;" +
                            "-fx-border-width: 0;" +
                            "-fx-cursor: hand;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-font-size: 12;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0.3, 0, 2);"
            );
        });

        return btn;
    }

    private void updateSidebarButtons(Button activeButton) {
        for (var node : sidebarMenu.getChildren()) {
            if (node instanceof Button) {
                Button btn = (Button) node;
                Object graphic = btn.getGraphic();
                if (graphic instanceof HBox) {
                    HBox content = (HBox) graphic;
                    if (content.getChildren().size() > 1) {
                        Text iconText = (Text) content.getChildren().get(0);
                        Text labelText = (Text) content.getChildren().get(1);

                        if (btn == activeButton) {
                            btn.setStyle("-fx-background-color: " + ACCENT_GOLD + ";-fx-background-radius: 12;-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0.3, 0, 3);");
                            labelText.setFill(Color.web(TEXT_DARK));
                            iconText.setFill(Color.web(TEXT_DARK));
                        } else if (!btn.getText().contains("MAINTENANCE")) {
                            btn.setStyle("-fx-background-color: transparent;-fx-effect: none;");
                            labelText.setFill(Color.web(TEXT_LIGHT));
                            iconText.setFill(Color.web(TEXT_LIGHT));
                        }
                    }
                }
            }
        }
    }

    // ==============================
    // ADMIN DASHBOARD VIEW
    // ==============================

    private void showAdminDashboard() {
        contentArea.getChildren().clear();

        VBox dashboardView = new VBox(25);
        dashboardView.setAlignment(Pos.TOP_LEFT);
        dashboardView.setPadding(new Insets(20));

        // Header
        HBox headerBox = new HBox();
        VBox headerText = new VBox(5);

        Text title = new Text("Admin Control Center");
        title.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 32));
        title.setFill(Color.web(PRIMARY_BLUE));

        Text subtitle = new Text("Complete system overview and control panel");
        subtitle.setFont(Font.font("Arial", 14));
        subtitle.setFill(Color.web("#7f8c8d"));

        headerText.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Quick stats - Maintenant avec les vraies données
        HBox quickStats = createRealQuickStats();

        headerBox.getChildren().addAll(headerText, spacer, quickStats);

        // System Status Cards
        VBox systemStatus = createRealSystemStatusCards();

        // Critical Alerts Section
        VBox alertsSection = createRealAlertsSection();

        // Real-time Data Grid
        VBox dataGrid = createRealDataGrid();

        dashboardView.getChildren().addAll(headerBox, systemStatus, alertsSection, dataGrid);

        ScrollPane scrollPane = new ScrollPane(dashboardView);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        contentArea.getChildren().add(scrollPane);
    }

    private HBox createRealQuickStats() {
        HBox statsBox = new HBox(20);
        statsBox.setAlignment(Pos.CENTER);

        try {
            // Appel DIRECT à Firestore
            System.out.println("🔄 Loading real stats from Firestore...");
            Firestore db = FirestoreClient.getFirestore();
            // 1. TOTAL USERS - DIRECT QUERY
            QuerySnapshot usersSnapshot = db.collection("users").get().get();
            long totalUsers = usersSnapshot.size();
            System.out.println("👥 Total users found: " + totalUsers);

            VBox usersStat = createStatCard("👥", "TOTAL USERS",
                    String.valueOf(totalUsers), "Registered users", PRIMARY_BLUE);

            // 2. ACTIVE USERS - VÉRIFICATION RÉELLE
            long activeUsers = 0;

            // Drivers actifs
            QuerySnapshot activeDriversSnapshot = db.collection("drivers")
                    .whereEqualTo("status", "Active")
                    .get()
                    .get();
            long activeDrivers = activeDriversSnapshot.size();
            System.out.println("🚗 Active drivers: " + activeDrivers);

            // Managers actifs (basé sur la connexion)
            // Pour l'instant, on suppose que tous les managers sont actifs
            QuerySnapshot managersSnapshot = db.collection("users")
                    .whereEqualTo("role", "manager")
                    .get()
                    .get();
            long activeManagers = managersSnapshot.size();

            activeUsers = activeDrivers + activeManagers;
            System.out.println("👤 Active users total: " + activeUsers);

            VBox activeUsersStat = createStatCard("👤", "ACTIVE USERS",
                    String.valueOf(activeUsers),
                    "Drivers: " + activeDrivers + ", Managers: " + activeManagers,
                    ACCENT_GOLD);

            // 3. ACTIVE COACHES
            QuerySnapshot activeCoachesSnapshot = db.collection("coaches")
                    .whereEqualTo("status", "Active")
                    .get()
                    .get();
            long activeCoaches = activeCoachesSnapshot.size();

            VBox coachesStat = createStatCard("🚌", "ACTIVE COACHES",
                    String.valueOf(activeCoaches), "In service", SUCCESS_GREEN);

            // 4. PENDING INCIDENTS
            QuerySnapshot pendingIncidentsSnapshot = db.collection("incidents")
                    .whereEqualTo("status", "pending")
                    .get()
                    .get();
            long pendingIncidents = pendingIncidentsSnapshot.size();

            VBox incidentsStat = createStatCard("⚠️", "PENDING INCIDENTS",
                    String.valueOf(pendingIncidents), "Requires attention", DANGER_RED);

            statsBox.getChildren().addAll(usersStat, activeUsersStat, coachesStat, incidentsStat);

        } catch (Exception e) {
            System.err.println("❌ ERROR in createRealQuickStats: " + e.getMessage());
            e.printStackTrace();

            // Affichage d'erreur clair
            VBox errorStat = createStatCard("❌", "ERROR", "0", "Check Firestore", DANGER_RED);
            statsBox.getChildren().add(errorStat);
        }

        return statsBox;
    }
    private VBox createRealSystemStatusCards() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(20, 0, 20, 0));

        Label sectionTitle = new Label("⚙ REAL-TIME SYSTEM STATUS");
        sectionTitle.setStyle("-fx-text-fill: " + PRIMARY_BLUE + "; -fx-font-size: 18; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setPadding(new Insets(15, 0, 0, 0));

        try {
            // Récupérer les statistiques en temps réel
            Map<String, Object> realtimeStats = FirebaseService.getLiveSystemStats();

            // Database Status
            boolean dbConnected = checkDatabaseConnection();
            VBox dbCard = createStatusCard("🗄 DATABASE", "Firestore",
                    dbConnected ? "Connected" : "Disconnected",
                    dbConnected ? "Latency: <100ms" : "Check connection",
                    dbConnected ? SUCCESS_GREEN : DANGER_RED);

            // System Health
            int cpuUsage = (int) realtimeStats.getOrDefault("cpuUsage", 0);
            int memoryUsage = (int) realtimeStats.getOrDefault("memoryUsage", 0);
            String systemHealth = cpuUsage < 80 && memoryUsage < 80 ? "Good" : "Warning";
            String healthColor = systemHealth.equals("Good") ? SUCCESS_GREEN : WARNING_ORANGE;
            VBox healthCard = createStatusCard("❤ SYSTEM HEALTH", systemHealth,
                    "CPU: " + cpuUsage + "%, RAM: " + memoryUsage + "%",
                    "All services operational", healthColor);

            // Active Sessions
            Long activeUsers = (Long) realtimeStats.getOrDefault("totalUsers", 0L);
            VBox sessionsCard = createStatusCard("🔗 ACTIVE SESSIONS",
                    String.valueOf(activeUsers), "Users online",
                    "Managers: " + realtimeStats.getOrDefault("activeManagers", 0) +
                            ", Drivers: " + realtimeStats.getOrDefault("activeDrivers", 0),
                    ACCENT_GOLD);

            // Incidents Status
            Long pendingIncidents = (Long) realtimeStats.getOrDefault("pendingIncidents", 0L);
            Long totalIncidents = (Long) realtimeStats.getOrDefault("totalIncidents", 0L);
            String incidentStatus = pendingIncidents > 0 ? "Attention Needed" : "All Clear";
            String incidentColor = pendingIncidents > 0 ? DANGER_RED : SUCCESS_GREEN;
            VBox incidentsCard = createStatusCard("⚠ INCIDENTS", incidentStatus,
                    pendingIncidents + " pending / " + totalIncidents + " total",
                    "Response rate: 95%", incidentColor);

            grid.add(dbCard, 0, 0);
            grid.add(healthCard, 1, 0);
            grid.add(sessionsCard, 0, 1);
            grid.add(incidentsCard, 1, 1);

        } catch (Exception e) {
            System.err.println("❌ Error loading system status: " + e.getMessage());
            // Cards par défaut en cas d'erreur
            VBox dbCard = createStatusCard("🗄 DATABASE", "Unknown",
                    "Connection error", "Check Firestore config", DANGER_RED);
            VBox healthCard = createStatusCard("❤ SYSTEM HEALTH", "Unknown",
                    "Unable to load", "Service unavailable", WARNING_ORANGE);
            VBox sessionsCard = createStatusCard("🔗 ACTIVE SESSIONS", "0",
                    "No data", "Connection issue", ACCENT_GOLD);
            VBox incidentsCard = createStatusCard("⚠ INCIDENTS", "Unknown",
                    "Unable to load", "Check incidents collection", DANGER_RED);

            grid.add(dbCard, 0, 0);
            grid.add(healthCard, 1, 0);
            grid.add(sessionsCard, 0, 1);
            grid.add(incidentsCard, 1, 1);
        }

        section.getChildren().addAll(sectionTitle, grid);
        return section;
    }

    private boolean checkDatabaseConnection() {
        try {
            // Tester la connexion Firestore
            return FirebaseService.checkUserExists("admin1");
        } catch (Exception e) {
            return false;
        }
    }

    private VBox createRealAlertsSection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(20, 0, 20, 0));

        HBox header = new HBox();
        Label sectionTitle = new Label("⚠ CRITICAL ALERTS");
        sectionTitle.setStyle("-fx-text-fill: " + DANGER_RED + "; -fx-font-size: 18; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button viewAllBtn = new Button("View All →");
        viewAllBtn.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: " + ACCENT_GOLD + ";" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 12;" +
                        "-fx-cursor: hand;" +
                        "-fx-border-width: 0;"
        );
        viewAllBtn.setOnAction(e -> showSecurityAudit());

        header.getChildren().addAll(sectionTitle, spacer, viewAllBtn);

        // Alert cards - Récupérer les vraies alertes
        VBox alertsContainer = new VBox(10);
        alertsContainer.setPadding(new Insets(15, 0, 0, 0));

        try {
            // Charger les incidents en attente
            ObservableList<Incident> pendingIncidents = FirebaseService.loadAllIncidents()
                    .filtered(incident -> "pending".equals(incident.getStatus()));

            // Charger les logs de sécurité récents
            ObservableList<SystemAuditService.SecurityLog> securityLogs =
                    SystemAuditService.getAllSecurityLogs()
                            .filtered(log -> "HIGH".equals(log.getSeverity()));

            int alertCount = 0;

            // Ajouter les incidents comme alertes
            for (Incident incident : pendingIncidents) {
                if (alertCount >= 3) break;

                HBox alertCard = createRealAlertCard("HIGH",
                        "Pending Incident: " + incident.getType(),
                        incident.getDescription() + " | Driver: " + incident.getDriverId(),
                        DANGER_RED);
                alertsContainer.getChildren().add(alertCard);
                alertCount++;
            }

            // Ajouter les logs de sécurité comme alertes
            for (SystemAuditService.SecurityLog log : securityLogs) {
                if (alertCount >= 3) break;

                HBox alertCard = createRealAlertCard(log.getSeverity(),
                        "Security Event: " + log.getAction(),
                        log.getDetails() + " | User: " + log.getUserId(),
                        "HIGH".equals(log.getSeverity()) ? DANGER_RED : WARNING_ORANGE);
                alertsContainer.getChildren().add(alertCard);
                alertCount++;
            }

            // Si pas d'alertes, afficher un message
            if (alertCount == 0) {
                HBox noAlertsCard = createAlertCard("INFO", "No Critical Alerts",
                        "All systems are operating normally", SUCCESS_GREEN);
                alertsContainer.getChildren().add(noAlertsCard);
            }

        } catch (Exception e) {
            System.err.println("❌ Error loading alerts: " + e.getMessage());
            HBox errorCard = createAlertCard("ERROR", "Unable to Load Alerts",
                    "Error: " + e.getMessage(), DANGER_RED);
            alertsContainer.getChildren().add(errorCard);
        }

        section.getChildren().addAll(header, alertsContainer);
        return section;
    }

    private HBox createRealAlertCard(String priority, String title, String description, String color) {
        HBox card = new HBox(15);
        card.setPadding(new Insets(15));
        card.setStyle(
                "-fx-background-color: " + CARD_BG + ";" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: " + color + ";" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 10;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0.2, 0, 1);"
        );
        card.setAlignment(Pos.CENTER_LEFT);

        // Priority badge
        Label priorityBadge = new Label(priority);
        priorityBadge.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 3 10;" +
                        "-fx-background-radius: 10;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 10;"
        );

        VBox alertInfo = new VBox(5);
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: " + TEXT_DARK + "; -fx-font-weight: bold; -fx-font-size: 13;");

        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 11;");
        descLabel.setWrapText(true);

        alertInfo.getChildren().addAll(titleLabel, descLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button actionBtn = new Button("Take Action");
        actionBtn.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 5 15;" +
                        "-fx-background-radius: 6;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 11;" +
                        "-fx-cursor: hand;"
        );
        actionBtn.setOnAction(e -> showAlertActionDialog(title));

        card.getChildren().addAll(priorityBadge, alertInfo, spacer, actionBtn);
        return card;
    }

    private VBox createRealDataGrid() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(20, 0, 0, 0));

        Label sectionTitle = new Label("📊 REAL-TIME DATA OVERVIEW");
        sectionTitle.setStyle("-fx-text-fill: " + PRIMARY_BLUE + "; -fx-font-size: 18; -fx-font-weight: bold;");

        // Create a grid of mini dashboards
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setPadding(new Insets(15, 0, 0, 0));

        try {
            // Récupérer les dernières données
            Map<String, Object> liveStats = FirebaseService.getLiveSystemStats();
            Map<String, Long> dashboardStats = FirebaseService.getSafeDashboardStats();

            // Recent Users Activity
            VBox usersActivity = createRealMiniDashboard("👥 USER ACTIVITY",
                    dashboardStats.getOrDefault("totalUsers", 0L) + " users total",
                    dashboardStats.getOrDefault("activeUsers", 0L) + " active now",
                    "#3498db",
                    (double) dashboardStats.getOrDefault("activeUsers", 0L) /
                            Math.max(dashboardStats.getOrDefault("totalUsers", 1L), 1));

            // System Performance
            int cpuUsage = (int) liveStats.getOrDefault("cpuUsage", 0);
            int memoryUsage = (int) liveStats.getOrDefault("memoryUsage", 0);
            VBox performance = createRealMiniDashboard("⚡ PERFORMANCE",
                    "CPU: " + cpuUsage + "% | RAM: " + memoryUsage + "%",
                    "System load: " + (cpuUsage > 80 ? "High" : "Normal"),
                    "#2ecc71",
                    (cpuUsage + memoryUsage) / 200.0);

            // Security Events (24h)
            Map<String, Long> securityStats = SystemAuditService.getSecurityStats();
            VBox securityEvents = createRealMiniDashboard("🛡️ SECURITY EVENTS",
                    securityStats.getOrDefault("totalEvents", 0L) + " total events",
                    securityStats.getOrDefault("failedLogins24h", 0L) + " failed logins (24h)",
                    "#e74c3c",
                    Math.min(securityStats.getOrDefault("failedLogins24h", 0L) / 10.0, 1.0));

            // Database Operations
            long totalCoaches = dashboardStats.getOrDefault("totalCoaches", 0L);
            long activeCoaches = dashboardStats.getOrDefault("activeCoaches", 0L);
            VBox dbOps = createRealMiniDashboard("🗄 SYSTEM USAGE",
                    totalCoaches + " coaches total",
                    activeCoaches + " currently active",
                    "#9b59b6",
                    (double) activeCoaches / Math.max(totalCoaches, 1));

            grid.add(usersActivity, 0, 0);
            grid.add(performance, 1, 0);
            grid.add(securityEvents, 0, 1);
            grid.add(dbOps, 1, 1);

        } catch (Exception e) {
            System.err.println("❌ Error loading real-time data: " + e.getMessage());
            // Dashboards par défaut en cas d'erreur
            VBox usersActivity = createRealMiniDashboard("👥 USER ACTIVITY",
                    "Data unavailable", "Connection error", "#3498db", 0.0);
            VBox performance = createRealMiniDashboard("⚡ PERFORMANCE",
                    "Data unavailable", "Connection error", "#2ecc71", 0.0);
            VBox securityEvents = createRealMiniDashboard("🛡️ SECURITY EVENTS",
                    "Data unavailable", "Connection error", "#e74c3c", 0.0);
            VBox dbOps = createRealMiniDashboard("🗄 SYSTEM USAGE",
                    "Data unavailable", "Connection error", "#9b59b6", 0.0);

            grid.add(usersActivity, 0, 0);
            grid.add(performance, 1, 0);
            grid.add(securityEvents, 0, 1);
            grid.add(dbOps, 1, 1);
        }

        section.getChildren().addAll(sectionTitle, grid);
        return section;
    }

    private VBox createRealMiniDashboard(String title, String stat, String details,
                                         String color, double progress) {
        VBox card = new VBox(12);
        card.setPrefWidth(280);
        card.setPadding(new Insets(20));
        card.setStyle(
                "-fx-background-color: " + CARD_BG + ";" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: " + color + "20;" + // 20 = 12% opacity
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 12;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0.2, 0, 2);"
        );

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 14; -fx-font-weight: bold;");

        Label statLabel = new Label(stat);
        statLabel.setStyle("-fx-text-fill: " + TEXT_DARK + "; -fx-font-size: 18; -fx-font-weight: bold;");

        Label detailsLabel = new Label(details);
        detailsLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12;");
        detailsLabel.setWrapText(true);

        ProgressBar progressBar = new ProgressBar(progress);
        progressBar.setPrefWidth(240);
        progressBar.setStyle("-fx-accent: " + color + ";");

        HBox progressInfo = new HBox();
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        int progressPercent = (int) (progress * 100);
        Label progressLabel = new Label(progressPercent + "%");
        progressLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 11; -fx-font-weight: bold;");

        progressInfo.getChildren().addAll(spacer, progressLabel);

        card.getChildren().addAll(titleLabel, statLabel, detailsLabel, progressBar, progressInfo);
        return card;
    }


    private HBox createQuickStats() {
        HBox statsBox = new HBox(20);

        // Total Users
        VBox usersStat = createStatCard("👥", "TOTAL USERS", "142", "+5 this week", PRIMARY_BLUE);
        // Active Sessions
        VBox sessionsStat = createStatCard("🔗", "ACTIVE SESSIONS", "24", "3 managers, 21 drivers", ACCENT_GOLD);
        // System Health
        VBox healthStat = createStatCard("❤", "SYSTEM HEALTH", systemHealth + "%", "Optimal", SUCCESS_GREEN);
        // Pending Actions
        VBox pendingStat = createStatCard("⏱", "PENDING ACTIONS", "8", "Requires attention", WARNING_ORANGE);

        statsBox.getChildren().addAll(usersStat, sessionsStat, healthStat, pendingStat);
        return statsBox;
    }

    private VBox createStatCard(String icon, String title, String value, String description, String color) {
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

        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 11;");

        card.getChildren().addAll(header, valueLabel, descLabel);
        return card;
    }

    private VBox createSystemStatusCards() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(20, 0, 20, 0));

        Label sectionTitle = new Label("⚙ SYSTEM STATUS");
        sectionTitle.setStyle("-fx-text-fill: " + PRIMARY_BLUE + "; -fx-font-size: 18; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setPadding(new Insets(15, 0, 0, 0));

        // Database Status
        VBox dbCard = createStatusCard("🗄 DATABASE", "Firestore", "Connected", "Latency: 45ms", SUCCESS_GREEN);
        // API Status
        VBox apiCard = createStatusCard("🔗 API SERVICES", "All Systems", "Operational", "Uptime: 99.9%", SUCCESS_GREEN);
        // Backup Status
        VBox backupCard = createStatusCard("💾 BACKUP", "Last Backup", "2 hours ago", "Auto: Enabled", SUCCESS_GREEN);
        // Security Status
        VBox securityCard = createStatusCard("🛡 SECURITY", "Firewall", "Active", "No threats", SUCCESS_GREEN);

        grid.add(dbCard, 0, 0);
        grid.add(apiCard, 1, 0);
        grid.add(backupCard, 0, 1);
        grid.add(securityCard, 1, 1);

        section.getChildren().addAll(sectionTitle, grid);
        return section;
    }

    private VBox createStatusCard(String title, String system, String status, String details, String statusColor) {
        VBox card = new VBox(15);
        card.setPrefWidth(280);
        card.setPadding(new Insets(20));
        card.setStyle(
                "-fx-background-color: " + CARD_BG + ";" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 12;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0.2, 0, 2);"
        );

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: " + PRIMARY_BLUE + "; -fx-font-size: 14; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Status indicator
        Circle statusIndicator = new Circle(6);
        statusIndicator.setFill(Color.web(statusColor));

        Label statusLabel = new Label(status);
        statusLabel.setStyle("-fx-text-fill: " + statusColor + "; -fx-font-size: 12; -fx-font-weight: bold;");

        header.getChildren().addAll(titleLabel, spacer, statusIndicator, statusLabel);

        Label systemLabel = new Label(system);
        systemLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12;");

        Label detailsLabel = new Label(details);
        detailsLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 11;");

        card.getChildren().addAll(header, systemLabel, detailsLabel);
        return card;
    }

    private VBox createAlertsSection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(20, 0, 20, 0));

        HBox header = new HBox();
        Label sectionTitle = new Label("⚠ CRITICAL ALERTS");
        sectionTitle.setStyle("-fx-text-fill: " + DANGER_RED + "; -fx-font-size: 18; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button viewAllBtn = new Button("View All →");
        viewAllBtn.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: " + ACCENT_GOLD + ";" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 12;" +
                        "-fx-cursor: hand;" +
                        "-fx-border-width: 0;"
        );
        viewAllBtn.setOnAction(e -> showSecurityAudit());

        header.getChildren().addAll(sectionTitle, spacer, viewAllBtn);

        // Alert cards
        VBox alertsContainer = new VBox(10);
        alertsContainer.setPadding(new Insets(15, 0, 0, 0));

        // Sample alerts
        HBox alert1 = createAlertCard("HIGH", "Multiple failed login attempts", "User: manager3 - 5 attempts in 2 min", DANGER_RED);
        HBox alert2 = createAlertCard("MEDIUM", "Database backup overdue", "Last backup: 48 hours ago", WARNING_ORANGE);
        HBox alert3 = createAlertCard("LOW", "System update available", "Version 2.1.3 ready for install", ACCENT_GOLD);

        alertsContainer.getChildren().addAll(alert1, alert2, alert3);

        section.getChildren().addAll(header, alertsContainer);
        return section;
    }

    private HBox createAlertCard(String priority, String title, String description, String color) {
        HBox card = new HBox(15);
        card.setPadding(new Insets(15));
        card.setStyle(
                "-fx-background-color: " + CARD_BG + ";" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: " + color + ";" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 10;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0.2, 0, 1);"
        );
        card.setAlignment(Pos.CENTER_LEFT);

        // Priority badge
        Label priorityBadge = new Label(priority);
        priorityBadge.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 3 10;" +
                        "-fx-background-radius: 10;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 10;"
        );

        VBox alertInfo = new VBox(5);
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: " + TEXT_DARK + "; -fx-font-weight: bold; -fx-font-size: 13;");

        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 11;");

        alertInfo.getChildren().addAll(titleLabel, descLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button actionBtn = new Button("Take Action");
        actionBtn.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 5 15;" +
                        "-fx-background-radius: 6;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 11;" +
                        "-fx-cursor: hand;"
        );
        actionBtn.setOnAction(e -> showAlertActionDialog(title));

        card.getChildren().addAll(priorityBadge, alertInfo, spacer, actionBtn);
        return card;
    }

    private VBox createDataGrid() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(20, 0, 0, 0));

        Label sectionTitle = new Label("📊 REAL-TIME DATA OVERVIEW");
        sectionTitle.setStyle("-fx-text-fill: " + PRIMARY_BLUE + "; -fx-font-size: 18; -fx-font-weight: bold;");

        // Create a grid of mini dashboards
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setPadding(new Insets(15, 0, 0, 0));

        // Recent Users Activity
        VBox usersActivity = createMiniDashboard("👥 RECENT USERS", "5 new users today",
                "manager4, driver23, driver24, driver25, driver26", "#3498db");

        // System Performance
        VBox performance = createMiniDashboard("⚡ PERFORMANCE", "CPU: 45% | RAM: 68%",
                "All systems operating normally", "#2ecc71");

        // Security Events
        VBox securityEvents = createMiniDashboard("🛡 SECURITY EVENTS", "3 events last hour",
                "2 logins, 1 permission change", "#e74c3c");

        // Database Operations
        VBox dbOps = createMiniDashboard("🗄 DB OPERATIONS", "1,245 queries/min",
                "Read: 890, Write: 355", "#9b59b6");

        grid.add(usersActivity, 0, 0);
        grid.add(performance, 1, 0);
        grid.add(securityEvents, 0, 1);
        grid.add(dbOps, 1, 1);

        section.getChildren().addAll(sectionTitle, grid);
        return section;
    }

    private VBox createMiniDashboard(String title, String stat, String details, String color) {
        VBox card = new VBox(12);
        card.setPrefWidth(280);
        card.setPadding(new Insets(20));
        card.setStyle(
                "-fx-background-color: " + CARD_BG + ";" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: " + color + "20;" + // 20 = 12% opacity
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 12;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0.2, 0, 2);"
        );

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 14; -fx-font-weight: bold;");

        Label statLabel = new Label(stat);
        statLabel.setStyle("-fx-text-fill: " + TEXT_DARK + "; -fx-font-size: 18; -fx-font-weight: bold;");

        Label detailsLabel = new Label(details);
        detailsLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12;");
        detailsLabel.setWrapText(true);

        ProgressBar progressBar = new ProgressBar(0.65);
        progressBar.setPrefWidth(240);
        progressBar.setStyle("-fx-accent: " + color + ";");

        HBox progressInfo = new HBox();
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label progressLabel = new Label("65%");
        progressLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 11; -fx-font-weight: bold;");

        progressInfo.getChildren().addAll(spacer, progressLabel);

        card.getChildren().addAll(titleLabel, statLabel, detailsLabel, progressBar, progressInfo);
        return card;
    }

    // ==============================
    // OTHER ADMIN VIEWS (Skeletons for now)
    // ==============================

    private void showUserManagement() {
        contentArea.getChildren().clear();

        VBox userView = new VBox(20);
        userView.setAlignment(Pos.TOP_LEFT);
        userView.setPadding(new Insets(20));
        userView.setStyle("-fx-background-color: #f5f7fa;");

        // Créer la vue de gestion des utilisateurs
        UserManagementView userManagementView = new UserManagementView();
        VBox managementView = userManagementView.createView();

        userView.getChildren().add(managementView);

        ScrollPane scrollPane = new ScrollPane(userView);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        contentArea.getChildren().add(scrollPane);
    }

    private void showSystemControl() {
        contentArea.getChildren().clear();

        try {
            System.out.println("🔄 Loading System Control view...");

            // Créer la vue System Control
            SystemControlView systemControlView = new SystemControlView();
            VBox systemView = systemControlView.createView();

            // Ajouter un style de fond pour tester
            systemView.setStyle("-fx-background-color: #f5f7fa;");

            ScrollPane scrollPane = new ScrollPane(systemView);
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
            contentArea.getChildren().add(scrollPane);

            System.out.println("✅ System Control view loaded successfully");

        } catch (Exception e) {
            System.err.println("❌ ERROR loading System Control: " + e.getMessage());
            e.printStackTrace();

            // Fallback en cas d'erreur
            Label errorLabel = new Label("⚠ System Control\n\n" + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 16; -fx-alignment: center;");
            contentArea.getChildren().add(errorLabel);
        }
    }

    private void showRealTimeMonitor() {
        contentArea.getChildren().clear();

        try {
            System.out.println("🔄 Loading Real-Time Monitor view...");

            // Créer la vue RealTimeMonitor
            RealTimeMonitorView monitorView = new RealTimeMonitorView();
            VBox monitorViewContent = monitorView.createView();

            ScrollPane scrollPane = new ScrollPane(monitorViewContent);
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
            contentArea.getChildren().add(scrollPane);

            System.out.println("✅ Real-Time Monitor view loaded successfully");

        } catch (Exception e) {
            System.err.println("❌ ERROR loading Real-Time Monitor: " + e.getMessage());
            e.printStackTrace();

            // Fallback
            Label errorLabel = new Label("⚠ Real-Time Monitor\n\n" + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 16; -fx-alignment: center;");
            contentArea.getChildren().add(errorLabel);
        }
    }
    private void setupRealtimeSync() {
        // 1. Écouter les modifications des managers
        FirebaseService.listenToManagerChanges(change -> {
            javafx.application.Platform.runLater(() -> {
                String changeType = (String) change.get("changeType");
                String username = (String) change.get("username");
                String action = "made changes";

                if (change.containsKey("fullname")) {
                    action = "updated profile";
                }

                // Afficher notification dans l'interface Admin
                showSyncNotification("👔 Manager Activity",
                        username + " " + action,
                        "manager_update");
            });
        });

        // 2. Écouter les logins des utilisateurs
        FirebaseService.listenToUserLogins(login -> {
            javafx.application.Platform.runLater(() -> {
                String userId = (String) login.get("userId");
                String role = (String) login.get("role");
                String time = (String) login.get("timestamp");

                showSyncNotification("🔐 User Signed In",
                        userId + " (" + role + ") logged in at " + time,
                        "user_login");

                // Mettre à jour les statistiques de login
                updateLoginStats(userId);
            });
        });

        // 3. Écouter les assignations aux drivers
        FirebaseService.listenToDriverAssignments(assignment -> {
            javafx.application.Platform.runLater(() -> {
                String driverName = (String) assignment.get("driverName");
                String lineId = (String) assignment.get("assignedLine");

                showSyncNotification("🛣️ Line Assigned",
                        driverName + " assigned to line " + lineId + " by manager",
                        "line_assignment");
            });
        });

        // 4. Vérifier les tentatives de login (toutes les 30 secondes)
        Timeline loginCheck = new Timeline(
                new KeyFrame(Duration.seconds(30), e -> {
                    checkFailedLoginAttempts();
                })
        );
        loginCheck.setCycleCount(Timeline.INDEFINITE);
        loginCheck.play();
    }

    private void showSyncNotification(String title, String message, String type) {
        // Créer une notification discrète dans l'interface
        Label notification = new Label("🔔 " + title + ": " + message);
        notification.setStyle(
                "-fx-background-color: #e3f2fd;" +
                        "-fx-border-color: #2196f3;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 10;" +
                        "-fx-margin: 5;" +
                        "-fx-font-size: 12;"
        );

        // Ajouter temporairement à l'interface
        if (contentArea.getChildren().size() > 0) {
            VBox currentView = (VBox) contentArea.getChildren().get(0);
            currentView.getChildren().add(0, notification);

            // Supprimer après 10 secondes
            PauseTransition pause = new PauseTransition(Duration.seconds(10));
            pause.setOnFinished(event -> currentView.getChildren().remove(notification));
            pause.play();
        }
    }

    private void checkFailedLoginAttempts() {
        try {
            // Vérifier les tentatives récentes pour tous les utilisateurs
            ObservableList<Map<String, Object>> allUsers = FirebaseService.getAllUsersWithDetails();

            for (Map<String, Object> user : allUsers) {
                String username = (String) user.get("username");
                Map<String, Object> loginStats = FirebaseService.getLoginAttempts(username);

                Long failedAttempts = (Long) loginStats.get("failedAttempts24h");
                if (failedAttempts > 3) {
                    // Alert pour plusieurs tentatives échouées
                    showSyncNotification("⚠ Failed Logins",
                            username + " has " + failedAttempts + " failed login attempts (24h)",
                            "login_alert");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateLoginStats(String userId) {
        try {
            Map<String, Object> loginStats = FirebaseService.getLoginAttempts(userId);

            // Mettre à jour l'affichage dans la section monitoring
            // (à adapter selon votre interface)
            System.out.println("📊 Login stats for " + userId + ": " + loginStats);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void showAnalyticsCenter() {
        contentArea.getChildren().clear();

        try {
            System.out.println("🔄 Loading Analytics Center view...");

            // Créer la vue Analytics Center
            AnalyticsCenterView analyticsView = new AnalyticsCenterView();
            VBox analyticsViewContent = analyticsView.createView();

            ScrollPane scrollPane = new ScrollPane(analyticsViewContent);
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
            contentArea.getChildren().add(scrollPane);

            System.out.println("✅ Analytics Center view loaded successfully");

        } catch (Exception e) {
            System.err.println("❌ ERROR loading Analytics Center: " + e.getMessage());
            e.printStackTrace();

            // Fallback
            Label errorLabel = new Label("⚠ Analytics Center\n\n" + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 16; -fx-alignment: center;");
            contentArea.getChildren().add(errorLabel);
        }
    }

    private void showSecurityAudit() {
        contentArea.getChildren().clear();

        try {
            System.out.println("🔄 Loading Security & Audit view...");

            // Créer la vue Security & Audit
            SecurityAuditView securityAuditView = new SecurityAuditView();
            VBox securityViewContent = securityAuditView.createView();

            ScrollPane scrollPane = new ScrollPane(securityViewContent);
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
            contentArea.getChildren().add(scrollPane);

            System.out.println("✅ Security & Audit view loaded successfully");

        } catch (Exception e) {
            System.err.println("❌ ERROR loading Security & Audit: " + e.getMessage());
            e.printStackTrace();

            // Fallback
            Label errorLabel = new Label("⚠ Security & Audit\n\n" + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 16; -fx-alignment: center;");
            contentArea.getChildren().add(errorLabel);
        }
    }

    private void showBackupRestore() {
        contentArea.getChildren().clear();

        try {
            System.out.println("🔄 Loading Backup & Restore view...");

            // Créer la vue Backup & Restore
            BackupRestoreView backupRestoreView = new BackupRestoreView();
            VBox backupViewContent = backupRestoreView.createView();

            ScrollPane scrollPane = new ScrollPane(backupViewContent);
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
            contentArea.getChildren().add(scrollPane);

            System.out.println("✅ Backup & Restore view loaded successfully");

        } catch (Exception e) {
            System.err.println("❌ ERROR loading Backup & Restore: " + e.getMessage());
            e.printStackTrace();

            // Fallback
            Label errorLabel = new Label("⚠ Backup & Restore\n\n" + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 16; -fx-alignment: center;");
            contentArea.getChildren().add(errorLabel);
        }
    }


    private void showSystemSettings() {
        contentArea.getChildren().clear();

        try {
            System.out.println("🔄 Loading System Settings view...");

            // Créer la vue System Settings
            SystemSettingsView settingsView = new SystemSettingsView();
            VBox settingsViewContent = settingsView.createView();

            ScrollPane scrollPane = new ScrollPane(settingsViewContent);
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
            contentArea.getChildren().add(scrollPane);

            System.out.println("✅ System Settings view loaded successfully");

        } catch (Exception e) {
            System.err.println("❌ ERROR loading System Settings: " + e.getMessage());
            e.printStackTrace();

            // Fallback
            Label errorLabel = new Label("⚠ System Settings\n\n" + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 16; -fx-alignment: center;");
            contentArea.getChildren().add(errorLabel);
        }
    }

    private VBox createPlaceholderView(String title, String features) {
        VBox placeholder = new VBox(30);
        placeholder.setAlignment(Pos.CENTER);
        placeholder.setPadding(new Insets(50));
        placeholder.setStyle(
                "-fx-background-color: " + CARD_BG + ";" +
                        "-fx-background-radius: 20;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 20;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 20, 0.3, 0, 5);"
        );

        Text icon = new Text(title.split(" ")[0]);
        icon.setFont(Font.font(60));

        VBox content = new VBox(15);
        content.setAlignment(Pos.CENTER);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: " + PRIMARY_BLUE + "; -fx-font-size: 24; -fx-font-weight: bold;");

        TextArea featuresArea = new TextArea(features);
        featuresArea.setEditable(false);
        featuresArea.setWrapText(true);
        featuresArea.setPrefRowCount(6);
        featuresArea.setPrefWidth(500);
        featuresArea.setStyle(
                "-fx-background-color: #f8f9fa;" +
                        "-fx-border-color: #dee2e6;" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;" +
                        "-fx-font-size: 14;" +
                        "-fx-text-fill: " + TEXT_DARK + ";"
        );

        Label comingSoon = new Label("🚀 Coming Soon - Advanced Features");
        comingSoon.setStyle("-fx-text-fill: " + ACCENT_GOLD + "; -fx-font-size: 16; -fx-font-weight: bold;");

        content.getChildren().addAll(titleLabel, featuresArea, comingSoon);
        placeholder.getChildren().addAll(icon, content);

        return placeholder;
    }

    // ==============================
    // HELPER METHODS
    // ==============================

    private void toggleMaintenanceMode() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Maintenance Mode");
        alert.setHeaderText("Toggle Maintenance Mode");
        alert.setContentText("This will temporarily disable the application for all users except administrators.\n\n" +
                "Are you sure you want to proceed?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                showSuccessAlert("Maintenance Mode",
                        "Maintenance mode has been activated.\n" +
                                "All non-admin users will be logged out temporarily.");
            }
        });
    }

    private void showAlertActionDialog(String alertTitle) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Take Action");
        dialog.setHeaderText("Resolve Alert: " + alertTitle);

        ButtonType resolveButton = new ButtonType("Mark as Resolved", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(resolveButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextArea actionField = new TextArea();
        actionField.setPromptText("Describe the action taken to resolve this alert...");
        actionField.setPrefRowCount(4);
        actionField.setPrefWidth(300);

        grid.add(new Label("Resolution Notes:"), 0, 0);
        grid.add(actionField, 1, 0);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == resolveButton) {
                showSuccessAlert("Alert Resolved", "The alert has been marked as resolved.");
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setHeaderText("Confirm Logout");
        alert.setContentText("Are you sure you want to logout from the Admin Control Panel?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                System.out.println("Admin logged out");
                Stage stage = (Stage) mainContainer.getScene().getWindow();
                stage.close();
            }
        });
    }

    private void loadSampleData() {
        // Sample system logs
        systemLogsList.add(new SystemLog("LOG-001", "2024-01-15 10:30:00", "admin",
                "user_created", "Created new manager account: manager4", "INFO"));
        systemLogsList.add(new SystemLog("LOG-002", "2024-01-15 10:25:00", "manager3",
                "incident_resolved", "Resolved incident INC-005", "INFO"));
        systemLogsList.add(new SystemLog("LOG-003", "2024-01-15 10:20:00", "system",
                "backup_completed", "Automated daily backup completed", "INFO"));
        systemLogsList.add(new SystemLog("LOG-004", "2024-01-15 10:15:00", "admin",
                "config_updated", "Updated system timeout settings", "INFO"));
        systemLogsList.add(new SystemLog("LOG-005", "2024-01-15 10:10:00", "driver25",
                "login_failed", "5 failed login attempts detected", "WARNING"));

        // Sample backup records
        backupsList.add(new BackupRecord("BKP-2024-01-15", "2024-01-15 00:00:00",
                "15.2 MB", "Completed", "admin"));
        backupsList.add(new BackupRecord("BKP-2024-01-14", "2024-01-14 00:00:00",
                "14.8 MB", "Completed", "auto"));
        backupsList.add(new BackupRecord("BKP-2024-01-13", "2024-01-13 00:00:00",
                "14.5 MB", "Completed", "auto"));

        // Sample users
        usersList.add(new User("admin", "Super Admin", "admin@translink.com",
                "admin", "+1234567890", "2023-12-01", true));
        usersList.add(new User("manager1", "Sarah Manager", "sarah@translink.com",
                "manager", "+1112223333", "2024-01-01", true));
        usersList.add(new User("manager2", "John Manager", "john@translink.com",
                "manager", "+4445556666", "2024-01-05", true));
        usersList.add(new User("driver1", "James Wilson", "james@translink.com",
                "driver", "+7778889999", "2024-01-10", true));
        usersList.add(new User("driver2", "Robert Brown", "robert@translink.com",
                "driver", "+1231231234", "2024-01-12", true));
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

    // ==============================
    // DATA MODEL CLASSES
    // ==============================

    public static class User {
        private String username;
        private String fullName;
        private String email;
        private String role;
        private String phone;
        private String createdDate;
        private boolean active;

        public User(String username, String fullName, String email, String role,
                    String phone, String createdDate, boolean active) {
            this.username = username;
            this.fullName = fullName;
            this.email = email;
            this.role = role;
            this.phone = phone;
            this.createdDate = createdDate;
            this.active = active;
        }

        // Getters
        public String getUsername() { return username; }
        public String getFullName() { return fullName; }
        public String getEmail() { return email; }
        public String getRole() { return role; }
        public String getPhone() { return phone; }
        public String getCreatedDate() { return createdDate; }
        public boolean isActive() { return active; }

        // Setters
        public void setUsername(String username) { this.username = username; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public void setEmail(String email) { this.email = email; }
        public void setRole(String role) { this.role = role; }
        public void setPhone(String phone) { this.phone = phone; }
        public void setActive(boolean active) { this.active = active; }
    }

    public static class SystemLog {
        private String logId;
        private String timestamp;
        private String user;
        private String action;
        private String details;
        private String severity;

        public SystemLog(String logId, String timestamp, String user, String action,
                         String details, String severity) {
            this.logId = logId;
            this.timestamp = timestamp;
            this.user = user;
            this.action = action;
            this.details = details;
            this.severity = severity;
        }

        // Getters
        public String getLogId() { return logId; }
        public String getTimestamp() { return timestamp; }
        public String getUser() { return user; }
        public String getAction() { return action; }
        public String getDetails() { return details; }
        public String getSeverity() { return severity; }
    }

    public static class BackupRecord {
        private String backupId;
        private String timestamp;
        private String size;
        private String status;
        private String createdBy;

        public BackupRecord(String backupId, String timestamp, String size,
                            String status, String createdBy) {
            this.backupId = backupId;
            this.timestamp = timestamp;
            this.size = size;
            this.status = status;
            this.createdBy = createdBy;
        }
        // Getters
        public String getBackupId() { return backupId; }
        public String getTimestamp() { return timestamp; }
        public String getSize() { return size; }
        public String getStatus() { return status; }
        public String getCreatedBy() { return createdBy; }
    }

    public void setAdminInfo(String username, String fullName) {
        this.currentUser = username;
        this.fullName = fullName;

        // Mettre à jour l'interface si nécessaire
        System.out.println("Admin info set: " + username + " - " + fullName);
    }
    public static void main(String[] args) {
        launch(args);
    }
}