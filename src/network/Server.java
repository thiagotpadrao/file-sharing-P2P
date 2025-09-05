package network;

import controller.PeerController;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private String ip;
    private int porta;
    private PeerController peerController;

    public Server(String ip, int porta, PeerController peerController) {
        this.ip = ip;
        this.porta = porta;
        this.peerController = peerController;
    }

    public void iniciar() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(porta)) {
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(new ClientHandler(socket, peerController)).start();
            }
        } catch (IOException e) {
            System.out.println("\nErro ao iniciar o servidor: " + e.getMessage());
            throw e;
        }
    }
}
