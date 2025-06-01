package todotodone.app.demo.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import todotodone.app.demo.*;

import java.io.IOException;

public class SceneSwitcher {

    // untuk mengubah tampilan (scene) aplikasi ke form Login.
    // Dipanggil saat pengguna ingin masuk/login.
    public static void switchToLoginForm(Stage currentStage) {
        switchScene(currentStage, "/todotodone/app/demo/login.fxml", "Login");
    }

    // untuk mengubah tampilan aplikasi ke form Registrasi.
    // Dipanggil saat pengguna ingin mendaftar akun baru.
    public static void switchToRegistrationForm(Stage currentStage) {
        switchScene(currentStage, "/todotodone/app/demo/registration.fxml", "Register");
    }

    //mengubah tampilan aplikasi ke form Ganti Password.
    // Digunakan jika pengguna ngin mengganti kata sandi.
    public static void switchToChangePasswordForm(Stage currentStage) {
        switchScene(currentStage, "/todotodone/app/demo/changePassword.fxml", "Change Password");
    }

    //mengubah tampilan aplikasi ke form "Change Password" untuk pengguna yang sudah login.
    public static void switchToChangePasswordFormLoggedIn(Stage currentStage, String username) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneSwitcher.class.getResource("/todotodone/app/demo/changePassword.fxml"));
            Parent root = loader.load();

            ChangePassword controller = loader.getController();
            controller.initializeForLoggedInUser(username); // manually configure the form

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

    //mengubah tampilan aplikasi ke form "Home" setelah pengguna login.
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

    // ✅ Pop TodoForm dengan userId
    public static void popTodoForm(Stage ownerStage, int userId) {
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
            Stage popupStage = new Stage();
            popupStage.setScene(new Scene(root));
            popupStage.setTitle("Add New To-Do");
            popupStage.initOwner(ownerStage);
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.centerOnScreen();
            popupStage.showAndWait();
        } catch (IOException e) {
            System.err.println("Failed to load To-Do Form.");
            e.printStackTrace();
        }
    }

    // ✅ Pop CategoryForm dengan userId
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

    // Menampilkan popup form profil berdasarkan username pengguna
    public static void popProfileForm(Stage ownerStage, String username) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneSwitcher.class.getResource("/todotodone/app/demo/profile.fxml"));
            Parent root = loader.load();

            // Get the controller and set the username
            Object controller = loader.getController();
            if (controller instanceof Profile) {
                ((Profile) controller).initializeWithUsername(username);
            }

            Stage popupStage = new Stage();
            popupStage.setScene(new Scene(root));
            popupStage.setTitle("Manage Profile");
            popupStage.initOwner(ownerStage);
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.centerOnScreen();
            popupStage.showAndWait();
        } catch (IOException e) {
            System.err.println("Failed to load Profile Form.");
            e.printStackTrace();
        }
    }

    // Fungsi untuk berpindah tampilan (scene) ke halaman baru berdasarkan file FXML yang diberikan
    private static void switchScene(Stage stage, String fxmlPath, String title) {
        try {//
            FXMLLoader loader = new FXMLLoader(SceneSwitcher.class.getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle(title);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) { //
            System.err.println("Failed to load scene: " + fxmlPath);
            e.printStackTrace();
        }
    }

    //
    private static void showPopup(Stage owner, String fxmlPath, String title) {
        try { //
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
        } catch (IOException e) { //
            System.err.println("Failed to load popup: " + fxmlPath);
            e.printStackTrace();
        }
    }
}
