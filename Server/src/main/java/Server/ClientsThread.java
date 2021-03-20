//Поток для работы основного сервера
package Server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ClientsThread extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);
    private final int port;
    boolean running = true;

    private Map<Channel, Path> channelDirs = new HashMap<>();

    public ClientsThread(int port) {
        this.port = port;
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
                    }
                    else if (key.isReadable()) {
                        clientHandler.Read(selector, key); //Необходимо ли вызывать этот метод в отедельном потоке
                                                            //для одновременного выполнения команд/up-download'ов разными клиентами?
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
