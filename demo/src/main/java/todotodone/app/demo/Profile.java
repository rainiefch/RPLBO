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
    private Integer userId;

    public void initializeWithUsername(String username, Integer userId) {
        this.username = username;
        this.userId = userId;
        if (lblHello != null) {
            lblHello.setText("Hello, " + username + "!");
        }
    }

    @FXML
    void onBtnChangePass(ActionEvent event) {
        Stage stage = (Stage) btnChangePass.getScene().getWindow();
        SceneSwitcher.switchToChangePasswordFormLoggedIn(stage, username, userId);
    }

    @FXML
    void onBtnLogout(ActionEvent event) {
        Platform.exit();
    }
}
