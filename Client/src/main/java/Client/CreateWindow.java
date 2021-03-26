package Client;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class CreateWindow {
    public CreateWindow(String fxml, String title, boolean resizable) throws IOException {
        Parent auth = FXMLLoader.load(CreateWindow.class.getResource(fxml));
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.setScene(new Scene(auth));
        stage.setResizable(resizable);
        stage.show();
    }
}
