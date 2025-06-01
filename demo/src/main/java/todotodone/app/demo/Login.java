package todotodone.app.demo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import todotodone.app.demo.util.AlertUtil;
import todotodone.app.demo.util.DBConnection;
import todotodone.app.demo.util.SceneSwitcher;

import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Login {

    private static String loggedInUsername = "";

    @FXML
    private TextField tfUsername, tfPasswordVisible;

    @FXML
    private PasswordField pfPassword;

    @FXML
    private Button btnSignIn, btnForgotPass, btnRegister;

    @FXML
    private ImageView picture, imgEye;

     //untuk cek pwnya lg visible atau tidak
    private boolean passwordVisible = false;

    //Untuk pindah antar text field username -> password -> klik sign in buttom
    @FXML
    public void initialize() {
        tfUsername.requestFocus();

        //Dari username ke password
        tfUsername.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                if (passwordVisible) { // Kalo pw visible
                    tfPasswordVisible.requestFocus();  // Pindah fokus
                    tfPasswordVisible.positionCaret(tfPasswordVisible.getText().length()); // Posisi caret di paling akhir
                } else { // kalo invisible
                    pfPassword.requestFocus();
                    pfPassword.positionCaret(pfPassword.getText().length());
                }
            }
        });

        // kalo visible trus enter -> klik isgn in btn
        pfPassword.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                onBtnSignInClick(new ActionEvent());
            }
        });
        // kalo invisible trus enter -> klik isgn in btn
        tfPasswordVisible.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                onBtnSignInClick(new ActionEvent());
            }
        });
    }

    @FXML
    void onBtnSignInClick(ActionEvent event) {
        String username = tfUsername.getText().trim();
        String password = passwordVisible ? tfPasswordVisible.getText().trim() : pfPassword.getText().trim();

        // konstrain text field tidak boleh kosong
        if (username.isEmpty() || password.isEmpty()) {
            AlertUtil.showError("Username and password cannot be empty.");
            return;
        }

        String query = "SELECT id_user, username FROM users WHERE username = ? AND password = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) { //coba dlu bisa ngga
                if (rs.next()) { // kalo isa
                    // id sm username buat dibawa ke home
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

    // untuk ubah visible password
    @FXML
    void onEyeClick(javafx.scene.input.MouseEvent event) {
        passwordVisible = !passwordVisible;

        if (passwordVisible) {
            tfPasswordVisible.setText(pfPassword.getText());
            tfPasswordVisible.setVisible(true);
            tfPasswordVisible.setManaged(true);
            pfPassword.setVisible(false);
            pfPassword.setManaged(false);
            imgEye.setImage(new javafx.scene.image.Image(getClass().getResourceAsStream("/todotodone/app/demo/imgs/hide.png")));
            tfPasswordVisible.requestFocus();
            tfPasswordVisible.positionCaret(tfPasswordVisible.getText().length());
        } else {
            pfPassword.setText(tfPasswordVisible.getText());
            pfPassword.setVisible(true);
            pfPassword.setManaged(true);
            tfPasswordVisible.setVisible(false);
            tfPasswordVisible.setManaged(false);
            imgEye.setImage(new javafx.scene.image.Image(getClass().getResourceAsStream("/todotodone/app/demo/imgs/view.png")));
            pfPassword.requestFocus();
            pfPassword.positionCaret(pfPassword.getText().length());
        }
    }
}
