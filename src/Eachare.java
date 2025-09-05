import network.Server;
import controller.PeerController;
import utils.*;

import java.io.IOException;

public class Eachare {

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("\nUso: java Eachare <endereco:porta> <vizinhos.txt> <diretorio_compartilhado>");
            return;
        }

        String[] partes = args[0].split(":");
        String ip = partes[0];
        String porta = partes[1];
        String caminhoArquivo = args[1];
        String nomeDiretorio = args[2];

        PeerController peerController = new PeerController(args[0], nomeDiretorio);
        if (!FileUtils.carregarPeers(caminhoArquivo, peerController)) {
            return;
        }

        if (!FileUtils.verificarDiretorio(nomeDiretorio)) {
            return;
        }

        try {
            Server server = new Server(ip, Integer.parseInt(porta), peerController);
            Thread serverThread = new Thread(() -> {
                try {
                    server.iniciar();
                } catch (IOException e) {
                    System.out.println("\nErro ao iniciar servidor: " + e.getMessage());
                    System.exit(-1);
                }
            });
            serverThread.start();
            Thread.sleep(1000);
        } catch (Exception e) {
            System.out.println("\nFalha ao iniciar servidor: " + e.getMessage());
            return;
        }

        Menu menu = new Menu(peerController, nomeDiretorio);
        menu.exibir();
    }
}
