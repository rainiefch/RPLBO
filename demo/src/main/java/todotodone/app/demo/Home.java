package todotodone.app.demo;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Home {

    @FXML private ComboBox<String> Category;
    @FXML private HBox btnAdd;
    @FXML private HBox btnCategory;
    @FXML private HBox btnHome;
    @FXML private HBox btnProfile;
    @FXML private ImageView btnSearch;
    @FXML private ComboBox<String> cbFilterStatus;
    @FXML private GridPane gridPane;
    @FXML private Label lblHome;
    @FXML private TextField txtSearch;

    private List<TodoItem> todoItems = new ArrayList<>();
    private List<TodoItem> filteredTodoItems = new ArrayList<>();

    static class TodoItem {
        int id;
        String title;
        String status;
        String dueDate;
        String category;
        String description;
        String attachment;

        public TodoItem(int id, String title, String status, String dueDate,
                        String category, String description, String attachment) {
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
        fetchAllTodos();
        filterTodos();
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
        cbFilterStatus.getItems().clear();
        cbFilterStatus.getItems().addAll("All Status", "Pending", "In Progress", "Completed", "Overdue");
        cbFilterStatus.getSelectionModel().selectFirst();

        Category.getItems().clear();
        Category.getItems().addAll("All Category", "Work", "Personal", "Others");
        Category.getSelectionModel().selectFirst();


    }

    private void setupSearchAndFilter() {
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filterTodos();
        });

        btnSearch.setOnMouseClicked(event -> filterTodos());

        cbFilterStatus.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            filterTodos();
        });

        Category.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            filterTodos();
        });
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
        String sql = "UPDATE todo SET status = ? WHERE id_todo = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newStatus);
            stmt.setInt(2, todoId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error updating todo status: " + e.getMessage());
        }
    }

    private void fetchAllTodos() {
        todoItems.clear();
        String sql = "SELECT id_todo, title, status, due_date, category, description, attachment FROM todo";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            List<TodoItem> todosToUpdate = new ArrayList<>();

            while (rs.next()) {
                String status = rs.getString("status");
                String dueDate = rs.getString("due_date");

                TodoItem item = new TodoItem(
                        rs.getInt("id_todo"),
                        rs.getString("title"),
                        status,
                        dueDate,
                        rs.getString("category"),
                        rs.getString("description"),
                        rs.getString("attachment")
                );

                if (!"Completed".equals(status) && dueDate != null && !dueDate.isEmpty() && isOverdue(dueDate)) {
                    item.status = "Overdue";
                    todosToUpdate.add(item);
                }

                todoItems.add(item);
            }

            if (!todosToUpdate.isEmpty()) {
                updateTodoStatuses(todosToUpdate);
            }

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
            showError("Error loading todos. Please try again.");
        }
    }

    private void updateTodoStatuses(List<TodoItem> todos) {
        String sql = "UPDATE todo SET status = ? WHERE id_todo = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (TodoItem item : todos) {
                stmt.setString(1, "Overdue");
                stmt.setInt(2, item.id);
                stmt.addBatch();
            }

            stmt.executeBatch();

        } catch (SQLException e) {
            System.err.println("Error updating todo statuses: " + e.getMessage());
        }
    }

    private void filterTodos() {
        String searchText = txtSearch.getText().toLowerCase();
        String statusFilter = cbFilterStatus.getValue();
        String categoryFilter = Category.getValue();

        filteredTodoItems.clear();

        for (TodoItem item : todoItems) {
            boolean matchesSearch = searchText.isEmpty() ||
                    item.title.toLowerCase().contains(searchText) ||
                    item.description.toLowerCase().contains(searchText);

            boolean matchesStatus;
            if (statusFilter == null || statusFilter.equals("All Status")) {
                matchesStatus = true;
            } else {
                matchesStatus = item.status.equals(statusFilter);
            }

            boolean matchesCategory;
            if (categoryFilter == null || categoryFilter.equals("All Category")) {
                matchesCategory = true;
            } else {
                matchesCategory = item.category.equals(categoryFilter);
            }

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

    private void refreshTodos() {
        fetchAllTodos();
        filterTodos();
    }

    private Stage getStage() {
        return (Stage) btnAdd.getScene().getWindow();
    }

    // Button handlers
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
    }

    @FXML void onBtnHomeClick(MouseEvent event) {
        refreshTodos();
    }

    @FXML void onBtnProfileClick(MouseEvent event) {
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
}