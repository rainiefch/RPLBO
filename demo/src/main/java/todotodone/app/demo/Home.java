package todotodone.app.demo;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
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

    @FXML private HBox btnAdd;
    @FXML private HBox btnCategory;
    @FXML private HBox btnHome;
    @FXML private HBox btnProfile;
    @FXML private Label lblHome;
    @FXML private GridPane gridPane;

    private List<TodoItem> todoItems = new ArrayList<>();

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
        refreshTodos();
    }

    private void setupGridPane() {

        gridPane.getRowConstraints().clear();

        for (int i = 0; i < 3; i++) {
            RowConstraints rc = new RowConstraints();
            rc.setVgrow(javafx.scene.layout.Priority.NEVER);
            rc.setPrefHeight(40); // Header row height
            gridPane.getRowConstraints().add(rc);
        }
    }

    private void fetchAllTodos() {
        todoItems.clear();
        String sql = "SELECT id_todo, title, status, due_date, category, description, attachment FROM todo";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                todoItems.add(new TodoItem(
                        rs.getInt("id_todo"),
                        rs.getString("title"),
                        rs.getString("status"),
                        rs.getString("due_date"),
                        rs.getString("category"),
                        rs.getString("description"),
                        rs.getString("attachment")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
            showError("Error loading todos. Please try again.");
        }
    }

    private void displayTodos() {
        gridPane.getChildren().removeIf(node ->
                GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) >= 2
        );

        while (gridPane.getRowConstraints().size() > 2) {
            gridPane.getRowConstraints().remove(2);
        }

        if (todoItems.isEmpty()) {
            showNoItemsMessage();
            return;
        }

        int rowIndex = 2;
        for (TodoItem item : todoItems) {

            RowConstraints rc = new RowConstraints();
            rc.setVgrow(javafx.scene.layout.Priority.NEVER);
            rc.setPrefHeight(40);
            gridPane.getRowConstraints().add(rc);

            Label titleLabel = new Label(item.title);
            styleTodoLabel(titleLabel);
            gridPane.add(titleLabel, 0, rowIndex);
            GridPane.setMargin(titleLabel, new Insets(0, 0, 0, 20));

            ComboBox<String> statusCombo = new ComboBox<>();
            statusCombo.getItems().addAll("Pending", "In Progress", "Completed");
            statusCombo.setValue(item.status);
            styleComboBox(statusCombo);
            gridPane.add(statusCombo, 2, rowIndex);
            GridPane.setMargin(statusCombo, new Insets(0, 0, 0, 0));

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
        label.setStyle("-fx-font-size: 16px; -fx-text-fill: black; -fx-background-color: #ffffff;");
        label.setMaxWidth(Double.MAX_VALUE);
        label.setPadding(new Insets(5));
    }

    private void styleComboBox(ComboBox<String> comboBox) {
        comboBox.setStyle("-fx-font-size: 16px; -fx-background-color: #ffffff;");
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

    @FXML
    void onBtnAddClick(MouseEvent event) {
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


    private void refreshTodos() {
        fetchAllTodos();
        displayTodos();
    }

    private Stage getStage() {
        return (Stage) btnAdd.getScene().getWindow();
    }

    private void setupRowClickHandlers() {
        for (int i = 0; i < todoItems.size(); i++) {
            int gridRow = i + 3;
            final int todoIndex = i;

            gridPane.getChildren().forEach(node -> {
                if (GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) == gridRow) {
                    node.setOnMouseClicked(event -> {
                        if (event.getClickCount() == 1 || event.getClickCount() == 2) {
                            openTodoForEditing(todoItems.get(todoIndex));
                        }
                    });
                }
            });
        }
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

    @FXML
    void onBtnCategoryClick(MouseEvent event) {
        SceneSwitcher.popCategoryForm(getStage());
    }

    @FXML
    void onBtnHomeClick(MouseEvent event) {
        refreshTodos();
    }

    @FXML
    void onBtnProfileClick(MouseEvent event) {
    }

    @FXML
    void onHomeClick(MouseEvent event) {
        refreshTodos();
    }
}