package todotodone.app.demo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import todotodone.app.demo.util.DBCategory;
import todotodone.app.demo.util.DBConnection;

import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class CategoryForm{

    @FXML private Button btnAdd, btnCancel, btnDelete;
    @FXML private TextField txtCategoryName, txtCategoryDesc;
    @FXML private ScrollPane scrollCategoryList;
    @FXML private GridPane gridCategoryList;

    private Integer editingCategoryId = null;
    public boolean isEditing = false;

    private final int userId;

    // Constructor to receive the current user's ID
    public CategoryForm(int userId) {
        this.userId = userId;
    }


    public void initialize() {
        loadCategoryList();
        btnAdd.setText("Add");
        btnDelete.setVisible(false);

        gridCategoryList.prefWidthProperty().bind(scrollCategoryList.widthProperty().subtract(20));
        gridCategoryList.minHeightProperty().bind(gridCategoryList.heightProperty());

        scrollCategoryList.setFitToWidth(true);
        scrollCategoryList.setFitToHeight(false);
    }

    public void loadCategoryList() {
        // Clear all rows except headers (row=0)
        gridCategoryList.getChildren().removeIf(node -> {
            Integer rowIndex = GridPane.getRowIndex(node);
            return rowIndex != null && rowIndex > 0;
        });
        gridCategoryList.getRowConstraints().clear();

        List<Category> categories = getCategoriesForUser(userId);

        int row = 1; // Start after header row
        for (Category category : categories) {
            Label nameLabel = new Label(category.getName());
            nameLabel.setStyle("-fx-font-weight: bold;");
            nameLabel.setPadding(new Insets(5));
            nameLabel.setOnMouseClicked(e -> populateFormFromCategory(category));

            Label descLabel = new Label(category.getDescription());
            descLabel.setWrapText(true);
            descLabel.setMaxWidth(300);
            descLabel.setPadding(new Insets(5));
            descLabel.setOnMouseClicked(e -> populateFormFromCategory(category));

            gridCategoryList.add(nameLabel, 0, row);
            gridCategoryList.add(descLabel, 1, row);

            RowConstraints rc = new RowConstraints();
            rc.setMinHeight(40);
            rc.setPrefHeight(40);
            rc.setMaxHeight(40);
            gridCategoryList.getRowConstraints().add(rc);

            row++;
        }
    }

    private List<Category> getCategoriesForUser(int userId) {
        List<Category> categories = new ArrayList<>();

        String sql = "SELECT id_category, name_category, desc_category FROM category WHERE id_user = ? or id_user = 1 ORDER BY name_category ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id_category");
                    String name = rs.getString("name_category");
                    String desc = rs.getString("desc_category");
                    categories.add(new Category(id, name, desc));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Failed to load categories: " + e.getMessage());
        }

        return categories;
    }

    private void populateFormFromCategory(Category category) {
        editingCategoryId = category.getId();
        txtCategoryName.setText(category.getName());
        txtCategoryDesc.setText(category.getDescription());
        isEditing = true;
        btnAdd.setText("Update");
        btnDelete.setVisible(true);
    }

    @FXML
    void onBtnAddClick(ActionEvent event) {
        String name = txtCategoryName.getText().trim();
        String description = txtCategoryDesc.getText().trim();

        if (name.isEmpty() || description.isEmpty()) {
            showAlert("Name and Description must not be empty.");
            return;
        }

        if (name.length() > 20) {
            showAlert("Name must not exceed 20 characters.");
            return;
        }

        if (description.length() > 50) {
            showAlert("Description must not exceed 50 characters.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sql;
            PreparedStatement stmt;

            if (editingCategoryId == null) {
                sql = "INSERT INTO category (name_category, desc_category, id_user) VALUES (?, ?, ?)";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, name);
                stmt.setString(2, description);
                stmt.setInt(3, userId);  // <-- set userId here for new categories
            } else {
                sql = "UPDATE category SET name_category = ?, desc_category = ? WHERE id_category = ? AND id_user = ?";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, name);
                stmt.setString(2, description);
                stmt.setInt(3, editingCategoryId);
                stmt.setInt(4, userId);  // <-- ensure updating only user's category
            }

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                showAlert("No category updated. It might not belong to the current user.");
            } else {
                showAlert(editingCategoryId == null ? "Category added!" : "Category updated!");
            }

            resetForm();
            loadCategoryList();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database error: " + e.getMessage());
        }
    }

    @FXML
    void onBtnDeleteClick(ActionEvent event) {
        if (editingCategoryId == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete this category?", ButtonType.OK, ButtonType.CANCEL);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try (Connection conn = DBConnection.getConnection()) {
                    String sql = "DELETE FROM category WHERE id_category = ? AND id_user = ?";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setInt(1, editingCategoryId);
                    stmt.setInt(2, userId);  // <-- only delete if belongs to current user

                    int affectedRows = stmt.executeUpdate();

                    if (affectedRows == 0) {
                        showAlert("No category deleted. It might not belong to the current user.");
                    } else {
                        showAlert("Category deleted.");
                    }

                    resetForm();
                    loadCategoryList();
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert("Failed to delete category: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    void onBtnCancelClick(ActionEvent event) {
        closeForm();
    }

    private void resetForm() {
        editingCategoryId = null;
        txtCategoryName.clear();
        txtCategoryDesc.clear();
        isEditing = false;
        btnAdd.setText("Add");
        btnDelete.setVisible(false);
    }

    private void closeForm() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Inner class to represent a Category record
    public static class Category {
        private final int id;
        private final String name;
        private final String description;

        public Category(int id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
    }
}
