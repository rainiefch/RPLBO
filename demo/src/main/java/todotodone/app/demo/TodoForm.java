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
    private Button btnCancel;

    @FXML
    private Button btnUpload;

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

    @FXML
    void initialize() {
        cbCategory.getItems().addAll("Work", "Personal", "Others");
    }

    @FXML
    void onBtnAddClick(ActionEvent event) {
        String title = txtTitle.getText();
        String category = cbCategory.getValue();
        String dueDate = dtpDueDate.getValue() != null ? dtpDueDate.getValue().toString() : null;
        String description = txtDescription.getText();
        String attachmentPath = selectedFile != null ? selectedFile.getAbsolutePath() : null;

        if (title.isEmpty() || category == null || dueDate == null) {
            showAlert("Please fill in all required fields.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO todo (title, status, category, due_date, description, attachment) VALUES (?, 'Pending', ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, title);
            stmt.setString(2, category);
            stmt.setString(3, dueDate);
            stmt.setString(4, description);
            stmt.setString(5, attachmentPath);

            stmt.executeUpdate();
            showAlert("To-Do added successfully!");
            clearForm();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Failed to add To-Do: " + e.getMessage());
        }
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
}
