package Client;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

public class DeleteAccountController {
    public AnchorPane window;
    public Button btnYes;
    public Button btnNo;

    private static boolean isDelete = false;
    private static String password = null;
    public TextField txtPassword;

    public void EnterDeleteAccount(ActionEvent actionEvent) {
        isDelete = true;
        password = txtPassword.getText();
        window.getScene().getWindow().hide();
    }

    public void CancelDeleteAccount(ActionEvent actionEvent) {
        isDelete = false;
        password = null;
        window.getScene().getWindow().hide();
    }

    public static boolean isDelete() {
        return isDelete;
    }

    public static String getPassword() {
        return password;
    }
}
