package network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import controller.PeerController;
import model.Message;

public class MessageHandler {

    private PeerController peerController;
    public static final List<Message> respostasLSList = Collections.synchronizedList(new ArrayList<>());
    public static final List<Message> respostasFILE = Collections.synchronizedList(new ArrayList<>());

    public MessageHandler(PeerController peerController) {
        this.peerController = peerController;
    }

    public void processarMensagem(Message mensagem) {
        try {
            peerController.getClock().atualizar(mensagem.getClock());
            peerController.getClock().incrementar();

            String remetente = mensagem.getOrigem();

            switch (mensagem.getTipo()) {
                case "HELLO":
                    if (peerController.getPeers().containsKey(remetente)) {
                        peerController.atualizarPeerStatus(remetente, true);
                        peerController.atualizarPeerClock(remetente, mensagem.getClock());
                    } else {
                        peerController.adicionarPeer(remetente.split(":")[0], (remetente.split(":")[1]), true, mensagem.getClock());
                    }
                    break;
                case "BYE":
                    peerController.atualizarPeerStatus(remetente, false);
                    peerController.atualizarPeerClock(remetente, mensagem.getClock());
                    break;
                case "GET_PEERS":
                    if (peerController.getPeers().containsKey(remetente)) {
                        peerController.atualizarPeerStatus(remetente, true);
                        peerController.atualizarPeerClock(remetente, mensagem.getClock());
                    } else {
                        peerController.adicionarPeer(remetente.split(":")[0], (remetente.split(":")[1]), true, mensagem.getClock());
                    }
                    peerController.enviarListaPeers(remetente);
                    break;
                case "PEER_LIST":
                    peerController.atualizarPeerStatus(remetente, true);
                    peerController.atualizarPeerClock(remetente, mensagem.getClock());
                    peerController.processarPeerList(mensagem);
                    break;
                case "LS":
                    if (peerController.getPeers().containsKey(remetente)) {
                        peerController.atualizarPeerStatus(remetente, true);
                        peerController.atualizarPeerClock(remetente, mensagem.getClock());
                    } else {
                        peerController.adicionarPeer(remetente.split(":")[0], (remetente.split(":")[1]), true, mensagem.getClock());
                    }
                    peerController.enviarListaArquivos(remetente);
                    break;
                case "LS_LIST":
                    peerController.atualizarPeerStatus(remetente, true);
                    peerController.atualizarPeerClock(remetente, mensagem.getClock());
                    synchronized (respostasLSList) {
                        respostasLSList.add(mensagem);
                    }
                    break;
                case "DL":
                    if (peerController.getPeers().containsKey(remetente)) {
                        peerController.atualizarPeerStatus(remetente, true);
                        peerController.atualizarPeerClock(remetente, mensagem.getClock());
                    } else {
                        peerController.adicionarPeer(remetente.split(":")[0], (remetente.split(":")[1]), true, mensagem.getClock());
                    }
                    peerController.enviarArquivo(mensagem);
                    break;
                case "FILE":
                    peerController.atualizarPeerStatus(remetente, true);
                    peerController.atualizarPeerClock(remetente, mensagem.getClock());
                    synchronized (respostasFILE) {
                        respostasFILE.add(mensagem);
                    }
                    break;
                
                default:
                    System.out.println("\nTipo de mensagem desconhecido: " + mensagem.getTipo());
            }

        } catch (IllegalArgumentException e) {
            System.out.println("\nErro ao processar mensagem: " + e.getMessage());
        }
    }
} 
