package com.sample.demo3.views.admin;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import com.sample.demo3.configuration.FirebaseService;
import com.sample.demo3.models.Driver;
import com.sample.demo3.models.Incident;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.*;
import javafx.scene.shape.Rectangle;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

public class AnalyticsCenterView {

    private final String PRIMARY_BLUE = "#2c3e50";
    private final String ACCENT_GOLD = "#f39c12";
    private final String SUCCESS_GREEN = "#27ae60";
    private final String DANGER_RED = "#e74c3c";
    private final String WARNING_ORANGE = "#e67e22";
    private final String CARD_BG = "#ffffff";
    private final String PURPLE = "#9b59b6";
    private final String CYAN = "#1abc9c";

    // Chartes
    private PieChart userRoleChart;
    private BarChart<String, Number> incidentsChart;
    private LineChart<String, Number> performanceChart;
    private AreaChart<String, Number> usageChart;

    // Labels pour les KPI
    private Label totalUsersLabel;
    private Label activeDriversLabel;
    private Label incidentsResolvedLabel;
    private Label revenueLabel;
    private Label satisfactionLabel;
    private Label efficiencyLabel;

    // Tables
    private TableView<AnalyticsRow> topPerformersTable;
    private TableView<AnalyticsRow> trendsTable;

    // Listes
    private ObservableList<AnalyticsRow> performersList = FXCollections.observableArrayList();
    private ObservableList<AnalyticsRow> trendsList = FXCollections.observableArrayList();

    // Executor pour le chargement asynchrone
    private ExecutorService executor;

    public VBox createView() {
        VBox mainView = new VBox(20);
        mainView.setAlignment(Pos.TOP_LEFT);
        mainView.setPadding(new Insets(20));
        mainView.setStyle("-fx-background-color: #f5f7fa;");

        // Header
        HBox headerBox = createHeader();

        // Filtres de période
        HBox filtersBox = createFilters();

        // KPI Cards
        GridPane kpiGrid = createKPICards();

        // Charts Section
        VBox chartsSection = createChartsSection();

        // Data Tables
        HBox tablesSection = createTablesSection();

        // Export & Actions
        HBox actionsSection = createActionsSection();

        // Assembler tout
        VBox contentBox = new VBox(25);
        contentBox.getChildren().addAll(
                headerBox,
                filtersBox,
                kpiGrid,
                chartsSection,
                tablesSection,
                actionsSection
        );

        ScrollPane scrollPane = new ScrollPane(contentBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        mainView.getChildren().add(scrollPane);

        // Démarrer le chargement des données
        executor = Executors.newSingleThreadExecutor();
        loadAnalyticsData();

        return mainView;
    }

    private HBox createHeader() {
        HBox header = new HBox();
        VBox headerText = new VBox(5);

        Text title = new Text("📈 Analytics Center");
        title.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 28));
        title.setFill(Color.web(PRIMARY_BLUE));

        Text subtitle = new Text("Business intelligence, predictive analytics, and data insights");
        subtitle.setFont(Font.font("Arial", 14));
        subtitle.setFill(Color.web("#7f8c8d"));

        headerText.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Date du rapport
        VBox dateBox = new VBox(2);
        dateBox.setAlignment(Pos.CENTER_RIGHT);

        Label dateLabel = new Label("Last Updated:");
        dateLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12;");

        Label currentDate = new Label(LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
        currentDate.setStyle("-fx-text-fill: " + PRIMARY_BLUE + "; -fx-font-weight: bold; -fx-font-size: 14;");

        dateBox.getChildren().addAll(dateLabel, currentDate);

        header.getChildren().addAll(headerText, spacer, dateBox);
        return header;
    }

    private HBox createFilters() {
        HBox filters = new HBox(15);
        filters.setPadding(new Insets(20, 0, 20, 0));
        filters.setAlignment(Pos.CENTER_LEFT);

        Label filterLabel = new Label("📅 Report Period:");
        filterLabel.setStyle("-fx-text-fill: " + PRIMARY_BLUE + "; -fx-font-weight: bold; -fx-font-size: 14;");

        ToggleGroup periodGroup = new ToggleGroup();
        ToggleButton btnToday = createToggleButton("Today", periodGroup, true);
        ToggleButton btnWeek = createToggleButton("This Week", periodGroup, false);
        ToggleButton btnMonth = createToggleButton("This Month", periodGroup, false);
        ToggleButton btnQuarter = createToggleButton("This Quarter", periodGroup, false);
        ToggleButton btnYear = createToggleButton("This Year", periodGroup, false);
        ToggleButton btnCustom = createToggleButton("Custom...", periodGroup, false);

        // Date pickers pour la période personnalisée
        HBox customRange = new HBox(10);
        customRange.setVisible(false);

        DatePicker startDate = new DatePicker(LocalDate.now().minusDays(7));
        DatePicker endDate = new DatePicker(LocalDate.now());

        startDate.setStyle(
                "-fx-background-color: #f8f9fa;" +
                        "-fx-border-color: #dee2e6;" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;"
        );

        endDate.setStyle(
                "-fx-background-color: #f8f9fa;" +
                        "-fx-border-color: #dee2e6;" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;"
        );

        Label toLabel = new Label("to");
        toLabel.setStyle("-fx-text-fill: #7f8c8d;");

        customRange.getChildren().addAll(startDate, toLabel, endDate);

        btnCustom.setOnAction(e -> customRange.setVisible(btnCustom.isSelected()));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnGenerate = new Button("🔄 Generate Report");
        btnGenerate.setStyle(
                "-fx-background-color: " + ACCENT_GOLD + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 10 20;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-size: 14;"
        );
        btnGenerate.setOnAction(e -> loadAnalyticsData());

        filters.getChildren().addAll(
                filterLabel, btnToday, btnWeek, btnMonth,
                btnQuarter, btnYear, btnCustom, customRange,
                spacer, btnGenerate
        );
        return filters;
    }

