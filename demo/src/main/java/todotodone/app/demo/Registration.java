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

public class Registration {

    @FXML
    private Button btnRegister, btnSignIn;

    @FXML
    private PasswordField pfPassword, pfConfirmPassword;

    @FXML
    private TextField tfUsername;

    @FXML
    private ImageView picture;

    @FXML
    void onBtnRegisterClick(ActionEvent event) {
        String username = tfUsername.getText().trim();
        String password = pfPassword.getText();
        String confirmPassword = pfConfirmPassword.getText();

        if (username.length() > 20) {
            AlertUtil.showError("Username must be 20 characters or less.");
            return;
        }

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            AlertUtil.showError("All fields must be filled!");
            return;
        }

        if (password.length() < 10) {
            AlertUtil.showError("Password must be at least 10 characters long.");
            return;
        }

        if (!password.matches(".*[A-Z].*")) {
            AlertUtil.showError("Password must contain at least one uppercase letter.");
            return;
        }

        if (!password.matches(".*[^a-zA-Z0-9].*")) {
            AlertUtil.showError("Password must contain at least one special character.");
            return;
        }

        if (!password.matches(".*\\d.*")) {
            AlertUtil.showError("Password must contain at least one number.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            AlertUtil.showError("Passwords do not match!");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String checkQuery = "SELECT COUNT(*) FROM users WHERE username = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setString(1, username);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next() && rs.getInt(1) > 0) {
                    AlertUtil.showError("Username already exists.");
                    return;
                }
            }

            String query = "INSERT INTO users (username, password) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, username);
                stmt.setString(2, password);

                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    AlertUtil.showInfo("Registration successful!");
                    SceneSwitcher.switchToLoginForm((Stage) btnRegister.getScene().getWindow());
                } else {
                    AlertUtil.showError("Registration failed.");
                }
            }

        } catch (SQLException e) {
            AlertUtil.showError("Database Error" + e.getMessage());
        }
    }

    @FXML
    void onBtnSignInClick(ActionEvent event) {
        SceneSwitcher.switchToLoginForm((Stage) btnSignIn.getScene().getWindow());
    }

}
