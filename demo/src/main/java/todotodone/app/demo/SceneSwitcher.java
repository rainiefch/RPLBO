package todotodone.app.demo;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class SceneSwitcher {


    public static void switchToLoginForm(Stage currentStage) {
        switchScene(currentStage, "/todotodone/app/demo/login.fxml", "Login");
    }

    public static void switchToRegistrationForm(Stage currentStage) {
        switchScene(currentStage, "/todotodone/app/demo/registration.fxml", "Register");
    }

    public static void switchToChangePasswordForm(Stage currentStage) {
        switchScene(currentStage, "/todotodone/app/demo/changePassword.fxml", "Change Password");
    }

    public static void switchToHomeForm(Stage currentStage, String username) {

        try {
            FXMLLoader loader = new FXMLLoader(SceneSwitcher.class.getResource("/todotodone/app/demo/home.fxml"));
            loader.setControllerFactory(param -> {
                if (param == Home.class) {
                    return new Home(username); // username langsung diset saat controller dibuat
                }
                try {
                    return param.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            Parent root = loader.load(); // sekarang initialize() akan punya username!

            Label lblUsername = (Label) root.lookup("#lblUsername");
            if (lblUsername != null) {
                lblUsername.setText("Welcome, " + username);
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


    public static void popTodoForm(Stage ownerStage) {
        showPopup(ownerStage, "/todotodone/app/demo/todoForm.fxml", "Add New To-Do");
    }

    public static void popCategoryForm(Stage ownerStage) {
        showPopup(ownerStage, "/todotodone/app/demo/categoryForm.fxml", "Manage Categories");
    }

    public static void popProfileForm(Stage ownerStage) {
        System.out.println("cobadah");
        showPopup(ownerStage, "/todotodone/app/demo/profile.fxml", "Manage Profile");
    }


    private static void switchScene(Stage stage, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneSwitcher.class.getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle(title);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            System.err.println("Failed to load scene: " + fxmlPath);
            e.printStackTrace();
        }
    }

    private static void showPopup(Stage owner, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneSwitcher.class.getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            Stage popupStage = new Stage();
            popupStage.setScene(scene);
            popupStage.setTitle(title);
            popupStage.initOwner(owner);
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.centerOnScreen();
            popupStage.showAndWait();
        } catch (IOException e) {
            System.err.println("Failed to load popup: " + fxmlPath);
            e.printStackTrace();
        }
    }
}
