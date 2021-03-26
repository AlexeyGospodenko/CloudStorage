//Утилитарный класс для работы с консолью по телнету
package Server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public final class ConsoleUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(TelnetHandler.class);

    public static final String ROOT_FOLDER = "Storage";
    private static final String LS_COMMAND = "   ls\t\t\t view all files from current directory\n\r";
    private static final String MKDIR_COMMAND = "   mkdir <dirname>\t create directory\n\r";
    private static final String TOUCH_COMMAND = "   touch <filename>\t create file\n\r";
    private static final String REMOVE_COMMAND = "   remove <name>\t remove file or directory\n\r";
    private static final String COPY_COMMAND = "   copy <src> <target>\t copy file src=filename target=filename\n\r";
    private static final String CAT_COMMAND = "   cat <filename>\t display content file\n\r";
    private static final String CD_COMMAND = "   cd <dirname>\t\t change directory\n\r";
    private static final String CD_UP_COMMAND = "   cd ..\t\t change to upper directory\n\r";
    private static final String CD_ROOT_COMMAND = "   cd \\\t\t\t change to root directory\n\r";

    private ConsoleUtils() {
    }

    //Вывод доступных команд
    public static String getCommandList() {
        return LS_COMMAND +
                MKDIR_COMMAND +
                TOUCH_COMMAND +
                REMOVE_COMMAND +
                COPY_COMMAND +
                CAT_COMMAND +
                CD_COMMAND +
                CD_UP_COMMAND +
                CD_ROOT_COMMAND;
    }

    //Получение списка файлов в директории
    public static String getFileList(Path currFolder) {
        StringBuilder listFiles = new StringBuilder();
        try {
            Files.list(currFolder)
                    .filter(isDir -> Files.isDirectory(isDir))
                    .forEach(path -> listFiles.append(path.getFileName())
                            .append(new String(new char[30 - path.getFileName().toString().length()])
                                    .replace('\0', ' '))
                            .append("DIR").append("\n\r"));

            Files.list(currFolder)
                    .filter(isDir -> !Files.isDirectory(isDir))
                    .forEach(path -> listFiles.append(path.getFileName()).append("\n\r"));
        } catch (IOException e) {
            LOGGER.error(null, e);
        }
        return listFiles.toString();
    }

    //Создание папки
    public static boolean createDir(Path currFolder, String dirName) {
        Path newDir = Paths.get(currFolder + File.separator + dirName);
        try {
            Files.createDirectory(newDir);
            return true;
        } catch (IOException e) {
            LOGGER.error(null, e);
            return false;
        }
    }

    //Создание файла
    public static boolean createFile(Path currFolder, String filename) throws IOException {
        Path newFileName = Paths.get(currFolder + File.separator + filename);
        try {
            Files.createFile(newFileName);
            return true;
        } catch (IOException e) {
            LOGGER.error(null, e);
            return false;
        }
    }

    //Смена директории
    public static Path changeDirectory(Path currFolder, String dirName) {
        if (dirName.equals(".")) return currFolder;
        Path newDir;
        try {
            //Подняться на директорию выше
            if (dirName.equals("..")) {
                //Выше ROOT_FOLDER не поднимаемся
                newDir = (!currFolder.toString().equals(ROOT_FOLDER)) ? currFolder.getParent() : currFolder;
                //Подняться в ROOT_FOLDER
            } else if (dirName.equals("\\") || dirName.equals("/")) {
                newDir = Paths.get(ROOT_FOLDER);
            }
            //Сменить директорию на указанную
            else {
                newDir = Paths.get(currFolder + File.separator + dirName);
            }
            //Если путь=директории и путь существует
            return (Files.exists(newDir) && Files.isDirectory(newDir)) ? newDir : currFolder;
        } catch (NullPointerException e) { //Если поднялись выше зфк
            LOGGER.error(null, e);
            return currFolder;
        }
    }

    //Удаление файла или каталога
    public static boolean removeFile(Path currFolder, String delName) {
        Path delPath = Paths.get(currFolder + File.separator + delName);
        if (!Files.exists(delPath)) return false;
        try {
            Files.walkFileTree(delPath, new SimpleFileVisitor<>() {
                //то начинаем обходить дерево файлов и удалять их
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                //После удаления файлов после посещений удаляем папку
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
            return true;
        } catch (IOException e) {
            LOGGER.error(null, e);
            return false;
        }
    }

    //Копирование файла
    public static boolean copyFile(Path currFolder, String src, String target) {
        Path srcPath = Paths.get(currFolder + File.separator + src);
        Path targetPath = Paths.get(currFolder + File.separator + target);
        try {
            //Если src существует, и таргет не директория и таргет еще не существует
            if (Files.exists(srcPath) && !Files.isDirectory(srcPath) && !Files.exists(targetPath)) {
                Files.copy(srcPath, targetPath);
                return true;
            }
        } catch (IOException e) {
            LOGGER.error(null, e);
        }
        return false;
    }

}
