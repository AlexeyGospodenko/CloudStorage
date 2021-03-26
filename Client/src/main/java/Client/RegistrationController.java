package Client;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class RegistrationController {
    public AnchorPane window;
    public TextField txtLogin;
    public TextField txtPassword;
    public Button btnExit;
    private boolean checkLogin = false;

    private final static String ACTION_FAIL = "-fx-border-radius: 5; -fx-border-color: red; -fx-background-insets: 0;";
    private final static String ACTION_SUCCESS = "-fx-border-radius: 5; -fx-border-color: lightgreen; -fx-background-insets: 0;";

    public void register() throws Exception {
        if (txtLogin.getText().equals("") || txtPassword.getText().equals("")) {
            txtLogin.setStyle(ACTION_FAIL);
            txtPassword.setStyle(ACTION_FAIL);
            txtPassword.clear();
        } else {
            NetworkService.getInstance().getOs().write(("-register;" + txtLogin.getText() + ";" + txtPassword.getText())
                    .getBytes());
            txtPassword.setStyle(ACTION_SUCCESS);

            byte[] buffer = new byte[512];
            int readBytes = NetworkService.getInstance().getIs().read(buffer);
            String result = new String(buffer, 0, readBytes);
            if (result.equals("SUCCESS")) {
                txtLogin.setStyle(ACTION_SUCCESS);
                checkLogin = true;
            } else {
                txtLogin.setStyle(ACTION_FAIL);
                txtLogin.clear();
                checkLogin = false;
                txtLogin.setPromptText("Login is busy");
            }

        }

        if (checkLogin) {
            exit();
        }
    }

    public void exit() throws IOException {
        NetworkService.getInstance().destroyInstance();
        new CreateWindow("fxml/Auth.fxml", "CloudStorage - Authorization", false);
        window.getScene().getWindow().hide();
    }
}
