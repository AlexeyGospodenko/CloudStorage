package Server;

import java.io.File;
import java.io.IOException;

//Утилитарный класс для работы с консолью
public final class ConsoleUtils {

    private static final String LS_COMMAND = "   ls\t\t\t view all files from current directory\n\r";
    private static final String MKDIR_COMMAND = "   mkdir <dirname>\t create directory\n\r";
    private static final String TOUCH_COMMAD = "   touch <filename>\t create file\n\r";
    private static final String REMOVE_COMMAD = "   remove <name>\t remove file or directory\n\r";
    private static final String COPY_COMMAD = "   copy <src> <target>\t copy file\n\r";
    private static final String CAT_COMMAD = "   cat <filename>\t display content file\n\r";
    private static final String CD_COMMAD = "   cd <dirname>\t\t change directory\n\r";
    private static final String CD_UP_COMMAD = "   cd..\t\t\t change to upper directory\n\r";
    private static final String CD_ROOT_COMMAD = "   cd /\t\t\t change to root directory\n\r";

    private ConsoleUtils() {
    }

    //Вывод доступных команд
    public static String getCommandList () {
        return LS_COMMAND +
               MKDIR_COMMAND +
               TOUCH_COMMAD +
               REMOVE_COMMAD +  //TODO
               COPY_COMMAD +    //TODO
               CAT_COMMAD +     //TODO
               CD_COMMAD +      //TODO
               CD_UP_COMMAD +   //TODO
               CD_ROOT_COMMAD;  //TODO
    }

    //Получение списка файлов в директории
    public static String getFileList (File currFolder) {
        StringBuilder listFiles = new StringBuilder();
        for (File f: currFolder.listFiles()) {
            if (f.isDirectory()) {
                listFiles.append(f.getName()).append("\t\t").append("DIR").append("\n\r");
            }
        }
        for (File f: currFolder.listFiles()) {
            if (f.isFile()) {
                listFiles.append(f.getName()).append("\n\r");
            }
        }

        return listFiles.toString();
    }

    //Создание папки
    public static boolean createDir(File currFolder, String dirName) {
        File newDir = new File(currFolder + File.separator + dirName);
        return newDir.mkdir();
    }

    //Создание файла
    public static boolean createFile(File currFolder, String filename) throws IOException {
        File newDir = new File(currFolder + File.separator + filename);
        return newDir.createNewFile();
    }
}
