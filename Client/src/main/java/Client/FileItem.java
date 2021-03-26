package Client;

import javafx.beans.property.SimpleStringProperty;

public class FileItem {
    private final SimpleStringProperty fileName;
    private final SimpleStringProperty  fileType;

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

}
