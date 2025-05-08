package todotodone.app.demo;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class Home {

    @FXML
    private Label welcomeLabel;

    public void initializeUser(String username) {
        welcomeLabel.setText("Welcome, " + username + "!");
    }
}
