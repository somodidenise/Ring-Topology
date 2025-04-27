import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class RingNode {
    private int listenPort;
    private String nextHost;
    private int nextPort;
    private boolean isInitiator;

    public RingNode(int listenPort, String nextHost, int nextPort, boolean isInitiator) {
        this.listenPort = listenPort;
        this.nextHost = nextHost;
        this.nextPort = nextPort;
        this.isInitiator = isInitiator;
    }

    public void start() {
        new Thread(this::listen).start();

        if (isInitiator) {
            try {
                Thread.sleep(1000);
                sendValue(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void listen() {
        try (ServerSocket serverSocket = new ServerSocket(listenPort)) {
            System.out.println("Listening on port " + listenPort);
            while (true) {
                try (Socket socket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                    String line = in.readLine();
                    int receivedValue = Integer.parseInt(line);
                    System.out.println("Received: " + receivedValue + " on port " + listenPort);

                    if (receivedValue >= 100) {
                        System.out.println("Reached 100, stopping...");
                        sendValue(receivedValue);
                        break;
                    }

                    sendValue(receivedValue + 1);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendValue(int value) {
        try (Socket socket = new Socket(nextHost, nextPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            out.println(value);
            System.out.println("Sent: " + value + " to " + nextHost + ":" + nextPort);
        } catch (IOException e) {
            System.out.println("Ending communication here.");
        }
    }

    public static void main(String[] args) {
        if (args.length != 4) {
            return;
        }

        int listenPort = Integer.parseInt(args[0]);
        String nextHost = args[1];
        int nextPort = Integer.parseInt(args[2]);
        boolean isInitiator = Boolean.parseBoolean(args[3]);

        RingNode node = new RingNode(listenPort, nextHost, nextPort, isInitiator);
        node.start();
    }
}
