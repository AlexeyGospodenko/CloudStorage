//Обработчик для телнет сервера
package Server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class TelnetHandler {
    private final Logger LOGGER = LoggerFactory.getLogger(TelnetHandler.class);
    private final ByteBuffer buffer = ByteBuffer.allocate(512);
    private final Map<Channel, Path> channelDirs;

    public TelnetHandler(Map<Channel, Path> channelDirs) {
        this.channelDirs = channelDirs;
    }

    public void Accept(Selector selector, SelectionKey key) {
        try {
            SocketChannel channel = ((ServerSocketChannel) key.channel()).accept();
            channel.configureBlocking(false);
            LOGGER.info("Telnet client connected: \"{}\"", channel.getRemoteAddress());
            channel.register(selector, SelectionKey.OP_READ);
            channel.write(ByteBuffer.wrap("Enter -help for get available commands\n\r".getBytes()));
            //Ложим в МАПу ключ-канал который приняли, и даем ему базовое значение
            channelDirs.put(channel, Paths.get(ConsoleUtils.ROOT_FOLDER));
        } catch (IOException e) {
            LOGGER.error(null, e);
        }
    }

    public void Read(Selector selector, SelectionKey key) {
        try {
            SocketChannel channel = (SocketChannel) key.channel();

            int readBytes = channel.read(buffer);
            //Если количество непрочитанных байт -1, то есть клиент сделал дисконнект,
            // то удаляем его из МАПы и закрываем канал
            if (readBytes < 0) {
                LOGGER.info("Connection with user \"{}\" was lost", channel.getRemoteAddress());
                channelDirs.remove(channel);
                channel.close();
            }
            //Иначе читаем и выполняем команды которые пришли по телнету
            else {
                String command = new String(buffer.array(), 0, buffer.position())
                        .replaceAll("[\n\r]", "");
                buffer.clear();

                if (command.equals("-help")) {
                    String listCommands = ConsoleUtils.getCommandList();
                    channel.write(ByteBuffer.wrap(listCommands.getBytes()));
                } else if (command.equals("ls")) {
                    String listFiles = ConsoleUtils.getFileList(channelDirs.get(channel));
                    channel.write(ByteBuffer.wrap(listFiles.getBytes()));
                } else if (command.startsWith("mkdir ")) {
                    String[] dirName = command.split(" ");
                    if (dirName.length == 2) {
                        String result = ConsoleUtils.createDir(channelDirs.get(channel), dirName[1]) ? "success\n\r" : "failed\n\r";
                        channel.write(ByteBuffer.wrap(result.getBytes()));
                    }
                } else if (command.startsWith("touch ")) {
                    String[] fileName = command.split(" ");
                    if (fileName.length == 2) {
                        String result = ConsoleUtils.createFile(channelDirs.get(channel), fileName[1]) ? "success\n\r" : "failed\n\r";
                        channel.write(ByteBuffer.wrap(result.getBytes()));
                    }
                } else if (command.startsWith("cd ")) {
                    String[] dirName = command.split(" ");
                    if (dirName.length == 2) {
                        channelDirs.put(channel, ConsoleUtils.changeDirectory(channelDirs.get(channel), dirName[1]));
                    }
                } else if (command.startsWith("rm ")) {
                    String[] fileName = command.split(" ");
                    if (fileName.length == 2) {
                        String result = ConsoleUtils.removeFile(channelDirs.get(channel), fileName[1]) ? "success\n\r" : "failed\n\r";
                        channel.write(ByteBuffer.wrap(result.getBytes()));
                    }
                } else if (command.startsWith("copy ")) {
                    String[] patName = command.split(" ");
                    if (patName.length == 3) {
                        String result = ConsoleUtils.copyFile(channelDirs.get(channel), patName[1], patName[2]) ? "success\n\r" : "failed\n\r";
                        channel.write(ByteBuffer.wrap(result.getBytes()));
                    }
                } else if (command.startsWith("cat ")) {
                    String[] fileName = command.split(" ");
                    if (fileName.length == 2) {
                        Path catFileName = Paths.get(channelDirs.get(channel) + File.separator + fileName[1]);
                        if (Files.isDirectory(catFileName) || !Files.exists(catFileName)) {
                            channel.write(ByteBuffer.wrap(("File " + fileName[1] + " is not found\n\r").getBytes()));
                        } else {
                            FileInputStream fis = new FileInputStream(catFileName.toFile());
                            FileChannel fileChannel = fis.getChannel();
                            ByteBuffer fileBuffer = ByteBuffer.allocate(512);
                            while (fileChannel.read(fileBuffer) > 0) {
                                fileBuffer.flip();
                                channel.write(fileBuffer);
                                fileBuffer.clear();
                            }
                            channel.write(ByteBuffer.wrap("\n\r".getBytes()));
                            fis.close();
                        }
                    }
                }
                channel.write(ByteBuffer.wrap((channel.getLocalAddress().toString() + " " + channelDirs.get(channel) + ">: ").getBytes()));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
