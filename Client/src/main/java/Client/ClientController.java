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
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class ClientController implements Initializable {
    public Button btnUpload;
    public Button btnDownload;
    public Button btnDelete;
    public Label lblStatus;
    public TableView tabLocalDir;
    public TableColumn tableColumnLocalDirName;
    public TableColumn tableColumnLocalDirType;
    public TableView tabServerDir;
    public TableColumn tableColumnServerDirName;
    public TableColumn tableColumnServerDirType;

    private Socket socket;
    private DataOutputStream os;
    private DataInputStream is;
    private byte[] buffer = new byte[512];


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
            fillLocalFileList(Paths.get(currLocalDir));

            sendName(username);
            //Заполнений таблицы файлов расположенных на сервере
            tableColumnServerDirName.setCellValueFactory(new PropertyValueFactory<FileItem, String>("fileName"));
            tableColumnServerDirType.setCellValueFactory(new PropertyValueFactory<FileItem, String>("fileType"));
            fillServerFileList();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fillLocalFileList(Path currDir) throws IOException {
        ObservableList<FileItem> observableList = FXCollections.observableArrayList(new FileItem("..", ""));
        Files.list(currDir).filter(isDir -> Files.isDirectory(isDir))
                .forEach(item -> observableList
                        .add(new FileItem(item.getFileName().toString(), "Dir")));

        Files.list(currDir).filter(isDir -> !Files.isDirectory(isDir))
                .forEach(item -> observableList
                        .add(new FileItem(item.getFileName().toString(), "file")));
        tabLocalDir.getItems().clear();
        tabLocalDir.getItems().addAll(observableList);
    }

    private void sendName(String username) {
        try {
            os.write(("-username " + username).getBytes());
            is.read(buffer);
            /* Предыдущая строка по сути заглушка-синхронное общение при проблеме -
            если дважды выполнить os.write то на сервере это все приходит в одном сообщении
            То есть я отправляю сообщения ("-username " + username) и дальше ("list")
            на сервер это приходит как одно сообщение: "-username testuser|list"
            На просторах интернета пишут что это происходит на уровне TCP/IP протокола (что странно ведь на IO сервере такое поведение не наблюдается)
            и рекомендуют в сообщении заголовком передавать длину сообщения, и на основании длины, на сервере уже,
            фрагментировать сообщения. Возможно есть более удобные варианты? Типа использование других технологий на клиенте
            вместо Socket'ов и DataOutputStream'ов.*/
            String status = new String(buffer);
            lblStatus.setText(status);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fillServerFileList() {
        try {
            os.write("-list".getBytes());
            StringBuilder stringFiles = new StringBuilder();
            while (true) {
                byte[] fileBuffer = new byte[512];
                int size = is.read(fileBuffer);
                stringFiles.append(new String(fileBuffer, 0, size));
                if (stringFiles.toString().endsWith("end")) {
                    break;
                }
            }
            String[] listFile = stringFiles.substring(0, stringFiles.length() - 3).split(";");

            ObservableList<FileItem> observableList = FXCollections.observableArrayList(new FileItem("..", ""));
            Arrays.stream(listFile)
                    .filter(isDir -> isDir.endsWith(":dir"))
                    .forEach(item -> observableList.add(new FileItem(item.substring(0, item.length() - 4), "Dir")));
            Arrays.stream(listFile)
                    .filter(isDir -> isDir.endsWith(":file"))
                    .forEach(item -> observableList.add(new FileItem(item.substring(0, item.length() - 5), "file")));
            tabServerDir.getItems().clear();
            tabServerDir.getItems().addAll(observableList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void uploadFile(ActionEvent actionEvent) {
        try {
            FileItem fileItem = (FileItem) tabLocalDir.getSelectionModel().getSelectedItem();
            File fileName = new File(currLocalDir + File.separator + fileItem.getFileName());
            if (fileName.exists()) {
                os.write(("-upload " + fileName.getName()).getBytes());
                FileInputStream fis = new FileInputStream(fileName);
                int read;
                byte[] buffer = new byte[512];
                while ((read = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
                fis.close();
                read = is.read(buffer);
                lblStatus.setText(new String(buffer, 0, read));
                fillServerFileList();
            } else {
                lblStatus.setText("File is not exists");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void downloadFile(ActionEvent actionEvent) {
        try {
            String fileName = ((FileItem) tabServerDir.getSelectionModel().getSelectedItem()).getFileName();
            os.write(("-download " + fileName).getBytes());
            byte[] buffer = new byte[512];
            int readBytes = is.read(buffer);
            long size = Long.valueOf(new String(buffer, 0 , readBytes));
            os.flush();

            FileOutputStream fos = new FileOutputStream(currLocalDir + File.separator + fileName);
            for (int i = 0; i < size / buffer.length + 1; i++) {
                readBytes = is.read(buffer);
                fos.write(buffer, 0, readBytes);
            }
            fos.close();

            lblStatus.setText("File downloaded successfully");

            fillLocalFileList(Paths.get(currLocalDir));
        } catch (IOException e) {
            lblStatus.setText("File was not downloaded");
            e.printStackTrace();
        }
    }

    public void deleteFile(ActionEvent actionEvent) {
        try {
            String fileName = ((FileItem) tabServerDir.getSelectionModel().getSelectedItem()).getFileName();
            os.write(("-delete " + fileName).getBytes());
            is.read(buffer);
            lblStatus.setText(new String(buffer));
            os.flush();
            fillServerFileList();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
