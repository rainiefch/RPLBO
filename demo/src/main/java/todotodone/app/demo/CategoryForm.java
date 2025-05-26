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
import java.util.List;
import java.util.ResourceBundle;

public class CategoryForm implements Initializable {

    @FXML private Button btnAdd, btnCancel, btnDelete;
    @FXML private TextField txtCategoryName, txtCategoryDesc;
    @FXML private ScrollPane scrollCategoryList;
    @FXML private GridPane gridCategoryList;

    private Integer editingCategoryId = null;
    public boolean isEditing = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadCategoryList();
        btnAdd.setText("Add");
        btnDelete.setVisible(false);

        gridCategoryList.prefWidthProperty().bind(scrollCategoryList.widthProperty().subtract(20)); // some padding maybe

        gridCategoryList.minHeightProperty().bind(gridCategoryList.heightProperty());

        scrollCategoryList.setFitToWidth(true);
        scrollCategoryList.setFitToHeight(false);

    }

    public void loadCategoryList() {
        gridCategoryList.getChildren().removeIf(node -> GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) > 0);

        List<DBCategory.Category> categories = DBCategory.getInstance().getAllCategories();

        int row = 1; // Start after header row
        for (DBCategory.Category category : categories) {
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

            // 🔒 Add fixed height constraint for this row
            RowConstraints rc = new RowConstraints();
            rc.setMinHeight(40);  // You can adjust this value as needed
            rc.setPrefHeight(40);
            rc.setMaxHeight(40);
            gridCategoryList.getRowConstraints().add(rc);

            row++;
        }
    }



    private void populateFormFromCategory(DBCategory.Category category) {
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

        if (name.isEmpty()) {
            showAlert("Please enter the category name.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sql;
            PreparedStatement stmt;

            if (editingCategoryId == null) {
                sql = "INSERT INTO category (name_category, desc_category) VALUES (?, ?)";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, name);
                stmt.setString(2, description);
            } else {
                sql = "UPDATE category SET name_category = ?, desc_category = ? WHERE id_category = ?";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, name);
                stmt.setString(2, description);
                stmt.setInt(3, editingCategoryId);
            }

            stmt.executeUpdate();
            showAlert(editingCategoryId == null ? "Category added!" : "Category updated!");
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
                    String sql = "DELETE FROM category WHERE id_category = ?";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setInt(1, editingCategoryId);
                    stmt.executeUpdate();

                    showAlert("Category deleted.");
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
}
