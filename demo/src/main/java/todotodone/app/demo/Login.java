package todotodone.app.demo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import todotodone.app.demo.util.DBConnection;
import todotodone.app.demo.util.SceneSwitcher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Login {

    @FXML
    private TextField tfUsername;

    @FXML
    private PasswordField pfPassword;

    @FXML
    private Button btnSignIn, btnForgotPass, btnRegister;

    @FXML
    private ImageView picture;

    private static String loggedInUsername = "";

    @FXML
    void onBtnSignInClick(ActionEvent event) {
        String username = tfUsername.getText().trim();
        String password = pfPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Username and password cannot be empty.");
            return;
        }

        String query = "SELECT * FROM users WHERE username = ? AND password = ?"; // use hashed password in production

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    loggedInUsername = username;
                    showAlert(Alert.AlertType.INFORMATION, "Login Successful", "Welcome, " + username + "!");
                    SceneSwitcher.switchToHomeForm((Stage) btnSignIn.getScene().getWindow(), loggedInUsername);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Login Failed", "Incorrect username or password.");
                }
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not connect: " + e.getMessage());
        }
    }

    @FXML
    void onBtnForgotPassClick(ActionEvent event) {
        SceneSwitcher.switchToChangePasswordForm((Stage) btnForgotPass.getScene().getWindow());
    }

    @FXML
    void onBtnRegisterClick(ActionEvent event) {
        SceneSwitcher.switchToRegistrationForm((Stage) btnRegister.getScene().getWindow());
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static String getLoggedInUsername() {
        return loggedInUsername;
    }
}
