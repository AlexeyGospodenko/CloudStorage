package Client;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class NetworkService {
    private static NetworkService instance;
    private Socket socket;
    private DataOutputStream os;
    private DataInputStream is;
    private String username;

    private NetworkService() {
        try {
            socket = new Socket("localhost", 1234);
            os = new DataOutputStream(socket.getOutputStream());
            is = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static NetworkService getInstance() {
        if (instance == null) {
            return null;
        }
        return instance;
    }

    public static boolean initInstance() {
        if (instance == null) {
            instance = new NetworkService();
            return true;
        }
        return false;
    }

    public DataOutputStream getOs() {
        return os;
    }

    public DataInputStream getIs() {
        return is;
    }

    public void destroyInstance() throws IOException {
        is.close();
        os.close();
        socket.close();
        instance = null;
        username = null;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
