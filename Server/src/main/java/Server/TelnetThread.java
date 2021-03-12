package Server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TelnetThread extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);
    private final int port;
    boolean running = true;

    public TelnetThread(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        ExecutorService executorClient = Executors.newFixedThreadPool(4);
        try {
            ServerSocket server = new ServerSocket(port);
            LOGGER.info("Telnet Server started on port {}", server.getLocalPort());
            while (running) {
                Socket socket = server.accept();
                executorClient.execute(new TelnetHandler(socket));
                LOGGER.info("Telnet client connected: {}", socket.getRemoteSocketAddress());
            }
        } catch (IOException e) {
            running = false;
            LOGGER.error(null, e);
        }
    }
}
