package todotodone.app.demo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import todotodone.app.demo.util.AlertUtil;
import todotodone.app.demo.util.DBConnection;
import todotodone.app.demo.util.SceneSwitcher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ChangePassword {
    @FXML
    private Button btnCancel, btnChangePass;

    @FXML
    private PasswordField txtNewPass, txtConfPass;

    @FXML
    private TextField txtUsername;

    private String username;
    private Integer userId;

    public ChangePassword() {}

    public void initializeForLoggedInUser(String username, Integer userId) {
        this.username = username;
        this.userId = userId;
        txtUsername.setText(username);
        txtUsername.setDisable(true);
    }

    @FXML
    void onBtnCancelClick(ActionEvent event) {
        if(username != null) {
            Stage stage = (Stage) btnCancel.getScene().getWindow();
            SceneSwitcher.switchToHomeForm(stage, username, userId);
        }
        else{ SceneSwitcher.switchToLoginForm((Stage) btnCancel.getScene().getWindow());}
    }

    @FXML
    void onBtnChangePassClick(ActionEvent event) {
        String username = txtUsername.getText().trim();
        String newPass = txtNewPass.getText();
        String confirmPass = txtConfPass.getText();

        if (username.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            AlertUtil.showError("All fields must be filled!");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            AlertUtil.showError("Passwords do not match!");
            return;
        }

        if (newPass.length() < 10) {
            AlertUtil.showError("Password must be at least 10 characters long.");
            return;
        }
        if (!newPass.matches(".*[A-Z].*")) {
            AlertUtil.showError("Password must contain at least one uppercase letter.");
            return;
        }
        if (!newPass.matches(".*\\d.*")) {
            AlertUtil.showError("Password must contain at least one number.");
            return;
        }
        if (!newPass.matches(".*[^a-zA-Z0-9].*")) {
            AlertUtil.showError("Password must contain at least one special character.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String query = "UPDATE users SET password = ? WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, newPass);
            stmt.setString(2, username);

            int rows = stmt.executeUpdate();

            if (rows > 0) {
                AlertUtil.showInfo("Password changed successfully!");
                SceneSwitcher.switchToLoginForm((Stage) btnChangePass.getScene().getWindow());
            } else {
                AlertUtil.showError("Username not found.");
            }

        } catch (SQLException e) {
            AlertUtil.showError( "Database Error" + e.getMessage());
        }
    }
}
