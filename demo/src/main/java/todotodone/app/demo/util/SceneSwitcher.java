package todotodone.app.demo.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import todotodone.app.demo.*;

import java.io.IOException;

public class SceneSwitcher {
    //untuk pindah ke tampilan login (login.fxml) dan beri judul jendela "Login"
    public static void switchToLoginForm(Stage currentStage) {
        switchScene(currentStage, "/todotodone/app/demo/login.fxml", "Login");
    }
    // untuk pindah ke tampilan registrasi (registration.fxml) dan beri judul jendela "Register"
    public static void switchToRegistrationForm(Stage currentStage) {
        switchScene(currentStage, "/todotodone/app/demo/registration.fxml", "Register");
    }
    //untuk pindah ke tampilan ubah password (changePassword.fxml) dan beri judul jendela "Change Password"
    public static void switchToChangePasswordForm(Stage currentStage) {
        switchScene(currentStage, "/todotodone/app/demo/changePassword.fxml", "Change Password");
    }
    //pindah ke tampilan ubah password untuk pengguna yang sudah login, sambil meneruskan data username dan userId
    public static void switchToChangePasswordFormLoggedIn(Stage currentStage, String username, Integer userId) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneSwitcher.class.getResource("/todotodone/app/demo/changePassword.fxml"));
            Parent root = loader.load();

            ChangePassword controller = loader.getController();
            controller.initializeForLoggedInUser(username, userId);

            Scene scene = new Scene(root);
            currentStage.setScene(scene);
            currentStage.setTitle("Change Password");
            currentStage.centerOnScreen();
            currentStage.show();
        } catch (IOException e) {
            System.err.println("Failed to load Change Password scene.");
            e.printStackTrace();
        }
    }

    // pindah ke tampilan Home sambil mengirim username dan userId ke controller Home
    public static void switchToHomeForm(Stage currentStage, String username, int userId) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneSwitcher.class.getResource("/todotodone/app/demo/home.fxml"));
            loader.setControllerFactory(param -> {
                if (param == Home.class) {
                    return new Home(username, userId);
                }
                try {
                    return param.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            Parent root = loader.load();
            Scene scene = new Scene(root);
            currentStage.setScene(scene);
            currentStage.setTitle("To Do To Done - Dashboard");
            currentStage.centerOnScreen();
            currentStage.show();
        } catch (IOException e) {
            System.err.println("Failed to load Home scene.");
            e.printStackTrace();
        }
    }

    // nampilin popup form untuk nambah atau edit to-do item milik user
    public static void popTodoForm(Stage ownerStage, int userId, Home.TodoItem todoToEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneSwitcher.class.getResource("/todotodone/app/demo/todoForm.fxml"));
            loader.setControllerFactory(param -> {
                if (param == TodoForm.class) {
                    return new TodoForm(userId);
                }
                try {
                    return param.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            Parent root = loader.load();
            TodoForm controller = loader.getController();

            if (todoToEdit != null) {
                controller.setTodoData(todoToEdit); // set data if editing
            }

            Stage popupStage = new Stage();
            popupStage.setScene(new Scene(root));
            popupStage.setTitle(todoToEdit != null ? "Edit To-Do" : "Add New To-Do");
            popupStage.initOwner(ownerStage);
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.centerOnScreen();
            popupStage.showAndWait();

        } catch (IOException e) {
            System.err.println("Failed to load To-Do Form.");
            e.printStackTrace();
        }
    }

    // nampilin popup form untuk mengelola kategori, dengan membawa userId ke controller
    public static void popCategoryForm(Stage ownerStage, int userId) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneSwitcher.class.getResource("/todotodone/app/demo/categoryForm.fxml"));
            loader.setControllerFactory(param -> {
                if (param == CategoryForm.class) {
                    return new CategoryForm(userId);
                }
                try {
                    return param.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            Parent root = loader.load();
            Stage popupStage = new Stage();
            popupStage.setScene(new Scene(root));
            popupStage.setTitle("Manage Categories");
            popupStage.initOwner(ownerStage);
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.centerOnScreen();
            popupStage.showAndWait();
        } catch (IOException e) {
            System.err.println("Failed to load Category Form.");
            e.printStackTrace();
        }
    }

    //nampilin popup form profil dan mengirimkan username serta userId ke controller Profile
    public static Stage popProfileForm(Stage ownerStage, String username, Integer userId) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneSwitcher.class.getResource("/todotodone/app/demo/profile.fxml"));
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof Profile) {
                ((Profile) controller).initializeWithUsername(username, userId);
            }

            Stage popupStage = new Stage();
            popupStage.setScene(new Scene(root));
            popupStage.setTitle("Manage Profile");
            popupStage.initOwner(ownerStage);
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.centerOnScreen();
            popupStage.show();

            return popupStage;
        } catch (IOException e) {
            System.err.println("Failed to load Profile Form.");
            e.printStackTrace();
            return null;
        }
    }


    //untuk mengganti tampilan utama (scene) aplikasi dengan tampilan lain
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

    //
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