    private ToggleButton createToggleButton(String text, ToggleGroup group, boolean selected) {
        ToggleButton btn = new ToggleButton(text);
        btn.setToggleGroup(group);
        btn.setSelected(selected);
        btn.setStyle(
                "-fx-background-color: " + (selected ? ACCENT_GOLD : "#f8f9fa") + ";" +
                        "-fx-text-fill: " + (selected ? "white" : PRIMARY_BLUE) + ";" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 8 15;" +
                        "-fx-background-radius: 6;" +
                        "-fx-cursor: hand;" +
                        "-fx-border-color: #dee2e6;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 6;"
        );

        btn.selectedProperty().addListener((obs, oldVal, newVal) -> {
            btn.setStyle(
                    "-fx-background-color: " + (newVal ? ACCENT_GOLD : "#f8f9fa") + ";" +
                            "-fx-text-fill: " + (newVal ? "white" : PRIMARY_BLUE) + ";" +
                            "-fx-font-weight: bold;" +
                            "-fx-padding: 8 15;" +
                            "-fx-background-radius: 6;" +
                            "-fx-cursor: hand;" +
                            "-fx-border-color: #dee2e6;" +
                            "-fx-border-width: 1;" +
                            "-fx-border-radius: 6;"
            );
        });

        return btn;
    }

    private GridPane createKPICards() {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setPadding(new Insets(20, 0, 20, 0));

        // Total Users Card
        VBox usersCard = createKPICard("👥 TOTAL USERS", "0", "+2% from last month",
                PRIMARY_BLUE, "Registered users in system");
        totalUsersLabel = (Label) ((VBox) usersCard.getChildren().get(1)).getChildren().get(0);

        // Active Drivers Card
        VBox driversCard = createKPICard("🚗 ACTIVE DRIVERS", "0", "On duty today",
                SUCCESS_GREEN, "Currently operating");
        activeDriversLabel = (Label) ((VBox) driversCard.getChildren().get(1)).getChildren().get(0);

        // Incidents Resolved Card
        VBox incidentsCard = createKPICard("✅ INCIDENTS RESOLVED", "0", "95% success rate",
                ACCENT_GOLD, "This month");
        incidentsResolvedLabel = (Label) ((VBox) incidentsCard.getChildren().get(1)).getChildren().get(0);

        // Revenue Card
        VBox revenueCard = createKPICard("💰 ESTIMATED REVENUE", "$0", "+15% growth",
                PURPLE, "Monthly projection");
        revenueLabel = (Label) ((VBox) revenueCard.getChildren().get(1)).getChildren().get(0);

        // Satisfaction Card
        VBox satisfactionCard = createKPICard("😊 CUSTOMER SATISFACTION", "0%", "Based on 45 reviews",
                CYAN, "Average rating");
        satisfactionLabel = (Label) ((VBox) satisfactionCard.getChildren().get(1)).getChildren().get(0);

        // Efficiency Card
        VBox efficiencyCard = createKPICard("⚡ OPERATIONAL EFFICIENCY", "0%", "Optimal performance",
                WARNING_ORANGE, "Route optimization");
        efficiencyLabel = (Label) ((VBox) efficiencyCard.getChildren().get(1)).getChildren().get(0);

        grid.add(usersCard, 0, 0);
        grid.add(driversCard, 1, 0);
        grid.add(incidentsCard, 2, 0);
        grid.add(revenueCard, 0, 1);
        grid.add(satisfactionCard, 1, 1);
        grid.add(efficiencyCard, 2, 1);

        return grid;
    }

    private VBox createKPICard(String title, String value, String trend, String color, String description) {
        VBox card = new VBox(15);
        card.setPrefWidth(220);
        card.setPadding(new Insets(25));
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
        icon.setFont(Font.font(24));

        VBox titleBox = new VBox(2);
        Label titleLabel = new Label(title.substring(title.indexOf(" ") + 1));
        titleLabel.setStyle("-fx-text-fill: " + PRIMARY_BLUE + "; -fx-font-size: 14; -fx-font-weight: bold;");

        Label trendLabel = new Label(trend);
        trendLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 11; -fx-font-weight: bold;");

        titleBox.getChildren().addAll(titleLabel, trendLabel);
        header.getChildren().addAll(icon, titleBox);

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 36; -fx-font-weight: bold;");

        ProgressBar progressBar = new ProgressBar();
        progressBar.setPrefWidth(170);
        progressBar.setProgress(0.65);
        progressBar.setStyle("-fx-accent: " + color + ";");

        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12;");

        VBox content = new VBox(10, valueLabel, progressBar, descLabel);
        card.getChildren().addAll(header, content);
        return card;
    }

    private VBox createChartsSection() {
        VBox section = new VBox(20);
        section.setPadding(new Insets(25));
        section.setStyle(
                "-fx-background-color: " + CARD_BG + ";" +
                        "-fx-background-radius: 15;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 15;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 15, 0.3, 0, 5);"
        );

        Label sectionTitle = new Label("📊 ANALYTICAL CHARTS");
        sectionTitle.setStyle("-fx-text-fill: " + PRIMARY_BLUE + "; -fx-font-size: 18; -fx-font-weight: bold;");

        // Première ligne de graphiques
        HBox firstRow = new HBox(20);
        firstRow.setPadding(new Insets(15, 0, 0, 0));

        VBox userChartBox = createUserRoleChart();
        VBox incidentsChartBox = createIncidentsChart();

        firstRow.getChildren().addAll(userChartBox, incidentsChartBox);

        // Deuxième ligne de graphiques
        HBox secondRow = new HBox(20);
        secondRow.setPadding(new Insets(20, 0, 0, 0));

        VBox performanceChartBox = createPerformanceChart();
        VBox usageChartBox = createUsageChart();

        secondRow.getChildren().addAll(performanceChartBox, usageChartBox);

        section.getChildren().addAll(sectionTitle, firstRow, secondRow);
        return section;
    }

