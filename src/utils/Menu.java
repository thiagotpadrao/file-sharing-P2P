package utils;

import controller.PeerController;

import java.util.Scanner;

public class Menu {
    private final PeerController peerController;
    private final String nomeDiretorio;

    public Menu(PeerController peerController, String nomeDiretorio) {
        this.peerController = peerController;
        this.nomeDiretorio = nomeDiretorio;
    }

    public void exibir() {
        @SuppressWarnings("resource")
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\nEscolha um comando:");
            System.out.println("\t[1] Listar peers");
            System.out.println("\t[2] Obter peers");
            System.out.println("\t[3] Listar arquivos locais");
            System.out.println("\t[4] Buscar arquivos");
            System.out.println("\t[5] Exibir estatísticas");
            System.out.println("\t[6] Alterar tamanho de chunk");
            System.out.println("\t[9] Sair");
            System.out.print(" > ");

            int opcao;
            try {
                opcao = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Opção inválida.");
                continue;
            }

            switch (opcao) {
                case 1:
                    peerController.listarPeers();
                    break;
                case 2:
                    peerController.obterPeers();
                    break;
                case 3:
                    FileUtils.listarArquivos(nomeDiretorio);
                    break;
                case 4:
                    peerController.buscarArquivos();
                    break;
                case 5:
                    EstatisticaManager.exibirEstatisticas();
                    break;
                case 6:
                    peerController.alterarChunk();
                    break;
                case 9:
                    peerController.despedidaPeers();
                    System.exit(0);
                default:
                    System.out.println("Opção não reconhecida.");
            }

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
