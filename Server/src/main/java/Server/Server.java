package Server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

    public Server(int port) {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        try {
            ServerSocket server = new ServerSocket(port);
            LOGGER.info("Server started on port {}", server.getLocalPort());
            while (true) {
                Socket socket = server.accept();
                executor.execute(new ClientHandler(socket));
                LOGGER.info("Client connected: {}", socket.getInetAddress());
            }
        } catch (IOException e) {
            LOGGER.error(null, e);
        }

    }

    public static void main(String[] args) {
        new Server(Integer.parseInt(args[0]));
    }
}
