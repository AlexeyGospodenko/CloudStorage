package Client;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

public class NewFolderController {
    public TextField txtFolderName;
    public Button btnOk;
    public Button btnCancel;
    public AnchorPane window;

    private static boolean isChange;
    private static String folderName;

    public void EnterFolderName(ActionEvent actionEvent) {
        folderName = txtFolderName.getText();
        isChange = true;
        window.getScene().getWindow().hide();
    }

    public void CancelFolderName(ActionEvent actionEvent) {
        isChange = false;
        window.getScene().getWindow().hide();
    }

    public static String getFolderName() {
        return folderName;
    }

    public static boolean isChange() {
        return isChange;
    }
}
