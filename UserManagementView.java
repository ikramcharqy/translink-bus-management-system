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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class UserManagementView {

    private final String PRIMARY_BLUE = "#2c3e50";
    private final String ACCENT_GOLD = "#f39c12";
    private final String SUCCESS_GREEN = "#27ae60";
    private final String DANGER_RED = "#e74c3c";
    private final String CARD_BG = "#ffffff";

    private TableView<UserRow> usersTable;
    private ObservableList<UserRow> usersList = FXCollections.observableArrayList();

    // Labels pour les statistiques
    private Label totalUsersLabel;
    private Label activeUsersLabel;
    private Label managersLabel;
    private Label driversLabel;

    public VBox createView() {
        VBox mainView = new VBox(20);
        mainView.setAlignment(Pos.TOP_LEFT);
        mainView.setPadding(new Insets(20));
        mainView.setStyle("-fx-background-color: #f5f7fa;");

        // Header
        HBox headerBox = createHeader();

        // Statistiques
        HBox statsRow = createStatsRow();

        // Table des utilisateurs
        VBox tableSection = createUsersTable();

        // Boutons d'action
        HBox actionBar = createActionBar();

        mainView.getChildren().addAll(headerBox, statsRow, tableSection, actionBar);

        // Charger les données
        loadDataFromFirestore();

        return mainView;
    }

    private HBox createHeader() {
        HBox header = new HBox();
        VBox headerText = new VBox(5);

        Text title = new Text("👥 User Management");
        title.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 28));
        title.setFill(Color.web(PRIMARY_BLUE));

        Text subtitle = new Text("Manage all user accounts, permissions, and access controls");
        subtitle.setFont(Font.font("Arial", 14));
        subtitle.setFill(Color.web("#7f8c8d"));

        headerText.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Refresh button
        Button refreshBtn = new Button("🔄 Refresh Data");
        refreshBtn.setStyle(
                "-fx-background-color: " + ACCENT_GOLD + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 10 20;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0.3, 0, 2);"
        );
        refreshBtn.setOnAction(e -> {
            refreshBtn.setText("🔄 Loading...");
            refreshBtn.setDisable(true);
            loadDataFromFirestore();
            refreshBtn.setText("🔄 Refresh Data");
            refreshBtn.setDisable(false);
        });

        header.getChildren().addAll(headerText, spacer, refreshBtn);
        return header;
    }

    private HBox createStatsRow() {
        HBox statsRow = new HBox(20);
        statsRow.setPadding(new Insets(20, 0, 20, 0));

        // Total Users Card
        VBox totalUsersCard = createStatCard("👥", "TOTAL USERS", "0", "All registered users", PRIMARY_BLUE);
        totalUsersLabel = (Label) ((VBox) totalUsersCard.getChildren().get(1)).getChildren().get(0);

        // Active Users Card
        VBox activeUsersCard = createStatCard("✅", "ACTIVE USERS", "0", "Currently active", SUCCESS_GREEN);
        activeUsersLabel = (Label) ((VBox) activeUsersCard.getChildren().get(1)).getChildren().get(0);

        // Managers Card
        VBox managersCard = createStatCard("👔", "MANAGERS", "0", "Team leaders", ACCENT_GOLD);
        managersLabel = (Label) ((VBox) managersCard.getChildren().get(1)).getChildren().get(0);

        // Drivers Card
        VBox driversCard = createStatCard("🚗", "DRIVERS", "0", "On duty", "#3498db");
        driversLabel = (Label) ((VBox) driversCard.getChildren().get(1)).getChildren().get(0);

        statsRow.getChildren().addAll(totalUsersCard, activeUsersCard, managersCard, driversCard);
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

    private VBox createUsersTable() {
        VBox tableSection = new VBox(15);
        tableSection.setPadding(new Insets(20));
        tableSection.setStyle(
                "-fx-background-color: " + CARD_BG + ";" +
                        "-fx-background-radius: 15;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 15;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 15, 0.3, 0, 5);"
        );

        HBox tableHeader = new HBox();
        Label tableTitle = new Label("📋 All System Users");
        tableTitle.setStyle("-fx-text-fill: " + PRIMARY_BLUE + "; -fx-font-size: 18; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Search field
        TextField searchField = new TextField();
        searchField.setPromptText("Search by username, name, email...");
        searchField.setPrefWidth(250);
        searchField.setStyle(
                "-fx-background-color: #f8f9fa;" +
                        "-fx-border-color: #dee2e6;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 8 15;" +
                        "-fx-font-size: 14;" +
                        "-fx-text-fill: black;" +
                        "-fx-text-inner-color: black;"
        );
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterUsers(newVal));

        tableHeader.getChildren().addAll(tableTitle, spacer, searchField);

        // Create table
        usersTable = new TableView<>();
        usersTable.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;" +
                        "-fx-font-size: 14;" +
                        "-fx-text-fill: black !important;" +
                        "-fx-control-inner-background: white;"
        );

        // FIX: Forcer le texte noir dans toute la table
        usersTable.setStyle(usersTable.getStyle() +
                "-fx-text-fill: black;" +
                "-fx-font-size: 14;");

        usersTable.setPlaceholder(new Label("No users found. Click 'Add New User' to create one."));

        // Columns avec texte noir forcé
        TableColumn<UserRow, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(cellData -> cellData.getValue().usernameProperty());
        usernameCol.setPrefWidth(120);
        usernameCol.setSortable(true);

        TableColumn<UserRow, String> nameCol = new TableColumn<>("Full Name");
        nameCol.setCellValueFactory(cellData -> cellData.getValue().fullNameProperty());
        nameCol.setPrefWidth(180);
        nameCol.setSortable(true);

        TableColumn<UserRow, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(cellData -> cellData.getValue().emailProperty());
        emailCol.setPrefWidth(200);
        emailCol.setSortable(true);

        TableColumn<UserRow, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(cellData -> cellData.getValue().roleProperty());
        roleCol.setPrefWidth(100);
        roleCol.setSortable(true);
        roleCol.setCellFactory(col -> new TableCell<UserRow, String>() {
            @Override
            protected void updateItem(String role, boolean empty) {
                super.updateItem(role, empty);
                if (empty || role == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(role.toUpperCase());
                    setFont(Font.font("Arial", FontWeight.BOLD, 12));
                    setAlignment(Pos.CENTER);

                    switch (role.toLowerCase()) {
                        case "admin":
                            setStyle("-fx-text-fill: " + DANGER_RED + "; -fx-background-color: #ffebee; " +
                                    "-fx-background-radius: 12; -fx-padding: 4 12;");
                            break;
                        case "manager":
                            setStyle("-fx-text-fill: " + ACCENT_GOLD + "; -fx-background-color: #fff3e0; " +
                                    "-fx-background-radius: 12; -fx-padding: 4 12;");
                            break;
                        case "driver":
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

        TableColumn<UserRow, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        statusCol.setPrefWidth(100);
        statusCol.setSortable(true);
        statusCol.setCellFactory(col -> new TableCell<UserRow, String>() {
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

                    if ("Active".equals(status)) {
                        setStyle("-fx-text-fill: " + SUCCESS_GREEN + "; -fx-background-color: #e8f5e9; " +
                                "-fx-background-radius: 12; -fx-padding: 4 12;");
                    } else if ("Inactive".equals(status)) {
                        setStyle("-fx-text-fill: #7f8c8d; -fx-background-color: #f5f5f5; " +
                                "-fx-background-radius: 12; -fx-padding: 4 12;");
                    } else if ("Suspended".equals(status)) {
                        setStyle("-fx-text-fill: " + DANGER_RED + "; -fx-background-color: #ffebee; " +
                                "-fx-background-radius: 12; -fx-padding: 4 12;");
                    }
                }
            }
        });

        TableColumn<UserRow, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(cellData -> cellData.getValue().phoneProperty());
        phoneCol.setPrefWidth(120);
        phoneCol.setSortable(true);

        TableColumn<UserRow, String> createdCol = new TableColumn<>("Created");
        createdCol.setCellValueFactory(cellData -> cellData.getValue().createdDateProperty());
        createdCol.setPrefWidth(120);
        createdCol.setSortable(true);

// Actions column reste identique
        TableColumn<UserRow, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(180);
        actionsCol.setCellFactory(col -> new TableCell<UserRow, Void>() {
            private final Button editBtn = new Button("✏️");
            private final Button deleteBtn = new Button("🗑️");
            private final Button resetBtn = new Button("🔐");
            private final HBox buttons = new HBox(8, editBtn, resetBtn, deleteBtn);

            {
                editBtn.setStyle(
                        "-fx-background-color: " + ACCENT_GOLD + ";" +
                                "-fx-text-fill: white;" +
                                "-fx-font-size: 12;" +
                                "-fx-padding: 6 12;" +
                                "-fx-background-radius: 6;" +
                                "-fx-cursor: hand;" +
                                "-fx-min-width: 40;"
                );
                editBtn.setTooltip(new Tooltip("Edit User"));

                resetBtn.setStyle(
                        "-fx-background-color: #3498db;" +
                                "-fx-text-fill: white;" +
                                "-fx-font-size: 12;" +
                                "-fx-padding: 6 12;" +
                                "-fx-background-radius: 6;" +
                                "-fx-cursor: hand;" +
                                "-fx-min-width: 40;"
                );
                resetBtn.setTooltip(new Tooltip("Reset Password"));

                deleteBtn.setStyle(
                        "-fx-background-color: " + DANGER_RED + ";" +
                                "-fx-text-fill: white;" +
                                "-fx-font-size: 12;" +
                                "-fx-padding: 6 12;" +
                                "-fx-background-radius: 6;" +
                                "-fx-cursor: hand;" +
                                "-fx-min-width: 40;"
                );
                deleteBtn.setTooltip(new Tooltip("Delete User"));

                editBtn.setOnAction(e -> {
                    UserRow user = getTableView().getItems().get(getIndex());
                    showEditUserDialog(user);
                });

                resetBtn.setOnAction(e -> {
                    UserRow user = getTableView().getItems().get(getIndex());
                    showResetPasswordDialog(user);
                });

                deleteBtn.setOnAction(e -> {
                    UserRow user = getTableView().getItems().get(getIndex());
                    showDeleteUserDialog(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttons);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        usersTable.getColumns().addAll(usernameCol, nameCol, emailCol, roleCol, statusCol, phoneCol, createdCol, actionsCol);

// CRITIQUE : Définir les items APRÈS avoir ajouté les colonnes
        usersTable.setItems(usersList);
        usersTable.setPrefHeight(400);

// Style global pour forcer le texte noir
        usersTable.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;"
        );

// Placeholder
        Label placeholderLabel = new Label("No users found. Click 'Add New User' to create one.");
        placeholderLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 14;");
        usersTable.setPlaceholder(placeholderLabel);

        tableSection.getChildren().addAll(tableHeader, usersTable);
        return tableSection;
    }
    private HBox createActionBar() {
        HBox actionBar = new HBox(15);
        actionBar.setPadding(new Insets(20, 0, 0, 0));

        Button btnAddUser = createActionButton("➕ Add New User", ACCENT_GOLD);
        Button btnExport = createActionButton("📤 Export CSV", "#3498db");
        Button btnBulkEdit = createActionButton("📋 Bulk Edit", PRIMARY_BLUE);

        btnAddUser.setOnAction(e -> showAddUserDialog());
        btnExport.setOnAction(e -> exportToCSV());
        btnBulkEdit.setOnAction(e -> showBulkEditDialog());

        actionBar.getChildren().addAll(btnAddUser, btnBulkEdit, btnExport);
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

    private String darkenColor(String hexColor) {
        if (hexColor.equals(ACCENT_GOLD)) return "#e67e22";
        if (hexColor.equals(PRIMARY_BLUE)) return "#2c3e50";
        if (hexColor.equals(SUCCESS_GREEN)) return "#229954";
        if (hexColor.equals(DANGER_RED)) return "#c0392b";
        if (hexColor.equals("#3498db")) return "#2980b9";
        return hexColor;
    }

    // ==============================
    // DATA LOADING FROM FIRESTORE
    // ==============================

    private void loadDataFromFirestore() {
        try {
            System.out.println("🔄 Loading users from Firestore...");

            // Afficher un indicateur de chargement
            usersTable.setPlaceholder(new Label("Loading users from database..."));
            usersList.clear();

            // Charger les utilisateurs depuis Firestore
            ObservableList<Map<String, Object>> firestoreUsers = FirebaseService.getAllUsersWithDetails();

            for (Map<String, Object> userData : firestoreUsers) {
                UserRow user = new UserRow(
                        (String) userData.get("username"),
                        (String) userData.getOrDefault("fullname", "N/A"),
                        (String) userData.getOrDefault("email", "N/A"),
                        (String) userData.getOrDefault("role", "user"),
                        (String) userData.getOrDefault("phone", "N/A"),
                        (String) userData.getOrDefault("status", "Active"),
                        (String) userData.getOrDefault("createdAt", "N/A")
                );
                usersList.add(user);
            }

            // Charger les statistiques
            Map<String, Long> stats = FirebaseService.getUserStatistics();
            updateStatistics(stats);

            System.out.println("✅ Loaded " + usersList.size() + " users from Firestore");
            usersTable.setPlaceholder(new Label("No users found. Click 'Add New User' to create one."));

        } catch (Exception e) {
            System.err.println("❌ Error loading users: " + e.getMessage());
            e.printStackTrace();
            usersTable.setPlaceholder(new Label("Error loading users: " + e.getMessage()));
            showAlert("Error", "Failed to load users: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void updateStatistics(Map<String, Long> stats) {
        javafx.application.Platform.runLater(() -> {
            totalUsersLabel.setText(String.valueOf(stats.get("totalUsers")));
            activeUsersLabel.setText(String.valueOf(stats.get("activeUsers")));
            managersLabel.setText(String.valueOf(stats.get("managers")));
            driversLabel.setText(String.valueOf(stats.get("drivers")));

            System.out.println("📊 Statistics updated: " + stats);
        });
    }

    private void filterUsers(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            usersTable.setItems(usersList);
            return;
        }

        String lowerCaseFilter = searchText.toLowerCase();
        ObservableList<UserRow> filteredList = FXCollections.observableArrayList();

        for (UserRow user : usersList) {
            if (user.getUsername().toLowerCase().contains(lowerCaseFilter) ||
                    user.getFullName().toLowerCase().contains(lowerCaseFilter) ||
                    user.getEmail().toLowerCase().contains(lowerCaseFilter) ||
                    user.getRole().toLowerCase().contains(lowerCaseFilter) ||
                    user.getPhone().toLowerCase().contains(lowerCaseFilter)) {
                filteredList.add(user);
            }
        }

        usersTable.setItems(filteredList);
    }

    // ==============================
    // DIALOG METHODS
    // ==============================

    private void showAddUserDialog() {
        Dialog<Map<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Add New User");
        dialog.setHeaderText("Create a new user account");
        dialog.getDialogPane().getStyleClass().add("custom-dialog");

        ButtonType createButton = new ButtonType("Create User", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(25, 35, 10, 35));

        // Champs de formulaire
        TextField usernameField = new TextField();
        usernameField.setPromptText("john.doe");
        usernameField.setPrefWidth(250);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Minimum 6 characters");
        passwordField.setPrefWidth(250);

        TextField fullNameField = new TextField();
        fullNameField.setPromptText("John Doe");
        fullNameField.setPrefWidth(250);

        TextField emailField = new TextField();
        emailField.setPromptText("john@example.com");
        emailField.setPrefWidth(250);

        TextField phoneField = new TextField();
        phoneField.setPromptText("+1234567890");
        phoneField.setPrefWidth(250);

        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("admin", "manager", "driver");
        roleCombo.setValue("driver");
        roleCombo.setPrefWidth(250);

        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("Active", "Inactive", "Suspended");
        statusCombo.setValue("Active");
        statusCombo.setPrefWidth(250);

        int row = 0;
        grid.add(createFormLabel("Username*:"), 0, row);
        grid.add(usernameField, 1, row++);
        grid.add(createFormLabel("Password*:"), 0, row);
        grid.add(passwordField, 1, row++);
        grid.add(createFormLabel("Full Name:"), 0, row);
        grid.add(fullNameField, 1, row++);
        grid.add(createFormLabel("Email:"), 0, row);
        grid.add(emailField, 1, row++);
        grid.add(createFormLabel("Phone:"), 0, row);
        grid.add(phoneField, 1, row++);
        grid.add(createFormLabel("Role*:"), 0, row);
        grid.add(roleCombo, 1, row++);
        grid.add(createFormLabel("Status:"), 0, row);
        grid.add(statusCombo, 1, row++);

        dialog.getDialogPane().setContent(grid);

        // Validation
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButton) {
                if (usernameField.getText().isEmpty() || passwordField.getText().isEmpty()) {
                    showAlert("Validation Error", "Username and password are required fields.", Alert.AlertType.ERROR);
                    return null;
                }

                if (passwordField.getText().length() < 6) {
                    showAlert("Validation Error", "Password must be at least 6 characters long.", Alert.AlertType.ERROR);
                    return null;
                }

                Map<String, String> result = new HashMap<>();
                result.put("username", usernameField.getText().trim());
                result.put("password", passwordField.getText());
                result.put("fullName", fullNameField.getText().trim());
                result.put("email", emailField.getText().trim());
                result.put("phone", phoneField.getText().trim());
                result.put("role", roleCombo.getValue());
                result.put("status", statusCombo.getValue());

                return result;
            }
            return null;
        });

        Optional<Map<String, String>> result = dialog.showAndWait();
        result.ifPresent(userData -> {
            try {
                // Créer l'utilisateur dans Firestore
                FirebaseService.createNewUser(
                        userData.get("username"),
                        userData.get("password"),
                        userData.get("fullName"),
                        userData.get("email"),
                        userData.get("phone"),
                        userData.get("role")
                );

                // Ajouter à la liste locale
                UserRow newUser = new UserRow(
                        userData.get("username"),
                        userData.get("fullName"),
                        userData.get("email"),
                        userData.get("role"),
                        userData.get("phone"),
                        userData.get("status"),
                        "Just now"
                );
                usersList.add(newUser);

                showAlert("Success", "User created successfully!\nUsername: " + userData.get("username"),
                        Alert.AlertType.INFORMATION);

                // Recharger les données pour les statistiques
                loadDataFromFirestore();

            } catch (Exception e) {
                showAlert("Error", "Failed to create user: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        });
    }

    private void showEditUserDialog(UserRow user) {
        Dialog<Map<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Edit User");
        dialog.setHeaderText("Edit user: " + user.getUsername());
        dialog.getDialogPane().getStyleClass().add("custom-dialog");

        ButtonType updateButton = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(25, 35, 10, 35));

        TextField fullNameField = new TextField(user.getFullName());
        fullNameField.setPrefWidth(250);

        TextField emailField = new TextField(user.getEmail());
        emailField.setPrefWidth(250);

        TextField phoneField = new TextField(user.getPhone());
        phoneField.setPrefWidth(250);

        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("admin", "manager", "driver");
        roleCombo.setValue(user.getRole());
        roleCombo.setPrefWidth(250);

        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("Active", "Inactive", "Suspended");
        statusCombo.setValue(user.getStatus());
        statusCombo.setPrefWidth(250);

        int row = 0;
        grid.add(createFormLabel("Full Name:"), 0, row);
        grid.add(fullNameField, 1, row++);
        grid.add(createFormLabel("Email:"), 0, row);
        grid.add(emailField, 1, row++);
        grid.add(createFormLabel("Phone:"), 0, row);
        grid.add(phoneField, 1, row++);
        grid.add(createFormLabel("Role:"), 0, row);
        grid.add(roleCombo, 1, row++);
        grid.add(createFormLabel("Status:"), 0, row);
        grid.add(statusCombo, 1, row++);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == updateButton) {
                Map<String, String> updates = new HashMap<>();
                updates.put("fullName", fullNameField.getText().trim());
                updates.put("email", emailField.getText().trim());
                updates.put("phone", phoneField.getText().trim());
                updates.put("role", roleCombo.getValue());
                updates.put("status", statusCombo.getValue());

                return updates;
            }
            return null;
        });

        Optional<Map<String, String>> result = dialog.showAndWait();
        result.ifPresent(updates -> {
            try {
                // Mettre à jour dans Firestore
                Map<String, Object> firebaseUpdates = new HashMap<>();
                firebaseUpdates.put("fullname", updates.get("fullName"));
                firebaseUpdates.put("email", updates.get("email"));
                firebaseUpdates.put("phone", updates.get("phone"));
                firebaseUpdates.put("role", updates.get("role"));
                firebaseUpdates.put("status", updates.get("status"));

                FirebaseService.updateUserDetails(user.getUsername(), firebaseUpdates);

                // Mettre à jour l'objet local
                user.setFullName(updates.get("fullName"));
                user.setEmail(updates.get("email"));
                user.setPhone(updates.get("phone"));
                user.setRole(updates.get("role"));
                user.setStatus(updates.get("status"));

                usersTable.refresh();
                showAlert("Success", "User updated successfully!", Alert.AlertType.INFORMATION);

            } catch (Exception e) {
                showAlert("Error", "Failed to update user: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        });
    }

    private void showResetPasswordDialog(UserRow user) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Reset Password");
        dialog.setHeaderText("Reset password for: " + user.getUsername());
        dialog.getDialogPane().getStyleClass().add("custom-dialog");

        ButtonType resetButton = new ButtonType("Reset Password", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(resetButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(25, 35, 10, 35));

        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Enter new password");
        newPasswordField.setPrefWidth(250);

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm new password");
        confirmPasswordField.setPrefWidth(250);

        int row = 0;
        grid.add(createFormLabel("New Password:"), 0, row);
        grid.add(newPasswordField, 1, row++);
        grid.add(createFormLabel("Confirm Password:"), 0, row);
        grid.add(confirmPasswordField, 1, row++);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == resetButton) {
                if (newPasswordField.getText().isEmpty() || confirmPasswordField.getText().isEmpty()) {
                    showAlert("Validation Error", "Both password fields are required.", Alert.AlertType.ERROR);
                    return null;
                }

                if (!newPasswordField.getText().equals(confirmPasswordField.getText())) {
                    showAlert("Validation Error", "Passwords do not match.", Alert.AlertType.ERROR);
                    return null;
                }

                if (newPasswordField.getText().length() < 6) {
                    showAlert("Validation Error", "Password must be at least 6 characters.", Alert.AlertType.ERROR);
                    return null;
                }

                return newPasswordField.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newPassword -> {
            try {
                FirebaseService.updateUserPassword(user.getUsername(), newPassword);
                showAlert("Success", "Password reset successfully for user: " + user.getUsername(),
                        Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Error", "Failed to reset password: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        });
    }

    private void showDeleteUserDialog(UserRow user) {
        // Empêcher la suppression de l'utilisateur admin actuel
        if ("admin1".equals(user.getUsername())) {
            showAlert("Cannot Delete", "You cannot delete the main admin account.", Alert.AlertType.WARNING);
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete User");
        alert.setHeaderText("Delete user: " + user.getUsername());
        alert.setContentText("Are you sure you want to delete this user?\n\n" +
                "⚠️ This will also delete all associated data (driver profile, etc.).\n" +
                "⚠️ This action cannot be undone!");

        // Style de l'alerte
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStyleClass().add("custom-dialog");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                FirebaseService.deleteUserAndAssociatedData(user.getUsername());
                usersList.remove(user);
                showAlert("Success", "User deleted successfully: " + user.getUsername(),
                        Alert.AlertType.INFORMATION);

                // Recharger les statistiques
                loadDataFromFirestore();

            } catch (Exception e) {
                showAlert("Error", "Failed to delete user: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void showBulkEditDialog() {
        showAlert("Info", "Bulk edit feature coming soon!\n\n" +
                "You will be able to:\n" +
                "• Select multiple users\n" +
                "• Change roles in bulk\n" +
                "• Update statuses\n" +
                "• Export selections", Alert.AlertType.INFORMATION);
    }

    private void exportToCSV() {
        try {
            // Créer un fichier CSV temporaire
            java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("users_export_", ".csv");

            try (java.io.BufferedWriter writer = java.nio.file.Files.newBufferedWriter(tempFile)) {
                // En-tête CSV
                writer.write("Username,Full Name,Email,Role,Status,Phone,Created\n");

                // Données
                for (UserRow user : usersList) {
                    writer.write(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                            user.getUsername(),
                            user.getFullName(),
                            user.getEmail(),
                            user.getRole(),
                            user.getStatus(),
                            user.getPhone(),
                            user.getCreatedDate()));
                }
            }

            showAlert("Export Successful",
                    "Users exported to CSV file:\n" + tempFile.toString() +
                            "\n\nTotal records: " + usersList.size(),
                    Alert.AlertType.INFORMATION);

        } catch (Exception e) {
            showAlert("Export Failed", "Failed to export users: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private Label createFormLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
        return label;
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        // Style de l'alerte
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStyleClass().add("custom-dialog");

        alert.showAndWait();
    }

    // ==============================
    // DATA MODEL CLASS
    // ==============================

    public static class UserRow {
        private final javafx.beans.property.SimpleStringProperty username;
        private final javafx.beans.property.SimpleStringProperty fullName;
        private final javafx.beans.property.SimpleStringProperty email;
        private final javafx.beans.property.SimpleStringProperty role;
        private final javafx.beans.property.SimpleStringProperty phone;
        private final javafx.beans.property.SimpleStringProperty status;
        private final javafx.beans.property.SimpleStringProperty createdDate;

        public UserRow(String username, String fullName, String email, String role,
                       String phone, String status, String createdDate) {
            this.username = new javafx.beans.property.SimpleStringProperty(username);
            this.fullName = new javafx.beans.property.SimpleStringProperty(fullName);
            this.email = new javafx.beans.property.SimpleStringProperty(email);
            this.role = new javafx.beans.property.SimpleStringProperty(role);
            this.phone = new javafx.beans.property.SimpleStringProperty(phone);
            this.status = new javafx.beans.property.SimpleStringProperty(status);
            this.createdDate = new javafx.beans.property.SimpleStringProperty(createdDate);
        }

        // Getters
        public String getUsername() { return username.get(); }
        public javafx.beans.property.StringProperty usernameProperty() { return username; }

        public String getFullName() { return fullName.get(); }
        public javafx.beans.property.StringProperty fullNameProperty() { return fullName; }

        public String getEmail() { return email.get(); }
        public javafx.beans.property.StringProperty emailProperty() { return email; }

        public String getRole() { return role.get(); }
        public javafx.beans.property.StringProperty roleProperty() { return role; }

        public String getPhone() { return phone.get(); }
        public javafx.beans.property.StringProperty phoneProperty() { return phone; }

        public String getStatus() { return status.get(); }
        public javafx.beans.property.StringProperty statusProperty() { return status; }

        public String getCreatedDate() { return createdDate.get(); }
        public javafx.beans.property.StringProperty createdDateProperty() { return createdDate; }

        // Setters
        public void setUsername(String username) { this.username.set(username); }
        public void setFullName(String fullName) { this.fullName.set(fullName); }
        public void setEmail(String email) { this.email.set(email); }
        public void setRole(String role) { this.role.set(role); }
        public void setPhone(String phone) { this.phone.set(phone); }
        public void setStatus(String status) { this.status.set(status); }
        public void setCreatedDate(String createdDate) { this.createdDate.set(createdDate); }
    }
}