package todotodone.app.demo;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneSwitcher {

    public static void switchToLoginForm(Stage currentStage) {
        switchScene(currentStage, "/todotodone/app/demo/login.fxml");
    }

    public static void switchToRegistrationForm(Stage currentStage) {
        switchScene(currentStage, "/todotodone/app/demo/registration.fxml");
    }

    public static void switchToChangePasswordForm(Stage currentStage) {
        switchScene(currentStage, "/todotodone/app/demo/changePassword.fxml");
    }

    public static void switchToHomeForm(Stage currentStage, String username) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneSwitcher.class.getResource("/todotodone/app/demo/home.fxml"));
            Parent root = loader.load();

            // Access the Label directly from the home.fxml
            Label lblUsername = (Label) root.lookup("#lblUsername");
            if (lblUsername != null) {
                lblUsername.setText("Welcome, " + username);  // Set the username
            }

            Scene scene = new Scene(root);
            currentStage.setScene(scene);
            currentStage.setTitle("To Do To Done - Dashboard");
            currentStage.centerOnScreen();
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void popTodoForm(Stage currentStage) {
        switchScene(currentStage, "/todotodone/app/demo/todoForm.fxml");
    }
    public static void popcategoryForm(Stage currentStage) {
        switchScene(currentStage, "/todotodone/app/demo/categoryForm.fxml");
    }

    public static void switchScene(Stage currentStage, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneSwitcher.class.getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            currentStage.setScene(scene);
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void showPopUp(Stage ownerStage, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneSwitcher.class.getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            Stage popupStage = new Stage();
            popupStage.setTitle(title);
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initOwner(ownerStage);
            popupStage.setScene(scene);
            popupStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
