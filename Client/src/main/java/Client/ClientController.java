package Client;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class ClientController implements Initializable {
    public Button btnUpload;
    public Button btnDownload;
    public Button btnDelete;
    public Label lblStatus;
    public ListView<String> lstServerFiles;
    public TableView tabLocalDir;
    public TableColumn tableColumnLocalDirName;
    public TableColumn tableColumnLocalDirType;

    private Socket socket;
    private DataOutputStream os;
    private DataInputStream is;

    private String username = "testuser";
    private String currLocalDir = "Client" + File.separator + username;
    private String currServerDir = "Client" + File.separator;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            socket = new Socket("localhost", 1234);
            os = new DataOutputStream(socket.getOutputStream());
            is = new DataInputStream(socket.getInputStream());

            //Заполнение таблицы файлов расположенных на локальной машине
            tableColumnLocalDirName.setCellValueFactory(new PropertyValueFactory<FileItem, String>("fileName"));
            tableColumnLocalDirType.setCellValueFactory(new PropertyValueFactory<FileItem, String>("fileType"));
            fillFileList(Paths.get(currLocalDir));

            //Заполнений таблицы файлов расположенных на сервере
            getFileList();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fillFileList(Path currDir) throws IOException {
        ObservableList<FileItem> observableList = FXCollections.observableArrayList(new FileItem("..", ""));
        Files.list(currDir).filter(isDir -> Files.isDirectory(isDir))
                .forEach(item -> observableList
                .add(new FileItem(item.getFileName().toString(),"dir")));

        Files.list(currDir).filter(isDir -> !Files.isDirectory(isDir))
                .forEach(item -> observableList
                        .add(new FileItem(item.getFileName().toString(),"file")));
        tabLocalDir.getItems().clear();
        tabLocalDir.getItems().addAll(observableList);
    }

    public void uploadFile(ActionEvent actionEvent) {
        try {
            FileItem fileItem = (FileItem) tabLocalDir.getSelectionModel().getSelectedItem();
            File file = new File(currLocalDir + File.separator + fileItem.getFileName());
            if (file.exists()) {
                os.writeUTF("upload");
                os.writeUTF(file.getName());
                long length = file.length();
                os.writeLong(length);
                FileInputStream fis = new FileInputStream(file);
                int read;
                byte[] buffer = new byte[512];
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
            os.writeUTF(lstServerFiles.getSelectionModel().getSelectedItem());
            String fileName = is.readUTF();
            File file = new File(currServerDir + File.separator + username + File.separator + fileName);
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
            fillFileList(Paths.get(currLocalDir));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteFile(ActionEvent actionEvent) {
        try {
            os.writeUTF("delete");
            os.writeUTF(lstServerFiles.getSelectionModel().getSelectedItem());
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
            lstServerFiles.setItems(observableList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
