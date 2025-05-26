package todotodone.app.demo;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public Home() {}

    public Home(String username, int userId) {
        this.username = username;
        this.userId = userId;
    }

    static class TodoItem {
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

    @FXML
    void initialize() {
        setupGridPane();
        initializeComboBoxes();
        setupSearchAndFilter();
        refreshTodos();
    }

    private void setupGridPane() {
        gridPane.getRowConstraints().clear();
        for (int i = 0; i < 3; i++) {
            RowConstraints rc = new RowConstraints();
            rc.setVgrow(javafx.scene.layout.Priority.NEVER);
            rc.setPrefHeight(40);
            gridPane.getRowConstraints().add(rc);
        }
    }

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

    private void fetchAllTodos() {
        todoItems.clear();
        String sql = "SELECT id_todo, title, status, due_date, category, description, attachment FROM todo WHERE id_user = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            List<TodoItem> overdueList = new ArrayList<>();

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

            if (!overdueList.isEmpty()) {
                updateTodoStatuses(overdueList);
            }

        } catch (SQLException e) {
            System.err.println("Error loading todos: " + e.getMessage());
        }
    }

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

        displayTodos();
    }

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

            Label titleLabel = new Label(item.title);
            styleTodoLabel(titleLabel);
            gridPane.add(titleLabel, 0, rowIndex);
            GridPane.setMargin(titleLabel, new Insets(0, 0, 0, 20));

            ComboBox<String> statusCombo = new ComboBox<>();
            statusCombo.getItems().addAll("Pending", "In Progress", "Completed", "Overdue");
            statusCombo.setValue(item.status);
            styleComboBox(statusCombo);
            gridPane.add(statusCombo, 2, rowIndex);
            GridPane.setMargin(statusCombo, new Insets(0, 0, 0, 0));

            statusCombo.setOnAction(event -> {
                String newStatus = statusCombo.getValue();
                if (!newStatus.equals(item.status)) {
                    updateTodoStatus(item.id, newStatus);
                    item.status = newStatus; // Update local copy
                    refreshTodos(); // Refresh to show changes
                }
            });

            Label dateLabel = new Label(item.dueDate);
            styleTodoLabel(dateLabel);
            gridPane.add(dateLabel, 4, rowIndex);
            GridPane.setMargin(dateLabel, new Insets(0, 0, 0, 0));

            Label categoryLabel = new Label(item.category);
            styleTodoLabel(categoryLabel);
            gridPane.add(categoryLabel, 6, rowIndex);
            GridPane.setMargin(categoryLabel, new Insets(0, 0, 0, 0));

            setupClickHandlersForRow(item, titleLabel, dateLabel, categoryLabel);

            rowIndex++;
        }
    }

    private void setupClickHandlersForRow(TodoItem item, Label... clickableNodes) {
        for (Label node : clickableNodes) {
            node.setOnMouseClicked(event -> {
                if (!(event.getTarget() instanceof ComboBox)) {
                    openTodoForEditing(item);
                }
            });
        }
    }

    private void styleTodoLabel(Label label) {
        label.setStyle("-fx-font-size: 16px; -fx-text-fill: black; -fx-background-color: #ffffff; -fx-cursor: hand;");
        label.setMaxWidth(Double.MAX_VALUE);
        label.setPadding(new Insets(5));
    }

    private void styleComboBox(ComboBox<String> comboBox) {
        comboBox.setStyle("-fx-font-size: 16px; -fx-background-color: #ffffff; -fx-cursor: default;");
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

    private void openTodoForEditing(TodoItem todo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/todotodone/app/demo/todoForm.fxml"));
            Parent root = loader.load();

            TodoForm controller = loader.getController();
            controller.setTodoData(todo);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Edit Todo");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            refreshTodos();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to open todo editor");
        }
    }


    private Stage getStage() {
        return (Stage) btnAdd.getScene().getWindow();
    }

    @FXML void onBtnAddClick(MouseEvent event) {

        SceneSwitcher.popTodoForm(getStage());
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

    @FXML void onBtnCategoryClick(MouseEvent event) {
        SceneSwitcher.popCategoryForm(getStage());
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

    @FXML void onBtnProfileClick(MouseEvent event) {
        SceneSwitcher.popProfileForm(getStage());
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
                case "Pending":
                    pending++;
                    break;
                case "In Progress":
                    progress++;
                    break;
                case "Completed":
                    completed++;
                    break;
                case "Overdue":
                    overdue++;
                    break;
            }
        }

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Pending", pending),
                new PieChart.Data("In Progress", progress),
                new PieChart.Data("Completed", completed),
                new PieChart.Data("Overdue", overdue)
        );

        todoPieChart.setData(pieChartData);
        todoPieChart.setTitle("Todo Status Overview");

        updatePiePercentLabels(pending, progress, completed, overdue);
    }



    private void updatePiePercentLabels(int pending, int inProgress, int completed, int overdue) {
        int total = pending + inProgress + completed + overdue;

        if (total == 0) total = 1; // Hindari divide by zero agar tidak NaN

        lblPersenPending.setText(String.format("%.0f%%", (pending * 100.0 / total)));
        lblPersenProgress.setText(String.format("%.0f%%", (inProgress * 100.0 / total)));
        lblPersenDone.setText(String.format("%.0f%%", (completed * 100.0 / total)));
        lblPersenOverdue.setText(String.format("%.0f%%", (overdue * 100.0 / total)));
    }



}


