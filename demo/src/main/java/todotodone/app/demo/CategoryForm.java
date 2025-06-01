package todotodone.app.demo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import todotodone.app.demo.util.DBConnection;
import todotodone.app.demo.model.Category;
import todotodone.app.demo.util.AlertUtil;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryForm {

    @FXML private Button btnAdd, btnCancel, btnDelete;
    @FXML private TextField txtCategoryName, txtCategoryDesc;
    @FXML private ScrollPane scrollCategoryList;
    @FXML private GridPane gridCategoryList;

    private Integer editingCategoryId = null;
    public boolean isEditing = false;
    private final int userId;

    public CategoryForm(int userId) {
        this.userId = userId;
    }


    public void initialize() {
        loadCategoryList();

        btnAdd.setText("Add");
        btnDelete.setVisible(false);

        gridCategoryList.prefWidthProperty().bind(scrollCategoryList.widthProperty().subtract(20));

        scrollCategoryList.setFitToWidth(true);
        scrollCategoryList.setFitToHeight(false);

        txtCategoryName.requestFocus();

        txtCategoryName.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                txtCategoryDesc.requestFocus();
            }
        });

        txtCategoryDesc.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                btnAdd.fire();
            }
        });
    }

    //munculin categori ke list
    public void loadCategoryList() {
        gridCategoryList.getChildren().removeIf(node -> {
            Integer rowIndex = GridPane.getRowIndex(node);
            return rowIndex != null && rowIndex > 0;
        });
        gridCategoryList.getRowConstraints().clear();

        List<Category> categories = getCategoriesForUser(userId);
        int row = 1;

        for (Category category : categories) {
            Label nameLabel = new Label(category.getName());
            nameLabel.setStyle("-fx-font-weight: bold;");
            nameLabel.setPadding(new Insets(5));
            nameLabel.setOnMousePressed(e -> populateFormFromCategory(category));

            Label descLabel = new Label(category.getDescription());
            descLabel.setWrapText(true);
            descLabel.setMaxWidth(300);
            descLabel.setPadding(new Insets(5));
            descLabel.setOnMousePressed(e -> populateFormFromCategory(category));

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

    //ngambil category dari db
    private List<Category> getCategoriesForUser(int userId) {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT id_category, name_category, desc_category FROM category WHERE id_user = ? OR id_user = 1 ORDER BY name_category ASC";

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
            AlertUtil.showError("Failed to load categories: " + e.getMessage());
        }

        return categories;
    }

    // masukin data category ke form
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
            AlertUtil.showError("Name and Description must not be empty.");
            return;
        }

        if (isCategoryNameDuplicate(name)) {
            AlertUtil.showError("Category name already exists. Please choose a different name.");
            return;
        }

        if (name.length() > 20) {
            AlertUtil.showError("Name must not exceed 20 characters.");
            return;
        }

        if (description.length() > 50) {
            AlertUtil.showError("Description must not exceed 50 characters.");
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
                stmt.setInt(3, userId);
            } else {
                sql = "UPDATE category SET name_category = ?, desc_category = ? WHERE id_category = ? AND id_user = ?";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, name);
                stmt.setString(2, description);
                stmt.setInt(3, editingCategoryId);
                stmt.setInt(4, userId);
            }

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                AlertUtil.showError("Default category cannot be edited");
            } else {
                AlertUtil.showInfo(editingCategoryId == null ? "Category added!" : "Category updated!");
            }

            resetForm();
            loadCategoryList();

        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtil.showError("Database error: " + e.getMessage());
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
                    stmt.setInt(2, userId);

                    int affectedRows = stmt.executeUpdate();

                    if (affectedRows == 0) {
                        AlertUtil.showError("Default category cannot be deleted.");
                    } else {
                        AlertUtil.showInfo("Category deleted.");
                    }

                    resetForm();
                    loadCategoryList();

                } catch (SQLException e) {
                    e.printStackTrace();
                    AlertUtil.showError("Failed to delete category: " + e.getMessage());
                }
            }
        });
    }


    @FXML
    void onBtnCancelClick(ActionEvent event) {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    private void resetForm() {
        editingCategoryId = null;
        txtCategoryName.clear();
        txtCategoryDesc.clear();
        isEditing = false;
        btnAdd.setText("Add");
        btnDelete.setVisible(false);
    }

        //ngecek dah ada category yg namanya dan idusernya sama nggak di db
    private boolean isCategoryNameDuplicate(String name) {
        String sql = "SELECT COUNT(*) FROM category WHERE name_category = ? AND id_user = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setInt(2, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);

                    if (isEditing && editingCategoryId != null) {
                        String checkSql = "SELECT id_category FROM category WHERE name_category = ? AND id_user = ?";
                        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                            checkStmt.setString(1, name);
                            checkStmt.setInt(2, userId);
                            try (ResultSet checkRs = checkStmt.executeQuery()) {
                                while (checkRs.next()) {
                                    int existingId = checkRs.getInt("id_category");
                                    if (existingId != editingCategoryId) {
                                        return true;
                                    }
                                }
                            }
                        }
                        return false;
                    }

                    return count > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtil.showError("Database error: " + e.getMessage());
            return true;
        }

        return false;
    }

}
