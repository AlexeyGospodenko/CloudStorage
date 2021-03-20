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
import java.util.Map;

public class ClientHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientHandler.class);
    private final Map<Channel, Path> channelDirs;

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
            }
            //Иначе читаем и выполняем команды которые пришли от клиента
            else {
                String command = new String(buffer.array(), 0, buffer.position())
                        .replaceAll("[\n\r]", "");
                buffer.clear();

                //Клиент при коннекте должен отправить свое имя пользователя
                if (command.startsWith("-username")) {
                    String[] args = command.split(" ");
                    if (args.length == 2) {
                        String username = args[1];
                        //и положить его в МАПу ключ=канал, директория=базовая директория + имя пользователя
                        channelDirs.put(channel, Paths.get(ConsoleUtils.ROOT_FOLDER + File.separator + username));
                        channel.write(ByteBuffer.wrap("Connected".getBytes()));
                    }
                }

                else if (command.equals("-list")) {
                    Path dir = channelDirs.get(channel);
                    if (Files.exists(dir) && Files.isDirectory(dir)) {
                        StringBuilder listFiles = new StringBuilder();
                        try {
                            Files.list(dir)
                                    .filter(isDir -> Files.isDirectory(isDir))
                                    .forEach(path -> listFiles.append(path.getFileName())
                                            .append(':')
                                            .append("dir")
                                            .append(";"));

                            Files.list(dir)
                                    .filter(isDir -> !Files.isDirectory(isDir))
                                    .forEach(path -> listFiles.append(path.getFileName())
                                            .append(':')
                                            .append("file")
                                            .append(";"));
                        } catch (IOException e) {
                            LOGGER.error(null, e);
                        }
                        listFiles.append("end");
                        channel.write(ByteBuffer.wrap(listFiles.toString().getBytes()));
                    }
                }

                else if (command.startsWith("-delete ")) {
                    String[] args = command.split(" ");
                    if (args.length == 2) {
                        String fileName = args[1];
                        String result = ConsoleUtils.removeFile(channelDirs.get(channel), fileName) ?
                                "File deleted successfully\n\r" : "File has not been deleted\n\r";
                        channel.write(ByteBuffer.wrap(result.getBytes()));
                    }
                }

                else if (command.startsWith("-upload ")) {
                    String[] args = command.split(" ");
                    if (args.length == 2) {
                        Path fileName = Paths.get(channelDirs.get(channel) + File.separator + args[1]);
                        FileChannel fileChannel = (new FileOutputStream(fileName.toFile())).getChannel();
                        ByteBuffer fileBuffer = ByteBuffer.allocate(512);
                        while (channel.read(fileBuffer) != 0) {
                            fileBuffer.flip();
                            while (fileBuffer.hasRemaining()) {
                                fileChannel.write(fileBuffer);
                            }
                            fileBuffer.clear();
                        }
                        fileChannel.close();
                    }
                    channel.write(ByteBuffer.wrap(("File uploaded successfully").getBytes()));
                }

                else if (command.startsWith("-download ")) {
                    String[] args = command.split(" ");
                    if (args.length == 2) {
                        Path fileName = Paths.get(channelDirs.get(channel) + File.separator + args[1]);
                        if (Files.exists(fileName)) {
                            String size = Long.toString(Files.size(fileName));
                            System.out.println(size);
                            channel.write(ByteBuffer.wrap(size.getBytes()));
                            FileChannel fileChannel = (new FileInputStream(fileName.toFile())).getChannel();
                            ByteBuffer fileBuffer = ByteBuffer.allocate(512);
                            while (fileChannel.read(fileBuffer) != -1 ) {
                                fileBuffer.flip();
                                channel.write(fileBuffer);
                                fileBuffer.clear();
                                System.out.println(1);
                            }
                            fileChannel.close();
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