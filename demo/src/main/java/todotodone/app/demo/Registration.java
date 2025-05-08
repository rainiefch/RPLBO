package todotodone.app.demo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Registration {

    @FXML
    private Button btnRegister;

    @FXML
    private Label lblConfirmPass, lblPassword, lblRplbo, lblSignInHere, lblToDoToDone, lblUsername, lblWelcome;

    @FXML
    private PasswordField pfPassword, tfPassword;

    @FXML
    private TextField tfUsername;

    @FXML
    private ImageView picture;

    @FXML
    void onBtnRegisterClick(ActionEvent event) {
        String username = tfUsername.getText();
        String password = pfPassword.getText();
        String confirmPassword = tfPassword.getText();

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "All fields must be filled!");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Error", "Passwords do not match!");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String query = "INSERT INTO users (username, password) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password);
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Registration successful!");
                goToLogin();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to register.");
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
        }
    }

    @FXML
    void onLblSignInHereClicked(MouseEvent event) {
        goToLogin();
    }

    private void goToLogin() {
        try {
            Stage stage = (Stage) btnRegister.getScene().getWindow();
            Scene scene = new Scene(FXMLLoader.load(getClass().getResource("login.fxml")));
            stage.setScene(scene);
            stage.setTitle("To Do To Done - Login");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
