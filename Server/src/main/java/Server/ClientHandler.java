//Обработчик для основного сервера
package Server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Map;

public class ClientHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientHandler.class);
    private final Map<Channel, Path> channelDirs;
    private final String PATH_FILE_TYPE = "file";
    private final String PATH_DIR_TYPE = "Dir";

    public ClientHandler(Map<Channel, Path> channelFolders) {
        this.channelDirs = channelFolders;
    }

    public void Accept(Selector selector, SelectionKey key) {
        try {
            SocketChannel channel = ((ServerSocketChannel) key.channel()).accept();
            channel.configureBlocking(false);
            LOGGER.info("Server Client connected: \"{}\"", channel.getRemoteAddress());
            channel.register(selector, SelectionKey.OP_READ);
        } catch (IOException e) {
            LOGGER.error(null, e);
        }
    }

    public void Read(Selector selector, SelectionKey key) {
        ByteBuffer buffer = ByteBuffer.allocate(512);
        SocketChannel channel = (SocketChannel) key.channel();

        try {
            int readBytes = channel.read(buffer);
            //Если количество непрочитанных байт -1, то есть клиент сделал дисконнект,
            // то удаляем его из МАПы и закрываем канал
            if (readBytes < 0) {
                LOGGER.info("Connection with user \"{}\" was lost", channel.getRemoteAddress());
                channelDirs.remove(channel);
                channel.close();
            } else
            //Иначе читаем и выполняем команды которые пришли от клиента
            {
                String command = new String(buffer.array(), 0, buffer.position())
                        .replaceAll("[\n\r]", "");
                buffer.clear();

                //Авторизация
                if (command.startsWith("-auth;")) {
                    String[] args = command.split(";");
                    if (args.length == 3) {
                        try {
                            boolean valid = DatabaseService.getInstance().auth(args[1], args[2]);
                            if (!valid) {
                                channel.write(ByteBuffer.wrap(("FAIL").getBytes()));
                                LOGGER.info("Auth failed login={{}}", args[1]);
                            } else {
                                channelDirs.put(channel, Paths.get(ConsoleUtils.ROOT_FOLDER + File.separator + args[1]));
                                channel.write(ByteBuffer.wrap(("SUCCESS").getBytes()));
                                LOGGER.info("Auth success login={{}}", args[1]);
                            }
                        } catch (SQLException e) {
                            LOGGER.info(null, e);
                        }
                    }
                }

                //Регистрация
                else if (command.startsWith("-register;")) {
                    String[] args = command.split(";");
                    if (args.length == 3) {
                        try {
                            boolean isLoginExists = DatabaseService.getInstance().isLoginExists(args[1]);
                            if (isLoginExists) {
                                channel.write(ByteBuffer.wrap(("FAIL").getBytes()));
                            } else {
                                DatabaseService.getInstance().addUser(args[1], args[2]);
                                Path newUserDir = Paths.get(ConsoleUtils.ROOT_FOLDER + File.separator + args[1]);
                                Files.createDirectory(newUserDir);
                                channel.write(ByteBuffer.wrap(("SUCCESS").getBytes()));
                            }
                        } catch (SQLException e) {
                            LOGGER.info(null, e);
                        }
                    }
                }

                //Получение списка файлов в репозитории
                else if (command.startsWith("-list;")) {
                    String[] args = command.split(";", 2);
                    Path dir = channelDirs.get(channel);
                    if (args[1].equals("..")) {
                        dir = dir.toString().substring(ConsoleUtils.ROOT_FOLDER.length() + 1).contains("\\") ? dir.getParent() : dir;
                        channelDirs.put(channel, dir);
                    } else if (!args[1].equals("null")) {
                        dir = Paths.get(dir + File.separator + args[1]);
                        channelDirs.put(channel, dir);
                    }
                    if (Files.exists(dir) && Files.isDirectory(dir)) {
                        StringBuilder listFiles = new StringBuilder();
                        channel.write(ByteBuffer.wrap((dir.toString().substring(ConsoleUtils.ROOT_FOLDER.length())).getBytes()));
                        try {
                            Files.list(dir)
                                    .filter(isDir -> Files.isDirectory(isDir))
                                    .forEach(path -> listFiles.append(path.getFileName())
                                            .append(':')
                                            .append(PATH_DIR_TYPE)
                                            .append(";"));

                            Files.list(dir)
                                    .filter(isDir -> !Files.isDirectory(isDir))
                                    .forEach(path -> listFiles.append(path.getFileName())
                                            .append(':')
                                            .append(PATH_FILE_TYPE)
                                            .append(";"));
                        } catch (IOException e) {
                            LOGGER.error(null, e);
                        }
                        listFiles.append("end");
                        channel.write(ByteBuffer.wrap(listFiles.toString().getBytes()));
                    }
                }

                //Удаление файла или папки
                else if (command.startsWith("-delete;")) {
                    String[] args = command.split(";", 2);
                    if (args.length == 2) {
                        String fileName = args[1];
                        String result = ConsoleUtils.removeFile(channelDirs.get(channel), fileName) ?
                                "File deleted successfully\n\r" : "File has not been deleted\n\r";
                        channel.write(ByteBuffer.wrap(result.getBytes()));
                    }
                }

                //Загрузка файла на сервер
                else if (command.startsWith("-upload;")) {
                    String[] args = command.split(";", 3);
                    if (args.length == 3) {
                        channel.write(ByteBuffer.wrap(("OK").getBytes()));
                        Path fileName = Paths.get(channelDirs.get(channel) + File.separator + args[1]);
                        long fileSize = Long.valueOf(args[2]);
                        long fileSizeRemaind = fileSize;
                        FileChannel fileChannel = (new FileOutputStream(fileName.toFile())).getChannel();
                        ByteBuffer fileBuffer = ByteBuffer.allocate(256);
                        fileBuffer.clear();
                        int read;
                        //Какой-то ужас начало
                        while (fileSizeRemaind > 0) {
                            read = channel.read(fileBuffer);
                            //for (int i = 0; i < fileSize / fileBuffer.capacity() + 1; i++) {
                            if (read > 0) {
                                fileBuffer.flip();
                                while (fileBuffer.hasRemaining()) {
                                    fileChannel.write(fileBuffer);
                                }
                                fileBuffer.clear();
                                fileSizeRemaind = fileSizeRemaind - fileBuffer.capacity();
                            }
                        }
                        fileChannel.close();
                        String result = (fileSize == Files.size(fileName)) ? "File uploaded successfully" :
                                "File uploaded NOT successfully";
                        channel.write(ByteBuffer.wrap(result.getBytes()));
                    }
                }

                //Загрузка файла с сервера
                else if (command.startsWith("-download;")) {
                    String[] args = command.split(";");
                    if (args.length == 2) {
                        Path fileName = Paths.get(channelDirs.get(channel) + File.separator + args[1]);
                        if (Files.exists(fileName)) {
                            String size = Long.toString(Files.size(fileName));
                            channel.write(ByteBuffer.wrap(size.getBytes()));
                            FileChannel fileChannel = (new FileInputStream(fileName.toFile())).getChannel();
                            ByteBuffer fileBuffer = ByteBuffer.allocate(512);
                            while (fileChannel.read(fileBuffer) != -1) {
                                fileBuffer.flip();
                                channel.write(fileBuffer);
                                fileBuffer.clear();
                            }
                            fileChannel.close();
                        }
                    }
                }

                //Создание папки на сервере
                else if (command.startsWith("-mkdir;")) {
                    String[] args = command.split(";");
                    if (args.length == 2) {
                        Path newDir = Paths.get(channelDirs.get(channel) + File.separator + args[1]);
                        Files.createDirectory(newDir);
                        channel.write(ByteBuffer.wrap(("Folder successfully created").getBytes()));
                    }
                }

                //Смена пароля
                else if (command.startsWith("-changePassword;")) {
                    String[] args = command.split(";");
                    if (args.length == 3) {
                        if (DatabaseService.getInstance().changePassword(args[1], args[2])) {
                            LOGGER.info("Password changed successfully. login=={{}}", args[1]);
                            channel.write(ByteBuffer.wrap(("Password changed successfully").getBytes()));
                        } else {
                            LOGGER.info("Password change error. login={{}}", args[1]);
                            channel.write(ByteBuffer.wrap(("Password change error").getBytes()));
                        }
                    }
                }

                //Удаление аккаунта
                else if (command.startsWith("-deleteAccount ; ")) {
                    String[] args = command.split(" ; ");
                    if (args.length == 3) {
                        try {
                            boolean valid = DatabaseService.getInstance().auth(args[1], args[2]);
                            if (valid) {
                                ConsoleUtils.removeFile(Paths.get(ConsoleUtils.ROOT_FOLDER), args[1]);
                                DatabaseService.getInstance().deleteAccount(args[1], args[2]);
                                channel.write(ByteBuffer.wrap(("SUCCESS").getBytes()));
                                channelDirs.remove(channel);
                                channel.close();
                            } else {
                                channel.write(ByteBuffer.wrap(("Wrong password").getBytes()));
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        } catch (SocketException e) {
            try {
                LOGGER.info("Connection with user \"{}\" was lost", channel.getRemoteAddress());
                channelDirs.remove(channel);
                channel.close();
            } catch (IOException ioException) {
                LOGGER.error(null, e);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}