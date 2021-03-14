package Server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class TelnetHandler implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(TelnetHandler.class);
    private final Socket socket;
    private final DataOutputStream os;
    private final DataInputStream is;
    private boolean running;
    private final byte[] buffer;

    public static final String ROOT_FOLDER = "Server" + File.separator + "Storage";

    public TelnetHandler(Socket socket) throws IOException {
        this.socket = socket;
        running = true;
        os = new DataOutputStream(socket.getOutputStream());
        is = new DataInputStream(socket.getInputStream());
        buffer = new byte[512];
    }

    @Override
    public void run() {
        try {
            os.write("type -help for get available commands\n\r".getBytes());
            while (running) {
                File currFolder = new File(ROOT_FOLDER);

                int bytesRead = is.read(buffer);
                if (bytesRead < 0) {
                    running = false;
                    LOGGER.info("Connection with user \"{}\" was lost", socket.getRemoteSocketAddress());
                } else {
                    String command = new String(buffer, 0, bytesRead)
                            .replaceAll("[\n\r]", "");
                    if (command.isEmpty()) {
                        continue;
                    }

                    if (command.equals("-help")) {
                        String listCommands = ConsoleUtils.getCommandList();
                        os.write(listCommands.getBytes());
                    } else if (command.equals("ls")) {
                        String listFiles = ConsoleUtils.getFileList(currFolder);
                        os.write(listFiles.getBytes());
                    } else if (command.startsWith("mkdir ")) {
                        String[] dirName = command.split(" ");
                        if (dirName.length == 2) {
                            String result = ConsoleUtils.createDir(currFolder, dirName[1]) ? "success\n\r" : "failed\n\r";
                            os.write(result.getBytes());
                        }
                    } else if (command.startsWith("touch ")) {
                        String[] fileName = command.split(" ");
                        if (fileName.length == 2) {
                            String result = ConsoleUtils.createFile(currFolder, fileName[1]) ? "success\n\r" : "failed\n\r";
                            os.write(result.getBytes());
                        }
                    }
                    os.write((socket.getLocalSocketAddress().toString() + " " + currFolder.getPath() + ">: ").getBytes());
                    os.flush();
                }
            }
        } catch (SocketException e) {
            LOGGER.info("Connection with user \"{}\" was lost", socket.getRemoteSocketAddress());
        } catch (IOException e) {
            LOGGER.error(null, e);
            running = false;
        }
    }

}
