package todotodone.app.demo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TodoForm {

    @FXML
    private Button btnAdd;

    @FXML
    private Button btnCancel, btnUpload, btnDelete;


    @FXML
    private ComboBox<String> cbCategory;

    @FXML
    private DatePicker dtpDueDate;

    @FXML
    private Label lblChosenFile;

    @FXML
    private TextArea txtDescription;

    @FXML
    private TextField txtTitle;

    private File selectedFile;
    private Home.TodoItem todoToEdit = null;


    @FXML
    void initialize() {
        cbCategory.getItems().addAll("Work", "Personal", "Others");
        btnAdd.setText("Add");
        btnDelete.setVisible(false);
    }


    @FXML
    void onBtnCancelClick(ActionEvent event) {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    @FXML
    void onBtnUploadFileClick(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose File");
        selectedFile = fileChooser.showOpenDialog(btnUpload.getScene().getWindow());

        if (selectedFile != null) {
            lblChosenFile.setText(selectedFile.getName());
        } else {
            lblChosenFile.setText("No file chosen");
        }
    }

    @FXML
    void onLblChosenFileClick() {
        if (selectedFile == null || !selectedFile.exists()) {
            showAlert("No file selected or file not found.");
            return;
        }

        String fileName = selectedFile.getName().toLowerCase();
        if (fileName.endsWith(".pdf")) {
            try {
                java.awt.Desktop.getDesktop().open(selectedFile);
            } catch (Exception e) {
                showAlert("Unable to open PDF file.");
            }
        } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png")) {
            showImagePopup();
        } else {
            showAlert("Unsupported file format.");
        }
    }

    private void showImagePopup() {
        try {
            Stage popupStage = new Stage();
            popupStage.setTitle("Image Preview");

            javafx.scene.image.Image image = new javafx.scene.image.Image(selectedFile.toURI().toString());
            javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView(image);
            imageView.setFitWidth(400);
            imageView.setPreserveRatio(true);

            ScrollPane scrollPane = new ScrollPane(imageView);
            Scene scene = new Scene(scrollPane, 420, 500);
            popupStage.setScene(scene);
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.showAndWait();
        } catch (Exception e) {
            showAlert("Failed to preview image.");
        }
    }

    private void clearForm() {
        txtTitle.clear();
        cbCategory.setValue(null);
        dtpDueDate.setValue(null);
        txtDescription.clear();
        lblChosenFile.setText("");
        selectedFile = null;
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setContentText(message);
        alert.showAndWait();
    }


    private Integer editingTodoId = null;

    public boolean isEditing = false;

    public void setTodoData(Home.TodoItem todo) {
        this.isEditing = true;
        this.editingTodoId = todo.id;

        txtTitle.setText(todo.title);
        cbCategory.setValue(todo.category);
        if (todo.dueDate != null) {
            dtpDueDate.setValue(java.time.LocalDate.parse(todo.dueDate));
        }
        txtDescription.setText(todo.description);
        if (todo.attachment != null) {
            selectedFile = new File(todo.attachment);
            lblChosenFile.setText(selectedFile.getName());
        }

        btnAdd.setText("Update");
        btnDelete.setVisible(true);
    }



    @FXML
    void onBtnAddClick(ActionEvent event) {
        String title = txtTitle.getText().trim();
        String category = cbCategory.getValue();
        java.time.LocalDate dueDateValue = dtpDueDate.getValue();
        String description = txtDescription.getText();
        String attachmentPath = selectedFile != null ? selectedFile.getAbsolutePath() : null;

        if (title.isEmpty() || category == null || dueDateValue == null) {
            showAlert("Please fill all required fields: Title, Due Date, Category.");
            return;
        }

        String dueDate = dueDateValue.toString();
        String status = "Pending";

        if (isOverdue(dueDate)) {
            status = "Overdue";
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sql;
            if (editingTodoId == null) {
                sql = "INSERT INTO todo (title, status, category, due_date, description, attachment) VALUES (?, ?, ?, ?, ?, ?)";
            } else {
                sql = "UPDATE todo SET title=?, status=?, category=?, due_date=?, description=?, attachment=? WHERE id_todo=?";
            }

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, title);
            stmt.setString(2, status);
            stmt.setString(3, category);
            stmt.setString(4, dueDate);
            stmt.setString(5, description);
            stmt.setString(6, attachmentPath);

            if (editingTodoId != null) {
                stmt.setInt(7, editingTodoId);
            }

            stmt.executeUpdate();
            showAlert(editingTodoId == null ? "To-Do added successfully!" : "To-Do updated successfully!");
            closeForm();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Failed to save To-Do: " + e.getMessage());
        }
    }

    private boolean isOverdue(String dueDateStr) {
        try {
            java.time.LocalDate dueDate = java.time.LocalDate.parse(dueDateStr);
            return dueDate.isBefore(java.time.LocalDate.now());
        } catch (Exception e) {
            return false;
        }
    }


    @FXML
    void onBtnDeleteClick(ActionEvent event) {
        if (editingTodoId == null) {
            showAlert("No To-Do selected.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Are you sure you want to delete this To-Do?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try (Connection conn = DBConnection.getConnection()) {
                    String sql = "DELETE FROM todo WHERE id_todo=?";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setInt(1, editingTodoId);
                    stmt.executeUpdate();
                    showAlert("To-Do deleted successfully!");
                    closeForm();
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert("Failed to delete To-Do: " + e.getMessage());
                }
            }
        });
    }


    private void closeForm() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }
}
