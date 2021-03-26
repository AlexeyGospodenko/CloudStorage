//Поток для работы основного сервера
package Server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ClientsThread extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);
    private final int port;
    boolean running = true;

    private final Map<Channel, Path> channelDirs = new HashMap<>();

    public ClientsThread(int port) {
        this.port = port;
        Path mainDir = Paths.get(ConsoleUtils.ROOT_FOLDER);

        if (!Files.exists(mainDir)) {
            try {
                Files.createDirectory(mainDir);
            } catch (IOException e) {
                LOGGER.error(null, e);

            }
        }
    }

    @Override
    public void run() {
        try {
            ServerSocketChannel server = ServerSocketChannel.open();
            server.bind(new InetSocketAddress(port));
            server.configureBlocking(false);
            Selector selector = Selector.open();
            server.register(selector, SelectionKey.OP_ACCEPT);
            LOGGER.info("\"Server Client started on port \"{}\"", server.socket().getLocalPort());
            ClientHandler clientHandler = new ClientHandler(channelDirs);
            while (server.isOpen()) {
                selector.select();
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    if (key.isAcceptable()) {
                        clientHandler.Accept(selector, key);
                    } else if (key.isReadable()) {
                        clientHandler.Read(selector, key);
                    }
                    iterator.remove();
                }
            }
        } catch (IOException e) {
            running = false;
            LOGGER.error(null, e);
        }
    }

}
