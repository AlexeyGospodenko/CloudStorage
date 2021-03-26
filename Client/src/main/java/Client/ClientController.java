package Client;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class ClientController implements Initializable {
    public AnchorPane window;
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
    public TextField txtLocalPath;
    public TextField txtServerPath;
    public MenuItem conMenuServerTabCreateFolder;

    private DataOutputStream os;
    private DataInputStream is;

    private Path currLocalDir;
    private final String PATH_FILE_TYPE = "file";
    private final String PATH_DIR_TYPE = "Dir";
    private final long MAX_FILE_SIZE = 1073741824; //1024 MB

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            currLocalDir = (Paths.get("Client" + File.separator)).toAbsolutePath();
            os = NetworkService.getInstance().getOs();
            is = NetworkService.getInstance().getIs();

            //Заполнение таблицы файлов расположенных на локальной машине
            tableColumnLocalDirName.setCellValueFactory(new PropertyValueFactory<FileItem, String>("fileName"));
            tableColumnLocalDirType.setCellValueFactory(new PropertyValueFactory<FileItem, String>("fileType"));
            fillLocalFileList(currLocalDir);

            //Заполнений таблицы файлов расположенных на сервере
            tableColumnServerDirName.setCellValueFactory(new PropertyValueFactory<FileItem, String>("fileName"));
            tableColumnServerDirType.setCellValueFactory(new PropertyValueFactory<FileItem, String>("fileType"));
            fillServerFileList(null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fillLocalFileList(Path currDir) throws IOException {
        ObservableList<FileItem> observableList = FXCollections.observableArrayList(new FileItem("..", ""));
        Files.list(currDir).filter(isDir -> Files.isDirectory(isDir))
                .forEach(item -> observableList
                        .add(new FileItem(item.getFileName().toString(), PATH_DIR_TYPE)));

        Files.list(currDir).filter(isDir -> !Files.isDirectory(isDir))
                .forEach(item -> observableList
                        .add(new FileItem(item.getFileName().toString(), PATH_FILE_TYPE)));
        tabLocalDir.getItems().clear();
        tabLocalDir.getItems().addAll(observableList);

        txtLocalPath.setText(currLocalDir.toString());
    }

    private void fillServerFileList(String newDir) {
        try {
            byte[] buffer = new byte[512];
            os.write(("-list" + ";" + newDir).getBytes());

            //Получение текущий директории на сервере
            int readBytes = is.read(buffer);
            txtServerPath.setText(new String(buffer, 0, readBytes));

            StringBuilder stringFiles = new StringBuilder();
            while (true) {
                readBytes = is.read(buffer);
                stringFiles.append(new String(buffer, 0, readBytes));
                if (stringFiles.toString().endsWith("end")) {
                    break;
                }
            }
            String[] listFile = stringFiles.substring(0, stringFiles.length() - 3).split(";");

            ObservableList<FileItem> observableList = FXCollections.observableArrayList(new FileItem("..", ""));
            Arrays.stream(listFile)
                    .filter(isDir -> isDir.endsWith(":" + PATH_DIR_TYPE))
                    .forEach(item -> observableList.add(new FileItem(item.substring(0, item.length() - 4), PATH_DIR_TYPE)));
            Arrays.stream(listFile)
                    .filter(isDir -> isDir.endsWith(":" + PATH_FILE_TYPE))
                    .forEach(item -> observableList.add(new FileItem(item.substring(0, item.length() - 5), PATH_FILE_TYPE)));
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
            if (fileName.length() > MAX_FILE_SIZE) {
                lblStatus.setText("File cannot be larger than " + MAX_FILE_SIZE / 1024 / 1024 + " MB");
                return;
            }
            if (fileName.exists()) {
                os.write(("-upload;" + fileName.getName() + ";" + fileName.length()).getBytes());
                FileInputStream fis = new FileInputStream(fileName);
                byte[] buffer = new byte[512];
                int readBytes = is.read(buffer);
                while ((readBytes = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, readBytes);
                    os.flush();
                }
                fis.close();
                readBytes = is.read(buffer);
                lblStatus.setText(new String(buffer, 0, readBytes));
                fillServerFileList(null);
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
            os.write(("-download;" + fileName).getBytes());
            byte[] buffer = new byte[512];
            int readBytes = is.read(buffer);
            long size = Long.valueOf(new String(buffer, 0, readBytes));
            os.flush();
            FileOutputStream fos = new FileOutputStream(currLocalDir + File.separator + fileName);
            //Если файл на сервере пустой то просто добавляем в файл пустую строку
            if (size == 0) {
                fos.write("".getBytes());
            } else if (size != 0) {
                for (int i = 0; i < size / buffer.length + 1; i++) {
                    readBytes = is.read(buffer);
                    fos.write(buffer, 0, readBytes);
                }
            }
            fos.close();

            lblStatus.setText("File downloaded successfully");
            fillLocalFileList(currLocalDir);
        } catch (IOException e) {
            lblStatus.setText("File was not downloaded");
            e.printStackTrace();
        }
    }

    public void deleteFile(ActionEvent actionEvent) {
        try {
            byte[] buffer = new byte[512];
            String fileName = ((FileItem) tabServerDir.getSelectionModel().getSelectedItem()).getFileName();
            os.write(("-delete;" + fileName).getBytes());
            int readBytes = is.read(buffer);
            lblStatus.setText(new String(buffer, 0, readBytes));
            os.flush();
            fillServerFileList(null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tabLocalPressed(MouseEvent mouseEvent) {
        if (mouseEvent.isPrimaryButtonDown() && mouseEvent.getClickCount() == 2) {
            FileItem fileItem = (FileItem) tabLocalDir.getSelectionModel().getSelectedItem();
            //Если выбранный элемент = file то ничего не делаем
            if (fileItem.getFileType().equals(PATH_FILE_TYPE)) {
                return;
            }

            Path newDir = null;
            //Если выбранный элемент = Dir, то получаем новый Path.
            if (fileItem.getFileType().equals(PATH_DIR_TYPE)) {
                newDir = Paths.get(currLocalDir + File.separator + fileItem.getFileName());
            } else if (fileItem.getFileName().equals("..")) {
                newDir = currLocalDir.toAbsolutePath().getParent();
            }

            //На случай что произошли изменения в файлах
            if (newDir != null && Files.exists(newDir) && Files.isDirectory(newDir)) {
                currLocalDir = newDir;
            }

            try {
                fillLocalFileList(currLocalDir);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public void tabServerPressed(MouseEvent mouseEvent) {
        if (mouseEvent.isPrimaryButtonDown() && mouseEvent.getClickCount() == 2) {
            FileItem fileItem = (FileItem) tabServerDir.getSelectionModel().getSelectedItem();
            if (fileItem.getFileType().equals(PATH_DIR_TYPE) || fileItem.getFileName().equals("..")) {
                fillServerFileList(fileItem.getFileName());
            }
        }
    }

    public void CreateFolder(ActionEvent actionEvent) {
        try {
            Parent createFolder = FXMLLoader.load(NewFolderController.class.getResource("fxml/DialogFolderName.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(createFolder));
            stage.setTitle("New folder");
            stage.setResizable(false);
            stage.showAndWait();

            if (NewFolderController.isChange()) {
                String newDir = NewFolderController.getFolderName(); //Через статику получать - решение плохое но дешёвое
                ObservableList<FileItem> observableList = tabServerDir.getItems();
                for (FileItem f : observableList) {
                    if (f.getFileName().equals(newDir)) {
                        lblStatus.setText("Folder already exists");
                        return;
                    }
                }

                byte[] buffer = new byte[512];
                os.write(("-mkdir;" + newDir).getBytes());
                int readBytes = is.read(buffer);
                lblStatus.setText(new String(buffer, 0, readBytes));
                fillServerFileList(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void changePassword(ActionEvent actionEvent) {
        try {
            Parent createFolder = FXMLLoader.load(NewFolderController.class.getResource("fxml/DialogChangePass.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(createFolder));
            stage.setTitle("New password");
            stage.setResizable(false);
            stage.showAndWait();

            if (NewPassController.isChange()) {
                String newPassword = NewPassController.getPassword(); //Через статику получать - решение плохое но дешёвое
                byte[] buffer = new byte[512];
                os.write(("-changePassword;" + NetworkService.getInstance().getUsername() + ";" + newPassword).getBytes());
                int readBytes = is.read(buffer);
                lblStatus.setText(new String(buffer, 0, readBytes));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void deleteAccount(ActionEvent actionEvent) throws IOException {
        Parent createFolder = FXMLLoader.load(NewFolderController.class.getResource("fxml/DialogDeleteAccount.fxml"));
        Stage stage = new Stage();
        stage.setScene(new Scene(createFolder));
        stage.setTitle("Delete account");
        stage.setResizable(false);
        stage.showAndWait();

        if (DeleteAccountController.isDelete()) {
            byte[] buffer = new byte[512];
            os.write(("-deleteAccount ; " + NetworkService.getInstance().getUsername() + " ; " + DeleteAccountController.getPassword())
                    .getBytes());
            int readBytes = is.read(buffer);
            String result = new String(buffer, 0, readBytes);
            if (result.equals("SUCCESS")) {
                NetworkService.getInstance().destroyInstance();
                new CreateWindow("fxml/Auth.fxml", "CloudStorage - Authorization", false);
                window.getScene().getWindow().hide();
            } else {
                lblStatus.setText(result);
            }
        }
    }
}
