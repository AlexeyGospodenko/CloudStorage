package Server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class ClientHandler implements Runnable {
    private boolean running;
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientHandler.class);
    DataOutputStream os;
    DataInputStream is;

    private String username = "testuser";
    private final static String ROOT_FOLDER = "Server" + File.separator;
    private String folder;

    public ClientHandler(Socket socket) throws IOException {
        running = true;
        os = new DataOutputStream(socket.getOutputStream());
        is = new DataInputStream(socket.getInputStream());
    }

    @Override
    public void run() {
        try {
            while (running) {
                String command = is.readUTF();
                if ("list".equals(command)) {
                    folder = ROOT_FOLDER + is.readUTF();
                    File file = new File(folder);
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    StringBuilder listFiles = new StringBuilder();
                    String[] arrListFile = file.list();
                    for (String itemListFile : arrListFile) {
                        listFiles.append(itemListFile).append(";");
                    }
                    os.writeUTF(listFiles.toString());
                } else if ("upload".equals(command)) {
                    try {
                        String fileName = is.readUTF();
                        File file = new File(folder + File.separator + fileName);
                        if (!file.exists()) {
                            file.createNewFile();
                        }
                        long size = is.readLong();
                        FileOutputStream fos = new FileOutputStream(file);
                        byte[] buffer = new byte[256];
                        for (int i = 0; i < (size + 255) / 256; i++) {
                            int read = is.read(buffer);
                            fos.write(buffer, 0, read);
                        }
                        fos.close();
                        os.writeUTF("File " + file.getName() + " uploaded");
                    } catch (IOException e) {
                        LOGGER.error(null, e);
                        os.writeUTF("ERROR");
                    }
                } else if ("download".equals(command)) {
                    String fileName = is.readUTF();
                    File file = new File(folder + File.separator + fileName);
                    if (file.exists()) {
                        os.writeUTF(fileName);
                        long length = file.length();
                        os.writeLong(length);
                        FileInputStream fis = new FileInputStream(file);
                        int read;
                        byte[] buffer = new byte[256];
                        while ((read = fis.read(buffer)) != -1) {
                            os.write(buffer, 0, read);
                        }
                        os.flush();
                        fis.close();

                        //os.writeUTF("File " + fileName + " was downloaded");
                    } else {
                        os.writeUTF("File " + fileName + " is not exists");
                    }
                } else if ("delete".equals(command)) {
                    try {
                        String fileName = is.readUTF();
                        File file = new File(folder + File.separator + fileName);
                        if (file.exists()) {
                            file.delete();
                            os.writeUTF("File " + fileName + " was delete");
                        } else {
                            os.writeUTF("File " + fileName + " is not exists");
                        }
                    } catch (IOException e) {
                        LOGGER.error(null, e);
                    }
                }
            }
        } catch (SocketException e) {
            LOGGER.info("Connection with user \"{}\" was lost", username);
            running = false;
        } catch (IOException e) {
            LOGGER.error(null, e);
            running = false;
        }
    }

}