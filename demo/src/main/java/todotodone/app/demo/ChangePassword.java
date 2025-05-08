package todotodone.app.demo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ChangePassword {

    @FXML
    private Button btnCancel, btnChangePass;

    @FXML
    private PasswordField txtConfPass, txtNewPass;

    @FXML
    private TextField txtUsername;

    @FXML
    void onBtnCancelClick(ActionEvent event) {
        txtUsername.clear();
        txtNewPass.clear();
        txtConfPass.clear();
    }

    @FXML
    void onBtnChangePassClick(ActionEvent event) {
        String username = txtUsername.getText();
        String newPass = txtNewPass.getText();
        String confirmPass = txtConfPass.getText();

        if (username.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "All fields must be filled!");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            showAlert(Alert.AlertType.ERROR, "Error", "Passwords do not match!");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String query = "UPDATE users SET password = ? WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, newPass);
            stmt.setString(2, username);

            int rows = stmt.executeUpdate();

            if (rows > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Password changed successfully!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Username not found.");
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
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
