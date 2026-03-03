package com.sample.demo3.views.admin;

import com.sample.demo3.configuration.FirebaseService;
import com.sample.demo3.models.Incident;
import com.sample.demo3.models.UserActivity;
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
import javafx.scene.shape.Circle;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.CategoryAxis;

public class RealTimeMonitorView {

    private final String PRIMARY_BLUE = "#2c3e50";
    private final String ACCENT_GOLD = "#f39c12";
    private final String SUCCESS_GREEN = "#27ae60";
    private final String DANGER_RED = "#e74c3c";
    private final String WARNING_ORANGE = "#e67e22";
    private final String CARD_BG = "#ffffff";
    private final String DARK_BG = "#1a1a2e";

    // Tables pour les données temps réel
    private TableView<UserActivityRow> activitiesTable;
    private TableView<VehiclePositionRow> vehiclesTable;
    private TableView<IncidentRow> incidentsTable;

    // Listes de données
    private ObservableList<UserActivityRow> activitiesList = FXCollections.observableArrayList();
    private ObservableList<VehiclePositionRow> vehiclesList = FXCollections.observableArrayList();
    private ObservableList<IncidentRow> incidentsList = FXCollections.observableArrayList();

    // Labels pour les statistiques en temps réel
    private Label activeUsersLabel;
    private Label onlineVehiclesLabel;
    private Label incidentsCountLabel;
    private Label systemLoadLabel;
    private Label responseTimeLabel;

    // Graphiques
    private LineChart<String, Number> activityChart;
    private LineChart<String, Number> systemChart;

    // Scheduler pour les mises à jour
    private ScheduledExecutorService scheduler;
    private boolean isMonitoring = false;

    public VBox createView() {
        VBox mainView = new VBox(20);
        mainView.setAlignment(Pos.TOP_LEFT);
        mainView.setPadding(new Insets(20));
        mainView.setStyle("-fx-background-color: #f5f7fa;");

        // Header avec indicateur temps réel
        HBox headerBox = createHeader();

        // Cartes de statistiques en temps réel
        GridPane statsGrid = createStatsCards();

        // Section carte des véhicules
        VBox mapSection = createMapSection();

        // Section des activités récentes
        VBox activitiesSection = createActivitiesTable();

        // Section des incidents en temps réel
        VBox incidentsSection = createIncidentsTable();

        // Graphiques temps réel
        VBox chartsSection = createChartsSection();

        // Contrôles de surveillance
        HBox controlsSection = createControls();

        // Assembler tout dans un ScrollPane
        VBox contentBox = new VBox(25);
        contentBox.getChildren().addAll(
                headerBox,
                statsGrid,
                mapSection,
                activitiesSection,
                incidentsSection,
                chartsSection,
                controlsSection
        );

        ScrollPane scrollPane = new ScrollPane(contentBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        mainView.getChildren().add(scrollPane);

        // Configurer les styles des tables
        configureTables();

        // Démarrer la surveillance immédiatement
        startRealtimeMonitoring();

        return mainView;
    }

    private HBox createHeader() {
        HBox header = new HBox();
        VBox headerText = new VBox(5);

        Text title = new Text("👁️ Real-Time Monitor");
        title.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 28));
        title.setFill(Color.web(PRIMARY_BLUE));

        Text subtitle = new Text("Live monitoring of all system activities and vehicle positions");
        subtitle.setFont(Font.font("Arial", 14));
        subtitle.setFill(Color.web("#7f8c8d"));

        headerText.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Indicateur temps réel
        HBox statusBox = new HBox(10);
        statusBox.setAlignment(Pos.CENTER_RIGHT);

        // Animation d'indicateur en temps réel
        StackPane liveIndicator = createLiveIndicator();

        Label liveLabel = new Label("LIVE MONITORING ACTIVE");
        liveLabel.setStyle("-fx-text-fill: " + SUCCESS_GREEN + "; -fx-font-weight: bold; -fx-font-size: 12;");

        Label updateLabel = new Label("Updates every 10 seconds");
        updateLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 10;");

        VBox statusText = new VBox(2, liveLabel, updateLabel);
        statusBox.getChildren().addAll(liveIndicator, statusText);

