package com.sample.demo3.controller;

import com.sample.demo3.models.Coach;
import com.sample.demo3.models.TransportLine;
import com.sample.demo3.models.Agency;
import com.sample.demo3.models.Driver;
import com.sample.demo3.models.Reclamation;
import com.sample.demo3.configuration.FirebaseService;
import com.sample.demo3.models.Incident;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.paint.Color;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.scene.shape.Circle;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Hyperlink;  // FIXED: lowercase 'l'
import javafx.scene.chart.PieChart;     // FIXED: javafx, not javax
import javafx.scene.chart.BarChart;     // FIXED: javafx, not javax
import javafx.scene.chart.CategoryAxis; // FIXED: javafx, not javax
import javafx.scene.chart.NumberAxis;   // FIXED: javafx, not javax
import javafx.scene.chart.XYChart;
import javafx.scene.control.Separator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Hyperlink;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.List;
import java.util.ArrayList;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;


public class ManagerController implements Initializable {

    @FXML private BorderPane mainContainer;
    @FXML private VBox sidebarMenu;
    @FXML private StackPane contentArea;
    @FXML private Text userNameText;
    @FXML private Text userRoleText;
    private ScheduledExecutorService incidentScheduler;


    // TableViews
    @FXML private TableView<Coach> coachesTable;
    @FXML private TableView<TransportLine> linesTable;
    @FXML private TableView<Agency> agenciesTable;
    @FXML private TableView<Driver> driversTable;
    @FXML private TableView<Reclamation> reclamationsTable;
    @FXML private TableView<Incident> incidentsTable;

    // Données
    private ObservableList<Coach> coachesList = FXCollections.observableArrayList();
    private ObservableList<TransportLine> linesList = FXCollections.observableArrayList();
    private ObservableList<Agency> agenciesList = FXCollections.observableArrayList();
    private ObservableList<Driver> driversList = FXCollections.observableArrayList();
    private ObservableList<Reclamation> reclamationsList = FXCollections.observableArrayList();
    private ObservableList<Incident> incidentsList = FXCollections.observableArrayList();

    private String currentUser = "MANAGER";
    private String fullName = "FULL NAME of USERNAME";
    private String managerUsername = "";

    // Color scheme
    private final String PRIMARY_GREEN = "#5fa422";
    private final String DARK_GREEN = "#4d8a1c";
    private final String LIGHT_BG = "#f5f5f5";
    private final String WHITE = "#ffffff";
    private final String TEXT_DARK = "#333333";
    private final String BLUE_ACCENT = "#2196f3";
    private final String ORANGE_ACCENT = "#ff8c00";
    private final String RED_ACCENT = "#f44336";
    private final String PURPLE_ACCENT = "#9c27b0";

    // 3. MODIFIER initialize() POUR DÉMARRER L'AUTO-REFRESH
    // ==============================
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialiser les tables
        initializeTables();

        // Ajouter des données de test ou charger depuis Firebase
        loadInitialData();

        // Afficher la vue par défaut
        showDashboardView();

        // Configurer les boutons de la sidebar
        setupSidebarActions();

