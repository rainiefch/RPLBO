package todotodone.app.demo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import todotodone.app.demo.util.AlertUtil;
import todotodone.app.demo.util.DBConnection;
import todotodone.app.demo.util.SceneSwitcher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Login {

    private static String loggedInUsername = "";

    @FXML
    private TextField tfUsername;

    @FXML
    private PasswordField pfPassword;

    @FXML
    private Button btnSignIn, btnForgotPass, btnRegister;

    @FXML
    private ImageView picture;

    @FXML
    void onBtnSignInClick(ActionEvent event) {
        String username = tfUsername.getText().trim();
        String password = pfPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            AlertUtil.showError("Username and password cannot be empty.");
            return;
        }

        String query = "SELECT id_user, username FROM users WHERE username = ? AND password = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int userId = rs.getInt("id_user");
                    String uname = rs.getString("username");

                    AlertUtil.showInfo("Welcome, " + uname + "!");

                    Stage stage = (Stage) btnSignIn.getScene().getWindow();
                    SceneSwitcher.switchToHomeForm(stage, uname, userId);
                } else {
                    AlertUtil.showError("Incorrect username or password.");
                }
            }

        } catch (SQLException e) {
            AlertUtil.showError("Could not connect: " + e.getMessage());
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
