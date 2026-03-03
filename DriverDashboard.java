package com.sample.demo3.views;

import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import com.sample.demo3.configuration.FirebaseConfig;
import com.sample.demo3.configuration.FirebaseService;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import com.sample.demo3.models.Incident;
import com.sample.demo3.models.Driver;
import com.sample.demo3.models.Coach;
import com.sample.demo3.models.TransportLine;
import javafx.util.Duration;

public class DriverDashboard extends Application {
    // Conteneur principal pour l'interface
    private BorderPane mainContainer;
    private VBox sidebarMenu;
    private StackPane contentArea;
    private String currentUser = "DRIVER";
    private String fullName = "JAMES WILSON";
    private String driverId = "D001";
    private String assignedCoachId = "C001";
    private String assignedLineId = "L1";

    // Color scheme matching Translink branding
    private final String PRIMARY_GREEN = "#6ba93a";
    private final String DARK_GREEN = "#4d8a1c";
    private final String LIGHT_BG = "#f5f5f5";
    private final String WHITE = "#ffffff";
    private final String TEXT_DARK = "#333333";
    private final String TEXT_SECONDARY = "#666666";
    private final String BLUE_ACCENT = "#2196f3";
    private final String ORANGE_ACCENT = "#ff8c00";
    private final String RED_ACCENT = "#f44336";

    // // Collections de données pour les tableaux
    private ObservableList<com.sample.demo3.models.Incident> incidentsList = FXCollections.observableArrayList();
    private ObservableList<TripHistory> tripHistoryList = FXCollections.observableArrayList();
    private ObservableList<MaintenanceItem> maintenanceList = FXCollections.observableArrayList();
    private ObservableList<UpcomingTrip> upcomingTripsList = FXCollections.observableArrayList();
    // Tableaux pour afficher les données
    private TableView<Incident> incidentsTable;
    private TableView<TripHistory> tripHistoryTable = new TableView<>();

    // Driver data from Firebase
    private Driver driverData;
    private Coach assignedCoach;
    private TransportLine assignedLine;

    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialize Firebase
            FirebaseConfig.init();

            // Load driver data
            loadDriverData();

            mainContainer = new BorderPane();

            // Create top header
            HBox header = createHeader();

            // Create sidebar navigation
            sidebarMenu = createSidebar();

            // Create content area
            contentArea = new StackPane();
            contentArea.setStyle("-fx-background-color: " + LIGHT_BG + ";");
            contentArea.setPadding(new Insets(30));

            // Set default view (Dashboard)
            showDashboardView();

            // Assemble layout
            mainContainer.setTop(header);
            mainContainer.setLeft(sidebarMenu);
            mainContainer.setCenter(contentArea);
            // Créer la scène principale
            Scene scene = new Scene(mainContainer, 1400, 800);
            primaryStage.setTitle("Translink -  Eco Mobility System");
            primaryStage.setScene(scene);
            primaryStage.show();

