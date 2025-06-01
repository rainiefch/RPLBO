package todotodone.app.demo.util;

import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class AlertUtil {
    // notif info
    public static void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText("Yayy!");
        alert.setContentText(message);
        styleAlert(alert, "black");
        alert.showAndWait();
    }

    // notif alert
    public static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("An error occurred");
        alert.setContentText(message);
        styleAlert(alert, "red");
        alert.showAndWait();
    }
    // cuman untuk styling alert
    private static void styleAlert(Alert alert, String contentColor) {
        DialogPane dialogPane = alert.getDialogPane();

        dialogPane.setStyle("-fx-font-size: 14pt;");

        String headerColor = "red";
        if (alert.getAlertType() == Alert.AlertType.INFORMATION) {
            headerColor = "#3f6de0";
        }

        Text header = new Text(alert.getHeaderText());
        header.setStyle("-fx-fill: " + headerColor + "; -fx-font-weight: bold; -fx-font-size: 16pt;");

        Text content = new Text(alert.getContentText());
        content.setStyle("-fx-fill: " + contentColor + "; -fx-font-size: 14pt;");

        VBox contentBox = new VBox(10, header, content);
        contentBox.setPadding(new Insets(20));
        contentBox.setMinWidth(350);
        contentBox.setMinHeight(120);

        dialogPane.setContent(contentBox);
        alert.setHeaderText(null);
        alert.setContentText(null);
    }


}
