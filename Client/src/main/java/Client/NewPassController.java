package Client;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

public class NewPassController {
    public AnchorPane window;
    public Button btnOk;
    public Button btnCancel;
    public TextField txtPassword;

    private static boolean isChange;
    private static String password;

    public void EnterNewPassword(ActionEvent actionEvent) {
        password = txtPassword.getText();
        isChange = true;
        window.getScene().getWindow().hide();
    }

    public void CancelNewPassword(ActionEvent actionEvent) {
        isChange = false;
        window.getScene().getWindow().hide();
    }

    public static String getPassword() {
        return password;
    }

    public static boolean isChange() {
        return isChange;
    }
}
