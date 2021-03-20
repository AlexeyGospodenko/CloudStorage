package Client;

import javafx.beans.property.SimpleStringProperty;

public class FileItem {
    private SimpleStringProperty fileName;
    private SimpleStringProperty  fileType;

    public FileItem(String fileName, String fileType) {
        this.fileName = new SimpleStringProperty(fileName);
        this.fileType = new SimpleStringProperty(fileType);
    }

    public String getFileName() {
        return fileName.get();
    }

    public String getFileType() {
        return fileType.get();
    }

    public void setFileName(String fileName) {
        this.fileName.set(fileName);
    }

    public void setFileType(String fileType) {
        this.fileType.set(fileType);
    }

}