        // ← NOUVEAU: Démarrer l'auto-refresh des incidents
        setupIncidentAutoRefresh();
    }
    private void setupIncidentAutoRefresh() {
        incidentScheduler = Executors.newScheduledThreadPool(1);

        incidentScheduler.scheduleAtFixedRate(() -> {
            try {
                ObservableList<Incident> newIncidents = FirebaseService.loadAllIncidents();

                Platform.runLater(() -> {
                    int oldSize = incidentsList.size();
                    incidentsList.clear();
                    incidentsList.addAll(newIncidents);

                    if (incidentsTable != null) {
                        incidentsTable.refresh();
                    }

                    // Notification console si nouveaux incidents
                    if (newIncidents.size() > oldSize) {
                        System.out.println("🔔 " + (newIncidents.size() - oldSize) + " nouveaux incident(s) détecté(s)");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 10, TimeUnit.SECONDS); // Rafraîchit toutes les 10 secondes
    }


    // Méthode pour définir les informations du manager
    public void setManagerInfo(String username, String fullName) {
        this.managerUsername = username != null ? username : "MANAGER";
        this.currentUser = "MANAGER";
        this.fullName = fullName != null ? fullName : "FULL NAME of USERNAME";

        if (userNameText != null) {
            userNameText.setText(this.fullName);
        }
        if (userRoleText != null) {
            userRoleText.setText(this.currentUser);
        }

        // Mettre à jour le header
        updateHeader();
    }

    private void updateHeader() {
        HBox header = createHeader();
        mainContainer.setTop(header);
    }

    private HBox createHeader() {
        HBox header = new HBox();
        header.setPadding(new Insets(15, 30, 15, 30));
        header.setStyle("-fx-background-color: " + WHITE + "; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 2 0;");
        header.setAlignment(Pos.CENTER_LEFT);

        // Logo and title
        VBox logoBox = new VBox(2);
        Text titleText = new Text("Translink");
        titleText.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleText.setFill(Color.web(PRIMARY_GREEN));

        Text subtitleText = new Text("MANAGER PORTAL");
        subtitleText.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
        subtitleText.setFill(Color.web(TEXT_DARK));

        logoBox.getChildren().addAll(titleText, subtitleText);

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // User info section
        VBox userInfo = new VBox(2);
        userInfo.setAlignment(Pos.CENTER_RIGHT);

        Text userName = new Text(fullName);
        userName.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        userName.setFill(Color.web(TEXT_DARK));

        Text userRole = new Text("MANAGER - " + managerUsername);
        userRole.setFont(Font.font("Arial", FontWeight.NORMAL, 11));
        userRole.setFill(Color.web("#666666"));

        userInfo.getChildren().addAll(userName, userRole);

        // User avatar circle
        Circle avatar = new Circle(25);
        avatar.setFill(Color.web(PRIMARY_GREEN));
        avatar.setStroke(Color.web(DARK_GREEN));
        avatar.setStrokeWidth(2);

        // Initials in avatar
        Text initials = new Text(getInitials(fullName));
        initials.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        initials.setFill(Color.WHITE);
        StackPane avatarStack = new StackPane(avatar, initials);
        avatarStack.setAlignment(Pos.CENTER);

        HBox userSection = new HBox(15);
        userSection.setAlignment(Pos.CENTER_RIGHT);
        userSection.getChildren().addAll(userInfo, avatarStack);

        header.getChildren().addAll(logoBox, spacer, userSection);

        return header;
    }

    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "M";
        String[] parts = name.split(" ");
        if (parts.length >= 2) {
            return String.valueOf(parts[0].charAt(0)) + parts[1].charAt(0);
        }
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }

    private void setupSidebarActions() {
        // Boutons de la sidebar
        for (var node : sidebarMenu.getChildren()) {
            if (node instanceof Button) {
                Button btn = (Button) node;
                HBox content = (HBox) btn.getGraphic();
                if (content != null && content.getChildren().size() > 1) {
                    Text labelText = (Text) content.getChildren().get(1);
                    String buttonText = labelText.getText();

                    btn.setOnAction(e -> {
                        switch (buttonText) {
                            case "DASHBOARD":
                                showDashboardView();
                                break;
                            case "COACHES":
                                showCoachesView();
                                break;
                            case "LINES":
                                showLinesView();
                                break;
                            case "DRIVERS":
                                showDriversView();
                                break;
                            case "INCIDENTS":
                                showIncidentsView();
                                break;
                            case "RECLAMATIONS":
                                showReclamationsView();
                                break;
                            case "AGENCIES":
                                showAgenciesView();
                                break;
                            case "REPORTS":
                                showReportsView();
                                break;
                            case "GENERATE DATA":
                                generateSampleData();
                                break;
                            case "LOGOUT":
                                handleLogout();
                                break;
                        }
                        updateSidebarButtons(btn);
                    });
                }
            }
        }
    }

    private void updateSidebarButtons(Button activeButton) {
        for (var node : sidebarMenu.getChildren()) {
            if (node instanceof Button) {
                Button btn = (Button) node;
                HBox content = (HBox) btn.getGraphic();
                if (content != null && content.getChildren().size() > 1) {
                    Text iconText = (Text) content.getChildren().get(0);
                    Text labelText = (Text) content.getChildren().get(1);

                    if (btn == activeButton) {
                        btn.setStyle("-fx-background-color: " + WHITE + ";-fx-background-radius: 25;-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0.3, 0, 2);");
                        labelText.setFill(Color.web(TEXT_DARK));
                        iconText.setFill(Color.web(TEXT_DARK));
                    } else {
                        btn.setStyle("-fx-background-color: transparent;-fx-effect: none;");
                        labelText.setFill(Color.WHITE);
                        iconText.setFill(Color.WHITE);
                    }
                }
            }
        }
    }

    private void initializeTables() {
        // Configuration de la table Coaches
        if (coachesTable != null) {
            TableColumn<Coach, String> idCol = new TableColumn<>("ID");
            idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
            idCol.setPrefWidth(80);

            TableColumn<Coach, String> modelCol = new TableColumn<>("Model");
            modelCol.setCellValueFactory(new PropertyValueFactory<>("model"));
            modelCol.setPrefWidth(200);

            TableColumn<Coach, String> capacityCol = new TableColumn<>("Capacity");
            capacityCol.setCellValueFactory(new PropertyValueFactory<>("capacity"));
            capacityCol.setPrefWidth(100);

            TableColumn<Coach, String> licenseCol = new TableColumn<>("License Plate");
            licenseCol.setCellValueFactory(new PropertyValueFactory<>("licensePlate"));
            licenseCol.setPrefWidth(120);

            TableColumn<Coach, String> statusCol = new TableColumn<>("Status");
            statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
            statusCol.setPrefWidth(120);

            coachesTable.getColumns().addAll(idCol, modelCol, capacityCol, licenseCol, statusCol);
            coachesTable.setItems(coachesList);
        }

        // Configuration de la table Lines
        if (linesTable != null) {
            TableColumn<TransportLine, String> idCol = new TableColumn<>("Line ID");
            idCol.setCellValueFactory(new PropertyValueFactory<>("lineId"));
            idCol.setPrefWidth(100);

            TableColumn<TransportLine, String> routeCol = new TableColumn<>("Route");
            routeCol.setCellValueFactory(new PropertyValueFactory<>("route"));
            routeCol.setPrefWidth(250);

            TableColumn<TransportLine, String> scheduleCol = new TableColumn<>("Schedule");
            scheduleCol.setCellValueFactory(new PropertyValueFactory<>("schedule"));
            scheduleCol.setPrefWidth(150);

            TableColumn<TransportLine, String> priceCol = new TableColumn<>("Price");
            priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
            priceCol.setPrefWidth(100);

            TableColumn<TransportLine, String> stopsCol = new TableColumn<>("Stops");
            stopsCol.setCellValueFactory(new PropertyValueFactory<>("stops"));
            stopsCol.setPrefWidth(150);

            linesTable.getColumns().addAll(idCol, routeCol, scheduleCol, priceCol, stopsCol);
            linesTable.setItems(linesList);
        }

        // Configuration de la table Agencies
        if (agenciesTable != null) {
            TableColumn<Agency, String> idCol = new TableColumn<>("Agency ID");
            idCol.setCellValueFactory(new PropertyValueFactory<>("agencyId"));
            idCol.setPrefWidth(100);

            TableColumn<Agency, String> nameCol = new TableColumn<>("Name");
            nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
            nameCol.setPrefWidth(200);

            TableColumn<Agency, String> locationCol = new TableColumn<>("Location");
            locationCol.setCellValueFactory(new PropertyValueFactory<>("location"));
            locationCol.setPrefWidth(150);

            TableColumn<Agency, String> managerCol = new TableColumn<>("Manager");
            managerCol.setCellValueFactory(new PropertyValueFactory<>("manager"));
            managerCol.setPrefWidth(150);

            TableColumn<Agency, String> phoneCol = new TableColumn<>("Phone");
            phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
            phoneCol.setPrefWidth(120);

            agenciesTable.getColumns().addAll(idCol, nameCol, locationCol, managerCol, phoneCol);
            agenciesTable.setItems(agenciesList);
        }

        // Configuration de la table Drivers
        if (driversTable != null) {
            TableColumn<Driver, String> idCol = new TableColumn<>("Driver ID");
            idCol.setCellValueFactory(new PropertyValueFactory<>("driverId"));
            idCol.setPrefWidth(100);

            TableColumn<Driver, String> nameCol = new TableColumn<>("Name");
            nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
            nameCol.setPrefWidth(200);

            TableColumn<Driver, String> licenseCol = new TableColumn<>("License");
            licenseCol.setCellValueFactory(new PropertyValueFactory<>("license"));
            licenseCol.setPrefWidth(120);

            TableColumn<Driver, String> assignedLineCol = new TableColumn<>("Assigned Line");
            assignedLineCol.setCellValueFactory(new PropertyValueFactory<>("assignedLine"));
            assignedLineCol.setPrefWidth(120);

            TableColumn<Driver, String> phoneCol = new TableColumn<>("Phone");
            phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
            phoneCol.setPrefWidth(120);

            TableColumn<Driver, String> statusCol = new TableColumn<>("Status");
            statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
            statusCol.setPrefWidth(100);

            TableColumn<Driver, Double> salaryCol = new TableColumn<>("Salary");
            salaryCol.setCellValueFactory(new PropertyValueFactory<>("salary"));
            salaryCol.setPrefWidth(100);

            driversTable.getColumns().addAll(idCol, nameCol, licenseCol, assignedLineCol, phoneCol, statusCol, salaryCol);
            driversTable.setItems(driversList);
        }

        // Configuration de la table Reclamations
        if (reclamationsTable != null) {
            TableColumn<Reclamation, String> idCol = new TableColumn<>("ID");
            idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
            idCol.setPrefWidth(80);

            TableColumn<Reclamation, String> dateCol = new TableColumn<>("Date");
            dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
            dateCol.setPrefWidth(100);

            TableColumn<Reclamation, String> customerCol = new TableColumn<>("Customer");
            customerCol.setCellValueFactory(new PropertyValueFactory<>("customer"));
            customerCol.setPrefWidth(150);

            TableColumn<Reclamation, String> typeCol = new TableColumn<>("Type");
            typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
            typeCol.setPrefWidth(120);

            TableColumn<Reclamation, String> descriptionCol = new TableColumn<>("Description");
            descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
            descriptionCol.setPrefWidth(200);

            TableColumn<Reclamation, String> statusCol = new TableColumn<>("Status");
            statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
            statusCol.setPrefWidth(100);

            reclamationsTable.getColumns().addAll(idCol, dateCol, customerCol, typeCol, descriptionCol, statusCol);
            reclamationsTable.setItems(reclamationsList);
        }

        // Configuration de la table Incidents
        if (incidentsTable != null) {
            TableColumn<Incident, String> idCol = new TableColumn<>("ID");
            idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
            idCol.setPrefWidth(100);

            TableColumn<Incident, String> driverCol = new TableColumn<>("Driver ID");
            driverCol.setCellValueFactory(new PropertyValueFactory<>("driverId"));
            driverCol.setPrefWidth(100);

            TableColumn<Incident, String> typeCol = new TableColumn<>("Type");
            typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
            typeCol.setPrefWidth(120);

            TableColumn<Incident, String> descCol = new TableColumn<>("Description");
            descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
            descCol.setPrefWidth(300);

            TableColumn<Incident, String> dateCol = new TableColumn<>("Date");
            dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
            dateCol.setPrefWidth(150);

            TableColumn<Incident, String> statusCol = new TableColumn<>("Status");
            statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
            statusCol.setPrefWidth(100);

            TableColumn<Incident, String> assignedCol = new TableColumn<>("Assigned To");
            assignedCol.setCellValueFactory(new PropertyValueFactory<>("assignedTo"));
            assignedCol.setPrefWidth(120);

            incidentsTable.getColumns().addAll(idCol, driverCol, typeCol, descCol, dateCol, statusCol, assignedCol);
            incidentsTable.setItems(incidentsList);
        }
    }

    private void loadInitialData() {
        try {
            System.out.println("🔄 Chargement des données depuis Firestore...");

            coachesList = FirebaseService.loadAllCoaches();
            linesList = FirebaseService.loadAllLines();
            agenciesList = FirebaseService.loadAllAgencies();
            driversList = FirebaseService.loadAllDrivers();
            reclamationsList = FirebaseService.loadAllReclamations();
            incidentsList = FirebaseService.loadAllIncidents();

            System.out.println("✅ Données chargées:");
            System.out.println("   • Coaches: " + coachesList.size());
            System.out.println("   • Lignes: " + linesList.size());
            System.out.println("   • Agences: " + agenciesList.size());
            System.out.println("   • Chauffeurs: " + driversList.size());
            System.out.println("   • Réclamations: " + reclamationsList.size());
            System.out.println("   • Incidents: " + incidentsList.size());

            // Si aucune donnée, offrir de générer des données de test
            if (driversList.isEmpty() && coachesList.isEmpty()) {
                askToGenerateSampleData();
            }

        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Load Error", "Failed to load data: " + e.getMessage());
        }
    }

    private void askToGenerateSampleData() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("No Data Found");
        alert.setHeaderText("No data found in the system");
        alert.setContentText("Would you like to generate sample data (5 drivers, 5 coaches, 5 lines, etc.)?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                generateSampleData();
            }
        });
    }

    private void generateSampleData() {
        try {
            FirebaseService.generateSampleData();
            loadInitialData();
            showSuccessAlert("Sample Data Generated",
                    "Sample data has been created successfully!\n\n" +
                            "Login credentials:\n" +
                            "• Manager: username=manager1, password=1234\n" +
                            "• Drivers: username=driver1 to driver5, password=1234");
        } catch (Exception e) {
            showErrorAlert("Generation Error", "Failed to generate sample data: " + e.getMessage());
        }
    }

    // ==============================
    // VIEW METHODS
    // ==============================

    private void showDashboardView() {
        contentArea.getChildren().clear();

        VBox dashboardView = new VBox(20);
        dashboardView.setAlignment(Pos.TOP_LEFT);
        dashboardView.setPadding(new Insets(20));

        // Header
        Text title = new Text("Manager Dashboard");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setFill(Color.web(PRIMARY_GREEN));

        Text subtitle = new Text("Overview of your transportation system");
        subtitle.setFont(Font.font("Arial", 14));
        subtitle.setFill(Color.web("#666666"));

        // Stats Cards Row
        HBox statsRow = new HBox(20);
        statsRow.setPadding(new Insets(20, 0, 20, 0));

        try {
            Map<String, Object> stats = FirebaseService.getDashboardStats();

            VBox driversCard = createStatCard("👨‍✈️ DRIVERS",
                    stats.get("totalDrivers").toString(),
                    stats.get("activeDrivers") + " active",
                    "Manage drivers", PRIMARY_GREEN);

            VBox coachesCard = createStatCard("🚌 COACHES",
                    stats.get("totalCoaches").toString(),
                    stats.get("activeCoaches") + " active",
                    "Fleet status", BLUE_ACCENT);

            VBox incidentsCard = createStatCard("🚨 INCIDENTS",
                    stats.get("totalIncidents").toString(),
                    stats.get("pendingIncidents") + " pending",
                    "Needs attention", RED_ACCENT);

            VBox reclamationsCard = createStatCard("📋 RECLAMATIONS",
                    String.valueOf(reclamationsList.size()),
                    "Customer feedback",
                    "View reports", ORANGE_ACCENT);

            statsRow.getChildren().addAll(driversCard, coachesCard, incidentsCard, reclamationsCard);

        } catch (Exception e) {
            e.printStackTrace();
            // Fallback cards
            statsRow.getChildren().addAll(
                    createStatCard("👨‍✈️ DRIVERS", String.valueOf(driversList.size()), "Total drivers", "Manage", PRIMARY_GREEN),
                    createStatCard("🚌 COACHES", String.valueOf(coachesList.size()), "Total coaches", "Fleet", BLUE_ACCENT),
                    createStatCard("🚨 INCIDENTS", String.valueOf(incidentsList.size()), "Total incidents", "Review", RED_ACCENT),
                    createStatCard("📋 RECLAMATIONS", String.valueOf(reclamationsList.size()), "Customer feedback", "View", ORANGE_ACCENT)
            );
        }

        // Quick Actions
        VBox quickActions = createQuickActionsSection();

        // Recent Incidents
        VBox recentIncidents = createRecentIncidentsSection();

        // Charts Row
        HBox chartsRow = new HBox(20);
        chartsRow.setPadding(new Insets(20, 0, 0, 0));

        try {
            PieChart driverStatusChart = createDriverStatusChart();
            BarChart<String, Number> incidentChart = createIncidentTypeChart();

            chartsRow.getChildren().addAll(driverStatusChart, incidentChart);
        } catch (Exception e) {
            e.printStackTrace();
        }

        dashboardView.getChildren().addAll(title, subtitle, statsRow, quickActions, recentIncidents, chartsRow);

        ScrollPane scrollPane = new ScrollPane(dashboardView);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        contentArea.getChildren().add(scrollPane);
    }

    private VBox createStatCard(String title, String value, String detail, String status, String color) {
        VBox card = new VBox(10);
        card.setPrefWidth(250);
        card.setMinHeight(140);
        card.setPadding(new Insets(20));
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 12;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0.2, 0, 2);"
        );

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", 12));
        titleLabel.setStyle("-fx-text-fill: #757575;");

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        valueLabel.setStyle("-fx-text-fill: " + color + ";");

        Label detailLabel = new Label(detail);
        detailLabel.setFont(Font.font("Arial", 13));
        detailLabel.setStyle("-fx-text-fill: #666666;");

        card.getChildren().addAll(titleLabel, valueLabel, detailLabel);

        if (!status.isEmpty()) {
            Label statusLabel = new Label(status);
            statusLabel.setFont(Font.font("Arial", 11));
            statusLabel.setStyle("-fx-text-fill: #666666; -fx-padding: 5 0 0 0;");
            card.getChildren().add(statusLabel);
        }

        return card;
    }

    private VBox createQuickActionsSection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(25));
        section.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 12;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0.2, 0, 2);"
        );

        Label title = new Label("⚡ Quick Actions");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        title.setStyle("-fx-text-fill: #212121;");

        GridPane actionsGrid = new GridPane();
        actionsGrid.setHgap(20);
        actionsGrid.setVgap(20);
        actionsGrid.setPadding(new Insets(15, 0, 0, 0));

        // ← MODIFIÉ: Utilise les mêmes dialogs complets
        Button btnAddDriver = createQuickActionButton("➕ Add New Driver", PRIMARY_GREEN);
        btnAddDriver.setOnAction(e -> showCreateDriverDialog());

        Button btnAddCoach = createQuickActionButton("🚌 Add New Coach", BLUE_ACCENT);
        btnAddCoach.setOnAction(e -> showAddCoachDialog());

        Button btnViewIncidents = createQuickActionButton("🚨 View Incidents", RED_ACCENT);
        btnViewIncidents.setOnAction(e -> showIncidentsView());

        Button btnGenerateReport = createQuickActionButton("📊 Generate Report", PURPLE_ACCENT);
        btnGenerateReport.setOnAction(e -> showReportsView());

        actionsGrid.add(btnAddDriver, 0, 0);
        actionsGrid.add(btnAddCoach, 1, 0);
        actionsGrid.add(btnViewIncidents, 0, 1);
        actionsGrid.add(btnGenerateReport, 1, 1);

        section.getChildren().addAll(title, actionsGrid);
        return section;
    }


    private Button createQuickActionButton(String text, String color) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        btn.setTextFill(Color.WHITE);
        btn.setPrefHeight(60);
        btn.setPrefWidth(200);
        btn.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-background-radius: 10;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 6, 0.3, 0, 2);"
        );

        btn.setOnMouseEntered(e -> {
            btn.setStyle(
                    "-fx-background-color: " + color + ";" +
                            "-fx-background-radius: 10;" +
                            "-fx-cursor: hand;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0.4, 0, 3);"
            );
        });

        btn.setOnMouseExited(e -> {
            btn.setStyle(
                    "-fx-background-color: " + color + ";" +
                            "-fx-background-radius: 10;" +
                            "-fx-cursor: hand;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 6, 0.3, 0, 2);"
            );
        });

        return btn;
    }

    private VBox createRecentIncidentsSection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(25));
        section.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 12;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0.2, 0, 2);"
        );

        HBox headerBox = new HBox();
        Label title = new Label("🚨 Recent Incidents");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        title.setStyle("-fx-text-fill: #212121;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Hyperlink viewAll = new Hyperlink("View All Incidents");
        viewAll.setStyle("-fx-text-fill: " + PRIMARY_GREEN + "; -fx-font-size: 14; -fx-underline: false; -fx-font-weight: bold;");
        viewAll.setOnAction(e -> showIncidentsView());

        headerBox.getChildren().addAll(title, spacer, viewAll);

        // Recent incidents list
        VBox incidentsListBox = new VBox(10);
        incidentsListBox.setPadding(new Insets(15, 0, 0, 0));

        // Get last 5 incidents
        List<Incident> recentIncidents = incidentsList.stream()
                .limit(5)
                .toList();

        if (recentIncidents.isEmpty()) {
            Label noIncidents = new Label("No recent incidents");
            noIncidents.setStyle("-fx-text-fill: #666666; -fx-font-size: 14; -fx-padding: 20;");
            incidentsListBox.getChildren().add(noIncidents);
        } else {
            for (Incident incident : recentIncidents) {
                HBox incidentItem = createIncidentItem(incident);
                incidentsListBox.getChildren().add(incidentItem);
            }
        }

        section.getChildren().addAll(headerBox, incidentsListBox);
        return section;
    }

    private HBox createIncidentItem(Incident incident) {
        HBox item = new HBox(15);
        item.setPadding(new Insets(15));
        item.setStyle(
                "-fx-background-color: #fafafa;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.03), 5, 0.2, 0, 1);"
        );
        item.setAlignment(Pos.CENTER_LEFT);

        // Status indicator
        Circle statusIndicator = new Circle(6);
        String statusColor = switch (incident.getStatus()) {
            case "pending" -> RED_ACCENT;
            case "in_progress" -> ORANGE_ACCENT;
            case "resolved" -> PRIMARY_GREEN;
            default -> "#cccccc";
        };
        statusIndicator.setFill(Color.web(statusColor));

        VBox infoBox = new VBox(5);
        Label typeLabel = new Label(incident.getType().toUpperCase());
        typeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        typeLabel.setStyle("-fx-text-fill: #212121;");

        Label descLabel = new Label(incident.getDescription());
        descLabel.setFont(Font.font("Arial", 12));
        descLabel.setStyle("-fx-text-fill: #666666;");
        descLabel.setWrapText(true);
        descLabel.setPrefWidth(400);

        HBox metaBox = new HBox(20);
        Label driverLabel = new Label("Driver: " + incident.getDriverId());
        driverLabel.setFont(Font.font("Arial", 11));
        driverLabel.setStyle("-fx-text-fill: #999999;");

        Label dateLabel = new Label("Date: " + incident.getDate());
        dateLabel.setFont(Font.font("Arial", 11));
        dateLabel.setStyle("-fx-text-fill: #999999;");

        metaBox.getChildren().addAll(driverLabel, dateLabel);

        infoBox.getChildren().addAll(typeLabel, descLabel, metaBox);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Status badge
        Label statusBadge = new Label(incident.getStatus().toUpperCase());
        statusBadge.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        statusBadge.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-background-color: " + statusColor + ";" +
                        "-fx-padding: 4 12;" +
                        "-fx-background-radius: 12;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 3, 0.2, 0, 1);"
        );

        item.getChildren().addAll(statusIndicator, infoBox, spacer, statusBadge);
        return item;
    }

    private PieChart createDriverStatusChart() throws Exception {
        PieChart pieChart = new PieChart();
        pieChart.setTitle("Driver Status Distribution");
        pieChart.setLegendVisible(true);
        pieChart.setPrefSize(400, 300);

        // Count drivers by status
        Map<String, Integer> statusCount = new HashMap<>();
        for (Driver driver : driversList) {
            String status = driver.getStatus() != null ? driver.getStatus() : "unknown";
            statusCount.put(status, statusCount.getOrDefault(status, 0) + 1);
        }

        for (Map.Entry<String, Integer> entry : statusCount.entrySet()) {
            PieChart.Data slice = new PieChart.Data(
                    entry.getKey().toUpperCase() + " (" + entry.getValue() + ")",
                    entry.getValue()
            );
            pieChart.getData().add(slice);
        }

        // Apply colors
        for (PieChart.Data data : pieChart.getData()) {
            String name = data.getName();
            if (name.contains("ACTIVE")) {
                data.getNode().setStyle("-fx-pie-color: " + PRIMARY_GREEN + ";");
            } else if (name.contains("INACTIVE")) {
                data.getNode().setStyle("-fx-pie-color: #cccccc;");
            } else if (name.contains("ON_LEAVE")) {
                data.getNode().setStyle("-fx-pie-color: " + ORANGE_ACCENT + ";");
            } else if (name.contains("SUSPENDED")) {
                data.getNode().setStyle("-fx-pie-color: " + RED_ACCENT + ";");
            }
        }

        return pieChart;
    }

    private BarChart<String, Number> createIncidentTypeChart() throws Exception {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);

        barChart.setTitle("Incidents by Type");
        barChart.setLegendVisible(false);
        barChart.setPrefSize(500, 300);

        xAxis.setLabel("Incident Type");
        yAxis.setLabel("Count");

        XYChart.Series<String, Number> series = new XYChart.Series<>();

        // Count incidents by type
        Map<String, Integer> typeCount = new HashMap<>();
        for (Incident incident : incidentsList) {
            String type = incident.getType() != null ? incident.getType() : "Other";
            typeCount.put(type, typeCount.getOrDefault(type, 0) + 1);
        }

        for (Map.Entry<String, Integer> entry : typeCount.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        barChart.getData().add(series);

        // Apply colors to bars
        for (XYChart.Data<String, Number> data : series.getData()) {
            data.getNode().setStyle("-fx-bar-fill: " + RED_ACCENT + ";");
        }

        return barChart;
    }

    private void showCoachesView() {
        contentArea.getChildren().clear();

        VBox coachesView = new VBox(20);
        coachesView.setAlignment(Pos.TOP_LEFT);
        coachesView.setPadding(new Insets(20));

        Text title = new Text("🚌 Coaches Management");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        title.setFill(Color.web(PRIMARY_GREEN));

        Text info = new Text("Manage your fleet of coaches, add new vehicles, track maintenance schedules, and monitor vehicle status.");
        info.setFont(Font.font("Arial", 14));
        info.setFill(Color.web(TEXT_DARK));
        info.setWrappingWidth(800);

        // Action buttons
        HBox actionBar = new HBox(15);
        Button btnAddCoach = createActionButton("➕ Add Coach", PRIMARY_GREEN);
        Button btnEditCoach = createActionButton("✏️ Edit Coach", DARK_GREEN);
        Button btnDeleteCoach = createActionButton("🗑️ Delete Coach", RED_ACCENT);
        Button btnRefresh = createActionButton("🔄 Refresh", "#6c757d");

        btnAddCoach.setOnAction(e -> showAddCoachDialog());
        btnEditCoach.setOnAction(e -> showEditCoachDialog());
        btnDeleteCoach.setOnAction(e -> showDeleteCoachDialog());
        btnRefresh.setOnAction(e -> refreshCoachesTable());

        actionBar.getChildren().addAll(btnAddCoach, btnEditCoach, btnDeleteCoach, btnRefresh);

        coachesView.getChildren().addAll(title, info, actionBar, coachesTable);

        ScrollPane scrollPane = new ScrollPane(coachesView);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        contentArea.getChildren().add(scrollPane);
    }

    private void showLinesView() {
        contentArea.getChildren().clear();

        VBox linesView = new VBox(20);
        linesView.setAlignment(Pos.TOP_LEFT);
        linesView.setPadding(new Insets(20));

        Text title = new Text("🛣️ Transport Lines Management");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        title.setFill(Color.web(PRIMARY_GREEN));

        Text info = new Text("Manage transport lines, routes, schedules, and pricing.");
        info.setFont(Font.font("Arial", 14));
        info.setFill(Color.web(TEXT_DARK));
        info.setWrappingWidth(800);

        // Action buttons
        HBox actionBar = new HBox(15);
        Button btnAddLine = createActionButton("➕ Add Line", PRIMARY_GREEN);
        Button btnEditLine = createActionButton("✏️ Edit Line", DARK_GREEN);
        Button btnDeleteLine = createActionButton("🗑️ Delete Line", RED_ACCENT);
        Button btnRefresh = createActionButton("🔄 Refresh", "#6c757d");

        btnAddLine.setOnAction(e -> showAddLineDialog());
        btnEditLine.setOnAction(e -> showEditLineDialog());
        btnDeleteLine.setOnAction(e -> showDeleteLineDialog());
        btnRefresh.setOnAction(e -> refreshLinesTable());

        actionBar.getChildren().addAll(btnAddLine, btnEditLine, btnDeleteLine, btnRefresh);

        linesView.getChildren().addAll(title, info, actionBar, linesTable);

        ScrollPane scrollPane = new ScrollPane(linesView);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        contentArea.getChildren().add(scrollPane);
    }

    private void showDriversView() {
        contentArea.getChildren().clear();

        VBox driversView = new VBox(20);
        driversView.setAlignment(Pos.TOP_LEFT);
        driversView.setPadding(new Insets(20));

        Text title = new Text("👨‍✈️ Drivers Management");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        title.setFill(Color.web(PRIMARY_GREEN));

        Text info = new Text("Manage driver personnel, licenses, assignments, and schedules.");
        info.setFont(Font.font("Arial", 14));
        info.setFill(Color.web(TEXT_DARK));
        info.setWrappingWidth(800);

        // Action buttons
        HBox actionBar = new HBox(15);
        Button btnAddDriver = createActionButton("➕ Add Driver", PRIMARY_GREEN);
        Button btnEditDriver = createActionButton("✏️ Edit Driver", DARK_GREEN);
        Button btnDeleteDriver = createActionButton("🗑️ Delete Driver", RED_ACCENT);
        Button btnManageDriver = createActionButton("⚙️ Manage", BLUE_ACCENT);
        Button btnRefresh = createActionButton("🔄 Refresh", "#6c757d");

        btnAddDriver.setOnAction(e -> showCreateDriverDialog());
        btnEditDriver.setOnAction(e -> showEditDriverDialog());
        btnDeleteDriver.setOnAction(e -> showDeleteDriverDialog());
        btnManageDriver.setOnAction(e -> showManageDriverDialog());
        btnRefresh.setOnAction(e -> refreshDriversTable());

        actionBar.getChildren().addAll(btnAddDriver, btnEditDriver, btnDeleteDriver, btnManageDriver, btnRefresh);

        driversView.getChildren().addAll(title, info, actionBar, driversTable);

        ScrollPane scrollPane = new ScrollPane(driversView);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        contentArea.getChildren().add(scrollPane);
    }

    private void showIncidentsView() {
        contentArea.getChildren().clear();

        VBox incidentsView = new VBox(20);
        incidentsView.setAlignment(Pos.TOP_LEFT);
        incidentsView.setPadding(new Insets(20));

        HBox headerBox = new HBox();
        VBox headerText = new VBox(5);
        Text title = new Text("🚨 Incident Management");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setFill(Color.web(PRIMARY_GREEN));

        Text subtitle = new Text("Review and manage driver-reported incidents (Auto-refreshes every 10s)");
        subtitle.setFont(Font.font("Arial", 14));
        subtitle.setFill(Color.web(TEXT_DARK));

        headerText.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshBtn = createActionButton("🔄 Refresh Now", "#6c757d");
        refreshBtn.setOnAction(e -> refreshIncidentsTable());

        headerBox.getChildren().addAll(headerText, spacer, refreshBtn);

        // Action buttons for incidents
        HBox actionBar = new HBox(15);
        actionBar.setPadding(new Insets(0, 0, 20, 0));

        Button btnAssign = createActionButton("👤 Assign to Me", PRIMARY_GREEN);
        Button btnResolve = createActionButton("✅ Mark Resolved", DARK_GREEN);
        Button btnClose = createActionButton("🔒 Close Incident", "#6c757d");

        btnAssign.setOnAction(e -> assignIncidentToManager());
        btnResolve.setOnAction(e -> resolveIncident());
        btnClose.setOnAction(e -> closeIncident());

        actionBar.getChildren().addAll(btnAssign, btnResolve, btnClose);

        incidentsView.getChildren().addAll(headerBox, actionBar, incidentsTable);

        ScrollPane scrollPane = new ScrollPane(incidentsView);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        contentArea.getChildren().add(scrollPane);
    }

    private void showReclamationsView() {
        contentArea.getChildren().clear();

        VBox reclamationsView = new VBox(20);
        reclamationsView.setAlignment(Pos.TOP_LEFT);
        reclamationsView.setPadding(new Insets(20));

        Text title = new Text("📋 Customer Reclamations");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        title.setFill(Color.web(PRIMARY_GREEN));

        Text info = new Text("Handle customer complaints, feedback, and service issues.");
        info.setFont(Font.font("Arial", 14));
        info.setFill(Color.web(TEXT_DARK));
        info.setWrappingWidth(800);

        // Action buttons for status management
        HBox actionBar = new HBox(15);
        Button btnAddReclamation = createActionButton("➕ Add Reclamation", PRIMARY_GREEN);
        Button btnProcess = createActionButton("🔄 Process", DARK_GREEN);
        Button btnResolve = createActionButton("✅ Resolve", BLUE_ACCENT);
        Button btnClose = createActionButton("🔒 Close", "#6c757d");
        Button btnRefresh = createActionButton("🔄 Refresh", "#6c757d");

        btnAddReclamation.setOnAction(e -> showAddReclamationDialog());
        btnProcess.setOnAction(e -> processReclamation());
        btnResolve.setOnAction(e -> resolveReclamation());
        btnClose.setOnAction(e -> closeReclamation());
        btnRefresh.setOnAction(e -> refreshReclamationsTable());

        actionBar.getChildren().addAll(btnAddReclamation, btnProcess, btnResolve, btnClose, btnRefresh);

        reclamationsView.getChildren().addAll(title, info, actionBar, reclamationsTable);

        ScrollPane scrollPane = new ScrollPane(reclamationsView);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        contentArea.getChildren().add(scrollPane);
    }

    private void showAgenciesView() {
        contentArea.getChildren().clear();

        VBox agenciesView = new VBox(20);
        agenciesView.setAlignment(Pos.TOP_LEFT);
        agenciesView.setPadding(new Insets(20));

        Text title = new Text("🏢 Agencies Management");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        title.setFill(Color.web(PRIMARY_GREEN));

        Text info = new Text("Manage your agency network, locations, and staff.");
        info.setFont(Font.font("Arial", 14));
        info.setFill(Color.web(TEXT_DARK));
        info.setWrappingWidth(800);

        // Action buttons
        HBox actionBar = new HBox(15);
        Button btnAddAgency = createActionButton("➕ Add Agency", PRIMARY_GREEN);
        Button btnEditAgency = createActionButton("✏️ Edit Agency", DARK_GREEN);
        Button btnDeleteAgency = createActionButton("🗑️ Delete Agency", RED_ACCENT);
        Button btnRefresh = createActionButton("🔄 Refresh", "#6c757d");

        btnAddAgency.setOnAction(e -> showAddAgencyDialog());
        btnEditAgency.setOnAction(e -> showEditAgencyDialog());
        btnDeleteAgency.setOnAction(e -> showDeleteAgencyDialog());
        btnRefresh.setOnAction(e -> refreshAgenciesTable());

        actionBar.getChildren().addAll(btnAddAgency, btnEditAgency, btnDeleteAgency, btnRefresh);

        agenciesView.getChildren().addAll(title, info, actionBar, agenciesTable);

        ScrollPane scrollPane = new ScrollPane(agenciesView);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        contentArea.getChildren().add(scrollPane);
    }

    private void showReportsView() {
        contentArea.getChildren().clear();

        VBox reportsView = new VBox(20);
        reportsView.setAlignment(Pos.TOP_LEFT);
        reportsView.setPadding(new Insets(20));

        Text title = new Text("📈 Reports & Analytics");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setFill(Color.web(PRIMARY_GREEN));

        Text subtitle = new Text("Generate reports and view system analytics");
        subtitle.setFont(Font.font("Arial", 14));
        subtitle.setFill(Color.web("#666666"));

        // Report Cards
        HBox reportCards = new HBox(20);
        reportCards.setPadding(new Insets(20, 0, 20, 0));

        VBox driverReportCard = createReportCard("👨‍✈️ Driver Report",
                "Generate driver performance and activity report",
                "Generate", PRIMARY_GREEN);
        VBox incidentReportCard = createReportCard("🚨 Incident Report",
                "Generate incident analysis and resolution report",
                "Generate", RED_ACCENT);
        VBox fleetReportCard = createReportCard("🚌 Fleet Report",
                "Generate vehicle utilization and maintenance report",
                "Generate", BLUE_ACCENT);
        VBox financialReportCard = createReportCard("💰 Financial Report",
                "Generate salary and operational costs report",
                "Generate", PURPLE_ACCENT);

        driverReportCard.setOnMouseClicked(e -> generateDriverReport());
        incidentReportCard.setOnMouseClicked(e -> generateIncidentReport());
        fleetReportCard.setOnMouseClicked(e -> generateFleetReport());
        financialReportCard.setOnMouseClicked(e -> generateFinancialReport());

        reportCards.getChildren().addAll(driverReportCard, incidentReportCard, fleetReportCard, financialReportCard);

        // Charts Section
        VBox chartsSection = new VBox(20);
        chartsSection.setPadding(new Insets(20));
        chartsSection.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 12;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0.2, 0, 2);"
        );

        Label chartsTitle = new Label("📊 System Analytics");
        chartsTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        chartsTitle.setStyle("-fx-text-fill: #212121;");

        try {
            PieChart driverChart = createDriverStatusChart();
            BarChart<String, Number> incidentChart = createIncidentTypeChart();

            HBox chartsRow = new HBox(20);
            chartsRow.getChildren().addAll(driverChart, incidentChart);
            chartsSection.getChildren().addAll(chartsTitle, chartsRow);
        } catch (Exception e) {
            e.printStackTrace();
        }

        reportsView.getChildren().addAll(title, subtitle, reportCards, chartsSection);

        ScrollPane scrollPane = new ScrollPane(reportsView);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        contentArea.getChildren().add(scrollPane);
    }

    private VBox createReportCard(String title, String description, String action, String color) {
        VBox card = new VBox(15);
        card.setPrefWidth(280);
        card.setPadding(new Insets(25));
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: " + color + ";" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 12;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0.2, 0, 2);" +
                        "-fx-cursor: hand;"
        );

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        titleLabel.setStyle("-fx-text-fill: " + color + ";");

        Label descLabel = new Label(description);
        descLabel.setFont(Font.font("Arial", 13));
        descLabel.setStyle("-fx-text-fill: #666666;");
        descLabel.setWrapText(true);

        Label actionLabel = new Label("→ " + action);
        actionLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        actionLabel.setStyle("-fx-text-fill: " + color + "; -fx-padding: 10 0 0 0;");

        card.getChildren().addAll(titleLabel, descLabel, actionLabel);

        // Hover effect
        card.setOnMouseEntered(e -> {
            card.setStyle(
                    "-fx-background-color: #f9f9f9;" +
                            "-fx-background-radius: 12;" +
                            "-fx-border-color: " + color + ";" +
                            "-fx-border-width: 2;" +
                            "-fx-border-radius: 12;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 12, 0.3, 0, 3);" +
                            "-fx-cursor: hand;"
            );
        });

        card.setOnMouseExited(e -> {
            card.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-background-radius: 12;" +
                            "-fx-border-color: " + color + ";" +
                            "-fx-border-width: 2;" +
                            "-fx-border-radius: 12;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0.2, 0, 2);" +
                            "-fx-cursor: hand;"
            );
        });

        return card;
    }

    // ==============================
    // DIALOG METHODS - CREATE DRIVER
    // ==============================

    private void showCreateDriverDialog() {
        Dialog<Map<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Create New Driver Account");
        dialog.setHeaderText("Create a complete driver profile with account");

        ButtonType createButton = new ButtonType("Create Account", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Account Information
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username (for login)");

        TextField passwordField = new TextField();
        passwordField.setPromptText("Password");

        TextField fullNameField = new TextField();
        fullNameField.setPromptText("Full Name");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone");

        // Driver Details
        TextField licenseField = new TextField();
        licenseField.setPromptText("License Number");

        DatePicker licenseExpiryPicker = new DatePicker(LocalDate.now().plusYears(1));
        DatePicker hireDatePicker = new DatePicker(LocalDate.now());

        TextField salaryField = new TextField();
        salaryField.setPromptText("Salary");
        salaryField.setText("8000");

        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("active", "on_leave", "inactive");
        statusCombo.setValue("active");

        ComboBox<String> coachCombo = new ComboBox<>();
        coachCombo.setPromptText("Select Coach");
        try {
            for (Coach coach : coachesList) {
                coachCombo.getItems().add(coach.getId() + " - " + coach.getModel() + " (" + coach.getLicensePlate() + ")");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        ComboBox<String> lineCombo = new ComboBox<>();
        lineCombo.setPromptText("Select Line");
        try {
            for (TransportLine line : linesList) {
                lineCombo.getItems().add(line.getLineId() + " - " + line.getRoute());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        int row = 0;
        // Account Section
        grid.add(new Label("Account Information:"), 0, row++);
        grid.add(new Label("Username:"), 0, row); grid.add(usernameField, 1, row++);
        grid.add(new Label("Password:"), 0, row); grid.add(passwordField, 1, row++);
        grid.add(new Label("Full Name:"), 0, row); grid.add(fullNameField, 1, row++);
        grid.add(new Label("Email:"), 0, row); grid.add(emailField, 1, row++);
        grid.add(new Label("Phone:"), 0, row); grid.add(phoneField, 1, row++);

        grid.add(new Separator(), 0, row); grid.add(new Separator(), 1, row++);

        // Driver Section
        grid.add(new Label("Driver Details:"), 0, row++);
        grid.add(new Label("License Number:"), 0, row); grid.add(licenseField, 1, row++);
        grid.add(new Label("License Expiry:"), 0, row); grid.add(licenseExpiryPicker, 1, row++);
        grid.add(new Label("Hire Date:"), 0, row); grid.add(hireDatePicker, 1, row++);
        grid.add(new Label("Salary:"), 0, row); grid.add(salaryField, 1, row++);
        grid.add(new Label("Status:"), 0, row); grid.add(statusCombo, 1, row++);
        grid.add(new Label("Assigned Coach:"), 0, row); grid.add(coachCombo, 1, row++);
        grid.add(new Label("Assigned Line:"), 0, row); grid.add(lineCombo, 1, row++);

        dialog.getDialogPane().setContent(grid);

        // Validate before creating
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButton) {
                // Validation
                if (usernameField.getText().isEmpty() || passwordField.getText().isEmpty() ||
                        fullNameField.getText().isEmpty() || licenseField.getText().isEmpty()) {
                    showErrorAlert("Validation Error", "Please fill all required fields.");
                    return null;
                }

                try {
                    // Check if username exists
                    if (FirebaseService.checkUserExists(usernameField.getText())) {
                        showErrorAlert("Username Exists", "This username is already taken. Please choose another.");
                        return null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Map<String, String> result = new HashMap<>();
                result.put("username", usernameField.getText());
                result.put("password", passwordField.getText());
                result.put("fullName", fullNameField.getText());
                result.put("email", emailField.getText());
                result.put("phone", phoneField.getText());
                result.put("license", licenseField.getText());
                result.put("licenseExpiry", licenseExpiryPicker.getValue().toString());
                result.put("hireDate", hireDatePicker.getValue().toString());
                result.put("salary", salaryField.getText());
                result.put("status", statusCombo.getValue());
                result.put("coach", coachCombo.getValue());
                result.put("line", lineCombo.getValue());
                return result;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(data -> {
            if (data != null) {
                try {
                    String coachId = null;
                    if (data.get("coach") != null && !data.get("coach").isEmpty()) {
                        coachId = data.get("coach").split(" - ")[0];
                    }

                    String lineId = null;
                    if (data.get("line") != null && !data.get("line").isEmpty()) {
                        lineId = data.get("line").split(" - ")[0];
                    }

                    FirebaseService.createCompleteDriverAccount(
                            data.get("username"),
                            data.get("password"),
                            data.get("fullName"),
                            data.get("email"),
                            data.get("phone"),
                            data.get("license"),
                            data.get("licenseExpiry"),
                            data.get("hireDate"),
                            Double.parseDouble(data.get("salary")),
                            coachId,
                            lineId
                    );

                    showSuccessAlert("Driver Created",
                            "Driver account created successfully!\n\n" +
                                    "Login Credentials:\n" +
                                    "• Username: " + data.get("username") + "\n" +
                                    "• Password: " + data.get("password") + "\n\n" +
                                    "The driver can now login to their dashboard.");

                    // Refresh drivers list
                    driversList = FirebaseService.loadAllDrivers();
                    driversTable.setItems(driversList);
                    driversTable.refresh();

                } catch (Exception e) {
                    e.printStackTrace();
                    showErrorAlert("Creation Error", "Failed to create driver account: " + e.getMessage());
                }
            }
        });
    }

    // ==============================
    // DIALOG METHODS - COACHES
    // ==============================

    private void showAddCoachDialog() {
        Dialog<Coach> dialog = new Dialog<>();
        dialog.setTitle("Add New Coach");
        dialog.setHeaderText("Enter coach details");

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField modelField = new TextField();
        modelField.setPromptText("Model");
        TextField capacityField = new TextField();
        capacityField.setPromptText("Capacity");
        TextField licenseField = new TextField();
        licenseField.setPromptText("License Plate");
        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("Active", "Maintenance", "Inactive");
        statusCombo.setValue("Active");

        grid.add(new Label("Model:"), 0, 0);
        grid.add(modelField, 1, 0);
        grid.add(new Label("Capacity:"), 0, 1);
        grid.add(capacityField, 1, 1);
        grid.add(new Label("License Plate:"), 0, 2);
        grid.add(licenseField, 1, 2);
        grid.add(new Label("Status:"), 0, 3);
        grid.add(statusCombo, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                String id = "C" + String.format("%03d", coachesList.size() + 1);
                return new Coach(id, modelField.getText(), capacityField.getText(),
                        licenseField.getText(), statusCombo.getValue());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(coach -> {
            if (coach != null) {
                try {
                    FirebaseService.addCoach(coach);
                    coachesList.add(coach);
                    coachesTable.refresh();
                    showSuccessAlert("Coach Added", "Coach has been successfully added.");
                } catch (Exception e) {
                    showErrorAlert("Error", "Failed to add coach: " + e.getMessage());
                }
            }
        });
    }

    private void showEditCoachDialog() {
        Coach selectedCoach = coachesTable.getSelectionModel().getSelectedItem();
        if (selectedCoach == null) {
            showErrorAlert("No Selection", "Please select a coach to edit.");
            return;
        }

        Dialog<Coach> dialog = new Dialog<>();
        dialog.setTitle("Edit Coach");
        dialog.setHeaderText("Edit coach details");

        ButtonType updateButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField modelField = new TextField(selectedCoach.getModel());
        TextField capacityField = new TextField(selectedCoach.getCapacity());
        TextField licenseField = new TextField(selectedCoach.getLicensePlate());
        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("Active", "Maintenance", "Inactive");
        statusCombo.setValue(selectedCoach.getStatus());

        grid.add(new Label("Model:"), 0, 0);
        grid.add(modelField, 1, 0);
        grid.add(new Label("Capacity:"), 0, 1);
        grid.add(capacityField, 1, 1);
        grid.add(new Label("License Plate:"), 0, 2);
        grid.add(licenseField, 1, 2);
        grid.add(new Label("Status:"), 0, 3);
        grid.add(statusCombo, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == updateButtonType) {
                return new Coach(
                        selectedCoach.getId(),
                        modelField.getText(),
                        capacityField.getText(),
                        licenseField.getText(),
                        statusCombo.getValue()
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updatedCoach -> {
            if (updatedCoach != null) {
                try {
                    FirebaseService.updateCoach(updatedCoach);
                    int index = coachesList.indexOf(selectedCoach);
                    if (index >= 0) {
                        coachesList.set(index, updatedCoach);
                        coachesTable.refresh();
                    }
                    showSuccessAlert("Coach Updated", "Coach has been successfully updated.");
                } catch (Exception e) {
                    showErrorAlert("Error", "Failed to update coach: " + e.getMessage());
                }
            }
        });
    }

    private void showDeleteCoachDialog() {
        Coach selectedCoach = coachesTable.getSelectionModel().getSelectedItem();
        if (selectedCoach == null) {
            showErrorAlert("No Selection", "Please select a coach to delete.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Coach");
        alert.setHeaderText("Confirm Deletion");
        alert.setContentText("Are you sure you want to delete coach: " + selectedCoach.getModel() + "?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    FirebaseService.deleteCoach(selectedCoach.getId());
                    coachesList.remove(selectedCoach);
                    showSuccessAlert("Coach Deleted", "Coach has been removed.");
                } catch (Exception e) {
                    showErrorAlert("Error", "Failed to delete coach: " + e.getMessage());
                }
            }
        });
    }

    // ==============================
    // DIALOG METHODS - LINES
    // ==============================

    private void showAddLineDialog() {
        Dialog<TransportLine> dialog = new Dialog<>();
        dialog.setTitle("Add New Transport Line");
        dialog.setHeaderText("Enter transport line details");

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField routeField = new TextField();
        routeField.setPromptText("Route (e.g., City A - City B)");

        TextField scheduleField = new TextField();
        scheduleField.setPromptText("Schedule (e.g., 06:00-22:00)");

        TextField priceField = new TextField();
        priceField.setPromptText("Price (e.g., $25)");

        TextField stopsField = new TextField();
        stopsField.setPromptText("Stops (e.g., 10 stops)");

        grid.add(new Label("Route:"), 0, 0);
        grid.add(routeField, 1, 0);
        grid.add(new Label("Schedule:"), 0, 1);
        grid.add(scheduleField, 1, 1);
        grid.add(new Label("Price:"), 0, 2);
        grid.add(priceField, 1, 2);
        grid.add(new Label("Stops:"), 0, 3);
        grid.add(stopsField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    String nextId = "L" + String.format("%03d", linesList.size() + 1);
                    return new TransportLine(
                            nextId,
                            routeField.getText(),
                            scheduleField.getText(),
                            priceField.getText(),
                            stopsField.getText()
                    );
                } catch (Exception e) {
                    showErrorAlert("Error", "Invalid input: " + e.getMessage());
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(line -> {
            if (line != null) {
                try {
                    FirebaseService.addLine(line);
                    linesList.add(line);
                    linesTable.refresh();
                    showSuccessAlert("Line Added", "Transport line has been successfully added.");
                } catch (Exception e) {
                    showErrorAlert("Error", "Failed to add line: " + e.getMessage());
                }
            }
        });
    }

    private void showEditLineDialog() {
        TransportLine selectedLine = linesTable.getSelectionModel().getSelectedItem();
        if (selectedLine == null) {
            showErrorAlert("No Selection", "Please select a line to edit.");
            return;
        }

        Dialog<TransportLine> dialog = new Dialog<>();
        dialog.setTitle("Edit Transport Line");
        dialog.setHeaderText("Edit line details");

        ButtonType updateButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField routeField = new TextField(selectedLine.getRoute());
        TextField scheduleField = new TextField(selectedLine.getSchedule());
        TextField priceField = new TextField(selectedLine.getPrice());
        TextField stopsField = new TextField(selectedLine.getStops());

        grid.add(new Label("Route:"), 0, 0);
        grid.add(routeField, 1, 0);
        grid.add(new Label("Schedule:"), 0, 1);
        grid.add(scheduleField, 1, 1);
        grid.add(new Label("Price:"), 0, 2);
        grid.add(priceField, 1, 2);
        grid.add(new Label("Stops:"), 0, 3);
        grid.add(stopsField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == updateButtonType) {
                return new TransportLine(
                        selectedLine.getLineId(),
                        routeField.getText(),
                        scheduleField.getText(),
                        priceField.getText(),
                        stopsField.getText()
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updatedLine -> {
            if (updatedLine != null) {
                try {
                    FirebaseService.updateLine(updatedLine);
                    int index = linesList.indexOf(selectedLine);
                    if (index >= 0) {
                        linesList.set(index, updatedLine);
                        linesTable.refresh();
                    }
                    showSuccessAlert("Line Updated", "Transport line has been successfully updated.");
                } catch (Exception e) {
                    showErrorAlert("Error", "Failed to update line: " + e.getMessage());
                }
            }
        });
    }

    private void showDeleteLineDialog() {
        TransportLine selectedLine = linesTable.getSelectionModel().getSelectedItem();
        if (selectedLine == null) {
            showErrorAlert("No Selection", "Please select a line to delete.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Transport Line");
        alert.setHeaderText("Confirm Deletion");
        alert.setContentText("Are you sure you want to delete line: " + selectedLine.getRoute() + "?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    FirebaseService.deleteLine(selectedLine.getLineId());
                    linesList.remove(selectedLine);
                    showSuccessAlert("Line Deleted", "Transport line has been removed.");
                } catch (Exception e) {
                    showErrorAlert("Error", "Failed to delete line: " + e.getMessage());
                }
            }
        });
    }

    // ==============================
    // DIALOG METHODS - DRIVERS
    // ==============================

    private void showEditDriverDialog() {
        Driver selectedDriver = driversTable.getSelectionModel().getSelectedItem();
        if (selectedDriver == null) {
            showErrorAlert("No Selection", "Please select a driver to edit.");
            return;
        }

        Dialog<Driver> dialog = new Dialog<>();
        dialog.setTitle("Edit Driver");
        dialog.setHeaderText("Edit driver basic information");

        ButtonType updateButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField(selectedDriver.getName());
        TextField licenseField = new TextField(selectedDriver.getLicense());
        TextField phoneField = new TextField(selectedDriver.getPhone());
        TextField emailField = new TextField(selectedDriver.getEmail());
        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("active", "on_leave", "suspended", "inactive");
        statusCombo.setValue(selectedDriver.getStatus());

        grid.add(new Label("Full Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("License:"), 0, 1);
        grid.add(licenseField, 1, 1);
        grid.add(new Label("Phone:"), 0, 2);
        grid.add(phoneField, 1, 2);
        grid.add(new Label("Email:"), 0, 3);
        grid.add(emailField, 1, 3);
        grid.add(new Label("Status:"), 0, 4);
        grid.add(statusCombo, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == updateButtonType) {
                return new Driver(
                        selectedDriver.getDriverId(),
                        nameField.getText(),
                        licenseField.getText(),
                        selectedDriver.getAssignedLine(),
                        phoneField.getText(),
                        emailField.getText(),
                        statusCombo.getValue(),
                        selectedDriver.getSalary(),
                        selectedDriver.getAssignedCoachId(),
                        selectedDriver.getUserId()
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updatedDriver -> {
            if (updatedDriver != null) {
                try {
                    FirebaseService.updateDriver(updatedDriver);
                    int index = driversList.indexOf(selectedDriver);
                    if (index >= 0) {
                        driversList.set(index, updatedDriver);
                        driversTable.refresh();
                    }
                    showSuccessAlert("Driver Updated", "Driver information has been updated.");
                } catch (Exception e) {
                    showErrorAlert("Error", "Failed to update driver: " + e.getMessage());
                }
            }
        });
    }

    private void showDeleteDriverDialog() {
        Driver selectedDriver = driversTable.getSelectionModel().getSelectedItem();
        if (selectedDriver == null) {
            showErrorAlert("No Selection", "Please select a driver to delete.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Driver");
        alert.setHeaderText("Confirm Deletion");
        alert.setContentText("Are you sure you want to delete driver: " + selectedDriver.getName() + "?\n\n" +
                "This will also delete their user account from the system.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Delete from drivers collection
                    FirebaseService.deleteDriver(selectedDriver.getDriverId());

                    // Also delete from users collection if userId exists
                    if (selectedDriver.getUserId() != null && !selectedDriver.getUserId().isEmpty()) {
                        Firestore db = FirestoreClient.getFirestore();
                        db.collection("users").document(selectedDriver.getUserId()).delete();
                    }

                    driversList.remove(selectedDriver);
                    showSuccessAlert("Driver Deleted", "Driver and associated account have been removed.");

                } catch (Exception e) {
                    showErrorAlert("Error", "Failed to delete driver: " + e.getMessage());
                }
            }
        });
    }

    private void showManageDriverDialog() {
        Driver selectedDriver = driversTable.getSelectionModel().getSelectedItem();
        if (selectedDriver == null) {
            showErrorAlert("No Selection", "Please select a driver to manage.");
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Manage Driver: " + selectedDriver.getName());
        dialog.setHeaderText("Update driver assignments and status");

        ButtonType updateButton = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Status
        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("active", "on_leave", "suspended", "inactive");
        statusCombo.setValue(selectedDriver.getStatus());

        // Coach assignment
        ComboBox<String> coachCombo = new ComboBox<>();
        coachCombo.setPromptText("Select Coach");
        for (Coach coach : coachesList) {
            coachCombo.getItems().add(coach.getId() + " - " + coach.getModel());
        }
        // Set current coach if exists
        if (selectedDriver.getAssignedCoachId() != null) {
            coachCombo.setValue(selectedDriver.getAssignedCoachId() + " - " +
                    coachesList.stream()
                            .filter(c -> c.getId().equals(selectedDriver.getAssignedCoachId()))
                            .findFirst()
                            .map(Coach::getModel)
                            .orElse(""));
        }

        // Line assignment
        ComboBox<String> lineCombo = new ComboBox<>();
        lineCombo.setPromptText("Select Line");
        for (TransportLine line : linesList) {
            lineCombo.getItems().add(line.getLineId() + " - " + line.getRoute());
        }
        // Set current line if exists
        if (selectedDriver.getAssignedLine() != null) {
            lineCombo.setValue(selectedDriver.getAssignedLine() + " - " +
                    linesList.stream()
                            .filter(l -> l.getLineId().equals(selectedDriver.getAssignedLine()))
                            .findFirst()
                            .map(TransportLine::getRoute)
                            .orElse(""));
        }

        // Salary
        TextField salaryField = new TextField(selectedDriver.getSalary().toString());

        int row = 0;
        grid.add(new Label("Status:"), 0, row); grid.add(statusCombo, 1, row++);
        grid.add(new Label("Assigned Coach:"), 0, row); grid.add(coachCombo, 1, row++);
        grid.add(new Label("Assigned Line:"), 0, row); grid.add(lineCombo, 1, row++);
        grid.add(new Label("Salary:"), 0, row); grid.add(salaryField, 1, row++);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == updateButton) {
                try {
                    Driver updatedDriver = new Driver(
                            selectedDriver.getDriverId(),
                            selectedDriver.getName(),
                            selectedDriver.getLicense(),
                            lineCombo.getValue() != null ? lineCombo.getValue().split(" - ")[0] : null,
                            selectedDriver.getPhone(),
                            selectedDriver.getEmail(),
                            statusCombo.getValue(),
                            Double.parseDouble(salaryField.getText()),
                            coachCombo.getValue() != null ? coachCombo.getValue().split(" - ")[0] : null,
                            selectedDriver.getUserId()
                    );

                    FirebaseService.updateDriver(updatedDriver);

                    int index = driversList.indexOf(selectedDriver);
                    if (index >= 0) {
                        driversList.set(index, updatedDriver);
                        driversTable.refresh();
                    }

                    showSuccessAlert("Updated", "Driver information updated successfully.");

                } catch (Exception e) {
                    showErrorAlert("Error", "Failed to update driver: " + e.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    // ==============================
    // DIALOG METHODS - AGENCIES
    // ==============================

    private void showAddAgencyDialog() {
        Dialog<Agency> dialog = new Dialog<>();
        dialog.setTitle("Add New Agency");
        dialog.setHeaderText("Enter agency details");

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Agency Name");

        TextField locationField = new TextField();
        locationField.setPromptText("Location (Address)");

        TextField managerField = new TextField();
        managerField.setPromptText("Manager Name");

        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone Number");

        TextField emailField = new TextField();
        emailField.setPromptText("Email Address");

        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("Active", "Temporary Closed", "Permanent Closed");
        statusCombo.setValue("Active");

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Location:"), 0, 1);
        grid.add(locationField, 1, 1);
        grid.add(new Label("Manager:"), 0, 2);
        grid.add(managerField, 1, 2);
        grid.add(new Label("Phone:"), 0, 3);
        grid.add(phoneField, 1, 3);
        grid.add(new Label("Email:"), 0, 4);
        grid.add(emailField, 1, 4);
        grid.add(new Label("Status:"), 0, 5);
        grid.add(statusCombo, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    String nextId = "A" + String.format("%03d", agenciesList.size() + 1);
                    return new Agency(
                            nextId,
                            nameField.getText(),
                            locationField.getText(),
                            managerField.getText(),
                            phoneField.getText()
                    );
                } catch (Exception e) {
                    showErrorAlert("Error", "Invalid input: " + e.getMessage());
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(agency -> {
            if (agency != null) {
                try {
                    // For agencies, we need to add email and status to Firestore
                    Map<String, Object> agencyData = new HashMap<>();
                    agencyData.put("agencyId", agency.getAgencyId());
                    agencyData.put("name", agency.getName());
                    agencyData.put("location", agency.getLocation());
                    agencyData.put("manager", agency.getManager());
                    agencyData.put("phone", agency.getPhone());
                    agencyData.put("email", emailField.getText());
                    agencyData.put("status", statusCombo.getValue());

                    Firestore db = FirestoreClient.getFirestore();
                    db.collection("agencies").document(agency.getAgencyId()).set(agencyData);

                    // Update the local list with the Agency object (email/status not in model)
                    agenciesList.add(agency);
                    agenciesTable.refresh();

                    showSuccessAlert("Agency Added", "Agency has been successfully added.");
                } catch (Exception e) {
                    showErrorAlert("Error", "Failed to add agency: " + e.getMessage());
                }
            }
        });
    }

    private void showEditAgencyDialog() {
        Agency selectedAgency = agenciesTable.getSelectionModel().getSelectedItem();
        if (selectedAgency == null) {
            showErrorAlert("No Selection", "Please select an agency to edit.");
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Edit Agency");
        dialog.setHeaderText("Edit agency details");

        ButtonType updateButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // We need to get the full agency data from Firestore to get email and status
        String agencyEmail = "";
        String agencyStatus = "Active";

        try {
            Firestore db = FirestoreClient.getFirestore();
            var doc = db.collection("agencies")
                    .document(selectedAgency.getAgencyId())
                    .get()
                    .get();

            if (doc.exists()) {
                agencyEmail = doc.getString("email");
                if (agencyEmail == null) agencyEmail = "";

                agencyStatus = doc.getString("status");
                if (agencyStatus == null) agencyStatus = "Active";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        TextField nameField = new TextField(selectedAgency.getName());
        TextField locationField = new TextField(selectedAgency.getLocation());
        TextField managerField = new TextField(selectedAgency.getManager());
        TextField phoneField = new TextField(selectedAgency.getPhone());
        TextField emailField = new TextField(agencyEmail);

        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("Active", "Temporary Closed", "Permanent Closed");
        statusCombo.setValue(agencyStatus);

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Location:"), 0, 1);
        grid.add(locationField, 1, 1);
        grid.add(new Label("Manager:"), 0, 2);
        grid.add(managerField, 1, 2);
        grid.add(new Label("Phone:"), 0, 3);
        grid.add(phoneField, 1, 3);
        grid.add(new Label("Email:"), 0, 4);
        grid.add(emailField, 1, 4);
        grid.add(new Label("Status:"), 0, 5);
        grid.add(statusCombo, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == updateButtonType) {
                try {
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("name", nameField.getText());
                    updates.put("location", locationField.getText());
                    updates.put("manager", managerField.getText());
                    updates.put("phone", phoneField.getText());
                    updates.put("email", emailField.getText());
                    updates.put("status", statusCombo.getValue());

                    Firestore db = FirestoreClient.getFirestore();
                    db.collection("agencies")
                            .document(selectedAgency.getAgencyId())
                            .update(updates);

                    // Update local Agency object (basic fields only)
                    selectedAgency.setName(nameField.getText());
                    selectedAgency.setLocation(locationField.getText());
                    selectedAgency.setManager(managerField.getText());
                    selectedAgency.setPhone(phoneField.getText());

                    agenciesTable.refresh();
                    showSuccessAlert("Agency Updated", "Agency has been successfully updated.");

                } catch (Exception e) {
                    showErrorAlert("Error", "Failed to update agency: " + e.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void showDeleteAgencyDialog() {
        Agency selectedAgency = agenciesTable.getSelectionModel().getSelectedItem();
        if (selectedAgency == null) {
            showErrorAlert("No Selection", "Please select an agency to delete.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Agency");
        alert.setHeaderText("Confirm Deletion");
        alert.setContentText("Are you sure you want to delete agency: " + selectedAgency.getName() + "?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    FirebaseService.deleteAgency(selectedAgency.getAgencyId());
                    agenciesList.remove(selectedAgency);
                    showSuccessAlert("Agency Deleted", "Agency has been removed.");
                } catch (Exception e) {
                    showErrorAlert("Error", "Failed to delete agency: " + e.getMessage());
                }
            }
        });
    }

    // ==============================
    // INCIDENT MANAGEMENT METHODS
    // ==============================

    private void assignIncidentToManager() {
        Incident selectedIncident = incidentsTable.getSelectionModel().getSelectedItem();
        if (selectedIncident == null) {
            showErrorAlert("No Selection", "Please select an incident to assign.");
            return;
        }

        try {
            FirebaseService.assignIncidentToManager(selectedIncident.getId(), managerUsername);
            selectedIncident.setAssignedTo(managerUsername);
            selectedIncident.setStatus("in_progress");
            incidentsTable.refresh();
            showSuccessAlert("Incident Assigned", "Incident has been assigned to you.");
        } catch (Exception e) {
            showErrorAlert("Error", "Failed to assign incident: " + e.getMessage());
        }
    }

    private void resolveIncident() {
        Incident selectedIncident = incidentsTable.getSelectionModel().getSelectedItem();
        if (selectedIncident == null) {
            showErrorAlert("No Selection", "Please select an incident to resolve.");
            return;
        }

        try {
            FirebaseService.updateIncidentStatus(selectedIncident.getId(), "resolved");
            selectedIncident.setStatus("resolved");
            incidentsTable.refresh();
            showSuccessAlert("Incident Resolved", "Incident has been marked as resolved.");
        } catch (Exception e) {
            showErrorAlert("Error", "Failed to resolve incident: " + e.getMessage());
        }
    }

    private void closeIncident() {
        Incident selectedIncident = incidentsTable.getSelectionModel().getSelectedItem();
        if (selectedIncident == null) {
            showErrorAlert("No Selection", "Please select an incident to close.");
            return;
        }

        try {
            FirebaseService.updateIncidentStatus(selectedIncident.getId(), "closed");
            selectedIncident.setStatus("closed");
            incidentsTable.refresh();
            showSuccessAlert("Incident Closed", "Incident has been closed.");
        } catch (Exception e) {
            showErrorAlert("Error", "Failed to close incident: " + e.getMessage());
        }
    }

    // ==============================
    // RECLAMATION METHODS
    // ==============================

    private void showAddReclamationDialog() {
        Dialog<Reclamation> dialog = new Dialog<>();
        dialog.setTitle("Add New Reclamation");
        dialog.setHeaderText("Enter customer reclamation details");

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField customerField = new TextField();
        customerField.setPromptText("Customer Name");

        TextField emailField = new TextField();
        emailField.setPromptText("Customer Email");

        TextField phoneField = new TextField();
        phoneField.setPromptText("Customer Phone");

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("Delay", "Service Quality", "Vehicle Condition",
                "Driver Behavior", "Pricing Issue", "Other");
        typeCombo.setValue("Service Quality");

        TextArea descriptionField = new TextArea();
        descriptionField.setPromptText("Detailed description of the issue...");
        descriptionField.setPrefRowCount(4);
        descriptionField.setPrefWidth(300);

        DatePicker datePicker = new DatePicker(LocalDate.now());

        grid.add(new Label("Customer Name:"), 0, 0);
        grid.add(customerField, 1, 0);
        grid.add(new Label("Customer Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(new Label("Customer Phone:"), 0, 2);
        grid.add(phoneField, 1, 2);
        grid.add(new Label("Issue Type:"), 0, 3);
        grid.add(typeCombo, 1, 3);
        grid.add(new Label("Description:"), 0, 4);
        grid.add(descriptionField, 1, 4);
        grid.add(new Label("Date:"), 0, 5);
        grid.add(datePicker, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                if (customerField.getText().isEmpty() || descriptionField.getText().isEmpty()) {
                    showErrorAlert("Validation Error", "Customer name and description are required.");
                    return null;
                }

                try {
                    String nextId = "R" + String.format("%03d", reclamationsList.size() + 1);
                    return new Reclamation(
                            nextId,
                            datePicker.getValue().toString(),
                            customerField.getText(),
                            typeCombo.getValue(),
                            descriptionField.getText(),
                            "Pending"
                    );
                } catch (Exception e) {
                    showErrorAlert("Error", "Invalid input: " + e.getMessage());
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(reclamation -> {
            if (reclamation != null) {
                try {
                    // Add to Firestore with additional fields
                    Map<String, Object> reclamationData = new HashMap<>();
                    reclamationData.put("id", reclamation.getId());
                    reclamationData.put("date", reclamation.getDate());
                    reclamationData.put("customer", reclamation.getCustomer());
                    reclamationData.put("type", reclamation.getType());
                    reclamationData.put("description", reclamation.getDescription());
                    reclamationData.put("status", reclamation.getStatus());
                    reclamationData.put("customerEmail", emailField.getText());
                    reclamationData.put("customerPhone", phoneField.getText());
                    reclamationData.put("createdBy", managerUsername);
                    reclamationData.put("createdDate", LocalDate.now().toString());

                    Firestore db = FirestoreClient.getFirestore();
                    db.collection("reclamations").document(reclamation.getId()).set(reclamationData);

                    // Update local list
                    reclamationsList.add(reclamation);
                    reclamationsTable.refresh();

                    showSuccessAlert("Reclamation Added", "Customer reclamation has been recorded successfully.");

                } catch (Exception e) {
                    showErrorAlert("Error", "Failed to add reclamation: " + e.getMessage());
                }
            }
        });
    }

    private void processReclamation() {
        Reclamation selectedReclamation = reclamationsTable.getSelectionModel().getSelectedItem();
        if (selectedReclamation == null) {
            showErrorAlert("No Selection", "Please select a reclamation to process.");
            return;
        }

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Process Reclamation");
        dialog.setHeaderText("Process reclamation: " + selectedReclamation.getId());

        ButtonType processButton = new ButtonType("Process", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(processButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextArea notesField = new TextArea();
        notesField.setPromptText("Add processing notes or comments...");
        notesField.setPrefRowCount(4);
        notesField.setPrefWidth(300);

        ComboBox<String> priorityCombo = new ComboBox<>();
        priorityCombo.getItems().addAll("Low", "Medium", "High", "Urgent");
        priorityCombo.setValue("Medium");

        grid.add(new Label("Processing Notes:"), 0, 0);
        grid.add(notesField, 1, 0);
        grid.add(new Label("Priority:"), 0, 1);
        grid.add(priorityCombo, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == processButton) {
                return notesField.getText();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(notes -> {
            if (notes != null) {
                try {
                    // Update status in Firestore
                    FirebaseService.updateReclamationStatus(selectedReclamation.getId(), "In Progress");

                    // Also update priority and notes in Firestore
                    Firestore db = FirestoreClient.getFirestore();
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("status", "In Progress");
                    updates.put("priority", priorityCombo.getValue());
                    updates.put("processingNotes", notes);
                    updates.put("processedBy", managerUsername);
                    updates.put("processedDate", LocalDate.now().toString());

                    db.collection("reclamations").document(selectedReclamation.getId()).update(updates);

                    // Update local object
                    selectedReclamation.setStatus("In Progress");
                    reclamationsTable.refresh();

                    showSuccessAlert("Reclamation Processed",
                            "Reclamation is now being processed.\n" +
                                    "Priority: " + priorityCombo.getValue() + "\n" +
                                    "Assigned to: " + managerUsername);

                } catch (Exception e) {
                    showErrorAlert("Error", "Failed to process reclamation: " + e.getMessage());
                }
            }
        });
    }

    private void resolveReclamation() {
        Reclamation selectedReclamation = reclamationsTable.getSelectionModel().getSelectedItem();
        if (selectedReclamation == null) {
            showErrorAlert("No Selection", "Please select a reclamation to resolve.");
            return;
        }

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Resolve Reclamation");
        dialog.setHeaderText("Mark reclamation as resolved: " + selectedReclamation.getId());

        ButtonType resolveButton = new ButtonType("Mark Resolved", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(resolveButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextArea resolutionField = new TextArea();
        resolutionField.setPromptText("Describe the resolution or action taken...");
        resolutionField.setPrefRowCount(4);
        resolutionField.setPrefWidth(300);

        ComboBox<String> satisfactionCombo = new ComboBox<>();
        satisfactionCombo.getItems().addAll("Very Dissatisfied", "Dissatisfied", "Neutral", "Satisfied", "Very Satisfied");
        satisfactionCombo.setValue("Satisfied");

        grid.add(new Label("Resolution Details:"), 0, 0);
        grid.add(resolutionField, 1, 0);
        grid.add(new Label("Customer Satisfaction:"), 0, 1);
        grid.add(satisfactionCombo, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == resolveButton) {
                return resolutionField.getText();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(resolution -> {
            if (resolution != null) {
                try {
                    // Update status in Firestore
                    FirebaseService.updateReclamationStatus(selectedReclamation.getId(), "Resolved");

                    // Also update resolution details
                    Firestore db = FirestoreClient.getFirestore();
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("status", "Resolved");
                    updates.put("resolution", resolution);
                    updates.put("satisfaction", satisfactionCombo.getValue());
                    updates.put("resolvedBy", managerUsername);
                    updates.put("resolvedDate", LocalDate.now().toString());

                    db.collection("reclamations").document(selectedReclamation.getId()).update(updates);

                    // Update local object
                    selectedReclamation.setStatus("Resolved");
                    reclamationsTable.refresh();

                    showSuccessAlert("Reclamation Resolved",
                            "Reclamation has been marked as resolved.\n" +
                                    "Customer Satisfaction: " + satisfactionCombo.getValue() + "\n" +
                                    "Resolved by: " + managerUsername);

                } catch (Exception e) {
                    showErrorAlert("Error", "Failed to resolve reclamation: " + e.getMessage());
                }
            }
        });
    }

    private void closeReclamation() {
        Reclamation selectedReclamation = reclamationsTable.getSelectionModel().getSelectedItem();
        if (selectedReclamation == null) {
            showErrorAlert("No Selection", "Please select a reclamation to close.");
            return;
        }

        // Check if reclamation is already resolved
        if (!"Resolved".equals(selectedReclamation.getStatus()) &&
                !"In Progress".equals(selectedReclamation.getStatus())) {
            showErrorAlert("Cannot Close",
                    "Only reclamations with status 'Resolved' or 'In Progress' can be closed.\n" +
                            "Current status: " + selectedReclamation.getStatus());
            return;
        }

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Close Reclamation");
        dialog.setHeaderText("Close reclamation: " + selectedReclamation.getId());

        ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(closeButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextArea closingNotesField = new TextArea();
        closingNotesField.setPromptText("Add closing notes or final comments...");
        closingNotesField.setPrefRowCount(3);
        closingNotesField.setPrefWidth(300);

        CheckBox notifyCustomerCheck = new CheckBox("Notify customer by email");
        notifyCustomerCheck.setSelected(true);

        grid.add(new Label("Closing Notes:"), 0, 0);
        grid.add(closingNotesField, 1, 0);
        grid.add(new Label(""), 0, 1);
        grid.add(notifyCustomerCheck, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == closeButton) {
                return closingNotesField.getText();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(closingNotes -> {
            if (closingNotes != null) {
                try {
                    // Update status in Firestore
                    FirebaseService.updateReclamationStatus(selectedReclamation.getId(), "Closed");

                    // Also update closing details
                    Firestore db = FirestoreClient.getFirestore();
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("status", "Closed");
                    updates.put("closingNotes", closingNotes);
                    updates.put("closedBy", managerUsername);
                    updates.put("closedDate", LocalDate.now().toString());
                    updates.put("customerNotified", notifyCustomerCheck.isSelected());

                    db.collection("reclamations").document(selectedReclamation.getId()).update(updates);

                    // Update local object
                    selectedReclamation.setStatus("Closed");
                    reclamationsTable.refresh();

                    String notificationMsg = notifyCustomerCheck.isSelected() ?
                            "\nCustomer has been notified." : "\nCustomer was not notified.";

                    showSuccessAlert("Reclamation Closed",
                            "Reclamation has been closed successfully." + notificationMsg);

                } catch (Exception e) {
                    showErrorAlert("Error", "Failed to close reclamation: " + e.getMessage());
                }
            }
        });
    }

    // ==============================
    // REPORT GENERATION METHODS
    // ==============================

    private void generateDriverReport() {
        try {
            StringBuilder report = new StringBuilder();
            report.append("DRIVER PERFORMANCE REPORT\n");
            report.append("Generated on: ").append(LocalDate.now()).append("\n");
            report.append("Generated by: ").append(managerUsername).append("\n");
            report.append("=".repeat(50)).append("\n\n");

            report.append("Total Drivers: ").append(driversList.size()).append("\n");

            // Count by status
            long active = driversList.stream().filter(d -> "active".equals(d.getStatus())).count();
            long onLeave = driversList.stream().filter(d -> "on_leave".equals(d.getStatus())).count();
            long suspended = driversList.stream().filter(d -> "suspended".equals(d.getStatus())).count();

            report.append("Active: ").append(active).append("\n");
            report.append("On Leave: ").append(onLeave).append("\n");
            report.append("Suspended: ").append(suspended).append("\n\n");

            report.append("DRIVER DETAILS:\n");
            report.append("-".repeat(50)).append("\n");

            for (Driver driver : driversList) {
                report.append(String.format("ID: %-10s Name: %-20s Status: %-10s Salary: $%.2f\n",
                        driver.getDriverId(), driver.getName(), driver.getStatus(), driver.getSalary()));
            }

            showReportDialog("Driver Report", report.toString());

        } catch (Exception e) {
            showErrorAlert("Report Error", "Failed to generate driver report: " + e.getMessage());
        }
    }

    private void generateIncidentReport() {
        try {
            StringBuilder report = new StringBuilder();
            report.append("INCIDENT ANALYSIS REPORT\n");
            report.append("Generated on: ").append(LocalDate.now()).append("\n");
            report.append("Generated by: ").append(managerUsername).append("\n");
            report.append("=".repeat(50)).append("\n\n");

            report.append("Total Incidents: ").append(incidentsList.size()).append("\n");

            // Count by status
            long pending = incidentsList.stream().filter(i -> "pending".equals(i.getStatus())).count();
            long inProgress = incidentsList.stream().filter(i -> "in_progress".equals(i.getStatus())).count();
            long resolved = incidentsList.stream().filter(i -> "resolved".equals(i.getStatus())).count();
            long closed = incidentsList.stream().filter(i -> "closed".equals(i.getStatus())).count();

            report.append("Pending: ").append(pending).append("\n");
            report.append("In Progress: ").append(inProgress).append("\n");
            report.append("Resolved: ").append(resolved).append("\n");
            report.append("Closed: ").append(closed).append("\n\n");

            report.append("RECENT INCIDENTS:\n");
            report.append("-".repeat(50)).append("\n");

            for (Incident incident : incidentsList.stream().limit(10).toList()) {
                report.append(String.format("ID: %-10s Type: %-15s Driver: %-10s Status: %-12s Date: %s\n",
                        incident.getId(), incident.getType(), incident.getDriverId(),
                        incident.getStatus(), incident.getDate()));
            }

            showReportDialog("Incident Report", report.toString());

        } catch (Exception e) {
            showErrorAlert("Report Error", "Failed to generate incident report: " + e.getMessage());
        }
    }

    private void generateFleetReport() {
        try {
            StringBuilder report = new StringBuilder();
            report.append("FLEET UTILIZATION REPORT\n");
            report.append("Generated on: ").append(LocalDate.now()).append("\n");
            report.append("Generated by: ").append(managerUsername).append("\n");
            report.append("=".repeat(50)).append("\n\n");

            report.append("Total Coaches: ").append(coachesList.size()).append("\n");

            // Count by status
            long active = coachesList.stream().filter(c -> "Active".equalsIgnoreCase(c.getStatus())).count();
            long maintenance = coachesList.stream().filter(c -> "Maintenance".equalsIgnoreCase(c.getStatus())).count();
            long inactive = coachesList.stream().filter(c -> "Inactive".equalsIgnoreCase(c.getStatus())).count();

            report.append("Active: ").append(active).append("\n");
            report.append("In Maintenance: ").append(maintenance).append("\n");
            report.append("Inactive: ").append(inactive).append("\n\n");

            report.append("FLEET DETAILS:\n");
            report.append("-".repeat(50)).append("\n");

            for (Coach coach : coachesList) {
                report.append(String.format("ID: %-10s Model: %-20s Plate: %-10s Capacity: %-5s Status: %s\n",
                        coach.getId(), coach.getModel(), coach.getLicensePlate(),
                        coach.getCapacity(), coach.getStatus()));
            }

            showReportDialog("Fleet Report", report.toString());

        } catch (Exception e) {
            showErrorAlert("Report Error", "Failed to generate fleet report: " + e.getMessage());
        }
    }

    private void generateFinancialReport() {
        try {
            StringBuilder report = new StringBuilder();
            report.append("FINANCIAL REPORT\n");
            report.append("Generated on: ").append(LocalDate.now()).append("\n");
            report.append("Generated by: ").append(managerUsername).append("\n");
            report.append("=".repeat(50)).append("\n\n");

            double totalSalary = driversList.stream()
                    .mapToDouble(Driver::getSalary)
                    .sum();

            double avgSalary = driversList.isEmpty() ? 0 : totalSalary / driversList.size();

            report.append("FINANCIAL SUMMARY:\n");
            report.append("Total Drivers: ").append(driversList.size()).append("\n");
            report.append("Total Monthly Salary Cost: $").append(String.format("%.2f", totalSalary)).append("\n");
            report.append("Average Driver Salary: $").append(String.format("%.2f", avgSalary)).append("\n\n");

            report.append("SALARY BREAKDOWN:\n");
            report.append("-".repeat(50)).append("\n");

            for (Driver driver : driversList) {
                report.append(String.format("%-20s: $%.2f\n", driver.getName(), driver.getSalary()));
            }

            showReportDialog("Financial Report", report.toString());

        } catch (Exception e) {
            showErrorAlert("Report Error", "Failed to generate financial report: " + e.getMessage());
        }
    }

    private void showReportDialog(String title, String content) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText("Report Generated");

        ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.OK_DONE);
        ButtonType exportButton = new ButtonType("Export to File", ButtonBar.ButtonData.OTHER);
        dialog.getDialogPane().getButtonTypes().addAll(exportButton, closeButton);

        TextArea textArea = new TextArea(content);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefSize(600, 400);

        dialog.getDialogPane().setContent(textArea);

        // Set up export button action
        Button exportBtn = (Button) dialog.getDialogPane().lookupButton(exportButton);
        exportBtn.setOnAction(e -> {
            showSuccessAlert("Export", "Report exported successfully (file save dialog would open here).");
        });

        dialog.showAndWait();
    }

    // ==============================
    // REFRESH METHODS
    // ==============================

    private void refreshCoachesTable() {
        try {
            coachesList = FirebaseService.loadAllCoaches();
            coachesTable.setItems(coachesList);
            coachesTable.refresh();
            showSuccessAlert("Refreshed", "Coaches list has been refreshed.");
        } catch (Exception e) {
            showErrorAlert("Error", "Failed to refresh coaches: " + e.getMessage());
        }
    }

    private void refreshLinesTable() {
        try {
            linesList = FirebaseService.loadAllLines();
            linesTable.setItems(linesList);
            linesTable.refresh();
            showSuccessAlert("Refreshed", "Lines list has been refreshed.");
        } catch (Exception e) {
            showErrorAlert("Error", "Failed to refresh lines: " + e.getMessage());
        }
    }

    private void refreshAgenciesTable() {
        try {
            agenciesList = FirebaseService.loadAllAgencies();
            agenciesTable.setItems(agenciesList);
            agenciesTable.refresh();
            showSuccessAlert("Refreshed", "Agencies list has been refreshed.");
        } catch (Exception e) {
            showErrorAlert("Error", "Failed to refresh agencies: " + e.getMessage());
        }
    }

    private void refreshDriversTable() {
        try {
            driversList = FirebaseService.loadAllDrivers();
            driversTable.setItems(driversList);
            driversTable.refresh();
            showSuccessAlert("Refreshed", "Drivers list has been refreshed.");
        } catch (Exception e) {
            showErrorAlert("Error", "Failed to refresh drivers: " + e.getMessage());
        }
    }

    private void refreshIncidentsTable() {
        try {
            int oldSize = incidentsList.size();
            incidentsList = FirebaseService.loadAllIncidents();

            incidentsTable.setItems(incidentsList);
            incidentsTable.refresh();

            int newSize = incidentsList.size();
            if (newSize > oldSize) {
                showSuccessAlert("New Incidents",
                        (newSize - oldSize) + " new incident(s) detected from drivers!");
            } else {
                showSuccessAlert("Refreshed", "Incidents list has been refreshed.");
            }
        } catch (Exception e) {
            showErrorAlert("Error", "Failed to refresh incidents: " + e.getMessage());
        }
    }


    private void refreshReclamationsTable() {
        try {
            reclamationsList = FirebaseService.loadAllReclamations();
            reclamationsTable.setItems(reclamationsList);
            reclamationsTable.refresh();
            showSuccessAlert("Refreshed", "Reclamations list has been refreshed.");
        } catch (Exception e) {
            showErrorAlert("Error", "Failed to refresh reclamations: " + e.getMessage());
        }
    }

    // ==============================
    // UTILITY METHODS
    // ==============================

    private Button createActionButton(String text, String color) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        btn.setTextFill(Color.WHITE);
        btn.setPrefHeight(40);
        btn.setPadding(new Insets(10, 25, 10, 25));
        btn.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 6, 0.3, 0, 2);"
        );

        btn.setOnMouseEntered(e -> {
            btn.setStyle(
                    "-fx-background-color: " + DARK_GREEN + ";" +
                            "-fx-background-radius: 8;" +
                            "-fx-cursor: hand;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0.4, 0, 3);"
            );
        });

        btn.setOnMouseExited(e -> {
            btn.setStyle(
                    "-fx-background-color: " + color + ";" +
                            "-fx-background-radius: 8;" +
                            "-fx-cursor: hand;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 6, 0.3, 0, 2);"
            );
        });

        return btn;
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

    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setHeaderText("Confirm Logout");
        alert.setContentText("Are you sure you want to logout?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Arrêter le scheduler
                if (incidentScheduler != null && !incidentScheduler.isShutdown()) {
                    incidentScheduler.shutdown();
                    System.out.println("✅ Incident auto-refresh scheduler stopped");
                }

                Stage stage = (Stage) mainContainer.getScene().getWindow();
                stage.close();
            }
        });
    }
}

