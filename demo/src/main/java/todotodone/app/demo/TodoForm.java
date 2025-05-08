package todotodone.app.demo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class TodoForm {

    @FXML
    private Button btnAdd;

    @FXML
    private Button btnCancel;

    @FXML
    private Button btnUploadFile;

    @FXML
    private ComboBox<?> cbCategory;

    @FXML
    private DatePicker dtpDueDate;

    @FXML
    private Label lblDeksripsi;

    @FXML
    private Label lblJudul;

    @FXML
    private Label lblKategori;

    @FXML
    private Label lblUpload;

    @FXML
    private Label lblWaktu;

    @FXML
    private Label lblWaktu1;

    @FXML
    private TextArea txtDescription;

    @FXML
    private TextField txtTitle;

    @FXML
    void onBtnAddClick(ActionEvent event) {

    }

    @FXML
    void onBtnCancelClick(ActionEvent event) {

    }

    @FXML
    void onBtnUploadFileClick(ActionEvent event) {

    }

}