            // Load sample data
            loadSampleData();
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Application Error", "Failed to start application: " + e.getMessage());
        }
    }

    // Method to set driver information
    public void setDriverInfo(String username, String fullName, String driverId) {
        this.currentUser = username != null ? username : "DRIVER";
        this.fullName = fullName != null ? fullName : "JAMES WILSON";
        this.driverId = driverId != null ? driverId : "D001";

        // Load driver-specific data
        try {
            loadDriverData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadDriverData() throws Exception {
        // Load driver info from Firebase
        ObservableList<Driver> drivers = FirebaseService.loadAllDrivers();
        for (Driver driver : drivers) {
            if (driver.getDriverId().equals(driverId)) {
                this.driverData = driver;
                this.assignedLineId = driver.getAssignedLine();
                break;
            }
        }

        // Load assigned coach
        ObservableList<Coach> coaches = FirebaseService.loadAllCoaches();
        //use the first coach
        if (!coaches.isEmpty()) {
            this.assignedCoach = coaches.get(0);
            this.assignedCoachId = assignedCoach.getId();
        }

        // Load assigned line
        ObservableList<TransportLine> lines = FirebaseService.loadAllLines();
        for (TransportLine line : lines) {
            if (line.getLineId().equals(assignedLineId)) {
                this.assignedLine = line;
                break;
            }
        }
    }
    // Créer l'en-tête supérieur avec logo et informations utilisateur
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

        Text subtitleText = new Text("ECO MOBILITY SYSTEMS");
        subtitleText.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
        subtitleText.setFill(Color.web(TEXT_DARK));

        logoBox.getChildren().addAll(titleText, subtitleText);

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Section informations utilisateur
        VBox userInfo = new VBox(2);
        userInfo.setAlignment(Pos.CENTER_RIGHT);

        Text userName = new Text(fullName);
        userName.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        userName.setFill(Color.web(TEXT_DARK));

        Text userRole = new Text("DRIVER");
        userRole.setFont(Font.font("Arial", FontWeight.NORMAL, 11));
        userRole.setFill(Color.web(TEXT_SECONDARY));

        userInfo.getChildren().addAll(userName, userRole);

        // User avatar circle
        Circle avatar = new Circle(25);
        avatar.setFill(Color.web("#cccccc"));
        avatar.setStroke(Color.web(PRIMARY_GREEN));
        avatar.setStrokeWidth(2);

        HBox userSection = new HBox(15);
        userSection.setAlignment(Pos.CENTER_RIGHT);
        userSection.getChildren().addAll(userInfo, avatar);

        header.getChildren().addAll(logoBox, spacer, userSection);

        return header;
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(15);
        sidebar.setPrefWidth(280);
        sidebar.setPadding(new Insets(30, 15, 30, 15));
        sidebar.setStyle("-fx-background-color: " + PRIMARY_GREEN + "; -fx-background-radius: 0 10 10 0;");

        // Menu buttons avec icônes modernes
        Button btnDashboard = createMenuButton("📊", "DASHBOARD", true);
        Button btnSchedule = createMenuButton("🗓", "MY SCHEDULE", false);
        Button btnCoach = createMenuButton("🚍", "MY VEHICLE", false);
        Button btnIncidents = createMenuButton("🚨", "INCIDENTS", false);
        Button btnMaintenance = createMenuButton("🔩", "MAINTENANCE", false);
        Button btnTripHistory = createMenuButton("📈", "TRIP HISTORY", false);

        // Actions des boutons
        btnDashboard.setOnAction(e -> {
            showDashboardView();
            updateSidebarButtons(btnDashboard);
        });
        btnSchedule.setOnAction(e -> {
            showScheduleView();
            updateSidebarButtons(btnSchedule);
        });
        btnCoach.setOnAction(e -> {
            showVehicleView();
            updateSidebarButtons(btnCoach);
        });
        btnIncidents.setOnAction(e -> {
            showIncidentsView();
            updateSidebarButtons(btnIncidents);
        });
        btnMaintenance.setOnAction(e -> {
            showMaintenanceView();
            updateSidebarButtons(btnMaintenance);
        });
        btnTripHistory.setOnAction(e -> {
            showTripHistoryView();
            updateSidebarButtons(btnTripHistory);
        });

        // Espaceur pour pousser le bouton logout vers le bas
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Logout button
        Button btnLogout = createLogoutButton();
        btnLogout.setOnAction(e -> handleLogout());

        sidebar.getChildren().addAll(
                btnDashboard,
                btnSchedule,
                btnCoach,
                btnIncidents,
                btnMaintenance,
                btnTripHistory,
                spacer,
                btnLogout
        );
        return sidebar;
    }
    // Vérifier périodiquement les assignations de ligne
    private void checkLineAssignments() {
        // Vérifier périodiquement si une ligne a été assignée
        Timeline assignmentCheck = new Timeline(
                new KeyFrame(Duration.seconds(10), e -> {
                    try {
                        // Recharger les données du driver depuis Firestore
                        ObservableList<Driver> drivers = FirebaseService.loadAllDrivers();
                        for (Driver driver : drivers) {
                            if (driver.getDriverId().equals(driverId)) {
                                if (driver.getAssignedLine() != null &&
                                        !driver.getAssignedLine().equals(assignedLineId)) {

                                    // Mise à jour détectée !
                                    assignedLineId = driver.getAssignedLine();
                                    assignedLine = findLineById(assignedLineId);
                                    // Rafraîchir l'affichage
                                    showDashboardView();
                                }
                                break;
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                })
        );
        assignmentCheck.setCycleCount(Timeline.INDEFINITE);
        assignmentCheck.play();
    }

    // Trouver une ligne par son ID
    private TransportLine findLineById(String lineId) {
        try {
            ObservableList<TransportLine> lines = FirebaseService.loadAllLines();
            for (TransportLine line : lines) {
                if (line.getLineId().equals(lineId)) {
                    return line;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    // Créer un bouton de menu stylisé
    private Button createMenuButton(String icon, String text, boolean active) {
        Button btn = new Button();

        HBox content = new HBox(15);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setPadding(new Insets(0, 20, 0, 20));

        Text iconText = new Text(icon);
        iconText.setFont(Font.font(22));
        iconText.setFill(active ? Color.web(TEXT_DARK) : Color.WHITE);

        Text labelText = new Text(text);
        labelText.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        labelText.setFill(active ? Color.web(TEXT_DARK) : Color.WHITE);

        content.getChildren().addAll(iconText, labelText);
        btn.setGraphic(content);

        btn.setPrefWidth(250);
        btn.setPrefHeight(50);
        // Style différent selon si le bouton est actif ou non
        if (active) {
            btn.setStyle(
                    "-fx-background-color: " + WHITE + ";" +
                            "-fx-background-radius: 25;" +
                            "-fx-cursor: hand;" +
                            "-fx-border-width: 0;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0.3, 0, 2);"
            );
        } else {
            btn.setStyle(
                    "-fx-background-color: transparent;" +
                            "-fx-border-width: 0;" +
                            "-fx-cursor: hand;"
            );
        }
        // Effet hover au survol de la souris
        btn.setOnMouseEntered(e -> {
            if (!active) {
                btn.setStyle(
                        "-fx-background-color: rgba(255,255,255,0.15);" +
                                "-fx-background-radius: 25;" +
                                "-fx-cursor: hand;" +
                                "-fx-border-width: 0;" +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 5, 0.2, 0, 1);"
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
        // Ombre portée pour effet de profondeur
        DropShadow shadow = new DropShadow();
        shadow.setRadius(5);
        shadow.setColor(Color.rgb(0, 0, 0, 0.1));
        btn.setEffect(shadow);

        return btn;
    }

    // Mise à jour de l'apparence des boutons du sidebar
    private void updateSidebarButtons(Button activeButton) {
        for (var node : sidebarMenu.getChildren()) {
            if (node instanceof Button) {
                Button btn = (Button) node;
                HBox content = (HBox) btn.getGraphic();
                if (content != null && content.getChildren().size() > 1) {
                    Text iconText = (Text) content.getChildren().get(0);
                    Text labelText = (Text) content.getChildren().get(1);
                    // Applique le style actif au bouton sélectionné
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
   // Création du bouton de déconnexion
    private Button createLogoutButton() {
        Button btn = new Button();

        HBox content = new HBox(15);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setPadding(new Insets(0, 20, 0, 20));

        Text iconText = new Text("🚪");
        iconText.setFont(Font.font(20));
        iconText.setFill(Color.WHITE);

        Text labelText = new Text("LOGOUT");
        labelText.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        labelText.setFill(Color.WHITE);

        content.getChildren().addAll(iconText, labelText);
        btn.setGraphic(content);

        btn.setPrefWidth(250);
        btn.setPrefHeight(50);
        btn.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-border-width: 0;" +
                        "-fx-cursor: hand;"
        );

        btn.setOnMouseEntered(e -> {
            btn.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.15);" +
                            "-fx-background-radius: 15;" +
                            "-fx-border-width: 0;" +
                            "-fx-cursor: hand;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 5, 0.2, 0, 1);"
            );
        });
        // Effets hover pour le bouton logout
        btn.setOnMouseExited(e -> {
            btn.setStyle(
                    "-fx-background-color: transparent;" +
                            "-fx-border-width: 0;" +
                            "-fx-cursor: hand;" +
                            "-fx-effect: none;"
            );
        });

        return btn;
    }

    // DASHBOARD VIEW - Updated with Firebase data
    private void showDashboardView() {
        contentArea.getChildren().clear();

        VBox dashboardView = new VBox(20);
        dashboardView.setAlignment(Pos.TOP_LEFT);
        dashboardView.setPadding(new Insets(20));

        // Header
        Text title = new Text("Driver Dashboard");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setFill(Color.web(PRIMARY_GREEN));

        Text subtitle = new Text("Overview of your current assignment and vehicle status.");
        subtitle.setFont(Font.font("Arial", 14));
        subtitle.setFill(Color.web(TEXT_SECONDARY));

        // Ligne de cartes de statistiques
        HBox statsRow = new HBox(20);
        statsRow.setPadding(new Insets(20, 0, 20, 0));

        String lineInfo = assignedLine != null ? assignedLine.getRoute() : "Not Assigned";
        String coachInfo = assignedCoach != null ? "Plate: " + assignedCoach.getLicensePlate() : "Not Assigned";

        VBox nextDepartureCard = createStatCard("⏱ NEXT DEPARTURE", "08:00 AM",
                assignedLineId + " - " + lineInfo, "On Schedule", BLUE_ACCENT);
        VBox vehicleStatusCard = createStatCard("🚍 VEHICLE STATUS",
                assignedCoach != null ? assignedCoach.getStatus() : "N/A",
                coachInfo, "Ready for service", PRIMARY_GREEN);
        VBox fuelLevelCard = createStatCard("⛽ FUEL LEVEL", "95%", "Range: ~350 km", "", ORANGE_ACCENT);
        VBox weeklyHoursCard = createStatCard("⏰ WEEKLY HOURS", "39 hrs", "Total", "", "#6c757d");

        statsRow.getChildren().addAll(nextDepartureCard, vehicleStatusCard, fuelLevelCard, weeklyHoursCard);

        // Today's Schedule Section
        VBox scheduleSection = createTodaysSchedule();

        // Co-Driver Info
        VBox coDriverSection = createCoDriverInfo();

        // Assemblage final
        dashboardView.getChildren().addAll(title, subtitle, statsRow, scheduleSection, coDriverSection);

        ScrollPane scrollPane = new ScrollPane(dashboardView);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        contentArea.getChildren().add(scrollPane);
    }

    // Création d'une carte de statistique
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
        //titre de carte
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", 12));
        titleLabel.setStyle("-fx-text-fill: #757575;");
        //valeur principal
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        valueLabel.setStyle("-fx-text-fill: #212121;");

        Label detailLabel = new Label(detail);
        detailLabel.setFont(Font.font("Arial", 13));
        detailLabel.setStyle("-fx-text-fill: " + PRIMARY_GREEN + ";");

        card.getChildren().addAll(titleLabel, valueLabel, detailLabel);
        // Status optionnel
        if (!status.isEmpty()) {
            Label statusLabel = new Label(status);
            statusLabel.setFont(Font.font("Arial", 11));
            statusLabel.setStyle("-fx-text-fill: #666666; -fx-padding: 5 0 0 0;");
            card.getChildren().add(statusLabel);
        }

        return card;
    }

    private VBox createTodaysSchedule() {
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
        Label title = new Label("📅 Today's Schedule");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        title.setStyle("-fx-text-fill: #212121;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Hyperlink viewCalendar = new Hyperlink("View Full Calendar");
        viewCalendar.setStyle("-fx-text-fill: " + PRIMARY_GREEN + "; -fx-font-size: 14; -fx-underline: false; -fx-font-weight: bold;");
        viewCalendar.setOnAction(e -> showScheduleView());

        headerBox.getChildren().addAll(title, spacer, viewCalendar);

        // Today's trips using Firebase data
        if (assignedLine != null && assignedCoach != null) {
            VBox trip1 = createTripCard("08:00 AM", assignedLineId,"" ,
                    "Marackech", "Casablanca",
                    "10:30 AM", "2 Stops", assignedCoach.getModel(), true);
            VBox trip2 = createTripCard("02:00 PM", assignedLineId, "",
                    "Casablanca", "Meknes",
                    "05:30 PM", "2 Stops", assignedCoach.getModel(), false);
            VBox trip3 = createTripCard("06:00 PM", "LO04", "",
                    "Meknes", "Huceima",
                    "00:15 AM", "3 Stops", assignedCoach.getModel(), false);

            section.getChildren().addAll(headerBox, trip1, trip2, trip3);
        } else {
            Label noSchedule = new Label("No schedule assigned. Please contact manager.");
            noSchedule.setStyle("-fx-text-fill: #666666; -fx-font-size: 14; -fx-padding: 20;");
            section.getChildren().addAll(headerBox, noSchedule);
        }

        return section;
    }

    // Création d'une carte de trajet individuel
    private VBox createTripCard(String time, String line, String status, String from, String to,
                                String arrivalTime, String stops, String coach, boolean isLive) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setStyle(
                "-fx-background-color: #fafafa;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.03), 5, 0.2, 0, 1);"
        );

        HBox topRow = new HBox(15);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label timeLabel = new Label("⏰ " + time);
        timeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        timeLabel.setStyle("-fx-text-fill: #212121;");

        Label lineLabel = new Label("🚌 " + line);
        lineLabel.setStyle(
                "-fx-background-color: " + PRIMARY_GREEN + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 4 12;" +
                        "-fx-background-radius: 15;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 12;"
        );

        topRow.getChildren().addAll(timeLabel, lineLabel);

        if (isLive) {
            Label liveLabel = new Label("🔴 LIVE");
            liveLabel.setStyle(
                    "-fx-background-color: " + BLUE_ACCENT + ";" +
                            "-fx-text-fill: white;" +
                            "-fx-padding: 4 12;" +
                            "-fx-background-radius: 15;" +
                            "-fx-font-weight: bold;" +
                            "-fx-font-size: 11;"
            );
            topRow.getChildren().add(liveLabel);
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        topRow.getChildren().add(spacer);

        // Bouton pour visualiser l'itinéraire
        Button viewRouteBtn = new Button("🗺️ View Route");
        viewRouteBtn.setStyle(
                "-fx-background-color: " + BLUE_ACCENT + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 6 15;" +
                        "-fx-background-radius: 6;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 12;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0.2, 0, 1);"
        );

        viewRouteBtn.setOnMouseEntered(e -> viewRouteBtn.setStyle(
                "-fx-background-color: #1976d2;" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 6 15;" +
                        "-fx-background-radius: 6;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 12;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 6, 0.3, 0, 2);"
        ));

        viewRouteBtn.setOnMouseExited(e -> viewRouteBtn.setStyle(
                "-fx-background-color: " + BLUE_ACCENT + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 6 15;" +
                        "-fx-background-radius: 6;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 12;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0.2, 0, 1);"
        ));

        // Stocker les données du voyage pour le diagramme
        TripData tripData = new TripData(from, to, stops, time, arrivalTime);
        viewRouteBtn.setOnAction(e -> showRouteDiagram(tripData));

        topRow.getChildren().add(viewRouteBtn);

        // Route
        HBox routeBox = new HBox(10);
        routeBox.setAlignment(Pos.CENTER_LEFT);
        routeBox.setPadding(new Insets(10, 0, 0, 0));

        Label fromLabel = new Label("📍 " + from);
        fromLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        fromLabel.setStyle("-fx-text-fill: " + PRIMARY_GREEN + ";");

        Label arrow = new Label("➡");
        arrow.setStyle("-fx-text-fill: #757575;");

        Label toLabel = new Label("📍 " + to);
        toLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        toLabel.setStyle("-fx-text-fill: " + BLUE_ACCENT + ";");

        routeBox.getChildren().addAll(fromLabel, arrow, toLabel);

        // Details
        HBox detailsRow = new HBox(20);
        detailsRow.setAlignment(Pos.CENTER_LEFT);
        detailsRow.setPadding(new Insets(10, 0, 0, 0));

        Label arrivalLabel = new Label("🕒 Arrival: " + arrivalTime);
        arrivalLabel.setFont(Font.font("Arial", 13));
        arrivalLabel.setStyle("-fx-text-fill: #666666;");

        Label stopsLabel = new Label("🛑 " + stops);
        stopsLabel.setFont(Font.font("Arial", 13));
        stopsLabel.setStyle("-fx-text-fill: #666666;");

        Label coachLabel = new Label("🚌 " + coach);
        coachLabel.setFont(Font.font("Arial", 13));
        coachLabel.setStyle("-fx-text-fill: #666666;");

        detailsRow.getChildren().addAll(arrivalLabel, stopsLabel, coachLabel);

        card.getChildren().addAll(topRow, routeBox, detailsRow);
        return card;
    }

    private VBox createCoDriverInfo() {
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

        Label title = new Label("👥 Co-Driver Information");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        title.setStyle("-fx-text-fill: #212121;");

        HBox coDriverCard = new HBox(20);
        coDriverCard.setAlignment(Pos.CENTER_LEFT);
        coDriverCard.setPadding(new Insets(15, 0, 0, 0));

        Circle avatar = new Circle(40);
        avatar.setFill(Color.web(PRIMARY_GREEN));
        avatar.setStroke(Color.web(DARK_GREEN));
        avatar.setStrokeWidth(2);

        VBox infoBox = new VBox(8);
        Label nameLabel = new Label("Soufiane el omari");
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        nameLabel.setStyle("-fx-text-fill: #212121;");

        Label licenseLabel = new Label("📋 License:AE231A");
        licenseLabel.setFont(Font.font("Arial", 14));
        licenseLabel.setStyle("-fx-text-fill: #666666;");

        Label contactLabel = new Label("📱 Contact: +212654231879");
        contactLabel.setFont(Font.font("Arial", 14));
        contactLabel.setStyle("-fx-text-fill: #666666;");

        infoBox.getChildren().addAll(nameLabel, licenseLabel, contactLabel);
        coDriverCard.getChildren().addAll(avatar, infoBox);

        section.getChildren().addAll(title, coDriverCard);
        return section;
    }

    // MY SCHEDULE VIEW
    private void showScheduleView() {
        contentArea.getChildren().clear();

        VBox scheduleView = new VBox(20);
        scheduleView.setAlignment(Pos.TOP_LEFT);
        scheduleView.setPadding(new Insets(20));

        Text title = new Text("Full Schedule Calendar");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setFill(Color.web(PRIMARY_GREEN));

        Text subtitle = new Text("This module would display the interactive monthly calendar.");
        subtitle.setFont(Font.font("Arial", 14));
        subtitle.setFill(Color.web(TEXT_SECONDARY));

        // Calendar placeholder
        VBox calendarPlaceholder = new VBox(20);
        calendarPlaceholder.setAlignment(Pos.CENTER);
        calendarPlaceholder.setPadding(new Insets(50));
        calendarPlaceholder.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 12;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0.2, 0, 2);"
        );

        Text calendarIcon = new Text("🗓");
        calendarIcon.setFont(Font.font(60));

        Text calendarText = new Text("Monthly Schedule Calendar");
        calendarText.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        calendarText.setFill(Color.web("#212121"));

        Text calendarDesc = new Text("All reserved trips for the month would be displayed here in an interactive calendar view.");
        calendarDesc.setFont(Font.font("Arial", 14));
        calendarDesc.setFill(Color.web(TEXT_SECONDARY));
        calendarDesc.setWrappingWidth(600);
        calendarDesc.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        calendarPlaceholder.getChildren().addAll(calendarIcon, calendarText, calendarDesc);

        // Upcoming trips table
        VBox upcomingTrips = createUpcomingTripsTable();

        scheduleView.getChildren().addAll(title, subtitle, calendarPlaceholder, upcomingTrips);

        ScrollPane scrollPane = new ScrollPane(scheduleView);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        contentArea.getChildren().add(scrollPane);
    }

    private VBox createUpcomingTripsTable() {
        VBox tableBox = new VBox(15);
        tableBox.setPadding(new Insets(25));
        tableBox.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 12;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0.2, 0, 2);"
        );

        Label tableTitle = new Label("📋 Upcoming Trips (Next 7 Days)");
        tableTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        tableTitle.setStyle("-fx-text-fill: #212121;");

        TableView<UpcomingTrip> table = new TableView<>();

        // Appliquer le style CSS au TableView
        String tableStyle =
                ".table-view {" +
                        "    -fx-background-color: white;" +
                        "    -fx-border-color: #e0e0e0;" +
                        "    -fx-border-width: 1;" +
                        "    -fx-border-radius: 8;" +
                        "}" +
                        ".table-view .table-cell {" +
                        "    -fx-text-fill: #212121 !important;" +
                        "    -fx-font-size: 13px !important;" +
                        "    -fx-font-family: 'Arial' !important;" +
                        "    -fx-alignment: CENTER-LEFT;" +
                        "    -fx-padding: 10px !important;" +
                        "}" +
                        ".table-view .column-header {" +
                        "    -fx-background-color: #f8f9fa;" +
                        "    -fx-text-fill: #212121 !important;" +
                        "    -fx-font-weight: bold !important;" +
                        "    -fx-font-size: 12px !important;" +
                        "    -fx-border-color: #e0e0e0;" +
                        "    -fx-border-width: 0 0 1 0;" +
                        "}" +
                        ".table-view .column-header-background {" +
                        "    -fx-background-color: #f8f9fa;" +
                        "}" +
                        ".table-row-cell {" +
                        "    -fx-background-color: white;" +
                        "    -fx-border-color: transparent;" +
                        "}" +
                        ".table-row-cell:odd {" +
                        "    -fx-background-color: #fafafa;" +
                        "}" +
                        ".table-row-cell:selected {" +
                        "    -fx-background-color: #e3f2fd;" +
                        "    -fx-text-fill: #212121;" +
                        "}";

        table.setStyle(tableStyle);

        TableColumn<UpcomingTrip, String> dateCol = new TableColumn<>("📅 Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateCol.setPrefWidth(100);

        TableColumn<UpcomingTrip, String> timeCol = new TableColumn<>("⏰ Time");
        timeCol.setCellValueFactory(new PropertyValueFactory<>("time"));
        timeCol.setPrefWidth(80);

        TableColumn<UpcomingTrip, String> routeCol = new TableColumn<>("🛣️ Route");
        routeCol.setCellValueFactory(new PropertyValueFactory<>("route"));
        routeCol.setPrefWidth(200);

        TableColumn<UpcomingTrip, String> coachCol = new TableColumn<>("🚌 Coach");
        coachCol.setCellValueFactory(new PropertyValueFactory<>("coach"));
        coachCol.setPrefWidth(120);

        TableColumn<UpcomingTrip, String> statusCol = new TableColumn<>("📊 Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(100);

        statusCol.setCellFactory(col -> new TableCell<UpcomingTrip, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    setText(item);
                    setFont(Font.font("Arial", FontWeight.BOLD, 13));

                    if ("Scheduled".equals(item)) {
                        setTextFill(Color.web(PRIMARY_GREEN));
                        setStyle("-fx-background-color: #e8f5e9; -fx-background-radius: 4; -fx-padding: 4 8;");
                    } else if ("Confirmed".equals(item)) {
                        setTextFill(Color.web(BLUE_ACCENT));
                        setStyle("-fx-background-color: #e3f2fd; -fx-background-radius: 4; -fx-padding: 4 8;");
                    } else if ("Pending".equals(item)) {
                        setTextFill(Color.web(ORANGE_ACCENT));
                        setStyle("-fx-background-color: #fff3e0; -fx-background-radius: 4; -fx-padding: 4 8;");
                    }
                }
            }
        });

        table.getColumns().addAll(dateCol, timeCol, routeCol, coachCol, statusCol);
        table.setItems(upcomingTripsList);
        table.setPrefHeight(250);

        tableBox.getChildren().addAll(tableTitle, table);
        return tableBox;
    }

    // MY VEHICLE VIEW - Updated with Firebase data
    private void showVehicleView() {
        contentArea.getChildren().clear();

        VBox vehicleView = new VBox(20);
        vehicleView.setAlignment(Pos.TOP_LEFT);
        vehicleView.setPadding(new Insets(20));

        Text title = new Text("My Vehicle");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setFill(Color.web(PRIMARY_GREEN));

        Text subtitle = new Text("Technical specifications and status of your assigned coach.");
        subtitle.setFont(Font.font("Arial", 14));
        subtitle.setFill(Color.web(TEXT_SECONDARY));

        // Vehicle Card
        VBox vehicleCard = new VBox(20);
        vehicleCard.setPadding(new Insets(30));
        vehicleCard.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 12;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0.2, 0, 2);"
        );

        HBox headerRow = new HBox();
        VBox vehicleInfo = new VBox(5);
        String vehicleName = assignedCoach != null ? "🚍 " + assignedCoach.getModel() : "🚍 No Vehicle Assigned";
        Label vehicleNameLabel = new Label(vehicleName);
        vehicleNameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        vehicleNameLabel.setStyle("-fx-text-fill: #212121;");

        String plateNumber = assignedCoach != null ? "🔢 Plate: " + assignedCoach.getLicensePlate() : "🔢 Plate: N/A";
        Label plateNumberLabel = new Label(plateNumber);
        plateNumberLabel.setFont(Font.font("Arial", 14));
        plateNumberLabel.setStyle("-fx-text-fill: #666666;");

        vehicleInfo.getChildren().addAll(vehicleNameLabel, plateNumberLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        // Badge de statut du véhicule
        String statusText = assignedCoach != null ? assignedCoach.getStatus() : "N/A";
        String statusColor = "Good".equals(statusText) ? PRIMARY_GREEN : RED_ACCENT;

        Label statusBadge = new Label("✅ " + statusText);
        statusBadge.setStyle(
                "-fx-background-color: " + statusColor + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 8 20;" +
                        "-fx-background-radius: 20;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 14;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0.2, 0, 1);"
        );

        headerRow.getChildren().addAll(vehicleInfo, spacer, statusBadge);

        // Specifications Grid
        GridPane specsGrid = new GridPane();
        specsGrid.setHgap(40);
        specsGrid.setVgap(20);
        specsGrid.setPadding(new Insets(20, 0, 0, 0));

        if (assignedCoach != null) {
            addVehicleSpec(specsGrid, 0, 0, "📊 MILEAGE", "124,500 km");
            addVehicleSpec(specsGrid, 1, 0, "⚙ ENGINE TYPE", "Diesel Hybrid");
            addVehicleSpec(specsGrid, 0, 1, "💺 CAPACITY", assignedCoach.getCapacity() + " Seats");
            addVehicleSpec(specsGrid, 1, 1, "🔧 NEXT SERVICE", "125,000 km");
            addVehicleSpec(specsGrid, 0, 2, "⛽ FUEL TYPE", "Diesel");
            addVehicleSpec(specsGrid, 1, 2, "📅 YEAR", "2020");
        } else {
            addVehicleSpec(specsGrid, 0, 0, "📊 MILEAGE", "N/A");
            addVehicleSpec(specsGrid, 1, 0, "⚙️ ENGINE TYPE", "N/A");
            addVehicleSpec(specsGrid, 0, 1, "💺 CAPACITY", "N/A");
            addVehicleSpec(specsGrid, 1, 1, "🔧 NEXT SERVICE", "N/A");
        }

        vehicleCard.getChildren().addAll(headerRow, specsGrid);

        // Fuel Status
        VBox fuelSection = createFuelStatusSection();

        vehicleView.getChildren().addAll(title, subtitle, vehicleCard, fuelSection);

        ScrollPane scrollPane = new ScrollPane(vehicleView);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        contentArea.getChildren().add(scrollPane);
    }

    private void addVehicleSpec(GridPane grid, int col, int row, String label, String value) {
        VBox specBox = new VBox(5);
        Label labelText = new Label(label);
        labelText.setFont(Font.font("Arial", 12));
        labelText.setStyle("-fx-text-fill: #757575;");

        Label valueText = new Label(value);
        valueText.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        valueText.setStyle("-fx-text-fill: #212121;");

        specBox.getChildren().addAll(labelText, valueText);
        grid.add(specBox, col, row);
    }

    private VBox createFuelStatusSection() {
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

        Label title = new Label("⛽ Fuel Status");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        title.setStyle("-fx-text-fill: #212121;");

        // Fuel gauge
        HBox fuelGaugeContainer = new HBox();
        fuelGaugeContainer.setStyle(
                "-fx-background-color: #f5f5f5;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 10;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 4, 0.2, 0, 1);"
        );
        fuelGaugeContainer.setPrefHeight(40);
        fuelGaugeContainer.setPrefWidth(500);

        StackPane fuelFill = new StackPane();
        fuelFill.setStyle(
                "-fx-background-color: linear-gradient(to right, " + ORANGE_ACCENT + ", " + PRIMARY_GREEN + ");" +
                        "-fx-background-radius: 10;"
        );
        fuelFill.setPrefWidth(500 * 0.95); // 95%
        fuelFill.setPrefHeight(40);
        fuelFill.setAlignment(Pos.CENTER_LEFT);

        Label fuelPercentage = new Label("95%");
        fuelPercentage.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        fuelPercentage.setTextFill(Color.WHITE);
        StackPane.setAlignment(fuelPercentage, Pos.CENTER);

        fuelFill.getChildren().add(fuelPercentage);
        fuelGaugeContainer.getChildren().add(fuelFill);

        // Labels
        HBox labelsRow = new HBox();
        labelsRow.setPadding(new Insets(10, 0, 0, 0));

        Label emptyLabel = new Label("🟥 Empty");
        emptyLabel.setFont(Font.font("Arial", 12));
        emptyLabel.setStyle("-fx-text-fill: #757575;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label fullLabel = new Label("🟩 Full");
        fullLabel.setFont(Font.font("Arial", 12));
        fullLabel.setStyle("-fx-text-fill: #757575;");

        labelsRow.getChildren().addAll(emptyLabel, spacer, fullLabel);

        // Range info
        Label rangeLabel = new Label("📏 Estimated range: 350km based on current consumption.");
        rangeLabel.setFont(Font.font("Arial", 12));
        rangeLabel.setStyle("-fx-text-fill: #666666; -fx-padding: 10 0 0 0;");

        section.getChildren().addAll(title, fuelGaugeContainer, labelsRow, rangeLabel);
        return section;
    }

    // INCIDENTS VIEW - Updated with Firebase integration
    private void showIncidentsView() {
        contentArea.getChildren().clear();

        VBox incidentsView = new VBox(20);
        incidentsView.setAlignment(Pos.TOP_LEFT);
        incidentsView.setPadding(new Insets(20));

        HBox headerBox = new HBox();
        VBox headerText = new VBox(5);
        Text title = new Text("Incidents & Reclamations");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setFill(Color.web(PRIMARY_GREEN));

        Text subtitle = new Text("Report vehicle issues, delays, or accidents directly to management.");
        subtitle.setFont(Font.font("Arial", 14));
        subtitle.setFill(Color.web(TEXT_SECONDARY));

        headerText.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button reportBtn = createActionButton("🚨 Report New Issue", PRIMARY_GREEN);
        reportBtn.setOnAction(e -> showReportIncidentDialog());

        headerBox.getChildren().addAll(headerText, spacer, reportBtn);

        // Incidents Table
        VBox tableSection = createIncidentsTable();

        incidentsView.getChildren().addAll(headerBox, tableSection);

        ScrollPane scrollPane = new ScrollPane(incidentsView);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        contentArea.getChildren().add(scrollPane);
    }

    private VBox createIncidentsTable() {
        VBox tableBox = new VBox();
        tableBox.setPadding(new Insets(25));
        tableBox.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 12;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0.2, 0, 2);"
        );

        // Table header
        GridPane tableHeader = new GridPane();
        tableHeader.setHgap(20);
        tableHeader.setPadding(new Insets(0, 0, 20, 0));
        tableHeader.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 0 0 2 0;");

        String[] headers = {"🔢 ID", "📋 TYPE", "📝 DESCRIPTION", "📅 DATE SUBMITTED", "📊 STATUS"};
        int[] widths = {100, 150, 400, 150, 120};

        for (int i = 0; i < headers.length; i++) {
            Label headerLabel = new Label(headers[i]);
            headerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            headerLabel.setStyle("-fx-text-fill: #757575;");
            headerLabel.setPrefWidth(widths[i]);
            tableHeader.add(headerLabel, i, 0);
        }

        // Create table
        incidentsTable = new TableView<>();

        TableColumn<Incident, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(100);

        TableColumn<Incident, String> typeCol = new TableColumn<>("TYPE");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeCol.setPrefWidth(150);

        TableColumn<Incident, String> descCol = new TableColumn<>("DESCRIPTION");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setPrefWidth(400);

        TableColumn<Incident, String> dateCol = new TableColumn<>("DATE SUBMITTED");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateCol.setPrefWidth(150);

        TableColumn<Incident, String> statusCol = new TableColumn<>("STATUS");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(120);

        incidentsTable.getColumns().addAll(idCol, typeCol, descCol, dateCol, statusCol);
        incidentsTable.setItems(incidentsList);
        incidentsTable.setPrefHeight(300);

        tableBox.getChildren().addAll(tableHeader, incidentsTable);
        return tableBox;
    }

    private void showReportIncidentDialog() {
        Dialog<Map<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Report New Incident");
        dialog.setHeaderText("Submit a new incident report");

        ButtonType submitButton = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("Mechanical", "Delay", "Accident", "Passenger Incident", "Other");
        typeCombo.setPromptText("Select type");

        TextArea descriptionField = new TextArea();
        descriptionField.setPromptText("Describe the incident in detail");
        descriptionField.setPrefRowCount(5);
        descriptionField.setPrefWidth(300);

        TextField locationField = new TextField();
        locationField.setPromptText("Location where incident occurred");

        grid.add(new Label("Type:"), 0, 0);
        grid.add(typeCombo, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionField, 1, 1);
        grid.add(new Label("Location:"), 0, 2);
        grid.add(locationField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitButton) {
                Map<String, String> result = new HashMap<>();
                result.put("type", typeCombo.getValue());
                result.put("description", descriptionField.getText());
                result.put("location", locationField.getText());
                return result;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(data -> {
            if (data != null) {
                try {
                    // Générer un ID d'incident
                    String incidentId = FirebaseService.generateIncidentId();

                    // Ajouter l'incident à Firestore
                    FirebaseService.reportIncident(
                            incidentId,
                            driverId,  // ID du driver courant
                            data.get("type"),
                            data.get("description"),
                            data.get("location")
                    );

                    // Afficher un message de confirmation simple
                    Label successLabel = new Label("✅ Incident soumis avec succès (ID: " + incidentId + ")");
                    successLabel.setStyle("-fx-text-fill: " + PRIMARY_GREEN + "; -fx-font-size: 14; -fx-padding: 10;");

                    // Rafraîchir la liste locale (optionnel)
                    refreshDriverIncidentsList();

                    showSuccessAlert("Incident Reported",
                            "Votre incident a été soumis avec succès.\n" +
                                    "Le manager sera notifié automatiquement.");

                } catch (Exception e) {
                    showErrorAlert("Erreur", "Échec de soumission: " + e.getMessage());
                }
            }
        });
    }


    private void refreshDriverIncidentsList() {
        try {
            // Rafraîchir la liste des incidents du driver
            ObservableList<Incident> driverIncidents = FirebaseService.getDriverIncidents(driverId);
            // Mettre à jour votre tableau si nécessaire
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // MAINTENANCE VIEW
    private void showMaintenanceView() {
        contentArea.getChildren().clear();

        VBox maintenanceView = new VBox(20);
        maintenanceView.setAlignment(Pos.TOP_LEFT);
        maintenanceView.setPadding(new Insets(20));

        Text title = new Text("Maintenance Checklist");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setFill(Color.web(PRIMARY_GREEN));

        Text subtitle = new Text("Daily vehicle inspection and maintenance reporting.");
        subtitle.setFont(Font.font("Arial", 14));
        subtitle.setFill(Color.web(TEXT_SECONDARY));

        // Maintenance Checklist
        VBox checklistSection = createMaintenanceChecklist();

        // System Health
        VBox systemHealthSection = createSystemHealth();

        // Action Buttons
        HBox actionBar = new HBox(15);
        actionBar.setPadding(new Insets(20, 0, 0, 0));

        Button performInspection = createActionButton("🔍 Perform Daily Inspection", PRIMARY_GREEN);
        performInspection.setOnAction(e -> showInspectionDialog());

        Button reportIssue = createActionButton("⚠ Report Maintenance Issue", RED_ACCENT);
        reportIssue.setOnAction(e -> showMaintenanceIssueDialog());

        actionBar.getChildren().addAll(performInspection, reportIssue);

        maintenanceView.getChildren().addAll(title, subtitle, checklistSection, systemHealthSection, actionBar);

        ScrollPane scrollPane = new ScrollPane(maintenanceView);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        contentArea.getChildren().add(scrollPane);
    }

    private VBox createMaintenanceChecklist() {
        VBox checklistBox = new VBox(15);
        checklistBox.setPadding(new Insets(25));
        checklistBox.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 12;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0.2, 0, 2);"
        );

        Label title = new Label("📋 Daily Maintenance Checklist");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        title.setStyle("-fx-text-fill: #212121;");

        GridPane checklistGrid = new GridPane();
        checklistGrid.setHgap(40);
        checklistGrid.setVgap(15);
        checklistGrid.setPadding(new Insets(15, 0, 0, 0));

        addChecklistItem(checklistGrid, 0, 0, "🛑 Brakes & Fluid", "OK");
        addChecklistItem(checklistGrid, 1, 0, "🌀 Tire Pressure", "OK");
        addChecklistItem(checklistGrid, 0, 1, "💡 Lights & Signals", "OK");
        addChecklistItem(checklistGrid, 1, 1, "🆘 Emergency Kit", "OK");
        addChecklistItem(checklistGrid, 0, 2, "🛢 Oil Level", "OK");
        addChecklistItem(checklistGrid, 1, 2, "🌡 Coolant System", "OK");
        addChecklistItem(checklistGrid, 0, 3, "🧼 Wipers & Washers", "OK");
        addChecklistItem(checklistGrid, 1, 3, "🪑 Seat Belts", "OK");

        // Last inspection info
        HBox inspectionInfo = new HBox(10);
        inspectionInfo.setPadding(new Insets(20, 0, 0, 0));
        inspectionInfo.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 1 0 0 0; -fx-padding: 15 0 0 0;");

        Label lastInspectionLabel = new Label("📅 Last Inspection: ");
        lastInspectionLabel.setFont(Font.font("Arial", 12));
        lastInspectionLabel.setStyle("-fx-text-fill: #757575;");

        Label lastInspectionDate = new Label("2025-12-018 08:00 AM");
        lastInspectionDate.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        lastInspectionDate.setStyle("-fx-text-fill: " + PRIMARY_GREEN + ";");

        inspectionInfo.getChildren().addAll(lastInspectionLabel, lastInspectionDate);

        checklistBox.getChildren().addAll(title, checklistGrid, inspectionInfo);
        return checklistBox;
    }

    private void addChecklistItem(GridPane grid, int col, int row, String item, String status) {
        HBox itemBox = new HBox(15);
        itemBox.setAlignment(Pos.CENTER_LEFT);
        itemBox.setPrefWidth(250);

        Label itemLabel = new Label(item);
        itemLabel.setFont(Font.font("Arial", 14));
        itemLabel.setStyle("-fx-text-fill: #212121;");
        itemLabel.setPrefWidth(180);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label statusLabel = new Label(status);
        statusLabel.setStyle(
                "-fx-background-color: #e8f5e9;" +
                        "-fx-text-fill: #2e7d32;" +
                        "-fx-padding: 5 15;" +
                        "-fx-background-radius: 15;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 12;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 3, 0.2, 0, 1);"
        );

        itemBox.getChildren().addAll(itemLabel, spacer, statusLabel);
        grid.add(itemBox, col, row);
    }

    private VBox createSystemHealth() {
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

        Label title = new Label("🔋 System Health");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        title.setStyle("-fx-text-fill: #212121;");

        VBox healthItems = new VBox(15);
        healthItems.setPadding(new Insets(15, 0, 0, 0));

        healthItems.getChildren().addAll(
                createHealthItem("🔋 Battery Voltage", "24.2 V", "normal"),
                createHealthItem("🛢 Oil Pressure", "Normal", "normal"),
                createHealthItem("🌡 Engine Temperature", "88°C", "normal"),
                createHealthItem("🌀 Tire Pressure (Front)", "36 PSI", "normal"),
                createHealthItem("🌀 Tire Pressure (Rear)", "38 PSI", "normal"),
                createHealthItem("💧 AdBlue Level", "Low (15%)", "warning")
        );

        section.getChildren().addAll(title, healthItems);
        return section;
    }

    private HBox createHealthItem(String label, String value, String status) {
        HBox item = new HBox();
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(5, 0, 5, 0));

        Label labelText = new Label(label);
        labelText.setFont(Font.font("Arial", 14));
        labelText.setStyle("-fx-text-fill: #212121;");
        labelText.setPrefWidth(200);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label valueText = new Label(value);
        valueText.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        if (status.equals("warning")) {
            valueText.setStyle("-fx-text-fill: " + RED_ACCENT + ";");
        } else {
            valueText.setStyle("-fx-text-fill: " + PRIMARY_GREEN + ";");
        }

        item.getChildren().addAll(labelText, spacer, valueText);
        return item;
    }

    private void showInspectionDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Daily Inspection");
        alert.setHeaderText("Inspection Started");
        alert.setContentText("Please proceed with your daily vehicle inspection checklist.\nMark each item as you complete the inspection.");
        alert.showAndWait();
    }

    private void showMaintenanceIssueDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Report Maintenance Issue");
        dialog.setHeaderText("Submit a maintenance request");

        ButtonType submitButton = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<String> issueCombo = new ComboBox<>();
        issueCombo.getItems().addAll("Engine", "Transmission", "Brakes", "Lights", "AC/Heating", "Tires", "Electrical", "Other");

        ComboBox<String> urgencyCombo = new ComboBox<>();
        urgencyCombo.getItems().addAll("Critical - Immediate attention", "High - Within 24 hours", "Medium - Within 3 days", "Low - Next service");

        TextArea descriptionField = new TextArea();
        descriptionField.setPromptText("Describe the issue in detail");
        descriptionField.setPrefRowCount(5);

        grid.add(new Label("Issue Type:"), 0, 0);
        grid.add(issueCombo, 1, 0);
        grid.add(new Label("Urgency:"), 0, 1);
        grid.add(urgencyCombo, 1, 1);
        grid.add(new Label("Description:"), 0, 2);
        grid.add(descriptionField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.showAndWait();

        showSuccessAlert("Maintenance Request Submitted", "Your maintenance request has been sent to the service department.");
    }

    // TRIP HISTORY VIEW
    private void showTripHistoryView() {
        contentArea.getChildren().clear();

        VBox historyView = new VBox(20);
        historyView.setAlignment(Pos.TOP_LEFT);
        historyView.setPadding(new Insets(20));

        Text title = new Text("Trip History");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setFill(Color.web(PRIMARY_GREEN));

        Text subtitle = new Text("View your past trips, mileage reports, and performance data.");
        subtitle.setFont(Font.font("Arial", 14));
        subtitle.setFill(Color.web(TEXT_SECONDARY));

        // Trip History Table
        VBox tableSection = createTripHistoryTable();

        // Statistics
        VBox statsSection = createTripStats();

        // Weekly Distance Chart
        VBox chartSection = createWeeklyDistanceChart();

        historyView.getChildren().addAll(title, subtitle, statsSection, chartSection, tableSection);

        ScrollPane scrollPane = new ScrollPane(historyView);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        contentArea.getChildren().add(scrollPane);
    }

    private VBox createTripHistoryTable() {
        VBox tableBox = new VBox(15);
        tableBox.setPadding(new Insets(25));
        tableBox.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 12;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0.2, 0, 2);"
        );

        Label tableTitle = new Label("📊 Past Trips Log");
        tableTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        tableTitle.setStyle("-fx-text-fill: #212121;");

        TableColumn<TripHistory, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateCol.setPrefWidth(100);

        TableColumn<TripHistory, String> routeCol = new TableColumn<>("Route");
        routeCol.setCellValueFactory(new PropertyValueFactory<>("route"));
        routeCol.setPrefWidth(200);

        TableColumn<TripHistory, String> distanceCol = new TableColumn<>("Distance");
        distanceCol.setCellValueFactory(new PropertyValueFactory<>("distance"));
        distanceCol.setPrefWidth(80);

        TableColumn<TripHistory, String> durationCol = new TableColumn<>("Duration");
        durationCol.setCellValueFactory(new PropertyValueFactory<>("duration"));
        durationCol.setPrefWidth(100);

        TableColumn<TripHistory, String> passengersCol = new TableColumn<>("Passengers");
        passengersCol.setCellValueFactory(new PropertyValueFactory<>("passengers"));
        passengersCol.setPrefWidth(80);

        TableColumn<TripHistory, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(100);

        tripHistoryTable.getColumns().addAll(dateCol, routeCol, distanceCol, durationCol, passengersCol, statusCol);
        tripHistoryTable.setItems(tripHistoryList);
        tripHistoryTable.setPrefHeight(400);

        tableBox.getChildren().addAll(tableTitle, tripHistoryTable);
        return tableBox;
    }

    private VBox createTripStats() {
        VBox statsBox = new VBox(15);
        statsBox.setPadding(new Insets(25));
        statsBox.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 12;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0.2, 0, 2);"
        );

        Label statsTitle = new Label("📈 Monthly Statistics");
        statsTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        statsTitle.setStyle("-fx-text-fill: #212121;");

        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(40);
        statsGrid.setVgap(20);
        statsGrid.setPadding(new Insets(15, 0, 0, 0));

        addStatistic(statsGrid, 0, 0, "🚌 Total Trips", "42");
        addStatistic(statsGrid, 1, 0, "📏 Total Distance", "1,245 km");
        addStatistic(statsGrid, 0, 1, "👥 Average Passengers", "38");
        addStatistic(statsGrid, 1, 1, "⏱ On-Time Performance", "96.5%");
        addStatistic(statsGrid, 0, 2, "⛽ Fuel Efficiency", "3.2 km/L");
        addStatistic(statsGrid, 1, 2, "⏰ Total Driving Hours", "89.5 hrs");

        statsBox.getChildren().addAll(statsTitle, statsGrid);
        return statsBox;
    }

    private VBox createWeeklyDistanceChart() {
        VBox chartBox = new VBox(15);
        chartBox.setPadding(new Insets(25));
        chartBox.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 12;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0.2, 0, 2);"
        );

        Label chartTitle = new Label("📊 Weekly Distance (km)");
        chartTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        chartTitle.setStyle("-fx-text-fill: #212121;");

        // Header with days
        HBox daysHeader = new HBox();
        daysHeader.setPadding(new Insets(0, 0, 10, 0));

        String[] days = {"Mon", "Tue", "Wed", "", "", "", ""};
        int[] distances = {120, 200, 180, 300,0,0, };
        int maxDistance = 300;

        for (int i = 0; i < days.length; i++) {
            VBox dayColumn = new VBox(5);
            dayColumn.setAlignment(Pos.BOTTOM_CENTER);
            dayColumn.setPrefWidth(100);

            // Bar chart
            double barHeight = (distances[i] / (double) maxDistance) * 150;
            StackPane bar = new StackPane();
            bar.setPrefWidth(60);
            bar.setPrefHeight(barHeight);
            bar.setStyle(
                    "-fx-background-color: linear-gradient(to top, " + PRIMARY_GREEN + ", " + DARK_GREEN + ");" +
                            "-fx-background-radius: 4 4 0 0;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 3, 0.2, 0, 1);"
            );

            Label distanceLabel = new Label(distances[i] + " km");
            distanceLabel.setFont(Font.font("Arial", 10));
            distanceLabel.setTextFill(Color.WHITE);
            StackPane.setAlignment(distanceLabel, Pos.TOP_CENTER);
            StackPane.setMargin(distanceLabel, new Insets(5, 0, 0, 0));
            bar.getChildren().add(distanceLabel);

            Label dayLabel = new Label(days[i]);
            dayLabel.setFont(Font.font("Arial", 12));
            dayLabel.setStyle("-fx-text-fill: #666666;");

            dayColumn.getChildren().addAll(bar, dayLabel);
            daysHeader.getChildren().add(dayColumn);
        }

        chartBox.getChildren().addAll(chartTitle, daysHeader);
        return chartBox;
    }

    private void addStatistic(GridPane grid, int col, int row, String label, String value) {
        VBox statBox = new VBox(5);
        Label labelText = new Label(label);
        labelText.setFont(Font.font("Arial", 12));
        labelText.setStyle("-fx-text-fill: #757575;");

        Label valueText = new Label(value);
        valueText.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        valueText.setStyle("-fx-text-fill: " + PRIMARY_GREEN + ";");

        statBox.getChildren().addAll(labelText, valueText);
        grid.add(statBox, col, row);
    }

    // ROUTE DIAGRAM FEATURE
    private void showRouteDiagram(TripData tripData) {
        // Créer une boîte de dialogue moderne
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Route Visualization");
        dialog.setHeaderText(null);

        // Style de la fenêtre
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 12;" +
                        "-fx-background-radius: 12;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 20, 0.3, 0, 5);"
        );

        // Bouton de fermeture stylisé
        ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(closeButton);

        // Style du bouton
        Button closeBtn = (Button) dialog.getDialogPane().lookupButton(closeButton);
        closeBtn.setStyle(
                "-fx-background-color: " + PRIMARY_GREEN + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 8 20;" +
                        "-fx-background-radius: 6;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0.2, 0, 1);"
        );
        closeBtn.setOnMouseEntered(e -> closeBtn.setStyle(
                "-fx-background-color: " + DARK_GREEN + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 8 20;" +
                        "-fx-background-radius: 6;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 6, 0.3, 0, 2);"
        ));
        closeBtn.setOnMouseExited(e -> closeBtn.setStyle(
                "-fx-background-color: " + PRIMARY_GREEN + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 8 20;" +
                        "-fx-background-radius: 6;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0.2, 0, 1);"
        ));

        // Contenu principal avec style moderne
        VBox dialogContent = new VBox(20);
        dialogContent.setPadding(new Insets(15));
        dialogContent.setStyle("-fx-background-color: white;");

        // Header avec titre stylisé
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(0, 0, 10, 0));

        VBox titleBox = new VBox(5);
        Label title = new Label("TIMELINE & STOPS");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        title.setStyle("-fx-text-fill: #212121;");

        Label subtitle = new Label(tripData.getFrom() + " → " + tripData.getTo());
        subtitle.setFont(Font.font("Arial", 10));
        subtitle.setStyle("-fx-text-fill: " + PRIMARY_GREEN + ";");

        titleBox.getChildren().addAll(title, subtitle);

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        // Badge d'information
        Label infoBadge = new Label("🚌 " + tripData.getStops() + " • ⏱ " + calculateDuration(tripData));
        infoBadge.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        infoBadge.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-background-color: " + BLUE_ACCENT + ";" +
                        "-fx-padding: 6 12;" +
                        "-fx-background-radius: 15;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0.2, 0, 1);"
        );

        headerBox.getChildren().addAll(titleBox, headerSpacer, infoBadge);

        // Informations de base dans des cartes modernes
        HBox infoCards = new HBox(15);
        infoCards.setPadding(new Insets(0, 0, 20, 0));

        infoCards.getChildren().addAll(
                createInfoCard("🚀", "Departure", tripData.getDepartureTime(), tripData.getFrom(), PRIMARY_GREEN),
                createInfoCard("🛑", "Stops", tripData.getStops(), "Intermediate", ORANGE_ACCENT),
                createInfoCard("🏁", "Arrival", tripData.getArrivalTime(), tripData.getTo(), BLUE_ACCENT)
        );

        // Timeline horizontale avec scrollbar
        VBox timelineSection = createHorizontalTimelineSection();

        dialogContent.getChildren().addAll(headerBox, infoCards, timelineSection);
        dialog.getDialogPane().setContent(dialogContent);

        // Taille de la fenêtre
        dialog.getDialogPane().setPrefSize(1000, 700);

        dialog.showAndWait();
    }

    private VBox createInfoCard(String icon, String title, String value, String subtitle, String color) {
        VBox card = new VBox(10);
        card.setPrefWidth(180);
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.CENTER);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #e9ecef;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0.2, 0, 2);"
        );

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font(18));

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 9));
        titleLabel.setStyle("-fx-text-fill: #666666;");

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        valueLabel.setStyle("-fx-text-fill: " + color + ";");

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setFont(Font.font("Arial", 9));
        subtitleLabel.setStyle("-fx-text-fill: #999999;");
        subtitleLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        card.getChildren().addAll(iconLabel, titleLabel, valueLabel, subtitleLabel);
        return card;
    }

    private VBox createHorizontalTimelineSection() {
        VBox timelineContainer = new VBox(10);
        timelineContainer.setPadding(new Insets(12));
        timelineContainer.setStyle(
                "-fx-background-color: #f8f9fa;" +
                        "-fx-border-color: #dee2e6;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 10;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0.3, 0, 2);"
        );

        Label sectionTitle = new Label("📍 Route Timeline");
        sectionTitle.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        sectionTitle.setStyle("-fx-text-fill: #212121; -fx-padding: 0 0 10 0;");

        // Conteneur principal avec scrollbar horizontale
        ScrollPane scrollContainer = new ScrollPane();
        scrollContainer.setStyle(
                "-fx-background: transparent;" +
                        "-fx-background-color: transparent;" +
                        "-fx-padding: 5;" +
                        "-fx-border-color: transparent;"
        );
        scrollContainer.setFitToHeight(true);
        scrollContainer.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollContainer.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollContainer.setPrefHeight(250);
        scrollContainer.setMaxHeight(300);

        // Conteneur horizontal pour tous les stops
        HBox timelineContent = new HBox(5);
        timelineContent.setPadding(new Insets(15, 20, 10, 20));
        timelineContent.setAlignment(Pos.TOP_CENTER);
        timelineContent.setMinWidth(1400);

        // Définition des stops
        RouteStopWithType[] stops = {
                new RouteStopWithType("Marackech", "08:00", "DEPART", PRIMARY_GREEN),
                new RouteStopWithType("Safi", "09:15", "", ORANGE_ACCENT),
                new RouteStopWithType("Jedida", "12:15", "", ORANGE_ACCENT),
                new RouteStopWithType("Casablanca", "2:15", "", BLUE_ACCENT),
        };

        // Création des stops avec connecteurs
        for (int i = 0; i < stops.length; i++) {
            RouteStopWithType stop = stops[i];

            // Créer la carte de stop
            VBox stopCard = createHorizontalStopCard(stop);

            // Ajouter au conteneur
            timelineContent.getChildren().add(stopCard);

            // Ajouter une flèche de connexion (sauf pour le dernier stop)
            if (i < stops.length - 1) {
                VBox connector = createConnectorArrow();
                timelineContent.getChildren().add(connector);
            }
        }

        scrollContainer.setContent(timelineContent);

        // Légende
        HBox legend = createModernLegend();

        timelineContainer.getChildren().addAll(sectionTitle, scrollContainer, legend);
        return timelineContainer;
    }

    private VBox createHorizontalStopCard(RouteStopWithType stop) {
        VBox card = new VBox(10);
        card.setPrefWidth(140);
        card.setPadding(new Insets(12, 15, 12, 15));
        card.setAlignment(Pos.TOP_CENTER);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: " + stop.color + ";" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 12;" +
                        "-fx-background-radius: 12;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0.3, 0, 3);"
        );

        // Cercle indicateur
        StackPane indicator = new StackPane();
        indicator.setPrefSize(60, 60);

        Circle outerCircle = new Circle(30);
        outerCircle.setFill(Color.web(stop.color));
        outerCircle.setOpacity(0.1);

        Circle middleCircle = new Circle(22);
        middleCircle.setFill(Color.web(stop.color));
        middleCircle.setOpacity(0.3);

        Circle innerCircle = new Circle(14);
        innerCircle.setFill(Color.web(stop.color));

        indicator.getChildren().addAll(outerCircle, middleCircle, innerCircle);

        // Icône selon le type
        Label iconLabel = new Label();
        if (stop.type.equals("DEPART")) {
            iconLabel.setText("📍");
            iconLabel.setStyle("-fx-text-fill: " + PRIMARY_GREEN + ";");
        } else if (stop.type.equals("ARRIVE")) {
            iconLabel.setText("🏁");
            iconLabel.setStyle("-fx-text-fill: " + BLUE_ACCENT + ";");
        } else {
            iconLabel.setText("⏱");
            iconLabel.setStyle("-fx-text-fill: " + ORANGE_ACCENT + ";");
        }
        iconLabel.setFont(Font.font(20));
        StackPane.setAlignment(iconLabel, Pos.CENTER);

        indicator.getChildren().add(iconLabel);

        // Nom du stop
        Label nameLabel = new Label(stop.name);
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        nameLabel.setStyle("-fx-text-fill: #212121;");
        nameLabel.setWrapText(true);
        nameLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        nameLabel.setMaxWidth(180);

        // Heure
        Label timeLabel = new Label(stop.time);
        timeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        timeLabel.setStyle("-fx-text-fill: " + stop.color + ";");

        // Badge de type (seulement pour départ/arrivée)
        if (!stop.type.isEmpty()) {
            Label typeBadge = new Label(stop.type);
            typeBadge.setFont(Font.font("Arial", FontWeight.BOLD, 10));
            typeBadge.setStyle(
                    "-fx-text-fill: white;" +
                            "-fx-background-color: " + stop.color + ";" +
                            "-fx-padding: 5 15;" +
                            "-fx-background-radius: 15;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 3, 0.2, 0, 1);"
            );
            card.getChildren().addAll(indicator, nameLabel, timeLabel, typeBadge);
        } else {
            card.getChildren().addAll(indicator, nameLabel, timeLabel);
        }

        return card;
    }

    private VBox createConnectorArrow() {
        VBox connector = new VBox();
        connector.setPrefWidth(40);
        connector.setPrefHeight(100);
        connector.setAlignment(Pos.CENTER);

        // Ligne horizontale
        Region line = new Region();
        line.setPrefWidth(40);
        line.setPrefHeight(2);
        line.setStyle(
                "-fx-background-color: linear-gradient(to right, " + PRIMARY_GREEN + ", " + BLUE_ACCENT + ");" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 2, 0.2, 0, 0);"
        );

        // Flèche
        Label arrow = new Label("➡");
        arrow.setFont(Font.font(16));
        arrow.setStyle("-fx-text-fill: #666666;");

        connector.getChildren().addAll(line, arrow);
        return connector;
    }

    private HBox createModernLegend() {
        HBox legend = new HBox(25);
        legend.setPadding(new Insets(20, 0, 0, 0));
        legend.setAlignment(Pos.CENTER);
        legend.setStyle("-fx-border-color: #e9ecef; -fx-border-width: 1 0 0 0; -fx-padding: 15 0 0 0;");

        HBox startLegend = createLegendItem(PRIMARY_GREEN, "📍", "Departure");
        HBox stopLegend = createLegendItem(ORANGE_ACCENT, "⏱", "Intermediate Stop");
        HBox endLegend = createLegendItem(BLUE_ACCENT, "🏁", "Arrival");

        // Ajout d'info sur le scroll
        HBox scrollInfo = new HBox(5);
        scrollInfo.setAlignment(Pos.CENTER);
        Label scrollIcon = new Label("↔");
        scrollIcon.setFont(Font.font(14));
        Label scrollText = new Label("Scroll horizontally to view all stops");
        scrollText.setFont(Font.font("Arial", 11));
        scrollText.setStyle("-fx-text-fill: #666666;");
        scrollInfo.getChildren().addAll(scrollIcon, scrollText);

        legend.getChildren().addAll(startLegend, stopLegend, endLegend, scrollInfo);
        return legend;
    }

    private HBox createLegendItem(String color, String icon, String text) {
        HBox item = new HBox(10);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(5, 15, 5, 15));
        item.setStyle(
                "-fx-background-color: #f8f9fa;" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 5;"
        );

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font(14));

        Circle colorCircle = new Circle(6);
        colorCircle.setFill(Color.web(color));

        Label legendText = new Label(text);
        legendText.setFont(Font.font("Arial", 12));
        legendText.setStyle("-fx-text-fill: #495057;");

        item.getChildren().addAll(iconLabel, colorCircle, legendText);
        return item;
    }

    private String calculateDuration(TripData tripData) {
        try {
            String[] startParts = tripData.getDepartureTime().replace(" AM", "").replace(" PM", "").split(":");
            String[] endParts = tripData.getArrivalTime().replace(" AM", "").replace(" PM", "").split(":");

            int startHour = Integer.parseInt(startParts[0]);
            int startMin = Integer.parseInt(startParts[1]);
            int endHour = Integer.parseInt(endParts[0]);
            int endMin = Integer.parseInt(endParts[1]);

            int totalMinutes = (endHour * 60 + endMin) - (startHour * 60 + startMin);
            int hours = totalMinutes / 60;
            int minutes = totalMinutes % 60;

            return hours + "h " + minutes + "m";
        } catch (Exception e) {
            return "1h 10m";
        }
    }

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

    private void loadSampleData() {
        // Sample incidents
        incidentsList.add(new Incident("REC-001", "Mechanical", "AC unit making loud noise during idle.", "2024-05-20", "Resolved"));
        incidentsList.add(new Incident("REC-004", "Delay", "Heavy traffic due to construction on Route LO01.", "2024-05-28", "Pending"));

        // Sample trip history
        tripHistoryList.add(new TripHistory("2025-12-01", "Rabat → Tanger", "350 km", "4h 30m", "42", "Completed"));
        tripHistoryList.add(new TripHistory("2025-12-01", "Mekne → Huceima", "340 km", "4h 45m", "38", "Completed"));
        tripHistoryList.add(new TripHistory("2025-11-30", "Casablanca → Meknes", "290 km", "1h 15m", "51", "Completed"));
        tripHistoryList.add(new TripHistory("2025-11-30", "Marackech → Casablanca", "280 km", "1h 20m", "48", "Completed"));
        tripHistoryList.add(new TripHistory("2025-11-29", "Merleft → Safi", "350 km", "1h 50m", "40", "Completed"));

        // Sample upcoming trips
        upcomingTripsList.add(new UpcomingTrip("2025-12-18", "08:00", "Marackech → Casablanca", "Mercedes Travego", "Scheduled"));
        upcomingTripsList.add(new UpcomingTrip("2025-12-18", "14:00", "Casablanca → Huceima", "Mercedes Travego", "Scheduled"));
        upcomingTripsList.add(new UpcomingTrip("2025-12-19", "09:30", "Meknes → Huceima", "Mercedes Travego", "Confirmed"));
    }

    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setHeaderText("Confirm Logout");
        alert.setContentText("Are you sure you want to logout?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                System.out.println("Driver logged out");
                Stage stage = (Stage) mainContainer.getScene().getWindow();
                stage.close();
            }
        });
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

    public ObservableList<MaintenanceItem> getMaintenanceList() {
        return maintenanceList;
    }

    public void setMaintenanceList(ObservableList<MaintenanceItem> maintenanceList) {
        this.maintenanceList = maintenanceList;
    }

    private void showMaintenanceReportDialog() {
        Dialog<Map<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Report Maintenance Issue");
        dialog.setHeaderText("Submit maintenance request to manager");

        ButtonType submitButton = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<String> issueCombo = new ComboBox<>();
        issueCombo.getItems().addAll("Engine", "Transmission", "Brakes", "Lights",
                "AC/Heating", "Tires", "Electrical", "Other");

        ComboBox<String> urgencyCombo = new ComboBox<>();
        urgencyCombo.getItems().addAll("Critical - Immediate attention",
                "High - Within 24 hours", "Medium - Within 3 days", "Low - Next service");

        TextArea descriptionField = new TextArea();
        descriptionField.setPromptText("Describe the issue in detail");
        descriptionField.setPrefRowCount(5);

        grid.add(new Label("Issue Type:"), 0, 0);
        grid.add(issueCombo, 1, 0);
        grid.add(new Label("Urgency:"), 0, 1);
        grid.add(urgencyCombo, 1, 1);
        grid.add(new Label("Description:"), 0, 2);
        grid.add(descriptionField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitButton) {
                Map<String, String> result = new HashMap<>();
                result.put("type", issueCombo.getValue());
                result.put("urgency", urgencyCombo.getValue());
                result.put("description", descriptionField.getText());
                return result;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(data -> {
            try {
                // Sauvegarder dans Firestore
                String maintenanceId = "MAINT_" + System.currentTimeMillis();

                Map<String, Object> maintenanceData = new HashMap<>();
                maintenanceData.put("maintenanceId", maintenanceId);
                maintenanceData.put("driverId", driverId);
                maintenanceData.put("coachId", assignedCoachId);
                maintenanceData.put("type", data.get("type"));
                maintenanceData.put("urgency", data.get("urgency"));
                maintenanceData.put("description", data.get("description"));
                maintenanceData.put("status", "pending");
                maintenanceData.put("reportedDate", FieldValue.serverTimestamp());

                Firestore db = FirestoreClient.getFirestore();
                db.collection("maintenanceRequests").document(maintenanceId).set(maintenanceData);

                showSuccessAlert("Maintenance Request Submitted",
                        "Your maintenance request has been sent to the manager.\n" +
                                "Request ID: " + maintenanceId);

            } catch (Exception e) {
                showErrorAlert("Submission Error", "Failed to submit request: " + e.getMessage());
            }
        });
    }

    public static class TripHistory {
        private String date;
        private String route;
        private String distance;
        private String duration;
        private String passengers;
        private String status;

        public TripHistory(String date, String route, String distance, String duration, String passengers, String status) {
            this.date = date;
            this.route = route;
            this.distance = distance;
            this.duration = duration;
            this.passengers = passengers;
            this.status = status;
        }

        public String getDate() { return date; }
        public String getRoute() { return route; }
        public String getDistance() { return distance; }
        public String getDuration() { return duration; }
        public String getPassengers() { return passengers; }
        public String getStatus() { return status; }
    }

    public static class MaintenanceItem {
        private String item;
        private String status;
        private String date;

        public MaintenanceItem(String item, String status, String date) {
            this.item = item;
            this.status = status;
            this.date = date;
        }

        public String getItem() { return item; }
        public String getStatus() { return status; }
        public String getDate() { return date; }
    }

    public static class UpcomingTrip {
        private String date;
        private String time;
        private String route;
        private String coach;
        private String status;

        public UpcomingTrip(String date, String time, String route, String coach, String status) {
            this.date = date;
            this.time = time;
            this.route = route;
            this.coach = coach;
            this.status = status;
        }

        public String getDate() { return date; }
        public String getTime() { return time; }
        public String getRoute() { return route; }
        public String getCoach() { return coach; }
        public String getStatus() { return status; }
    }

    // NEW CLASS: TripData for route diagram
    public static class TripData {
        private String from;
        private String to;
        private String stops;
        private String departureTime;
        private String arrivalTime;

        public TripData(String from, String to, String stops, String departureTime, String arrivalTime) {
            this.from = from;
            this.to = to;
            this.stops = stops;
            this.departureTime = departureTime;
            this.arrivalTime = arrivalTime;
        }

        public String getFrom() { return from; }
        public String getTo() { return to; }
        public String getStops() { return stops; }
        public String getDepartureTime() { return departureTime; }
        public String getArrivalTime() { return arrivalTime; }
    }

    // NEW CLASS: RouteStopWithType pour l'affichage horizontal
    public static class RouteStopWithType {
        private String name;
        private String time;
        private String type;
        private String color;

        public RouteStopWithType(String name, String time, String type, String color) {
            this.name = name;
            this.time = time;
            this.type = type;
            this.color = color;
        }

        public String getName() { return name; }
        public String getTime() { return time; }
        public String getType() { return type; }
        public String getColor() { return color; }
    }
}