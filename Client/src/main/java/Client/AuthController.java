package Client;

import javafx.event.ActionEvent;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class AuthController {
    public AnchorPane window;
    public TextField txtLogin;
    public TextField txtPassword;
    private boolean isAuth = false;

    private final static String ACTION_FAIL = "-fx-border-radius: 5; -fx-border-color: red; -fx-background-insets: 0;";
    private final static String ACTION_SUCCESS = "-fx-border-radius: 5; -fx-border-color: lightgreen; -fx-background-insets: 0;";

    public void enter(ActionEvent actionEvent) throws IOException {
        //Инициализация сетевого подключения
        NetworkService.initInstance();
        NetworkService.getInstance().getOs().write(("-auth;" + txtLogin.getText() + ";" + txtPassword.getText()).getBytes());

        byte[] buffer = new byte[512];
        int readBytes = NetworkService.getInstance().getIs().read(buffer);
        String result = new String(buffer, 0, readBytes);
        System.out.println();
        if (result.equals("SUCCESS")) {
            txtLogin.setStyle(ACTION_SUCCESS);
            txtPassword.setStyle(ACTION_SUCCESS);
            isAuth = true;
        } else {
            txtLogin.setStyle(ACTION_FAIL);
            txtPassword.setStyle(ACTION_FAIL);
            txtPassword.clear();
        }

        if (isAuth) {
            NetworkService.getInstance().setUsername(txtLogin.getText());
            window.getScene().getWindow().hide();
            new CreateWindow("fxml/Client.fxml", "Cloud Storage", true);
        }

    }

    public void register(ActionEvent actionEvent) throws IOException {
        NetworkService.initInstance();
        new CreateWindow("fxml/Registration.fxml", "Cloud Storage", false);
        window.getScene().getWindow().hide();
    }
}
