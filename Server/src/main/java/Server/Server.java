package Server;

public class Server {

    public static void main(String[] args) {
        int portBaseServer = (args.length == 1) ? Integer.parseInt(args[0]) : 1234;
        ClientsThread clientThread = new ClientsThread(portBaseServer);
        clientThread.start();
        int portTelnetServer = (args.length == 2) ? Integer.parseInt(args[1]) : 23;
        TelnetThread telnetThread = new TelnetThread(portTelnetServer);
        telnetThread.start();
    }
}
