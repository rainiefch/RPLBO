package todotodone.app.demo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import todotodone.app.demo.model.Todo;
import todotodone.app.demo.util.AlertUtil;
import todotodone.app.demo.util.DBConnection;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TodoForm {

    private File selectedFile;
    private Home.TodoItem todoToEdit = null;
    private int userId;
    private String username;
    private Integer editingTodoId = null;
    public boolean isEditing = false;

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


    public TodoForm(){};

    public TodoForm(int userID){
        this.userId = userID;
    };

    @FXML
    //kl ke enter ke add jg
    void initialize() {
        initializeComboBoxes();
        btnAdd.setText("Add");
        btnDelete.setVisible(false);
        txtTitle.requestFocus();

        txtTitle.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                cbCategory.requestFocus();
            }
        });

        cbCategory.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                dtpDueDate.requestFocus();
            }
        });

        dtpDueDate.getEditor().setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                txtDescription.requestFocus();
            }
        });

        txtDescription.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                btnUpload.requestFocus();
            }
        });

        btnUpload.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                btnAdd.fire();
            }
        });
    }

    //ambil catfgory dr db
    private void initializeComboBoxes() {
        cbCategory.getItems().clear();

        String sql = "SELECT name_category FROM category WHERE id_user = 1 OR id_user = ? ORDER BY name_category ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    cbCategory.getItems().add(rs.getString("name_category"));
                }
            }

        } catch (SQLException e) { //kl gagal konek kategori dari db
            System.err.println("Failed to load categories from database: " + e.getMessage());
            cbCategory.getItems().addAll("Work", "Personal", "Others");
        }

        cbCategory.getSelectionModel().selectFirst();
    }


    @FXML
    void onBtnCancelClick(ActionEvent event) {
        clearForm();
        closeForm();
    }

    @FXML
    void onBtnUploadFileClick(ActionEvent event) { //buat upload file
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose File");
        selectedFile = fileChooser.showOpenDialog(btnUpload.getScene().getWindow());

        if (selectedFile != null) {
            lblChosenFile.setText(selectedFile.getName());
        } else { //kl gada filenya
            lblChosenFile.setText("No file chosen");
        }
    }

    @FXML
    void onLblChosenFileClick() {
        if (selectedFile == null || !selectedFile.exists()) {
            AlertUtil.showError("No file selected or file not found.");
            return;
        }

        String fileName = selectedFile.getName().toLowerCase();
        if (fileName.endsWith(".pdf")) { //file bs pdf
            try {
                java.awt.Desktop.getDesktop().open(selectedFile);
            } catch (Exception e) {
                AlertUtil.showError("Unable to open PDF file."); //kl gabisa open pdf
            }
        } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png")) {
            showImagePopup(); //file bs jpg jpeg
        } else {
            AlertUtil.showError("Unsupported file format."); //ga support format file
        }
    }

    @FXML
    void onCbClick(ActionEvent event) {
//        initializeComboBoxes();
    }

    //kl masukin image trs imagenya di klik
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
            AlertUtil.showError("Failed to preview image."); //gbs munculin preview
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

    //masukin data todo dr homepage ke formnya
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

        initializeComboBoxes();

        cbCategory.setValue(todo.category);
    }

    @FXML
    void onBtnAddClick(ActionEvent event) {
        String title = txtTitle.getText().trim();
        String category = cbCategory.getValue();
        java.time.LocalDate dueDateValue = dtpDueDate.getValue();
        String description = txtDescription.getText();
        String attachmentPath = selectedFile != null ? selectedFile.getAbsolutePath() : null;

        //data yg di add gaboleh empty
        if (title.isEmpty() || category == null || dueDateValue == null || description.isEmpty()) {
            AlertUtil.showError("All fields are required: Title, Category, Due Date, and Description.");
            return;
        }
        //gbs lebih dari 50 kata
        if (title.length() > 50) {
            AlertUtil.showError("Title must not exceed 50 characters.");
            return;
        }
        //gbs lebih dari 200 kata
        if (description.length() > 200) {
            AlertUtil.showError("Description must not exceed 200 characters.");
            return;
        }
        //dl gabisa di waktu yg udh lewat
        java.time.LocalDate today = java.time.LocalDate.now();
        if (dueDateValue.isBefore(today)) {
            AlertUtil.showError("Due date cannot be in the past.");
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
                sql = "INSERT INTO todo (title, status, category, due_date, description, attachment, id_user) VALUES (?, ?, ?, ?, ?, ?, ?)";
            } else {
                sql = "UPDATE todo SET title=?, status=?, category=?, due_date=?, description=?, attachment=?, id_user=? WHERE id_todo=?";
            }


            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, title);
            stmt.setString(2, status);
            stmt.setString(3, category);
            stmt.setString(4, dueDate);
            stmt.setString(5, description);
            stmt.setString(6, attachmentPath);
            stmt.setInt(7, userId);

            if (editingTodoId != null) {
                stmt.setInt(8, editingTodoId);
            }

            stmt.executeUpdate();
            AlertUtil.showInfo(editingTodoId == null ? "To-Do added successfully!" : "To-Do updated successfully!");
            closeForm();

        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtil.showError("Failed to save To-Do: " + e.getMessage());
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

    @FXML //buat delete todo
    void onBtnDeleteClick(ActionEvent event) {
        if (editingTodoId == null) {
            AlertUtil.showError("No To-Do selected.");
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
                    AlertUtil.showInfo("To-Do deleted successfully!");
                    closeForm();
                } catch (SQLException e) {
                    e.printStackTrace();
                    AlertUtil.showError("Failed to delete To-Do: " + e.getMessage());
                }
            }
        });
    }

    private void closeForm() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }
}