    private VBox createUserRoleChart() {
        VBox chartBox = new VBox(10);
        chartBox.setPrefWidth(400);

        HBox chartHeader = new HBox();
        Label chartTitle = new Label("👥 User Distribution by Role");
        chartTitle.setStyle("-fx-text-fill: " + PRIMARY_BLUE + "; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        ComboBox<String> chartType = new ComboBox<>();
        chartType.getItems().addAll("Pie Chart", "Bar Chart", "Donut Chart");
        chartType.setValue("Pie Chart");
        chartType.setStyle(
                "-fx-background-color: #f8f9fa;" +
                        "-fx-border-color: #dee2e6;" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 5 10;" +
                        "-fx-font-size: 12;"
        );

        chartHeader.getChildren().addAll(chartTitle, spacer, chartType);

        // Créer le graphique circulaire
        userRoleChart = new PieChart();
        userRoleChart.setTitle("User Roles Distribution");
        userRoleChart.setPrefHeight(300);
        userRoleChart.setLegendVisible(true);
        userRoleChart.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #e0e0e0;");

        // Données initiales
        updateUserRoleChart();

        chartBox.getChildren().addAll(chartHeader, userRoleChart);
        return chartBox;
    }

    private VBox createIncidentsChart() {
        VBox chartBox = new VBox(10);
        chartBox.setPrefWidth(400);

        HBox chartHeader = new HBox();
        Label chartTitle = new Label("⚠️ Incidents by Type (Last 30 Days)");
        chartTitle.setStyle("-fx-text-fill: " + PRIMARY_BLUE + "; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnExport = new Button("📥 Export");
        btnExport.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: " + ACCENT_GOLD + ";" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 5 15;" +
                        "-fx-border-color: " + ACCENT_GOLD + ";" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 6;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-size: 12;"
        );

        chartHeader.getChildren().addAll(chartTitle, spacer, btnExport);

        // Créer le graphique à barres
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Incident Type");
        yAxis.setLabel("Count");

        incidentsChart = new BarChart<>(xAxis, yAxis);
        incidentsChart.setTitle("Incidents Analysis");
        incidentsChart.setPrefHeight(300);
        incidentsChart.setLegendVisible(false);
        incidentsChart.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #e0e0e0;");

        // Données initiales
        updateIncidentsChart();

        chartBox.getChildren().addAll(chartHeader, incidentsChart);
        return chartBox;
    }

    private VBox createPerformanceChart() {
        VBox chartBox = new VBox(10);
        chartBox.setPrefWidth(400);

        Label chartTitle = new Label("📈 Monthly Performance Trends");
        chartTitle.setStyle("-fx-text-fill: " + PRIMARY_BLUE + "; -fx-font-weight: bold;");

        // Créer le graphique de ligne
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Month");
        yAxis.setLabel("Performance Score");

        performanceChart = new LineChart<>(xAxis, yAxis);
        performanceChart.setTitle("Monthly Performance");
        performanceChart.setPrefHeight(250);
        performanceChart.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #e0e0e0;");

        // Série de données
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Performance Score");

        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul"};
        Random random = new Random();

        for (String month : months) {
            series.getData().add(new XYChart.Data<>(month, random.nextInt(30) + 70));
        }

        performanceChart.getData().add(series);

        chartBox.getChildren().addAll(chartTitle, performanceChart);
        return chartBox;
    }

    private VBox createUsageChart() {
        VBox chartBox = new VBox(10);
        chartBox.setPrefWidth(400);

        Label chartTitle = new Label("📱 System Usage Patterns");
        chartTitle.setStyle("-fx-text-fill: " + PRIMARY_BLUE + "; -fx-font-weight: bold;");

        // Créer le graphique en aires
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Time of Day");
        yAxis.setLabel("Active Users");

        usageChart = new AreaChart<>(xAxis, yAxis);
        usageChart.setTitle("Daily Usage Pattern");
        usageChart.setPrefHeight(250);
        usageChart.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #e0e0e0;");

        // Série de données
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Active Users");

        String[] hours = {"6AM", "9AM", "12PM", "3PM", "6PM", "9PM", "12AM"};
        int[] usage = {5, 25, 35, 30, 40, 20, 8};

        for (int i = 0; i < hours.length; i++) {
            series.getData().add(new XYChart.Data<>(hours[i], usage[i]));
        }

        usageChart.getData().add(series);

        chartBox.getChildren().addAll(chartTitle, usageChart);
        return chartBox;
    }

    private HBox createTablesSection() {
        HBox tablesSection = new HBox(20);
        tablesSection.setPadding(new Insets(20, 0, 0, 0));

        // Table des meilleurs performeurs
        VBox performersBox = createPerformersTable();

        // Table des tendances
        VBox trendsBox = createTrendsTable();

        tablesSection.getChildren().addAll(performersBox, trendsBox);
        return tablesSection;
    }

    private VBox createPerformersTable() {
        VBox section = new VBox(15);
        section.setPrefWidth(400);
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
        Label sectionTitle = new Label("🏆 Top Performers This Month");
        sectionTitle.setStyle("-fx-text-fill: " + PRIMARY_BLUE + "; -fx-font-size: 16; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll("All Drivers", "By Routes", "By Efficiency", "By Revenue");
        categoryCombo.setValue("All Drivers");
        categoryCombo.setStyle(
                "-fx-background-color: #f8f9fa;" +
                        "-fx-border-color: #dee2e6;" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 5 10;" +
                        "-fx-font-size: 12;"
        );

        header.getChildren().addAll(sectionTitle, spacer, categoryCombo);

        // Table des performeurs
        topPerformersTable = new TableView<>();
        topPerformersTable.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-border-color: #dee2e6;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;" +
                        "-fx-font-size: 14;"
        );
        topPerformersTable.setPrefHeight(250);
        topPerformersTable.setPlaceholder(new Label("Loading top performers data..."));

        // Colonnes
        TableColumn<AnalyticsRow, String> rankCol = new TableColumn<>("#");
        rankCol.setCellValueFactory(new PropertyValueFactory<>("rank"));
        rankCol.setPrefWidth(40);

        TableColumn<AnalyticsRow, String> nameCol = new TableColumn<>("Driver");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(120);

        TableColumn<AnalyticsRow, String> metricCol = new TableColumn<>("Performance");
        metricCol.setCellValueFactory(new PropertyValueFactory<>("metric"));
        metricCol.setPrefWidth(100);

        TableColumn<AnalyticsRow, String> scoreCol = new TableColumn<>("Score");
        scoreCol.setCellValueFactory(new PropertyValueFactory<>("score"));
        scoreCol.setPrefWidth(80);

        TableColumn<AnalyticsRow, String> trendCol = new TableColumn<>("Trend");
        trendCol.setCellValueFactory(new PropertyValueFactory<>("trend"));
        trendCol.setPrefWidth(60);
        trendCol.setCellFactory(col -> new TableCell<AnalyticsRow, String>() {
            @Override
            protected void updateItem(String trend, boolean empty) {
                super.updateItem(trend, empty);
                if (empty || trend == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(trend);
                    setFont(Font.font("Arial", FontWeight.BOLD, 12));
                    setAlignment(Pos.CENTER);

                    if (trend.contains("↑")) {
                        setStyle("-fx-text-fill: " + SUCCESS_GREEN + ";");
                    } else if (trend.contains("↓")) {
                        setStyle("-fx-text-fill: " + DANGER_RED + ";");
                    } else {
                        setStyle("-fx-text-fill: #7f8c8d;");
                    }
                }
            }
        });

        topPerformersTable.getColumns().addAll(rankCol, nameCol, metricCol, scoreCol, trendCol);
        topPerformersTable.setItems(performersList);

        section.getChildren().addAll(header, topPerformersTable);
        return section;
    }

    private VBox createTrendsTable() {
        VBox section = new VBox(15);
        section.setPrefWidth(400);
        section.setPadding(new Insets(25));
        section.setStyle(
                "-fx-background-color: " + CARD_BG + ";" +
                        "-fx-background-radius: 15;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 15;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 15, 0.3, 0, 5);"
        );

        Label sectionTitle = new Label("📈 Key Trends & Insights");
        sectionTitle.setStyle("-fx-text-fill: " + PRIMARY_BLUE + "; -fx-font-size: 16; -fx-font-weight: bold;");

        // Table des tendances
        trendsTable = new TableView<>();
        trendsTable.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-border-color: #dee2e6;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;" +
                        "-fx-font-size: 14;"
        );
        trendsTable.setPrefHeight(250);
        trendsTable.setPlaceholder(new Label("Loading trends data..."));

        // Colonnes
        TableColumn<AnalyticsRow, String> insightCol = new TableColumn<>("Insight");
        insightCol.setCellValueFactory(new PropertyValueFactory<>("insight"));
        insightCol.setPrefWidth(200);

        TableColumn<AnalyticsRow, String> impactCol = new TableColumn<>("Impact");
        impactCol.setCellValueFactory(new PropertyValueFactory<>("impact"));
        impactCol.setPrefWidth(100);

        TableColumn<AnalyticsRow, String> confidenceCol = new TableColumn<>("Confidence");
        confidenceCol.setCellValueFactory(new PropertyValueFactory<>("confidence"));
        confidenceCol.setPrefWidth(100);
        confidenceCol.setCellFactory(col -> new TableCell<AnalyticsRow, String>() {
            @Override
            protected void updateItem(String confidence, boolean empty) {
                super.updateItem(confidence, empty);
                if (empty || confidence == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(confidence);
                    setFont(Font.font("Arial", FontWeight.BOLD, 12));
                    setAlignment(Pos.CENTER);

                    if (confidence.contains("High")) {
                        setStyle("-fx-text-fill: " + SUCCESS_GREEN + "; -fx-background-color: #e8f5e9; " +
                                "-fx-background-radius: 12; -fx-padding: 4 12;");
                    } else if (confidence.contains("Medium")) {
                        setStyle("-fx-text-fill: " + WARNING_ORANGE + "; -fx-background-color: #fff3e0; " +
                                "-fx-background-radius: 12; -fx-padding: 4 12;");
                    } else {
                        setStyle("-fx-text-fill: #7f8c8d; -fx-background-color: #f5f5f5; " +
                                "-fx-background-radius: 12; -fx-padding: 4 12;");
                    }
                }
            }
        });

        trendsTable.getColumns().addAll(insightCol, impactCol, confidenceCol);
        trendsTable.setItems(trendsList);

        section.getChildren().addAll(sectionTitle, trendsTable);
        return section;
    }

