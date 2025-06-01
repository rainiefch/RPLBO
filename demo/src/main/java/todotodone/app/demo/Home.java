package todotodone.app.demo;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.shape.Arc;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import todotodone.app.demo.util.DBConnection;
import todotodone.app.demo.util.SceneSwitcher;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Home {

    @FXML private ComboBox<String> Category;
    @FXML private HBox btnAdd, btnCategory, btnHome,btnProfile;
    @FXML private ImageView btnSearch;
    @FXML private ComboBox<String> cbFilterStatus;
    @FXML private GridPane gridPane;
    @FXML private Label lblHome;
    @FXML private TextField txtSearch;
    @FXML private Label lblPending, lblProgress, lblDone, lblOverdue;
    @FXML private PieChart todoPieChart;
    @FXML private Label lblPersenDone, lblPersenOverdue, lblPersenProgress, lblPersenPending;

    private int userId;
    private String username;
    private final List<TodoItem> todoItems = new ArrayList<>();
    private final List<TodoItem> filteredTodoItems = new ArrayList<>();

    public Home() {}

    public Home(String username, int userId) {
        this.username = username;
        this.userId = userId;
    }

    //untuk data todo
    public class TodoItem {
        int id;
        String title, status, dueDate, category, description, attachment;

        public TodoItem(int id, String title, String status, String dueDate, String category, String description, String attachment) {
            this.id = id;
            this.title = title;
            this.status = status;
            this.dueDate = dueDate;
            this.category = category;
            this.description = description;
            this.attachment = attachment;
        }
    }

    //supaya semua data dr db ke display
    @FXML
    void initialize() {
        setupGridPane(); //  buat display todo
        initializeComboBoxes(); // buat ngisi cb filter
        setupSearchAndFilter(); //
        refreshTodos(); //ambil data todo dr db
    }

    // gridpane buat todos
    private void setupGridPane() {
        gridPane.getRowConstraints().clear();
        for (int i = 0; i < 3; i++) {
            RowConstraints rc = new RowConstraints();
            rc.setVgrow(javafx.scene.layout.Priority.NEVER);
            rc.setPrefHeight(40);
            gridPane.getRowConstraints().add(rc);
        }
    }

    //ngisi cb untuk filter
    private void initializeComboBoxes() {
        cbFilterStatus.setItems(FXCollections.observableArrayList("All Status", "Pending", "In Progress", "Completed", "Overdue"));
        cbFilterStatus.getSelectionModel().selectFirst();

        Category.getItems().clear();
        Category.getItems().add("All Category");

        String sql = "SELECT name_category FROM category WHERE id_user = 1 OR id_user = ? ORDER BY name_category ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Category.getItems().add(rs.getString("name_category"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to load categories: " + e.getMessage());
        }

        Category.getSelectionModel().selectFirst();
    }

    private void setupSearchAndFilter() {
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> filterTodos());
        btnSearch.setOnMouseClicked(e -> filterTodos());
        cbFilterStatus.valueProperty().addListener((obs, oldVal, newVal) -> filterTodos());
        Category.valueProperty().addListener((obs, oldVal, newVal) -> filterTodos());
    }

    //buat ngecek kalo todonya overdue, tiap ngeload todo dr db bakal dicek
    private boolean isOverdue(String dueDateStr) {
        try {
            java.time.LocalDate dueDate = java.time.LocalDate.parse(dueDateStr);
            java.time.LocalDate today = java.time.LocalDate.now();
            return dueDate.isBefore(today);
        } catch (Exception e) {
            return false;
        }
    }

    private void updateTodoStatus(int todoId, String newStatus) {
        String sql = "UPDATE todo SET status = ? WHERE id_todo = ? AND id_user = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newStatus);
            stmt.setInt(2, todoId);
            stmt.setInt(3, userId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error updating status: " + e.getMessage());
        }
    }

    // buat ngambil todo dr db
    private void fetchAllTodos() {
        todoItems.clear();
        String sql = "SELECT id_todo, title, status, due_date, category, description, attachment FROM todo WHERE id_user = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            List<TodoItem> overdueList = new ArrayList<>(); //list diakses sm piechart

            while (rs.next()) {
                TodoItem item = new TodoItem(
                        rs.getInt("id_todo"),
                        rs.getString("title"),
                        rs.getString("status"),
                        rs.getString("due_date"),
                        rs.getString("category"),
                        rs.getString("description"),
                        rs.getString("attachment")
                );

                if (!"Completed".equals(item.status) && isOverdue(item.dueDate)) {
                    item.status = "Overdue";
                    overdueList.add(item);
                }
                todoItems.add(item);
            }

            if (!overdueList.isEmpty()) { //kalo dia melebihi dl nanti diganti jd overdue
                updateTodoStatuses(overdueList);
            }

        } catch (SQLException e) {
            System.err.println("Error loading todos: " + e.getMessage());
        }
    }

    //buat ganti status jd overdue
    private void updateTodoStatuses(List<TodoItem> todos) {
        String sql = "UPDATE todo SET status = 'Overdue' WHERE id_todo = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (TodoItem item : todos) {
                stmt.setInt(1, item.id);
                stmt.addBatch();
            }
            stmt.executeBatch();

        } catch (SQLException e) {
            System.err.println("Error batch updating status: " + e.getMessage());
        }
    }

    private void refreshTodos() {
        fetchAllTodos();
        filterTodos();
        updatePieChart();
        initializeComboBoxes();
    }

    private void filterTodos() {
        String search = txtSearch.getText().toLowerCase();
        String selectedStatus = cbFilterStatus.getValue();
        String selectedCategory = Category.getValue();

        filteredTodoItems.clear();

        for (TodoItem item : todoItems) {
            boolean matchesSearch = search.isEmpty() ||
                    item.title.toLowerCase().contains(search) ||
                    item.description.toLowerCase().contains(search);

            boolean matchesStatus = "All Status".equals(selectedStatus) || item.status.equals(selectedStatus);
            boolean matchesCategory = "All Category".equals(selectedCategory) || item.category.equals(selectedCategory);

            if (matchesSearch && matchesStatus && matchesCategory) {
                filteredTodoItems.add(item);
            }
        }

//        filteredTodoItems.sort((a, b) -> {
//            boolean aOverdue = "Overdue".equals(a.status);
//            boolean bOverdue = "Overdue".equals(b.status);
//
//            if (aOverdue && !bOverdue) return 1;
//            if (!aOverdue && bOverdue) return -1;
//            return a.dueDate.compareTo(b.dueDate);
//        });
        filteredTodoItems.sort((a, b) -> {
            int priorityA = getTodoPriority(a);
            int priorityB = getTodoPriority(b);

            if (priorityA != priorityB) {
                return Integer.compare(priorityA, priorityB);
            }
            return a.dueDate.compareTo(b.dueDate); // urut per tanggal dalam kelompok yang sama
        });


        displayTodos();
    }

    private int getTodoPriority(TodoItem item) {
        if ("Completed".equals(item.status)) {
            return 2;
        } else if ("Overdue".equals(item.status)) {
            return 1;
        } else {
            return 0;
        }
    }
    
    // buat display list todo
    private void displayTodos() {
        gridPane.getChildren().removeIf(node ->
                GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) >= 2
        );

        while (gridPane.getRowConstraints().size() > 2) {
            gridPane.getRowConstraints().remove(2);
        }

        if (filteredTodoItems.isEmpty()) {
            showNoItemsMessage();
            return;
        }

        int rowIndex = 2;

        for (TodoItem item : filteredTodoItems) {
            RowConstraints rc = new RowConstraints();
            rc.setVgrow(javafx.scene.layout.Priority.NEVER);
            rc.setPrefHeight(40);
            gridPane.getRowConstraints().add(rc);

            Pane bgPane = new Pane();
            bgPane.setStyle("-fx-background-color: " + getBackgroundColor(item) + ";");
            bgPane.setMinHeight(40);
            gridPane.add(bgPane, 0, rowIndex, 8, 1);
            GridPane.setMargin(bgPane, new Insets(0));

            Label titleLabel = new Label(item.title);
            styleTodoLabel(titleLabel, item);
            gridPane.add(titleLabel, 0, rowIndex);
            GridPane.setMargin(titleLabel, new Insets(0, 0, 0, 20));

            ComboBox<String> statusCombo = new ComboBox<>();
            statusCombo.getItems().addAll("Pending", "In Progress", "Completed");
            statusCombo.setValue(item.status);
            styleComboBox(statusCombo, statusCombo.getValue());
            gridPane.add(statusCombo, 2, rowIndex);
            GridPane.setMargin(statusCombo, new Insets(0));

            statusCombo.setOnAction(event -> {
                String newStatus = statusCombo.getValue();
                if (!newStatus.equals(item.status)) {
                    updateTodoStatus(item.id, newStatus);
                    item.status = newStatus;
                    refreshTodos();
                }
            });


            Label dateLabel = new Label(item.dueDate);
            styleTodoLabel(dateLabel, item);
            gridPane.add(dateLabel, 4, rowIndex);
            GridPane.setMargin(dateLabel, new Insets(5, 10, 5, 10));


            Label categoryLabel = new Label(item.category);
            styleTodoLabel(categoryLabel, item);
            gridPane.add(categoryLabel, 6, rowIndex);
            GridPane.setMargin(categoryLabel, new Insets(5, 10, 5, 10));

            setupClickHandlersForRow(item, titleLabel, dateLabel, categoryLabel);

            rowIndex++;
        }

    }

    //biar todonya bisa di klik
    private void setupClickHandlersForRow(TodoItem item, Label... clickableNodes) {
        for (Label node : clickableNodes) {
            node.setOnMouseClicked(event -> {
                if (!(event.getTarget() instanceof ComboBox)) {
                    openTodoForEditing(item);
                }
            });
        }
    }

    // ganti bg color buat kalo misalnya dia complete = ijo, overdue = merah, deadline = hari ini, atau besok = kuning
    private String getBackgroundColor(TodoItem item) {
        if ("Completed".equals(item.status)) return "#e2f7d5"; //ijo
        if ("Overdue".equals(item.status)) return "#fcdede"; //merah

        try {
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            java.time.LocalDateTime due = java.time.LocalDate.parse(item.dueDate).atStartOfDay();
            java.time.LocalDate today = java.time.LocalDate.now();
            java.time.LocalDate dueDate = java.time.LocalDate.parse(item.dueDate);

            long hoursUntilDue = java.time.Duration.between(now, due).toHours();

            if (hoursUntilDue <= 24 && hoursUntilDue >= 0 || dueDate.equals(today)) {
                return "#fff7cd"; //kuning
            }
        } catch (Exception e) {
            System.err.println("Error parsing due date: " + e.getMessage());
        }

        return "#ffffff";
    }

    private void styleTodoLabel(Label label, TodoItem item) {
        String bgColor = getBackgroundColor(item);

        label.setStyle(String.format(
                "-fx-font-size: 16px;" +
                        "-fx-text-fill: black;" +
                        "-fx-background-color: %s;" +
                        "-fx-cursor: hand;" +
                        "-fx-background-radius: 4;",
                bgColor
        ));

        label.setMaxWidth(Double.MAX_VALUE);
        label.setPadding(new Insets(5));
    }

    // buat kasih warna combo bx status per todo
    private void styleComboBox(ComboBox<String> comboBox, String status) {
        String bgColor;

        switch (status) {
            case "Pending":
                bgColor = "#ffe79e";
                break;
            case "In Progress":
                bgColor = "#b2eced";
                break;
            case "Completed":
                bgColor = "#c9ed9f";
                break;
            case "Overdue":
                bgColor = "#fcc5c5";
                break;
            default:
                bgColor = "#ffffff";
        }

        comboBox.setStyle(
                "-fx-font-size: 16px;" +
                        "-fx-background-color: " + bgColor + ";" +
                        "-fx-cursor: default;"
        );

        comboBox.setMaxWidth(Double.MAX_VALUE);
    }


    private void showError(String message) {
        Label errorLabel = new Label(message);
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");
        gridPane.add(errorLabel, 0, 3, 7, 1);
    }

    private void showNoItemsMessage() {
        Label noItemsLabel = new Label("No todos found. Add one to get started!");
        noItemsLabel.setStyle("-fx-text-fill: black; -fx-font-size: 16px; -fx-background-color: #ffffff;");
        gridPane.add(noItemsLabel, 0, 3, 7, 1);
    }

    //kalo klik row baris todo, nanti masuk ke todoform, ngirim data todo yg diklik ke todoform
    private void openTodoForEditing(TodoItem todo) {
        try {
            SceneSwitcher.popTodoForm((Stage) btnAdd.getScene().getWindow(), userId, todo);

            refreshTodos();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to open todo editor");
        }
    }

    //ini manggil todoform tanpa data todo
    @FXML void onBtnAddClick(MouseEvent event) {
        SceneSwitcher.popTodoForm((Stage) btnAdd.getScene().getWindow(), userId, null);
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        javafx.application.Platform.runLater(() -> refreshTodos());
                    }
                },
                500
        );
    }

    //manggil categoryform
    @FXML void onBtnCategoryClick(MouseEvent event) {
        SceneSwitcher.popCategoryForm((Stage) btnCategory.getScene().getWindow(), userId);
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        javafx.application.Platform.runLater(() -> refreshTodos());
                    }
                },
                500
        );
    }

    @FXML void onBtnHomeClick(MouseEvent event) {
        refreshTodos();
    }

    //manggil profile
    @FXML void onBtnProfileClick(MouseEvent event) {
        Stage currentStage = (Stage) btnProfile.getScene().getWindow();
        SceneSwitcher.popProfileForm(currentStage, this.username, this.userId);
    }

    @FXML void onHomeClick(MouseEvent event) {
        refreshTodos();
    }

    @FXML void onBtnSearchClick(MouseEvent event) {
        filterTodos();
    }

    @FXML void onCbFilterChoose(javafx.event.ActionEvent event) {
        filterTodos();
    }

    private void updatePieChart() {
        int pending = 0, progress = 0, completed = 0, overdue = 0;

        for (TodoItem item : todoItems) {
            switch (item.status) {
                case "Pending": pending++; break;
                case "In Progress": progress++; break;
                case "Completed": completed++; break;
                case "Overdue": overdue++; break;
            }
        }

        int total = pending + progress + completed + overdue;

        if (total == 0) {
            todoPieChart.setVisible(false);
            todoPieChart.setManaged(false);
            todoPieChart.setData(FXCollections.emptyObservableList());
            return;
        } else {
            todoPieChart.setVisible(true);
            todoPieChart.setManaged(true);
        }

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Pending", pending),
                new PieChart.Data("In Progress", progress),
                new PieChart.Data("Completed", completed),
                new PieChart.Data("Overdue", overdue)
        );

        todoPieChart.setData(pieChartData);
        todoPieChart.setTitle("To-Do Status Overview");

        String[] sliceColors = new String[] {
                "#ffe79e", // Pending
                "#b2eced", // In Progress (fix typo, add #)
                "#c9ed9f", // Completed
                "#fcc5c5"  // Overdue
        };

        Platform.runLater(() -> {
            for (int i = 0; i < pieChartData.size(); i++) {
                PieChart.Data data = pieChartData.get(i);
                Node node = data.getNode();
                if (node != null) {
                    node.setStyle("-fx-pie-color: " + sliceColors[i] + ";");
                }
            }

            for (Node label : todoPieChart.lookupAll(".chart-pie-label")) {
                label.setStyle("-fx-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
            }

            int i = 0;
            for (Node legendItem : todoPieChart.lookupAll(".chart-legend-item")) {
                Node symbol = legendItem.lookup(".chart-legend-item-symbol");
                if (symbol != null && i < sliceColors.length) {
                    symbol.setStyle("-fx-background-color: " + sliceColors[i] + ";");
                    i++;
                }
            }

            Node title = todoPieChart.lookup(".chart-title");
            if (title != null) {
                title.setStyle("-fx-text-fill: white; -fx-font-size: 36px; -fx-font-weight: bold;");
            }
        });

        updatePiePercentLabels(pending, progress, completed, overdue);
    }

    // styling piechart
    private void updatePiePercentLabels(int pending, int inProgress, int completed, int overdue) {
        int total = pending + inProgress + completed + overdue;

        if (total == 0) total = 1;

        lblPersenPending.setText(String.format("%.0f%%", (pending * 100.0 / total)));
        lblPersenProgress.setText(String.format("%.0f%%", (inProgress * 100.0 / total)));
        lblPersenDone.setText(String.format("%.0f%%", (completed * 100.0 / total)));
        lblPersenOverdue.setText(String.format("%.0f%%", (overdue * 100.0 / total)));
    }



}


