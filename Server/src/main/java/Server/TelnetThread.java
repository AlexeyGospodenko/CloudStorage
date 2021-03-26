//Поток для телнет сервера
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

public class TelnetThread extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);
    private final int port;
    boolean running = true;
    //Мап, в которой будем хранить состояние текущих директорий для каналов
    private final Map<Channel, Path> channelDirs = new HashMap<>();

    public TelnetThread(int port) {
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
            LOGGER.info("Telnet Server started on port \"{}\"", server.socket().getLocalPort());
            TelnetHandler telnetHandler = new TelnetHandler(channelDirs);
            while (server.isOpen()) {
                selector.select();
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    if (key.isAcceptable()) {
                        telnetHandler.Accept(selector, key);
                    }
                    else if (key.isReadable()) {
                        telnetHandler.Read(selector, key);
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
