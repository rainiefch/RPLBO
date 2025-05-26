package todotodone.app.demo;

import javafx.application.Application;
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
    private Label lblHello;

    @FXML
    private Button btnLogout;

    private String username;




    @FXML
    void onBtnChangePass(ActionEvent event) {
        SceneSwitcher.switchToChangePasswordFormLoggedIn((Stage) btnChangePass.getScene().getWindow());
        Stage stage = (Stage) btnChangePass.getScene().getWindow();
        SceneSwitcher.switchToChangePasswordFormLoggedIn(stage, username);
    }

    @FXML
    void onBtnLogout(ActionEvent event) {
        Application.Exit();
    }

}
