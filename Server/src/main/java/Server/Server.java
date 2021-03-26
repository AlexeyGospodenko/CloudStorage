package Server;

public class Server {

    public static void main(String[] args) {
        int portBaseServer = (args.length == 1) ? Integer.parseInt(args[0]) : 1234;
        ClientsThread clientThread = new ClientsThread(portBaseServer);
        clientThread.start();
        int portTelnetServer = (args.length == 2) ? Integer.parseInt(args[1]) : 2345;
        TelnetThread telnetThread = new TelnetThread(portTelnetServer);
        telnetThread.start();

        String jdbc = (args.length == 3) ? args[2] : "jdbc:oracle:thin:@localhost:1521:xe";
        DatabaseService.initInstance(jdbc);
    }
}
