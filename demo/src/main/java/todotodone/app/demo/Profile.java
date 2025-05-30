package todotodone.app.demo;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.stage.Window;
import todotodone.app.demo.util.SceneSwitcher;

public class Profile {

    private String username;
    private Integer userId;

    @FXML
    private Button btnChangePass, btnLogout;

    @FXML
    private Label lblHello;

    public void initializeWithUsername(String username, Integer userId) {
        this.username = username;
        this.userId = userId;
        lblHello.setText("Hello, " + username + "!");
    }

    @FXML
    void onBtnChangePass(ActionEvent event) {
        Stage stage = (Stage) btnChangePass.getScene().getWindow();
        SceneSwitcher.switchToChangePasswordFormLoggedIn(stage, username, userId);
    }

    @FXML
    void onBtnLogout(ActionEvent event) {

        Platform.runLater(() -> {

            Stage loginStage = new Stage();
            SceneSwitcher.switchToLoginForm(loginStage);

            for (Window window : Window.getWindows()) {
                if (window instanceof Stage stage && stage != loginStage) {
                    stage.close();
                }
            }
        });
    }
}
