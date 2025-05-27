package todotodone.app.demo;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import todotodone.app.demo.util.SceneSwitcher;

public class Profile {

    @FXML
    private Button btnChangePass;

    @FXML
    private Button btnLogout;

    @FXML
    private Label lblHello;

    private String username;

    // Method to be called from SceneSwitcher
    public void initializeWithUsername(String username) {
        this.username = username;
        if (lblHello != null) {
            lblHello.setText("Hello, " + username + "!");
        }
    }

    @FXML
    void onBtnChangePass(ActionEvent event) {
        Stage stage = (Stage) btnChangePass.getScene().getWindow();
        SceneSwitcher.switchToChangePasswordFormLoggedIn(stage, username);
    }

    @FXML
    void onBtnLogout(ActionEvent event) {
        Platform.exit();
    }
}