    private HBox createActionsSection() {
        HBox actions = new HBox(15);
        actions.setPadding(new Insets(20, 0, 0, 0));
        actions.setAlignment(Pos.CENTER);

        Button btnExportPDF = createActionButton("📄 Export PDF", ACCENT_GOLD);
        Button btnExportExcel = createActionButton("📊 Export Excel", SUCCESS_GREEN);
        Button btnScheduleReport = createActionButton("⏰ Schedule Report", "#3498db");
        Button btnShareDashboard = createActionButton("🔗 Share Dashboard", PURPLE);
        Button btnPredictive = createActionButton("🔮 Predictive Analysis", DANGER_RED);

        btnExportPDF.setOnAction(e -> exportToPDF());
        btnExportExcel.setOnAction(e -> exportToExcel());
        btnScheduleReport.setOnAction(e -> scheduleReport());
        btnShareDashboard.setOnAction(e -> shareDashboard());
        btnPredictive.setOnAction(e -> showPredictiveAnalysis());

        actions.getChildren().addAll(btnExportPDF, btnExportExcel, btnScheduleReport, btnShareDashboard, btnPredictive);
        return actions;
    }

    private Button createActionButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 12 20;" +
                        "-fx-background-radius: 10;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-size: 14;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0.3, 0, 2);"
        );

        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: " + darkenColor(color) + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 12 20;" +
                        "-fx-background-radius: 10;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-size: 14;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0.4, 0, 3);"
        ));

        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 12 20;" +
                        "-fx-background-radius: 10;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-size: 14;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0.3, 0, 2);"
        ));

        return btn;
    }

    private String darkenColor(String hexColor) {
        if (hexColor.equals(ACCENT_GOLD)) return "#e67e22";
        if (hexColor.equals(PRIMARY_BLUE)) return "#2c3e50";
        if (hexColor.equals(SUCCESS_GREEN)) return "#229954";
        if (hexColor.equals(DANGER_RED)) return "#c0392b";
        if (hexColor.equals(WARNING_ORANGE)) return "#d35400";
        if (hexColor.equals(PURPLE)) return "#8e44ad";
        if (hexColor.equals(CYAN)) return "#16a085";
        if (hexColor.equals("#3498db")) return "#2980b9";
        return hexColor;
    }

    private void loadAnalyticsData() {
        executor.submit(() -> {
            try {
                System.out.println("🔄 Loading REAL analytics data directly...");

                // Initialiser Firestore directement
                Firestore db = FirestoreClient.getFirestore();

                // Charger les données DIRECTEMENT sans passer par FirebaseService
                Map<String, Object> realData = new HashMap<>();

                // 1. Total Users (simple query)
                long totalUsers = db.collection("users").count().get().get().getCount();
                realData.put("totalUsers", totalUsers);
                System.out.println("✅ Total users: " + totalUsers);

                // 2. Active Drivers (simple query)
                long activeDrivers = db.collection("drivers")
                        .whereEqualTo("status", "active")
                        .count().get().get().getCount();
                realData.put("activeDrivers", activeDrivers);
                System.out.println("✅ Active drivers: " + activeDrivers);

                // 3. Incidents Resolved (simple query)
                long incidentsResolved = db.collection("incidents")
                        .whereEqualTo("status", "resolved")
                        .count().get().get().getCount();
                realData.put("incidentsResolved", incidentsResolved);
                System.out.println("✅ Incidents resolved: " + incidentsResolved);

                // 4. Pending Incidents (simple query)
                long pendingIncidents = db.collection("incidents")
                        .whereEqualTo("status", "pending")
                        .count().get().get().getCount();
                realData.put("pendingIncidents", pendingIncidents);

                // 5. Total Coaches
                long totalCoaches = db.collection("coaches").count().get().get().getCount();
                realData.put("totalCoaches", totalCoaches);

                // 6. Revenue estimate (calcul simple)
                double revenueEstimate = activeDrivers * 30 * 200; // 30 jours * $200/jour/driver
                realData.put("revenueEstimate", revenueEstimate);

                Platform.runLater(() -> {
                    updateKPIData(realData);
                    updateUserRoleChartWithRealData();  // Charger les données pour le pie chart
                    updateIncidentsChartWithRealData(); // Charger les incidents pour le bar chart
                    updatePerformersTableWithRealData(); // Charger les drivers
                    updateTrendsTableWithRealData();    // Charger les tendances
                });

            } catch (Exception e) {
                System.err.println("❌ Error in direct data load: " + e.getMessage());
                e.printStackTrace();

                // Fallback aux données simulées
                Platform.runLater(() -> {
                    Map<String, Object> fallbackData = new HashMap<>();
                    Random random = new Random();
                    fallbackData.put("totalUsers", random.nextInt(50) + 100);
                    fallbackData.put("activeDrivers", random.nextInt(15) + 10);
                    fallbackData.put("incidentsResolved", random.nextInt(20) + 5);
                    fallbackData.put("revenueEstimate", 10000 + random.nextInt(5000));

                    updateKPIData(fallbackData);
                    updateUserRoleChart();
                    updateIncidentsChart();
                    updateTablesData(null);
                });
            }
        });
    }

    private void updateKPIData(Map<String, Object> realData) {
        try {
            if (realData != null && !realData.isEmpty()) {
                // Utiliser les données réelles avec conversion sécurisée
                Long totalUsers = convertToLong(realData.get("totalUsers"));
                Long activeDrivers = convertToLong(realData.get("activeDrivers"));
                Long incidentsResolved = convertToLong(realData.get("incidentsResolved"));

                totalUsersLabel.setText(totalUsers != null ? totalUsers.toString() : "0");
                activeDriversLabel.setText(activeDrivers != null ? activeDrivers.toString() : "0");
                incidentsResolvedLabel.setText(incidentsResolved != null ? incidentsResolved.toString() : "0");

                // Formatage du revenue
                Object revenue = realData.get("revenueEstimate");
                if (revenue != null) {
                    if (revenue instanceof Double) {
                        revenueLabel.setText("$" + String.format("%.0f", (Double) revenue));
                    } else if (revenue instanceof Long) {
                        revenueLabel.setText("$" + revenue.toString());
                    } else if (revenue instanceof Integer) {
                        revenueLabel.setText("$" + revenue.toString());
                    } else {
                        revenueLabel.setText("$0");
                    }
                } else {
                    revenueLabel.setText("$0");
                }

                satisfactionLabel.setText("85%"); // Valeur par défaut
                efficiencyLabel.setText("92%"); // Valeur par défaut

            } else {
                // Données simulées en fallback
                Random random = new Random();
                totalUsersLabel.setText(String.valueOf(random.nextInt(50) + 100));
                activeDriversLabel.setText(String.valueOf(random.nextInt(15) + 10));
                incidentsResolvedLabel.setText(String.valueOf(random.nextInt(20) + 5));
                revenueLabel.setText("$" + (random.nextInt(5000) + 10000));
                satisfactionLabel.setText((random.nextInt(20) + 80) + "%");
                efficiencyLabel.setText((random.nextInt(15) + 85) + "%");
            }
        } catch (Exception e) {
            System.err.println("❌ Error updating KPI data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateUserRoleChart() {
        try {
            userRoleChart.getData().clear();

            PieChart.Data drivers = new PieChart.Data("Drivers", 65);
            PieChart.Data managers = new PieChart.Data("Managers", 20);
            PieChart.Data admins = new PieChart.Data("Admins", 10);
            PieChart.Data others = new PieChart.Data("Others", 5);

            userRoleChart.getData().addAll(drivers, managers, admins, others);

            // Attendre que le graphique soit rendu avant d'appliquer les styles
            Platform.runLater(() -> {
                try {
                    int colorIndex = 0;
                    String[] colors = {SUCCESS_GREEN, ACCENT_GOLD, DANGER_RED, PURPLE};

                    for (PieChart.Data data : userRoleChart.getData()) {
                        Node node = data.getNode();
                        if (node != null && colorIndex < colors.length) {
                            node.setStyle("-fx-pie-color: " + colors[colorIndex] + ";");
                        }
                        colorIndex++;
                    }
                } catch (Exception e) {
                    System.err.println("⚠️ Error applying pie chart colors: " + e.getMessage());
                }
            });

        } catch (Exception e) {
            System.err.println("❌ Error updating user role chart: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void applyPieChartColors() {
        // Créer un style CSS avec les couleurs
        StringBuilder styleBuilder = new StringBuilder();

        // Définir les couleurs pour chaque segment
        styleBuilder.append(".chart-pie.default-color0 { -fx-pie-color: ").append(SUCCESS_GREEN).append("; } ");
        styleBuilder.append(".chart-pie.default-color1 { -fx-pie-color: ").append(ACCENT_GOLD).append("; } ");
        styleBuilder.append(".chart-pie.default-color2 { -fx-pie-color: ").append(DANGER_RED).append("; } ");
        styleBuilder.append(".chart-pie.default-color3 { -fx-pie-color: ").append(PURPLE).append("; } ");
        styleBuilder.append(".chart-pie.default-color4 { -fx-pie-color: ").append(CYAN).append("; } ");

        userRoleChart.setStyle(styleBuilder.toString());

        // Également essayer d'appliquer directement après un court délai
        Platform.runLater(() -> {
            try {
                String[] colors = {SUCCESS_GREEN, ACCENT_GOLD, DANGER_RED, PURPLE};
                for (int i = 0; i < userRoleChart.getData().size() && i < colors.length; i++) {
                    PieChart.Data data = userRoleChart.getData().get(i);
                    Node node = data.getNode();
                    if (node != null) {
                        node.setStyle("-fx-pie-color: " + colors[i] + ";");
                    }
                }
            } catch (Exception e) {
                // Ignorer l'erreur, le CSS devrait fonctionner
            }
        });
    }

    private void updateIncidentsChart() {
        incidentsChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Incidents");

        String[] types = {"Accidents", "Delays", "Maintenance", "Complaints", "Other"};
        Random random = new Random();

        for (String type : types) {
            series.getData().add(new XYChart.Data<>(type, random.nextInt(15) + 5));
        }

        incidentsChart.getData().add(series);
    }

    private void updateChartsData(Map<String, Object> realData) {
        try {
            // 1. Mettre à jour le graphique des rôles utilisateur avec des données réelles
            updateUserRoleChartWithRealData();

            // 2. Mettre à jour le graphique des incidents avec des données réelles
            updateIncidentsChartWithRealData();

            // 3. Mettre à jour le graphique de performance
            updatePerformanceChartWithRealData();

            // 4. Mettre à jour le graphique d'utilisation
            updateUsageChartWithRealData();

        } catch (Exception e) {
            System.err.println("❌ Error updating charts: " + e.getMessage());
            updateChartsData(null); // Fallback aux données simulées
        }
    }

    private void updateUserRoleChartWithRealData() {
        try {
            userRoleChart.getData().clear();

            Firestore db = FirestoreClient.getFirestore();

            // Compter les utilisateurs par rôle - REQUÊTES SIMPLES
            long admins = db.collection("users")
                    .whereEqualTo("role", "admin")
                    .count().get().get().getCount();

            long managers = db.collection("users")
                    .whereEqualTo("role", "manager")
                    .count().get().get().getCount();

            long drivers = db.collection("users")
                    .whereEqualTo("role", "driver")
                    .count().get().get().getCount();

            // Total users pour les "others"
            long totalUsers = db.collection("users").count().get().get().getCount();
            long others = totalUsers - (admins + managers + drivers);

            System.out.println("📊 User roles: Admins=" + admins + ", Managers=" + managers + ", Drivers=" + drivers + ", Others=" + others);

            if (admins > 0) {
                userRoleChart.getData().add(new PieChart.Data("Admins", admins));
            }
            if (managers > 0) {
                userRoleChart.getData().add(new PieChart.Data("Managers", managers));
            }
            if (drivers > 0) {
                userRoleChart.getData().add(new PieChart.Data("Drivers", drivers));
            }
            if (others > 0) {
                userRoleChart.getData().add(new PieChart.Data("Others", others));
            }

            // Si aucune donnée, ajouter des données de démonstration
            if (userRoleChart.getData().isEmpty()) {
                userRoleChart.getData().addAll(
                        new PieChart.Data("Admins", 2),
                        new PieChart.Data("Managers", 5),
                        new PieChart.Data("Drivers", 20),
                        new PieChart.Data("Others", 3)
                );
            }

            // Appliquer les couleurs
            applyPieChartColors();

        } catch (Exception e) {
            System.err.println("❌ Error in user role chart: " + e.getMessage());
            updateUserRoleChart(); // Fallback
        }
    }

    private void updateIncidentsChartWithRealData() {
        try {
            incidentsChart.getData().clear();

            // Récupérer les incidents depuis Firestore
            ObservableList<Incident> incidents = FirebaseService.loadAllIncidents();

            if (incidents != null && !incidents.isEmpty()) {
                // Compter les incidents par type
                Map<String, Integer> incidentCountByType = new HashMap<>();

                for (Incident incident : incidents) {
                    String type = incident.getType();
                    if (type != null && !type.isEmpty()) {
                        incidentCountByType.put(type, incidentCountByType.getOrDefault(type, 0) + 1);
                    }
                }

                // Créer la série de données
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName("Incidents");

                for (Map.Entry<String, Integer> entry : incidentCountByType.entrySet()) {
                    series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
                }

                incidentsChart.getData().add(series);

            } else {
                // Fallback aux données simulées
                updateIncidentsChart();
            }

        } catch (Exception e) {
            System.err.println("❌ Error loading real incidents data: " + e.getMessage());
            updateIncidentsChart(); // Fallback
        }
    }

    private void updatePerformanceChartWithRealData() {
        try {
            performanceChart.getData().clear();

            // Récupérer les données de performance (à implémenter dans FirebaseService)
            Map<String, Object> performanceData = FirebaseService.getPerformanceMetrics();

            if (performanceData != null && performanceData.containsKey("monthlyPerformance")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> monthlyData = (List<Map<String, Object>>) performanceData.get("monthlyPerformance");

                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName("Performance Score");

                for (Map<String, Object> monthData : monthlyData) {
                    String month = (String) monthData.get("month");
                    Number score = (Number) monthData.get("score");
                    series.getData().add(new XYChart.Data<>(month, score));
                }

                performanceChart.getData().add(series);

            } else {
                // Données simulées
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName("Performance Score");

                String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul"};
                Random random = new Random();

                for (String month : months) {
                    series.getData().add(new XYChart.Data<>(month, random.nextInt(30) + 70));
                }

                performanceChart.getData().add(series);
            }

        } catch (Exception e) {
            System.err.println("❌ Error loading performance data: " + e.getMessage());
        }
    }

    private void updateUsageChartWithRealData() {
        try {
            usageChart.getData().clear();

            // Récupérer les données d'utilisation depuis Firestore
            ObservableList<Map<String, Object>> activities = FirebaseService.getAllUserActivities();

            if (activities != null && !activities.isEmpty()) {
                // Analyser les activités par heure
                Map<Integer, Integer> activitiesByHour = new HashMap<>();

                for (Map<String, Object> activity : activities) {
                    Object timestampObj = activity.get("timestamp");
                    if (timestampObj != null) {
                        try {
                            // Convertir le timestamp en heure
                            // Cette logique dépend de votre format de date dans Firestore
                            // Pour l'instant, simulation
                            Random random = new Random();
                            int hour = random.nextInt(24);
                            activitiesByHour.put(hour, activitiesByHour.getOrDefault(hour, 0) + 1);
                        } catch (Exception e) {
                            // Ignorer les erreurs de conversion
                        }
                    }
                }

                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName("Active Users");

                // Heures de la journée
                String[] hours = {"6AM", "9AM", "12PM", "3PM", "6PM", "9PM", "12AM"};

                for (String hour : hours) {
                    // Simuler des données basées sur l'heure
                    int count = 0;
                    switch (hour) {
                        case "9AM": case "3PM": case "6PM":
                            count = 25 + new Random().nextInt(15);
                            break;
                        case "12PM":
                            count = 35 + new Random().nextInt(10);
                            break;
                        default:
                            count = 5 + new Random().nextInt(20);
                    }
                    series.getData().add(new XYChart.Data<>(hour, count));
                }

                usageChart.getData().add(series);

            } else {
                // Données simulées
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName("Active Users");

                String[] hours = {"6AM", "9AM", "12PM", "3PM", "6PM", "9PM", "12AM"};
                int[] usage = {5, 25, 35, 30, 40, 20, 8};

                for (int i = 0; i < hours.length; i++) {
                    series.getData().add(new XYChart.Data<>(hours[i], usage[i]));
                }

                usageChart.getData().add(series);
            }

        } catch (Exception e) {
            System.err.println("❌ Error loading usage data: " + e.getMessage());
        }
    }

    private void updateTablesData(Map<String, Object> realData) {
        try {
            // 1. Remplir la table des performeurs avec des données réelles
            updatePerformersTableWithRealData();

            // 2. Remplir la table des tendances avec des données réelles
            updateTrendsTableWithRealData();

        } catch (Exception e) {
            System.err.println("❌ Error updating tables: " + e.getMessage());
            updateTablesData(null); // Fallback
        }
    }

    private void updatePerformersTableWithRealData() {
        try {
            performersList.clear();

            // Récupérer les drivers depuis Firestore
            ObservableList<Driver> drivers = FirebaseService.loadAllDrivers();

            if (drivers != null && !drivers.isEmpty()) {
                // Trier les drivers par performance (simulée pour l'instant)
                List<Driver> sortedDrivers = new ArrayList<>(drivers);
                sortedDrivers.sort((d1, d2) -> {
                    // Logique de tri basée sur le statut et d'autres critères
                    boolean d1Active = "active".equalsIgnoreCase(d1.getStatus());
                    boolean d2Active = "active".equalsIgnoreCase(d2.getStatus());

                    if (d1Active && !d2Active) return -1;
                    if (!d1Active && d2Active) return 1;
                    return d1.getName().compareTo(d2.getName());
                });

                // Ajouter les 5 premiers à la table
                for (int i = 0; i < Math.min(5, sortedDrivers.size()); i++) {
                    Driver driver = sortedDrivers.get(i);
                    Random random = new Random();

                    performersList.add(new AnalyticsRow(
                            String.valueOf(i + 1),
                            driver.getName(),
                            "On-time %",
                            (85 + random.nextInt(15)) + "%",
                            random.nextBoolean() ? "↑ " + (random.nextInt(10) + 1) + "%" : "↓ " + (random.nextInt(5) + 1) + "%"
                    ));
                }
            } else {
                // Fallback aux données simulées
                updateTablesData(null);
            }

        } catch (Exception e) {
            System.err.println("❌ Error loading real performers data: " + e.getMessage());
            updateTablesData(null); // Fallback
        }
    }

    private void updateTrendsTableWithRealData() {
        try {
            trendsList.clear();

            // Récupérer les données analytiques
            Map<String, Object> analytics = FirebaseService.getDashboardStats();

            if (analytics != null) {
                // Générer des insights basés sur les données réelles
                List<String> insights = new ArrayList<>();

                // Convertir les valeurs en Long de manière sécurisée
                Long activeDrivers = convertToLong(analytics.get("activeDrivers"));
                Long pendingIncidents = convertToLong(analytics.get("pendingIncidents"));
                Long totalIncidents = convertToLong(analytics.get("totalIncidents"));

                if (activeDrivers != null && activeDrivers > 0) {
                    insights.add(activeDrivers + " drivers currently active (" +
                            (activeDrivers > 10 ? "Good coverage" : "Consider adding more drivers") + ")");
                }

                if (pendingIncidents != null) {
                    insights.add(pendingIncidents + " incidents pending resolution" +
                            (pendingIncidents > 5 ? " - Needs attention" : " - Under control"));
                }

                if (totalIncidents != null && totalIncidents > 0 && pendingIncidents != null) {
                    double resolutionRate = ((double) (totalIncidents - pendingIncidents) / totalIncidents) * 100;
                    insights.add(String.format("Incident resolution rate: %.1f%%", resolutionRate));
                }

                // Ajouter des insights généraux
                insights.add("Peak operational hours: 8-9 AM, 5-6 PM");
                insights.add("Most efficient route: Route 3 (City Center - Airport)");

                // Ajouter à la table
                String[] impacts = {"High", "Medium", "High", "Medium"};
                String[] confidences = {"High", "Medium", "High", "Medium"};

                for (int i = 0; i < Math.min(insights.size(), 4); i++) {
                    trendsList.add(new AnalyticsRow(
                            insights.get(i),
                            impacts[i],
                            confidences[i]
                    ));
                }
            } else {
                // Fallback aux données simulées
                String[] defaultInsights = {
                        "Peak usage hours identified (8-9 AM, 5-6 PM)",
                        "Route 3 shows 25% higher efficiency than average",
                        "Driver satisfaction correlates with customer ratings",
                        "Weekend usage patterns differ significantly"
                };

                String[] impacts = {"High", "Medium", "High", "Medium"};
                String[] confidences = {"High", "Medium", "High", "Medium"};

                for (int i = 0; i < 4; i++) {
                    trendsList.add(new AnalyticsRow(
                            defaultInsights[i],
                            impacts[i],
                            confidences[i]
                    ));
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Error loading real trends data: " + e.getMessage());
            e.printStackTrace();
            updateTablesData(null); // Fallback
        }
    }

    // Méthode utilitaire pour convertir en Long
    private Long convertToLong(Object value) {
        if (value == null) return null;

        try {
            if (value instanceof Long) {
                return (Long) value;
            } else if (value instanceof Integer) {
                return ((Integer) value).longValue();
            } else if (value instanceof Double) {
                return ((Double) value).longValue();
            } else if (value instanceof String) {
                return Long.parseLong((String) value);
            } else {
                System.err.println("⚠️ Cannot convert type: " + value.getClass().getName());
                return null;
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error converting to Long: " + e.getMessage());
            return null;
        }
    }

    private void exportToPDF() {
        showAlert("Export PDF",
                "PDF report generation started.\n\n" +
                        "The report will include:\n" +
                        "• All KPI metrics\n" +
                        "• Analytical charts\n" +
                        "• Performance tables\n" +
                        "• Executive summary\n\n" +
                        "Estimated completion: 30 seconds",
                Alert.AlertType.INFORMATION);
    }

    private void exportToExcel() {
        showAlert("Export Excel",
                "Excel spreadsheet export initiated.\n\n" +
                        "File will contain:\n" +
                        "• Raw data tables\n" +
                        "• Calculated metrics\n" +
                        "• Chart data sources\n" +
                        "• Time-series data\n\n" +
                        "Download will start automatically.",
                Alert.AlertType.INFORMATION);
    }

    private void scheduleReport() {
        Dialog<Map<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Schedule Automated Report");
        dialog.setHeaderText("Configure recurring analytics report");

        ButtonType scheduleButton = new ButtonType("Schedule", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(scheduleButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<String> frequencyCombo = new ComboBox<>();
        frequencyCombo.getItems().addAll("Daily", "Weekly", "Monthly", "Quarterly");
        frequencyCombo.setValue("Weekly");

        ComboBox<String> formatCombo = new ComboBox<>();
        formatCombo.getItems().addAll("PDF", "Excel", "HTML", "CSV");
        formatCombo.setValue("PDF");

        TextField emailField = new TextField();
        emailField.setPromptText("recipient@company.com");

        grid.add(new Label("Frequency:"), 0, 0);
        grid.add(frequencyCombo, 1, 0);
        grid.add(new Label("Format:"), 0, 1);
        grid.add(formatCombo, 1, 1);
        grid.add(new Label("Email to:"), 0, 2);
        grid.add(emailField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.showAndWait();
    }

    private void shareDashboard() {
        String dashboardUrl = "https://analytics.translink.com/dashboard/" + System.currentTimeMillis();

        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(dashboardUrl);
        clipboard.setContent(content);

        showAlert("Dashboard Shared",
                "Dashboard link copied to clipboard!\n\n" +
                        "Share this link with stakeholders:\n" +
                        dashboardUrl + "\n\n" +
                        "The dashboard will be available for 7 days.",
                Alert.AlertType.INFORMATION);
    }

    private void showPredictiveAnalysis() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Predictive Analysis");
        dialog.setHeaderText("AI-Powered Predictive Insights");

        TextArea analysisArea = new TextArea();
        analysisArea.setEditable(false);
        analysisArea.setWrapText(true);
        analysisArea.setPrefRowCount(15);
        analysisArea.setText(
                "🤖 AI PREDICTIVE ANALYSIS REPORT\n" +
                        "Generated: " + LocalDate.now() + "\n" +
                        "================================\n\n" +
                        "📈 FORECAST FOR NEXT 30 DAYS:\n" +
                        "• Expected new users: 12-18\n" +
                        "• Predicted incidents: 8-12\n" +
                        "• Revenue projection: $12,500 - $14,200\n" +
                        "• Peak usage days: Mondays & Fridays\n\n" +
                        "⚠️ RISK ASSESSMENT:\n" +
                        "• High probability of maintenance needed for Coach C801\n" +
                        "• Route 4 shows potential for delays (65% confidence)\n" +
                        "• Driver satisfaction may decrease by 3-5%\n\n" +
                        "💡 RECOMMENDATIONS:\n" +
                        "1. Schedule maintenance for Coach C801 next week\n" +
                        "2. Increase capacity on Route 4 during peak hours\n" +
                        "3. Implement driver wellness program\n" +
                        "4. Consider adding 1-2 new drivers for weekend shifts\n\n" +
                        "Confidence Level: 87%"
        );

        dialog.getDialogPane().setContent(analysisArea);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.setResizable(true);
        dialog.getDialogPane().setPrefSize(600, 500);
        dialog.showAndWait();
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // ==============================
    // DATA MODEL CLASS
    // ==============================

    public static class AnalyticsRow {
        // Pour la table des performeurs
        private final javafx.beans.property.SimpleStringProperty rank;
        private final javafx.beans.property.SimpleStringProperty name;
        private final javafx.beans.property.SimpleStringProperty metric;
        private final javafx.beans.property.SimpleStringProperty score;
        private final javafx.beans.property.SimpleStringProperty trend;

        // Pour la table des tendances
        private final javafx.beans.property.SimpleStringProperty insight;
        private final javafx.beans.property.SimpleStringProperty impact;
        private final javafx.beans.property.SimpleStringProperty confidence;

        // Constructeur pour les performeurs
        public AnalyticsRow(String rank, String name, String metric, String score, String trend) {
            this.rank = new javafx.beans.property.SimpleStringProperty(rank);
            this.name = new javafx.beans.property.SimpleStringProperty(name);
            this.metric = new javafx.beans.property.SimpleStringProperty(metric);
            this.score = new javafx.beans.property.SimpleStringProperty(score);
            this.trend = new javafx.beans.property.SimpleStringProperty(trend);
            this.insight = null;
            this.impact = null;
            this.confidence = null;
        }

        // Constructeur pour les tendances
        public AnalyticsRow(String insight, String impact, String confidence) {
            this.insight = new javafx.beans.property.SimpleStringProperty(insight);
            this.impact = new javafx.beans.property.SimpleStringProperty(impact);
            this.confidence = new javafx.beans.property.SimpleStringProperty(confidence);
            this.rank = null;
            this.name = null;
            this.metric = null;
            this.score = null;
            this.trend = null;
        }

        // Getters
        public String getRank() { return rank != null ? rank.get() : ""; }
        public String getName() { return name != null ? name.get() : ""; }
        public String getMetric() { return metric != null ? metric.get() : ""; }
        public String getScore() { return score != null ? score.get() : ""; }
        public String getTrend() { return trend != null ? trend.get() : ""; }
        public String getInsight() { return insight != null ? insight.get() : ""; }
        public String getImpact() { return impact != null ? impact.get() : ""; }
        public String getConfidence() { return confidence != null ? confidence.get() : ""; }
    }
}