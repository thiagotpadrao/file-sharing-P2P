package network;

import controller.PeerController;
import model.Message;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;
    private MessageHandler messageHandler;

    public ClientHandler(Socket socket, PeerController peerController) {
        this.socket = socket;
        messageHandler = new MessageHandler(peerController);
    }

    @Override
    public void run() {
        try (BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true)) {
    
            String mensagemRecebida = input.readLine();
    
            if (mensagemRecebida != null) {
                Message mensagem = Message.fromString(mensagemRecebida);
                if (mensagemRecebida.length() >= 65 && mensagem.getTipo().equals("FILE")) {
                    System.out.println("\nResposta recebida: \"" + mensagemRecebida.substring(0, 65) + "\"");
                } else {
                    System.out.println("\nResposta recebida: \"" + mensagemRecebida + "\"");
                }
                messageHandler.processarMensagem(mensagem);
            }
        } catch (IOException e) {
            System.out.println("\nErro ao processar mensagem: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("\nErro ao fechar socket: " + e.getMessage());
            }
        }
    }
}
