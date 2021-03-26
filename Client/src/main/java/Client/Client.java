package Client;

import javafx.application.Application;
import javafx.stage.Stage;
import java.io.IOException;

public class Client extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        new CreateWindow("fxml/Auth.fxml", "CloudStorage - Authorization", false);
    }

}
