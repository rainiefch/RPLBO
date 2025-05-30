package todotodone.app.demo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import todotodone.app.demo.util.AlertUtil;
import todotodone.app.demo.util.DBConnection;
import todotodone.app.demo.util.SceneSwitcher;
import javafx.scene.input.MouseEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ChangePassword {

    @FXML private Button btnCancel, btnChangePass;

    @FXML private PasswordField txtNewPass, txtConfPass;
    @FXML private TextField tfNewPassVisible, tfConfirmPassVisible;

    @FXML private TextField txtUsername;

    @FXML private ImageView imgEye, imgEye1;

    private boolean isNewPassVisible = false;
    private boolean isConfPassVisible = false;

    private String username;
    private Integer userId;

    public ChangePassword() {}

    public void initializeForLoggedInUser(String username, Integer userId) {
        this.username = username;
        this.userId = userId;
        txtUsername.setText(username);
        txtUsername.setDisable(true);

        tfNewPassVisible.setVisible(false);
        tfNewPassVisible.setManaged(false);
        tfConfirmPassVisible.setVisible(false);
        tfConfirmPassVisible.setManaged(false);
    }

    @FXML
    void onBtnCancelClick(ActionEvent event) {
        if (username != null) {
            Stage stage = (Stage) btnCancel.getScene().getWindow();
            SceneSwitcher.switchToHomeForm(stage, username, userId);
        } else {
            SceneSwitcher.switchToLoginForm((Stage) btnCancel.getScene().getWindow());
        }
    }

    @FXML
    void onBtnChangePassClick(ActionEvent event) {
        String newPass = isNewPassVisible ? tfNewPassVisible.getText() : txtNewPass.getText();
        String confirmPass = isConfPassVisible ? tfConfirmPassVisible.getText() : txtConfPass.getText();

        if (username.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            AlertUtil.showError("All fields must be filled!");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            AlertUtil.showError("Passwords do not match!");
            return;
        }

        if (newPass.length() < 10 || !newPass.matches(".*[A-Z].*") ||
                !newPass.matches(".*\\d.*") || !newPass.matches(".*[^a-zA-Z0-9].*")) {
            AlertUtil.showError("Password must be at least 10 characters long, contain uppercase, number, and symbol.");
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
            AlertUtil.showError("Database Error: " + e.getMessage());
        }
    }

    @FXML
    void onEyeClick(MouseEvent event) {
        isNewPassVisible = !isNewPassVisible;

        if (isNewPassVisible) {
            tfNewPassVisible.setText(txtNewPass.getText());
            tfNewPassVisible.setVisible(true);
            tfNewPassVisible.setManaged(true);
            txtNewPass.setVisible(false);
            txtNewPass.setManaged(false);
            imgEye.setImage(new Image(getClass().getResourceAsStream("/todotodone/app/demo/imgs/hide.png")));
        } else {
            txtNewPass.setText(tfNewPassVisible.getText());
            txtNewPass.setVisible(true);
            txtNewPass.setManaged(true);
            tfNewPassVisible.setVisible(false);
            tfNewPassVisible.setManaged(false);
            imgEye.setImage(new Image(getClass().getResourceAsStream("/todotodone/app/demo/imgs/view.png")));
        }
    }

    @FXML
    void onEyeClick1(MouseEvent event) {
        isConfPassVisible = !isConfPassVisible;

        if (isConfPassVisible) {
            tfConfirmPassVisible.setText(txtConfPass.getText());
            tfConfirmPassVisible.setVisible(true);
            tfConfirmPassVisible.setManaged(true);
            txtConfPass.setVisible(false);
            txtConfPass.setManaged(false);
            imgEye1.setImage(new Image(getClass().getResourceAsStream("/todotodone/app/demo/imgs/hide.png")));
        } else {
            txtConfPass.setText(tfConfirmPassVisible.getText());
            txtConfPass.setVisible(true);
            txtConfPass.setManaged(true);
            tfConfirmPassVisible.setVisible(false);
            tfConfirmPassVisible.setManaged(false);
            imgEye1.setImage(new Image(getClass().getResourceAsStream("/todotodone/app/demo/imgs/view.png")));
        }
    }
}
