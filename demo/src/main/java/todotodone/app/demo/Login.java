package todotodone.app.demo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Login {

    @FXML
    private Button btnForgotPass;

    @FXML
    private Button btnSignIn;

    @FXML
    private Label lblPassword;

    @FXML
    private Label lblRegister;

    @FXML
    private Label lblRplbo;

    @FXML
    private Label lblToDoToDone;

    @FXML
    private Label lblUsername;

    @FXML
    private PasswordField pfPassword;

    @FXML
    private ImageView picture;

    @FXML
    private TextField tfUsername;

    @FXML
    void onBtnForgotPassClick(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Info", "Fitur 'Forgot Password' belum tersedia.");
    }

    @FXML
    void onBtnSignInClick(ActionEvent event) {
        String username = tfUsername.getText().trim();
        String password = pfPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Username and password must not be empty.");
            return;
        }

        String query = "SELECT * FROM users WHERE username = ? AND password = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/todotodone/app/demo/home.fxml"));
                Parent root = loader.load();

                Home homeController = loader.getController();
                homeController.initializeUser(username);

                Stage stage = (Stage) btnSignIn.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("To Do To Done - Dashboard");
                stage.show();

            } else {
                showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid username or password.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not connect to the database.");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "UI Error", "Failed to load main screen.");
        }
    }

    @FXML
    void onLblRegisterClick(MouseEvent event) {
        // TODO: Navigasi ke registration.fxml jika diinginkan
        showAlert(Alert.AlertType.INFORMATION, "Info", "Fitur register belum diimplementasikan.");
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