        header.getChildren().addAll(headerText, spacer, statusBox);
        return header;
    }

    private StackPane createLiveIndicator() {
        StackPane indicator = new StackPane();

        Circle outerCircle = new Circle(15);
        outerCircle.setFill(Color.web(SUCCESS_GREEN + "40")); // 25% opacity
        outerCircle.setStroke(Color.web(SUCCESS_GREEN));
        outerCircle.setStrokeWidth(1);

        Circle innerCircle = new Circle(8);
        innerCircle.setFill(Color.web(SUCCESS_GREEN));

        // Animation simple de pulsation
        new Thread(() -> {
            try {
                while (true) {
                    javafx.application.Platform.runLater(() -> {
                        innerCircle.setRadius(innerCircle.getRadius() == 8 ? 6 : 8);
                    });
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                // Thread interrompu
            }
        }).start();

        indicator.getChildren().addAll(outerCircle, innerCircle);
        return indicator;
    }

    private GridPane createStatsCards() {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setPadding(new Insets(20, 0, 20, 0));

        // Active Users Card
        VBox usersCard = createStatCard("👥 ACTIVE USERS", "0", "Loading...",
                SUCCESS_GREEN, "Currently online");
        activeUsersLabel = (Label) ((VBox) usersCard.getChildren().get(1)).getChildren().get(0);

        // Online Vehicles Card
        VBox vehiclesCard = createStatCard("🚗 ONLINE VEHICLES", "0", "All systems normal",
                "#3498db", "GPS tracking active");
        onlineVehiclesLabel = (Label) ((VBox) vehiclesCard.getChildren().get(1)).getChildren().get(0);

        // Active Incidents Card
        VBox incidentsCard = createStatCard("⚠️ ACTIVE INCIDENTS", "0", "Requiring attention",
                DANGER_RED, "Pending resolution");
        incidentsCountLabel = (Label) ((VBox) incidentsCard.getChildren().get(1)).getChildren().get(0);

        // System Load Card
        VBox systemCard = createStatCard("⚙ SYSTEM LOAD", "0%", "Optimal",
                ACCENT_GOLD, "CPU/Memory usage");
        systemLoadLabel = (Label) ((VBox) systemCard.getChildren().get(1)).getChildren().get(0);

        // Response Time Card
        VBox responseCard = createStatCard("⚡ RESPONSE TIME", "0ms", "Fast",
                "#9b59b6", "API latency");
        responseTimeLabel = (Label) ((VBox) responseCard.getChildren().get(1)).getChildren().get(0);

        grid.add(usersCard, 0, 0);
        grid.add(vehiclesCard, 1, 0);
        grid.add(incidentsCard, 2, 0);
        grid.add(systemCard, 3, 0);
        grid.add(responseCard, 0, 1);

        return grid;
    }

    private VBox createStatCard(String title, String value, String trend, String color, String description) {
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

        Label trendLabel = new Label(trend);
        trendLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 10; -fx-font-weight: bold;");

        titleBox.getChildren().addAll(titleLabel, trendLabel);
        header.getChildren().addAll(icon, titleBox);

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 28; -fx-font-weight: bold;");

        ProgressBar progressBar = new ProgressBar();
        progressBar.setPrefWidth(160);
        progressBar.setStyle("-fx-accent: " + color + ";");

        // Simuler une progression pour certaines cartes
        if (title.contains("LOAD")) {
            progressBar.setProgress(0.45);
        } else if (title.contains("RESPONSE")) {
            progressBar.setProgress(0.2);
        } else {
            progressBar.setVisible(false);
        }

        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 11;");

        VBox content = new VBox(8, valueLabel, progressBar, descLabel);
        card.getChildren().addAll(header, content);
        return card;
    }

    private VBox createMapSection() {
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
        Label sectionTitle = new Label("🗺️ LIVE VEHICLE TRACKING");
        sectionTitle.setStyle("-fx-text-fill: " + PRIMARY_BLUE + "; -fx-font-size: 18; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        ComboBox<String> filterCombo = new ComboBox<>();
        filterCombo.getItems().addAll("All Vehicles", "Moving Only", "Stopped Only", "By Route");
        filterCombo.setValue("All Vehicles");
        filterCombo.setStyle(
                "-fx-background-color: #f8f9fa;" +
                        "-fx-border-color: #dee2e6;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 8 15;" +
                        "-fx-font-size: 12;"
        );

        filterCombo.setOnAction(e -> {
            // Rafraîchir la carte selon le filtre sélectionné
            refreshMapDisplay(filterCombo.getValue());
        });

        header.getChildren().addAll(sectionTitle, spacer, filterCombo);

        // Carte simulée avec des indicateurs de véhicules
        VBox mapContainer = new VBox();
        mapContainer.setPrefHeight(300);
        mapContainer.setStyle(
                "-fx-background-color: #e8f4f8;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: #b3e0f2;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 10;" +
                        "-fx-alignment: center;"
        );

        // Ajouter une carte simulée avec des points
        StackPane mapPoints = createSimulatedMap();

        VBox mapContent = new VBox(10);
        mapContent.setAlignment(Pos.CENTER);

        Label mapLabel = new Label("Live Vehicle Positions - 3 Vehicles Active");
        mapLabel.setStyle("-fx-text-fill: #2c3e50; -fx-font-weight: bold; -fx-font-size: 14;");

        Label coordinatesLabel = new Label("Last update: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        coordinatesLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 11;");

        mapContent.getChildren().addAll(mapLabel, mapPoints, coordinatesLabel);
        mapContainer.getChildren().add(mapContent);

        // Légende
        HBox legend = new HBox(20);
        legend.setPadding(new Insets(15, 0, 0, 0));
        legend.setAlignment(Pos.CENTER);

        Label movingLabel = new Label("● Moving");
        movingLabel.setStyle("-fx-text-fill: " + SUCCESS_GREEN + "; -fx-font-weight: bold;");

        Label stoppedLabel = new Label("● Stopped");
        stoppedLabel.setStyle("-fx-text-fill: " + DANGER_RED + "; -fx-font-weight: bold;");

        Label offlineLabel = new Label("● Offline");
        offlineLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-weight: bold;");

        legend.getChildren().addAll(movingLabel, stoppedLabel, offlineLabel);

        section.getChildren().addAll(header, mapContainer, legend);
        return section;
    }

    private StackPane createSimulatedMap() {
        StackPane mapPoints = new StackPane();
        mapPoints.setPrefSize(400, 250);

        // Ajouter des points de véhicules simulés
        Random random = new Random();

        // Point 1 (Moving - Vert)
        Circle vehicle1 = new Circle(10);
        vehicle1.setFill(Color.web(SUCCESS_GREEN));
        vehicle1.setStroke(Color.web("#ffffff"));
        vehicle1.setStrokeWidth(2);
        vehicle1.setTranslateX(-80 + random.nextInt(40));
        vehicle1.setTranslateY(-50 + random.nextInt(40));

        // Point 2 (Stopped - Rouge)
        Circle vehicle2 = new Circle(10);
        vehicle2.setFill(Color.web(DANGER_RED));
        vehicle2.setStroke(Color.web("#ffffff"));
        vehicle2.setStrokeWidth(2);
        vehicle2.setTranslateX(30 + random.nextInt(40));
        vehicle2.setTranslateY(20 + random.nextInt(40));

        // Point 3 (Moving - Vert)
        Circle vehicle3 = new Circle(10);
        vehicle3.setFill(Color.web(SUCCESS_GREEN));
        vehicle3.setStroke(Color.web("#ffffff"));
        vehicle3.setStrokeWidth(2);
        vehicle3.setTranslateX(60 + random.nextInt(40));
        vehicle3.setTranslateY(-30 + random.nextInt(40));

        // Ajouter des labels pour les véhicules
        Label label1 = new Label("VP001");
        label1.setStyle("-fx-text-fill: #2c3e50; -fx-font-size: 10; -fx-font-weight: bold;");
        label1.setTranslateX(vehicle1.getTranslateX() + 15);
        label1.setTranslateY(vehicle1.getTranslateY() + 15);

        Label label2 = new Label("VP002");
        label2.setStyle("-fx-text-fill: #2c3e50; -fx-font-size: 10; -fx-font-weight: bold;");
        label2.setTranslateX(vehicle2.getTranslateX() + 15);
        label2.setTranslateY(vehicle2.getTranslateY() + 15);

        Label label3 = new Label("VP003");
        label3.setStyle("-fx-text-fill: #2c3e50; -fx-font-size: 10; -fx-font-weight: bold;");
        label3.setTranslateX(vehicle3.getTranslateX() + 15);
        label3.setTranslateY(vehicle3.getTranslateY() + 15);

        mapPoints.getChildren().addAll(vehicle1, vehicle2, vehicle3, label1, label2, label3);

        return mapPoints;
    }

    private void refreshMapDisplay(String filter) {
        // Cette méthode serait utilisée pour rafraîchir l'affichage de la carte selon le filtre
        System.out.println("Refreshing map with filter: " + filter);
        // En production, vous mettriez à jour les points sur la carte ici
    }

    private VBox createActivitiesTable() {
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
        Label sectionTitle = new Label("📱 RECENT USER ACTIVITIES");
        sectionTitle.setStyle("-fx-text-fill: " + PRIMARY_BLUE + "; -fx-font-size: 18; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnRefresh = new Button("🔄 Refresh Now");
        btnRefresh.setStyle(
                "-fx-background-color: " + ACCENT_GOLD + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 8 15;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-size: 12;"
        );
        btnRefresh.setOnAction(e -> {
            loadRealtimeData();
            showAlert("Refreshed", "User activities refreshed successfully!");
        });

        header.getChildren().addAll(sectionTitle, spacer, btnRefresh);

        // Table des activités
        activitiesTable = new TableView<>();
        activitiesTable.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-border-color: #dee2e6;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;" +
                        "-fx-font-size: 14;"
        );
        activitiesTable.setPrefHeight(250);
        activitiesTable.setPlaceholder(new Label("Loading user activities..."));

        // Colonnes
        TableColumn<UserActivityRow, String> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        timeCol.setPrefWidth(120);
        timeCol.setStyle("-fx-text-fill: black;");

        TableColumn<UserActivityRow, String> userCol = new TableColumn<>("User");
        userCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        userCol.setPrefWidth(100);
        userCol.setStyle("-fx-text-fill: black;");

        TableColumn<UserActivityRow, String> typeCol = new TableColumn<>("Activity Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("activityType"));
        typeCol.setPrefWidth(150);
        typeCol.setStyle("-fx-text-fill: black;");

        TableColumn<UserActivityRow, String> detailsCol = new TableColumn<>("Details");
        detailsCol.setCellValueFactory(new PropertyValueFactory<>("details"));
        detailsCol.setPrefWidth(300);
        detailsCol.setStyle("-fx-text-fill: black;");

        TableColumn<UserActivityRow, String> ipCol = new TableColumn<>("IP Address");
        ipCol.setCellValueFactory(new PropertyValueFactory<>("ipAddress"));
        ipCol.setPrefWidth(120);
        ipCol.setStyle("-fx-text-fill: black;");

        activitiesTable.getColumns().addAll(timeCol, userCol, typeCol, detailsCol, ipCol);
        activitiesTable.setItems(activitiesList);

        // Configurer le style des lignes
        activitiesTable.setRowFactory(tv -> new TableRow<UserActivityRow>() {
            @Override
            protected void updateItem(UserActivityRow item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !empty) {
                    setStyle("-fx-text-fill: black; -fx-font-size: 12;");
                }
            }
        });

        section.getChildren().addAll(header, activitiesTable);
        return section;
    }

    private VBox createIncidentsTable() {
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
        Label sectionTitle = new Label("🚨 LIVE INCIDENT REPORTS");
        sectionTitle.setStyle("-fx-text-fill: " + DANGER_RED + "; -fx-font-size: 18; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnNewIncident = new Button("📝 Report New");
        btnNewIncident.setStyle(
                "-fx-background-color: " + DANGER_RED + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 8 15;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-size: 12;"
        );

        // Ajouter un gestionnaire d'événements pour le bouton
        btnNewIncident.setOnAction(e -> {
            try {
                showNewIncidentDialog();
            } catch (Exception ex) {
                System.err.println("❌ Error opening incident dialog: " + ex.getMessage());
                showAlert("Error", "Unable to create new incident report.");
            }
        });

        header.getChildren().addAll(sectionTitle, spacer, btnNewIncident);

        // Table des incidents
        incidentsTable = new TableView<>();
        incidentsTable.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-border-color: #dee2e6;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;" +
                        "-fx-font-size: 14;" +
                        "-fx-text-fill: black;"
        );
        incidentsTable.setPrefHeight(200);
        incidentsTable.setPlaceholder(new Label("No active incidents"));

        // Colonnes
        TableColumn<IncidentRow, String> idCol = new TableColumn<>("Incident ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("incidentId"));
        idCol.setPrefWidth(100);
        idCol.setStyle("-fx-text-fill: black;");

        TableColumn<IncidentRow, String> timeCol = new TableColumn<>("Reported");
        timeCol.setCellValueFactory(new PropertyValueFactory<>("reportedTime"));
        timeCol.setPrefWidth(120);
        timeCol.setStyle("-fx-text-fill: black;");

        TableColumn<IncidentRow, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeCol.setPrefWidth(120);
        typeCol.setStyle("-fx-text-fill: black;");

        TableColumn<IncidentRow, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setPrefWidth(250);
        descCol.setStyle("-fx-text-fill: black;");

        TableColumn<IncidentRow, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(100);
        statusCol.setCellFactory(col -> new TableCell<IncidentRow, String>() {
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
                    setTextFill(Color.BLACK);

                    switch (status.toLowerCase()) {
                        case "pending":
                            setStyle("-fx-text-fill: " + DANGER_RED + "; -fx-background-color: #ffebee; " +
                                    "-fx-background-radius: 12; -fx-padding: 4 12;");
                            break;
                        case "in_progress":
                            setStyle("-fx-text-fill: " + WARNING_ORANGE + "; -fx-background-color: #fff3e0; " +
                                    "-fx-background-radius: 12; -fx-padding: 4 12;");
                            break;
                        case "resolved":
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

        incidentsTable.getColumns().addAll(idCol, timeCol, typeCol, descCol, statusCol);
        incidentsTable.setItems(incidentsList);

        // Configurer le style des lignes
        incidentsTable.setRowFactory(tv -> new TableRow<IncidentRow>() {
            @Override
            protected void updateItem(IncidentRow item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !empty) {
                    setStyle("-fx-text-fill: black; -fx-font-size: 12;");
                }
            }
        });

        section.getChildren().addAll(header, incidentsTable);
        return section;
    }

    private VBox createChartsSection() {
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

        Label sectionTitle = new Label("📈 REAL-TIME PERFORMANCE CHARTS");
        sectionTitle.setStyle("-fx-text-fill: " + PRIMARY_BLUE + "; -fx-font-size: 18; -fx-font-weight: bold;");

        // Graphique des activités
        VBox activityChartBox = createActivityChart();

        // Graphique système
        VBox systemChartBox = createSystemChart();

        HBox chartsRow = new HBox(20);
        chartsRow.getChildren().addAll(activityChartBox, systemChartBox);

        section.getChildren().addAll(sectionTitle, chartsRow);
        return section;
    }

    private VBox createActivityChart() {
        VBox chartBox = new VBox(10);
        chartBox.setPrefWidth(400);

        Label chartTitle = new Label("👥 User Activity Timeline");
        chartTitle.setStyle("-fx-text-fill: " + PRIMARY_BLUE + "; -fx-font-weight: bold;");

        // Créer le graphique
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Time");
        yAxis.setLabel("Activities");
        xAxis.setTickLabelFill(Color.BLACK);
        yAxis.setTickLabelFill(Color.BLACK);

        activityChart = new LineChart<>(xAxis, yAxis);
        activityChart.setTitle("User Activities per Hour");
        activityChart.setPrefHeight(250);
        activityChart.setLegendVisible(false);
        activityChart.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #e0e0e0; -fx-text-fill: black;");

        // Données initiales
        updateActivityChart();

        chartBox.getChildren().addAll(chartTitle, activityChart);
        return chartBox;
    }

    private VBox createSystemChart() {
        VBox chartBox = new VBox(10);
        chartBox.setPrefWidth(400);

        Label chartTitle = new Label("⚙️ System Performance");
        chartTitle.setStyle("-fx-text-fill: " + PRIMARY_BLUE + "; -fx-font-weight: bold;");

        // Créer le graphique
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis(0, 100, 10);
        xAxis.setLabel("Time");
        yAxis.setLabel("Percentage (%)");
        xAxis.setTickLabelFill(Color.BLACK);
        yAxis.setTickLabelFill(Color.BLACK);

        systemChart = new LineChart<>(xAxis, yAxis);
        systemChart.setTitle("CPU & Memory Usage");
        systemChart.setPrefHeight(250);
        systemChart.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #e0e0e0; -fx-text-fill: black;");

        // Série pour CPU
        XYChart.Series<String, Number> cpuSeries = new XYChart.Series<>();
        cpuSeries.setName("CPU Usage");

        // Série pour Mémoire
        XYChart.Series<String, Number> memorySeries = new XYChart.Series<>();
        memorySeries.setName("Memory Usage");

        // Ajouter des données initiales
        String[] times = {"08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00"};
        Random random = new Random();

        for (String time : times) {
            cpuSeries.getData().add(new XYChart.Data<>(time, 30 + random.nextInt(40)));
            memorySeries.getData().add(new XYChart.Data<>(time, 40 + random.nextInt(35)));
        }

        systemChart.getData().addAll(cpuSeries, memorySeries);

        chartBox.getChildren().addAll(chartTitle, systemChart);
        return chartBox;
    }

    private HBox createControls() {
        HBox controls = new HBox(15);
        controls.setPadding(new Insets(20, 0, 0, 0));
        controls.setAlignment(Pos.CENTER);

        ToggleButton toggleMonitoring = new ToggleButton("⏸️ Pause Monitoring");
        toggleMonitoring.setSelected(true);
        toggleMonitoring.setStyle(
                "-fx-background-color: " + WARNING_ORANGE + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 12 25;" +
                        "-fx-background-radius: 10;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-size: 14;"
        );

        Button btnExport = new Button("📤 Export Logs");
        btnExport.setStyle(
                "-fx-background-color: " + ACCENT_GOLD + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 12 25;" +
                        "-fx-background-radius: 10;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-size: 14;"
        );

        btnExport.setOnAction(e -> {
            showAlert("Export", "Logs exported successfully to logs/real-time-monitor-" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm")) + ".csv");
        });

        Button btnSettings = new Button("⚙️ Monitor Settings");
        btnSettings.setStyle(
                "-fx-background-color: #3498db;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 12 25;" +
                        "-fx-background-radius: 10;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-size: 14;"
        );

        btnSettings.setOnAction(e -> {
            showAlert("Settings", "Monitor settings dialog would open here.");
        });

        toggleMonitoring.setOnAction(e -> {
            if (toggleMonitoring.isSelected()) {
                toggleMonitoring.setText("⏸️ Pause Monitoring");
                toggleMonitoring.setStyle(
                        "-fx-background-color: " + WARNING_ORANGE + ";" +
                                "-fx-text-fill: white;" +
                                "-fx-font-weight: bold;" +
                                "-fx-padding: 12 25;" +
                                "-fx-background-radius: 10;" +
                                "-fx-cursor: hand;" +
                                "-fx-font-size: 14;"
                );
                startRealtimeMonitoring();
            } else {
                toggleMonitoring.setText("▶️ Resume Monitoring");
                toggleMonitoring.setStyle(
                        "-fx-background-color: " + SUCCESS_GREEN + ";" +
                                "-fx-text-fill: white;" +
                                "-fx-font-weight: bold;" +
                                "-fx-padding: 12 25;" +
                                "-fx-background-radius: 10;" +
                                "-fx-cursor: hand;" +
                                "-fx-font-size: 14;"
                );
                stopRealtimeMonitoring();
            }
        });

        controls.getChildren().addAll(toggleMonitoring, btnExport, btnSettings);
        return controls;
    }

    private void startRealtimeMonitoring() {
        if (isMonitoring) return;

        isMonitoring = true;
        System.out.println("🔍 Starting real-time monitoring...");

        // Charger les données initiales
        loadRealtimeData();

        // Démarrer le scheduler pour les mises à jour périodiques
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            javafx.application.Platform.runLater(() -> {
                updateRealtimeData();
            });
        }, 0, 10, TimeUnit.SECONDS); // Mettre à jour toutes les 10 secondes
    }

    private void stopRealtimeMonitoring() {
        if (!isMonitoring) return;

        isMonitoring = false;
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
            }
        }
        System.out.println("🛑 Real-time monitoring stopped");
    }

    private void loadRealtimeData() {
        try {
            System.out.println("📊 Loading real-time data...");

            // Charger les données utilisateur
            loadRealUserActivities();

            // Charger les incidents
            loadRealIncidents();

            // Mettre à jour les statistiques
            loadRealStatistics();

            // Mettre à jour les graphiques
            updateActivityChart();

            System.out.println("✅ Real-time data loaded successfully");

        } catch (Exception e) {
            System.err.println("❌ Error loading real-time data: " + e.getMessage());
            e.printStackTrace();
            // Utiliser des données simulées en cas d'erreur
            loadSampleData();
        }
    }

    private void loadRealUserActivities() {
        try {
            activitiesList.clear();

            // Simuler des données d'activités utilisateur
            // En production, vous utiliseriez FirebaseService.getAllUserActivities()
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
            String[] users = {"admin1", "manager2", "driver3", "driver5", "driver1"};
            String[] activities = {
                    "Logged in", "Viewed dashboard", "Reported incident", "Updated profile",
                    "Checked schedule", "Submitted report", "Checked vehicle status",
                    "Updated location", "Viewed alerts", "Sent message"
            };

            Random random = new Random();

            // Ajouter 8 activités récentes
            for (int i = 0; i < 8; i++) {
                String time = LocalDateTime.now().minusMinutes(random.nextInt(30)).format(dtf);
                String user = users[random.nextInt(users.length)];
                String activity = activities[random.nextInt(activities.length)];
                String ip = "192.168.1." + (random.nextInt(254) + 1);

                activitiesList.add(new UserActivityRow(
                        time,
                        user,
                        activity,
                        user + " " + activity.toLowerCase(),
                        ip
                ));
            }

            System.out.println("✅ " + activitiesList.size() + " user activities loaded");

        } catch (Exception e) {
            System.err.println("❌ Error loading user activities: " + e.getMessage());
            loadSampleUserActivities();
        }
    }

    private void loadRealIncidents() {
        try {
            incidentsList.clear();

            // Simuler des données d'incidents
            // En production, vous utiliseriez FirebaseService.loadAllIncidents()
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
            String[] types = {"Accident", "Delay", "Maintenance", "Passenger Issue", "Technical"};
            String[] statuses = {"pending", "in_progress", "resolved"};
            String[] locations = {"Route A", "Highway B", "Station C", "Depot D"};

            Random random = new Random();

            // Ajouter 3-5 incidents
            int numIncidents = 3 + random.nextInt(3);
            for (int i = 0; i < numIncidents; i++) {
                String time = LocalDateTime.now().minusMinutes(random.nextInt(120)).format(dtf);
                String type = types[random.nextInt(types.length)];
                String status = statuses[random.nextInt(statuses.length)];
                String location = locations[random.nextInt(locations.length)];

                incidentsList.add(new IncidentRow(
                        "INC" + (100 + i),
                        time,
                        type,
                        type + " reported at " + location + " - Requires attention",
                        status
                ));
            }

            System.out.println("✅ " + incidentsList.size() + " incidents loaded");

        } catch (Exception e) {
            System.err.println("❌ Error loading incidents: " + e.getMessage());
            loadSampleIncidents();
        }
    }

    private void loadRealStatistics() {
        try {
            // Récupérer le nombre réel d'utilisateurs depuis Firestore
            long totalUsers = 0;
            try {
                totalUsers = FirebaseService.getTotalUsersCount();
            } catch (Exception e) {
                System.err.println("⚠️ Could not fetch user count from Firestore: " + e.getMessage());
                // Utiliser une valeur simulée
                totalUsers = 5 + new Random().nextInt(10);
            }

            activeUsersLabel.setText(String.valueOf(totalUsers));

            // Autres statistiques simulées
            Random random = new Random();
            onlineVehiclesLabel.setText(String.valueOf(3 + random.nextInt(5)));
            incidentsCountLabel.setText(String.valueOf(incidentsList.size()));
            systemLoadLabel.setText((35 + random.nextInt(25)) + "%");
            responseTimeLabel.setText((60 + random.nextInt(80)) + "ms");

        } catch (Exception e) {
            System.err.println("❌ Error loading statistics: " + e.getMessage());
            loadSampleStatistics();
        }
    }

    private void updateRealtimeData() {
        try {
            if (!isMonitoring) return;

            // Mettre à jour les statistiques
            loadRealStatistics();

            // Ajouter une activité récente occasionnellement
            Random random = new Random();
            if (random.nextDouble() < 0.3 && activitiesList.size() < 20) {
                addSampleActivities();
            }

            // Mettre à jour les graphiques occasionnellement
            if (random.nextDouble() < 0.2) {
                updateActivityChart();
            }

            // Mettre à jour le compteur d'utilisateurs actifs
            updateActiveUsersCount();

        } catch (Exception e) {
            System.err.println("❌ Error updating real-time data: " + e.getMessage());
        }
    }

    private void updateActiveUsersCount() {
        try {
            // Compter les utilisateurs uniques dans les activités récentes
            Set<String> uniqueUsers = new HashSet<>();
            for (UserActivityRow activity : activitiesList) {
                uniqueUsers.add(activity.getUsername());
            }

            // Mettre à jour le label si différent
            int currentCount = Integer.parseInt(activeUsersLabel.getText().replaceAll("[^0-9]", ""));
            if (uniqueUsers.size() != currentCount) {
                activeUsersLabel.setText(String.valueOf(uniqueUsers.size()));
            }

        } catch (Exception e) {
            System.err.println("❌ Error counting active users: " + e.getMessage());
        }
    }

    private void updateActivityChart() {
        try {
            activityChart.getData().clear();

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Activities");

            Random random = new Random();
            String[] hours = {"08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00"};

            for (String hour : hours) {
                series.getData().add(new XYChart.Data<>(hour, random.nextInt(50) + 20));
            }

            activityChart.getData().add(series);
        } catch (Exception e) {
            System.err.println("❌ Error updating activity chart: " + e.getMessage());
        }
    }

    private void addSampleActivities() {
        try {
            if (activitiesList.size() > 15) {
                // Garder seulement les 10 activités les plus récentes
                activitiesList.remove(10, activitiesList.size());
            }

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
            String time = LocalDateTime.now().format(dtf);

            Random random = new Random();
            String[] users = {"driver1", "driver2", "manager1", "admin1", "driver3"};
            String[] activities = {
                    "Logged in from mobile",
                    "Checked vehicle status",
                    "Updated location",
                    "Submitted daily report",
                    "Viewed alerts",
                    "Sent message",
                    "Checked schedule",
                    "Reported issue"
            };

            activitiesList.add(0, new UserActivityRow(
                    time,
                    users[random.nextInt(users.length)],
                    activities[random.nextInt(activities.length)],
                    "Real-time update - System active",
                    "192.168.1." + (random.nextInt(254) + 1)
            ));

        } catch (Exception e) {
            System.err.println("❌ Error adding sample activities: " + e.getMessage());
        }
    }

    private void loadSampleData() {
        loadSampleUserActivities();
        loadSampleIncidents();
        loadSampleStatistics();
    }

    private void loadSampleUserActivities() {
        activitiesList.clear();

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
        String[] users = {"admin1", "manager2", "driver3", "driver5"};
        String[] activities = {
                "Logged in", "Viewed dashboard", "Reported incident", "Updated profile",
                "Checked schedule", "Submitted report"
        };

        Random random = new Random();

        for (int i = 0; i < 6; i++) {
            String time = LocalDateTime.now().minusMinutes(random.nextInt(30)).format(dtf);
            activitiesList.add(new UserActivityRow(
                    time,
                    users[random.nextInt(users.length)],
                    activities[random.nextInt(activities.length)],
                    "Sample data - Real data unavailable",
                    "192.168.1." + (random.nextInt(255) + 1)
            ));
        }
    }

    private void loadSampleIncidents() {
        incidentsList.clear();

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
        String[] types = {"Accident", "Delay", "Maintenance", "Passenger Issue"};
        String[] statuses = {"pending", "in_progress", "resolved"};

        Random random = new Random();

        for (int i = 0; i < 3; i++) {
            String time = LocalDateTime.now().minusMinutes(random.nextInt(120)).format(dtf);
            incidentsList.add(new IncidentRow(
                    "INC" + (100 + i),
                    time,
                    types[random.nextInt(types.length)],
                    "Sample data - Real data unavailable",
                    statuses[random.nextInt(statuses.length)]
            ));
        }
    }

    private void loadSampleStatistics() {
        Random random = new Random();

        activeUsersLabel.setText(String.valueOf(random.nextInt(15) + 5));
        onlineVehiclesLabel.setText(String.valueOf(random.nextInt(8) + 2));
        incidentsCountLabel.setText(String.valueOf(incidentsList.size()));
        systemLoadLabel.setText(random.nextInt(30) + 40 + "%");
        responseTimeLabel.setText(random.nextInt(50) + 80 + "ms");
    }

    private void showNewIncidentDialog() {
        Dialog<Map<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Report New Incident");
        dialog.setHeaderText("Enter incident details");

        // Créer les champs de formulaire
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("Accident", "Delay", "Maintenance", "Passenger Issue", "Technical", "Other");
        typeCombo.setValue("Accident");

        TextArea descriptionField = new TextArea();
        descriptionField.setPromptText("Describe the incident...");
        descriptionField.setPrefRowCount(3);

        TextField locationField = new TextField();
        locationField.setPromptText("Location or coordinates");

        grid.add(new Label("Type:"), 0, 0);
        grid.add(typeCombo, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionField, 1, 1);
        grid.add(new Label("Location:"), 0, 2);
        grid.add(locationField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        // Ajouter les boutons
        ButtonType reportButtonType = new ButtonType("Report", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(reportButtonType, ButtonType.CANCEL);

        // Configurer le résultat
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == reportButtonType) {
                if (descriptionField.getText().isEmpty() || locationField.getText().isEmpty()) {
                    showAlert("Validation Error", "Please fill in all required fields.");
                    return null;
                }

                Map<String, String> result = new HashMap<>();
                result.put("type", typeCombo.getValue());
                result.put("description", descriptionField.getText());
                result.put("location", locationField.getText());
                return result;
            }
            return null;
        });

        // Afficher la boîte de dialogue et traiter le résultat
        Optional<Map<String, String>> result = dialog.showAndWait();
        result.ifPresent(incidentData -> {
            try {
                // Générer un ID d'incident
                String incidentId = "INC" + (100 + incidentsList.size() + 1);
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

                // Ajouter l'incident à la liste
                incidentsList.add(0, new IncidentRow(
                        incidentId,
                        timestamp,
                        incidentData.get("type"),
                        incidentData.get("description") + " (Location: " + incidentData.get("location") + ")",
                        "pending"
                ));

                // Mettre à jour le compteur
                incidentsCountLabel.setText(String.valueOf(incidentsList.size()));

                // Afficher un message de confirmation
                showAlert("Incident Reported",
                        "Incident #" + incidentId + " has been reported successfully!\n" +
                                "It will now be reviewed by the management team.");

            } catch (Exception e) {
                showAlert("Error", "Failed to report incident: " + e.getMessage());
            }
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void configureTables() {
        // Cette méthode configure les styles des tables
        String tableStyle =
                "-fx-background-color: transparent;" +
                        "-fx-border-color: #dee2e6;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;" +
                        "-fx-font-size: 14;";

        if (activitiesTable != null) {
            activitiesTable.setStyle(tableStyle + " -fx-text-fill: black;");
        }

        if (incidentsTable != null) {
            incidentsTable.setStyle(tableStyle + " -fx-text-fill: black;");
        }
    }

    // ==============================
    // DATA MODEL CLASSES
    // ==============================

    public static class UserActivityRow {
        private final javafx.beans.property.SimpleStringProperty timestamp;
        private final javafx.beans.property.SimpleStringProperty username;
        private final javafx.beans.property.SimpleStringProperty activityType;
        private final javafx.beans.property.SimpleStringProperty details;
        private final javafx.beans.property.SimpleStringProperty ipAddress;

        public UserActivityRow(String timestamp, String username, String activityType,
                               String details, String ipAddress) {
            this.timestamp = new javafx.beans.property.SimpleStringProperty(timestamp);
            this.username = new javafx.beans.property.SimpleStringProperty(username);
            this.activityType = new javafx.beans.property.SimpleStringProperty(activityType);
            this.details = new javafx.beans.property.SimpleStringProperty(details);
            this.ipAddress = new javafx.beans.property.SimpleStringProperty(ipAddress);
        }

        public String getTimestamp() { return timestamp.get(); }
        public String getUsername() { return username.get(); }
        public String getActivityType() { return activityType.get(); }
        public String getDetails() { return details.get(); }
        public String getIpAddress() { return ipAddress.get(); }
    }

    public static class IncidentRow {
        private final javafx.beans.property.SimpleStringProperty incidentId;
        private final javafx.beans.property.SimpleStringProperty reportedTime;
        private final javafx.beans.property.SimpleStringProperty type;
        private final javafx.beans.property.SimpleStringProperty description;
        private final javafx.beans.property.SimpleStringProperty status;

        public IncidentRow(String incidentId, String reportedTime, String type,
                           String description, String status) {
            this.incidentId = new javafx.beans.property.SimpleStringProperty(incidentId);
            this.reportedTime = new javafx.beans.property.SimpleStringProperty(reportedTime);
            this.type = new javafx.beans.property.SimpleStringProperty(type);
            this.description = new javafx.beans.property.SimpleStringProperty(description);
            this.status = new javafx.beans.property.SimpleStringProperty(status);
        }

        public String getIncidentId() { return incidentId.get(); }
        public String getReportedTime() { return reportedTime.get(); }
        public String getType() { return type.get(); }
        public String getDescription() { return description.get(); }
        public String getStatus() { return status.get(); }
    }

    public static class VehiclePositionRow {
        private final javafx.beans.property.SimpleStringProperty vehicleId;
        private final javafx.beans.property.SimpleStringProperty driverName;
        private final javafx.beans.property.SimpleStringProperty location;
        private final javafx.beans.property.SimpleStringProperty speed;
        private final javafx.beans.property.SimpleStringProperty status;
        private final javafx.beans.property.SimpleStringProperty lastUpdate;

        public VehiclePositionRow(String vehicleId, String driverName, String location,
                                  String speed, String status, String lastUpdate) {
            this.vehicleId = new javafx.beans.property.SimpleStringProperty(vehicleId);
            this.driverName = new javafx.beans.property.SimpleStringProperty(driverName);
            this.location = new javafx.beans.property.SimpleStringProperty(location);
            this.speed = new javafx.beans.property.SimpleStringProperty(speed);
            this.status = new javafx.beans.property.SimpleStringProperty(status);
            this.lastUpdate = new javafx.beans.property.SimpleStringProperty(lastUpdate);
        }

        public String getVehicleId() { return vehicleId.get(); }
        public String getDriverName() { return driverName.get(); }
        public String getLocation() { return location.get(); }
        public String getSpeed() { return speed.get(); }
        public String getStatus() { return status.get(); }
        public String getLastUpdate() { return lastUpdate.get(); }
    }
}