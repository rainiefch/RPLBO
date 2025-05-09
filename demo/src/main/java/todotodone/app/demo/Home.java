package todotodone.app.demo;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class Home {

    @FXML
    private HBox btnAdd;

    @FXML
    private HBox btnCategory;

    @FXML
    private HBox btnHome;

    @FXML
    private HBox btnProfile;

    @FXML
    private ComboBox<?> cbstatus1;

    @FXML
    private Label lblHome;

    @FXML
    void onBtnAddClick(MouseEvent event) {
        SceneSwitcher.popTodoForm(getStage());
    }

    @FXML
    void onBtnCategoryClick(MouseEvent event) {
        SceneSwitcher.popCategoryForm(getStage());
    }

    @FXML
    void onBtnHomeClick(MouseEvent event) {
        // Optional: Refresh home content or reload dashboard
    }

    @FXML
    void onBtnProfileClick(MouseEvent event) {
        // Optional: Implement profile form later
    }

    @FXML
    void onHomeClick(MouseEvent event) {
        // Optional: Treat label click as home navigation
    }

    private Stage getStage() {
        return (Stage) btnAdd.getScene().getWindow();
    }
}
