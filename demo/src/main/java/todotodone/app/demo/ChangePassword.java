package todotodone.app.demo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import todotodone.app.demo.util.AlertUtil;
import todotodone.app.demo.util.DBConnection;
import todotodone.app.demo.util.SceneSwitcher;
import javafx.scene.input.MouseEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

// ini bisa diakses dengan 2 cara
// 1. dr login -> kosongan, a ada data username sm iduser. jadi username textfieldnya kosongg
// 2. setelah login, dari homepage profile. ada data id sm username, jadi nanti tf usernamenya ada isinya dan ga digbibsa digaanti
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

    // yg dr login
    public ChangePassword() {}

    // yg dr home page
    public void initializeForLoggedInUser(String username, Integer userId) {
        this.username = username;
        this.userId = userId;
        txtUsername.setText(username);
        txtUsername.setDisable(true);

        tfNewPassVisible.setVisible(false);
        tfNewPassVisible.setManaged(false);
        tfConfirmPassVisible.setVisible(false);
        tfConfirmPassVisible.setManaged(false);
        txtUsername.requestFocus();

        txtUsername.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                if (isNewPassVisible) {
                    tfNewPassVisible.requestFocus();
                    tfNewPassVisible.positionCaret(tfNewPassVisible.getText().length());
                } else {
                    txtNewPass.requestFocus();
                    txtNewPass.positionCaret(txtNewPass.getText().length());
                }
            }
        });

        txtNewPass.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                if (isConfPassVisible) {
                    tfConfirmPassVisible.requestFocus();
                    tfConfirmPassVisible.positionCaret(tfConfirmPassVisible.getText().length());
                } else {
                    txtConfPass.requestFocus();
                    txtConfPass.positionCaret(txtConfPass.getText().length());
                }
            }
        });

        tfNewPassVisible.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                if (isConfPassVisible) {
                    tfConfirmPassVisible.requestFocus();
                    tfConfirmPassVisible.positionCaret(tfConfirmPassVisible.getText().length());
                } else {
                    txtConfPass.requestFocus();
                    txtConfPass.positionCaret(txtConfPass.getText().length());
                }
            }
        });

        txtConfPass.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                onBtnChangePassClick(new ActionEvent());
            }
        });

        tfConfirmPassVisible.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                onBtnChangePassClick(new ActionEvent());
            }
        });
    }

    @FXML
    void initialize() {
        tfNewPassVisible.setVisible(false);
        tfNewPassVisible.setManaged(false);
        tfConfirmPassVisible.setVisible(false);
        tfConfirmPassVisible.setManaged(false);
        txtUsername.requestFocus();

        txtUsername.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                if (isNewPassVisible) {
                    tfNewPassVisible.requestFocus();
                    tfNewPassVisible.positionCaret(tfNewPassVisible.getText().length());
                } else {
                    txtNewPass.requestFocus();
                    txtNewPass.positionCaret(txtNewPass.getText().length());
                }
            }
        });

        txtNewPass.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                if (isConfPassVisible) {
                    tfConfirmPassVisible.requestFocus();
                    tfConfirmPassVisible.positionCaret(tfConfirmPassVisible.getText().length());
                } else {
                    txtConfPass.requestFocus();
                    txtConfPass.positionCaret(txtConfPass.getText().length());
                }
            }
        });

        tfNewPassVisible.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                if (isConfPassVisible) {
                    tfConfirmPassVisible.requestFocus();
                    tfConfirmPassVisible.positionCaret(tfConfirmPassVisible.getText().length());
                } else {
                    txtConfPass.requestFocus();
                    txtConfPass.positionCaret(txtConfPass.getText().length());
                }
            }
        });

        txtConfPass.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                onBtnChangePassClick(new ActionEvent());
            }
        });

        tfConfirmPassVisible.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                onBtnChangePassClick(new ActionEvent());
            }
        });
    }

    @FXML
    void onBtnCancelClick(ActionEvent event) {
        if (username != null) { //kalo ga ada username, berarti dia di login, belum masuk
            Stage stage = (Stage) btnCancel.getScene().getWindow();
            SceneSwitcher.switchToHomeForm(stage, username, userId);
        } else { // dia udah login
            SceneSwitcher.switchToLoginForm((Stage) btnCancel.getScene().getWindow());
        }
    }

    @FXML
    void onBtnChangePassClick(ActionEvent event) {
        String inputUsername = txtUsername.getText().trim();
        String newPass = isNewPassVisible ? tfNewPassVisible.getText() : txtNewPass.getText();
        String confirmPass = isConfPassVisible ? tfConfirmPassVisible.getText() : txtConfPass.getText();

        if (inputUsername.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            AlertUtil.showError("All fields must be filled!");
            return;
        }

        if (newPass.length() < 10 || !newPass.matches(".*[A-Z].*") ||
                !newPass.matches(".*\\d.*") || !newPass.matches(".*[^a-zA-Z0-9].*")) {
            AlertUtil.showError("Password must be at least 10 characters long, contain uppercase, number, and symbol.");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            AlertUtil.showError("Passwords do not match!");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String query = "UPDATE users SET password = ? WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, newPass);
            stmt.setString(2, inputUsername);

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
            tfNewPassVisible.requestFocus();
            tfNewPassVisible.positionCaret(tfNewPassVisible.getText().length());
        } else {
            txtNewPass.setText(tfNewPassVisible.getText());
            txtNewPass.setVisible(true);
            txtNewPass.setManaged(true);
            tfNewPassVisible.setVisible(false);
            tfNewPassVisible.setManaged(false);
            imgEye.setImage(new Image(getClass().getResourceAsStream("/todotodone/app/demo/imgs/view.png")));
            txtNewPass.requestFocus();
            txtNewPass.positionCaret(txtNewPass.getText().length());
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
            tfConfirmPassVisible.requestFocus();
            tfConfirmPassVisible.positionCaret(tfConfirmPassVisible.getText().length());
        } else {
            txtConfPass.setText(tfConfirmPassVisible.getText());
            txtConfPass.setVisible(true);
            txtConfPass.setManaged(true);
            tfConfirmPassVisible.setVisible(false);
            tfConfirmPassVisible.setManaged(false);
            imgEye1.setImage(new Image(getClass().getResourceAsStream("/todotodone/app/demo/imgs/view.png")));
            txtConfPass.requestFocus();
            txtConfPass.positionCaret(txtConfPass.getText().length());
        }
    }
}
