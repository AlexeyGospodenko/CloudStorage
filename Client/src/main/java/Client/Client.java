package Client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Client extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        Parent clientWindow = FXMLLoader.load(Client.class.getResource("fxml/Client.fxml"));
        stage.setTitle("Cloud Storage");
        Scene scene = new Scene(clientWindow);
        stage.setScene(scene);
        stage.show();
    }

}
