package Client;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class ClientController implements Initializable {
    public Button btnUpload;
    public Button btnDownload;
    public Button btnDelete;
    public TextField txtFile;
    public Label lblStatus;
    public ListView<String> lstFiles;

    private Socket socket;
    private DataOutputStream os;
    private DataInputStream is;

    private String username = "testuser";
    private final static String ROOT_FOLDER = "Client" + File.separator;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            socket = new Socket("localhost", 1234);
            os = new DataOutputStream(socket.getOutputStream());
            is = new DataInputStream(socket.getInputStream());
            getFileList();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void uploadFile(ActionEvent actionEvent) {
        try {
            File file = new File("Client" + File.separator + username + File.separator + txtFile.getText());
            if (file.exists()) {
                os.writeUTF("upload");
                os.writeUTF(txtFile.getText());
                long length = file.length();
                os.writeLong(length);
                FileInputStream fis = new FileInputStream(file);
                int read;
                byte[] buffer = new byte[256];
                while ((read = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
                fis.close();
                String status = is.readUTF();
                lblStatus.setText(status);
                getFileList();
            } else {
                lblStatus.setText("File is not exists");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void downloadFile(ActionEvent actionEvent) {
        try {
            os.writeUTF("download");
            os.writeUTF(lstFiles.getSelectionModel().getSelectedItem().toString());
            String fileName = is.readUTF();
            File file = new File(ROOT_FOLDER + File.separator + username + File.separator + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            long size = is.readLong();
            FileOutputStream fos = new FileOutputStream(file);
            byte[] buffer = new byte[256];
            for (int i = 0; i < (size + 255) / 256; i++) {
                int read = is.read(buffer);
                fos.write(buffer, 0, read);
            }
            fos.close();
            lblStatus.setText("File " + fileName + " was downloaded");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteFile(ActionEvent actionEvent) {
        try {
            os.writeUTF("delete");
            os.writeUTF(lstFiles.getSelectionModel().getSelectedItem().toString());
            String status = is.readUTF();
            lblStatus.setText(status);
            getFileList();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getFileList() {
        try {
            os.writeUTF("list");
            os.writeUTF(username);
            String[] listFile = is.readUTF().split(";");
            ObservableList<String> observableList = FXCollections.observableArrayList(listFile);
            lstFiles.setItems(observableList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
