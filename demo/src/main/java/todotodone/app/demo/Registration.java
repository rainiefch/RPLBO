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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Registration {

    @FXML private Button btnRegister, btnSignIn;

    @FXML private PasswordField pfPassword, pfConfirmPassword;
    @FXML private TextField tfUsername, tfPassVisible, tfConfirmPassVisible;

    @FXML private ImageView picture, imgEye, imgEye1;

    private boolean isPassVisible = false;
    private boolean isConfirmVisible = false;

    // ubah cursos ke text field
    @FXML
    void initialize() {
        tfPassVisible.setVisible(false);
        tfPassVisible.setManaged(false);
        tfConfirmPassVisible.setVisible(false);
        tfConfirmPassVisible.setManaged(false);
        tfUsername.requestFocus();

        tfUsername.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                if (isPassVisible) {
                    tfPassVisible.requestFocus();
                    tfPassVisible.positionCaret(tfPassVisible.getText().length());
                } else {
                    pfPassword.requestFocus();
                    pfPassword.positionCaret(pfPassword.getText().length());
                }
            }
        });

        pfPassword.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                if (isConfirmVisible) {
                    tfConfirmPassVisible.requestFocus();
                    tfConfirmPassVisible.positionCaret(tfConfirmPassVisible.getText().length());
                } else {
                    pfConfirmPassword.requestFocus();
                    pfConfirmPassword.positionCaret(pfConfirmPassword.getText().length());
                }
            }
        });

        tfPassVisible.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                if (isConfirmVisible) {
                    tfConfirmPassVisible.requestFocus();
                    tfConfirmPassVisible.positionCaret(tfConfirmPassVisible.getText().length());
                } else {
                    pfConfirmPassword.requestFocus();
                    pfConfirmPassword.positionCaret(pfConfirmPassword.getText().length());
                }
            }
        });

        pfConfirmPassword.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                onBtnRegisterClick(new ActionEvent());
            }
        });

        tfConfirmPassVisible.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                onBtnRegisterClick(new ActionEvent());
            }
        });
    }

    // register
    @FXML
    void onBtnRegisterClick(ActionEvent event) {
        String username = tfUsername.getText().trim();
        String password = isPassVisible ? tfPassVisible.getText() : pfPassword.getText();
        String confirmPassword = isConfirmVisible ? tfConfirmPassVisible.getText() : pfConfirmPassword.getText();

        //username ga boleh lebih dr 20
        if (username.length() > 20) {
            AlertUtil.showError("Username must be 20 characters or less.");
            return;
        }

        //harus isi semua
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            AlertUtil.showError("All fields must be filled!");
            return;
        }

        //semua konstrain pw jadi 1
        if (password.length() < 10 || !password.matches(".*[A-Z].*") ||
                !password.matches(".*\\d.*") || !password.matches(".*[^a-zA-Z0-9].*")) {
            AlertUtil.showError("Password must be at least 10 characters long, contain uppercase, number,and symbol.");
            return;
        }

        // pw sama confirmnya harus sama
        if (!password.equals(confirmPassword)) {
            AlertUtil.showError("Passwords do not match!");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String checkQuery = "SELECT COUNT(*) FROM users WHERE username = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setString(1, username);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next() && rs.getInt(1) > 0) { //gaboleh sama usernamenya
                    AlertUtil.showError("Username already exists.");
                    return;
                }
            }

            // kalo ga sama baru masukin ke d
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
            AlertUtil.showError("Database Error: " + e.getMessage());
        }
    }

    // ngubah ke window sign in/login
    @FXML
    void onBtnSignInClick(ActionEvent event) {
        SceneSwitcher.switchToLoginForm((Stage) btnSignIn.getScene().getWindow());
    }

    /// kode visible password
    @FXML
    void onEyeClick(javafx.scene.input.MouseEvent event) {
        isPassVisible = !isPassVisible;

        if (isPassVisible) {
            tfPassVisible.setText(pfPassword.getText());
            tfPassVisible.setVisible(true);
            tfPassVisible.setManaged(true);
            pfPassword.setVisible(false);
            pfPassword.setManaged(false);
            imgEye.setImage(new Image(getClass().getResourceAsStream("/todotodone/app/demo/imgs/hide.png")));
            tfPassVisible.requestFocus();
            tfPassVisible.positionCaret(tfPassVisible.getText().length());
        } else {
            pfPassword.setText(tfPassVisible.getText());
            pfPassword.setVisible(true);
            pfPassword.setManaged(true);
            tfPassVisible.setVisible(false);
            tfPassVisible.setManaged(false);
            imgEye.setImage(new Image(getClass().getResourceAsStream("/todotodone/app/demo/imgs/view.png")));
            pfPassword.requestFocus();
            pfPassword.positionCaret(pfPassword.getText().length());
        }
    }

    // ubah visible change password
    @FXML
    void onEyeClick1(javafx.scene.input.MouseEvent event) {
        isConfirmVisible = !isConfirmVisible;

        if (isConfirmVisible) {
            tfConfirmPassVisible.setText(pfConfirmPassword.getText());
            tfConfirmPassVisible.setVisible(true);
            tfConfirmPassVisible.setManaged(true);
            pfConfirmPassword.setVisible(false);
            pfConfirmPassword.setManaged(false);
            imgEye1.setImage(new Image(getClass().getResourceAsStream("/todotodone/app/demo/imgs/hide.png")));
            tfConfirmPassVisible.requestFocus();
            tfConfirmPassVisible.positionCaret(tfConfirmPassVisible.getText().length());

        } else {
            pfConfirmPassword.setText(tfConfirmPassVisible.getText());
            pfConfirmPassword.setVisible(true);
            pfConfirmPassword.setManaged(true);
            tfConfirmPassVisible.setVisible(false);
            tfConfirmPassVisible.setManaged(false);
            imgEye1.setImage(new Image(getClass().getResourceAsStream("/todotodone/app/demo/imgs/view.png")));
            pfConfirmPassword.requestFocus();
            pfConfirmPassword.positionCaret(pfConfirmPassword.getText().length());
        }
    }
}
