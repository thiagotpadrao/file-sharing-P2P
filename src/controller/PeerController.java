package controller;

import model.*;
import network.Client;
import network.MessageHandler;
import utils.EstatisticaManager;
import utils.FileUtils;
import utils.FileUtils.ChunkInfo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class PeerController {

    public static int CHUNK_PADRAO = 256;

    private Map<String, Peer> peers;
    private final String endereco;
    private Clock clockLocal;
    private String diretorioCompartilhado;
    private int chunk;

    public PeerController(String endereco, String diretorio) {
        this.peers = new HashMap<>();
        clockLocal = new Clock();
        this.endereco = endereco;
        this.diretorioCompartilhado = diretorio;
        chunk = CHUNK_PADRAO;
    }

    public void adicionarPeer(String endereco, String porta, boolean isOnline, int clock) {
        try {
            Integer.parseInt(porta);
        } catch (NumberFormatException e) {
            System.out.println("\nPorta inválida.");
            return;
        }
        String chave = endereco + ":" + porta;
        if (!peers.containsKey(chave)) {
            peers.put(chave, new Peer(endereco, Integer.parseInt(porta), isOnline, clock));
            System.out.println("\nAdicionando novo peer " + chave + " status " + (isOnline ? "ONLINE" : "OFFLINE"));
        }
    }

    public void atualizarPeerStatus(String enderecoCompleto, boolean isOnline) {
        Peer peer = peers.get(enderecoCompleto);
        if (peer != null) {
            peer.setStatus(isOnline);
            System.out.println("\nAtualizando peer " + enderecoCompleto + " status " + (isOnline ? "ONLINE" : "OFFLINE"));
        } else {
            System.out.println("Peer inválido");
        }
    }

    public boolean atualizarPeerClock(String enderecoCompleto, int clockRecebido) {
        Peer peer = peers.get(enderecoCompleto);
        boolean atualizou = false;
        if (peer != null) {
            atualizou = peer.getClock().atualizar(clockRecebido);
        } else {
            System.out.println("Peer inválido");
        }
        return atualizou;
    }

    public void listarPeers() {
        if (peers.isEmpty()) {
            System.out.println("\nNenhum peer conhecido.");
            return;
        }

        System.out.println("\nLista de peers:");
        System.out.println("\t[0] Voltar para o menu anterior");
        List<String> peerKeys = new ArrayList<>(peers.keySet());

        for (int i = 0; i < peerKeys.size(); i++) {
            Peer peer = peers.get(peerKeys.get(i));
            System.out.println("\t[" + (i + 1) + "] " + peer.getEndereco() + ":" + peer.getPorta() + " " +
                    (peer.isOnline() ? "ONLINE" : "OFFLINE") + " (clock: " + peer.getClock().getValor() + ")");
        }

        @SuppressWarnings("resource")
        Scanner scanner = new Scanner(System.in);
        System.out.print("> ");
        int escolha;

        try {
            escolha = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("\nEscolha inválida.");
            return;
        }

        if (escolha == 0) return;

        if (escolha > 0 && escolha <= peerKeys.size()) {
            String peerEscolhido = peerKeys.get(escolha - 1);
            Peer peer = peers.get(peerEscolhido);

            clockLocal.incrementar();
            int clockValor = clockLocal.getValor();
            
            Message mensagem = new Message(this.endereco, clockValor, "HELLO");
        
            boolean enviado = Client.enviarMensagem(peer.getEndereco(), peer.getPorta(), mensagem);
        
            atualizarPeerStatus(peerEscolhido, enviado);
        }
            else {
            System.out.println("\nOpção inválida.");
        }
    }

    public void obterPeers() {
        
        for (Peer peer : peers.values()) {
            clockLocal.incrementar();
            int clockValor = clockLocal.getValor();

            String endereco = peer.getEndereco();
            int porta = peer.getPorta();
    
            Message mensagem = new Message(this.endereco, clockValor, "GET_PEERS");
    
            Client.enviarMensagem(endereco, porta, mensagem);
        }
    } 

    public void enviarListaPeers(String destino) {
        clockLocal.incrementar();
        int clockValor = clockLocal.getValor();

        StringBuilder sb = new StringBuilder();
        StringBuilder fim = new StringBuilder("PEER_LIST ");
        StringBuilder zerado = new StringBuilder("PEER_LIST ");
        int contador = 0;
        for (Peer peer : peers.values()) {
            String teste = peer.getEndereco() + ":" + String.valueOf(peer.getPorta());
            if (!teste.equals(destino)) {
                sb.append(" ").append(peer.getEndereco()).append(":").append(peer.getPorta()).append(":");
                sb.append(peer.isOnline() ? "ONLINE" : "OFFLINE").append(":").append(peer.getClock().getValor());
                contador++;
            }
        }
        fim.append(String.valueOf(contador));
        fim.append(sb);
        zerado.append(String.valueOf(0));
        Message mensagem = new Message(this.endereco, clockValor, fim.toString());
        Message mensagem1= new Message(this.endereco, clockValor, zerado.toString());
        String[] partes = destino.split(":");
        if (contador > 0) {
            Client.enviarMensagem(partes[0], Integer.parseInt(partes[1]), mensagem);
        } else {
            Client.enviarMensagem(partes[0], Integer.parseInt(partes[1]), mensagem1);
        }
    }

    public void enviarListaArquivos(String destino) {
        clockLocal.incrementar();
        int clockValor = clockLocal.getValor();

        String lsList = FileUtils.montaLsList(diretorioCompartilhado);

        Message mensagem = new Message(this.endereco, clockValor, lsList);
        String[] partes = destino.split(":");
        Client.enviarMensagem(partes[0], Integer.parseInt(partes[1]), mensagem); 
    }

    public void processarPeerList(Message mensagem) {
        String[] argumentos = mensagem.getArgumentos();
        
        for (int i = 1; i < argumentos.length; i++) {
            String[] peerInfo = mensagem.getArgumentos()[i].split(":");
            
            String endereco = peerInfo[0] + ":" + peerInfo[1];
            boolean status = peerInfo[2].equals("ONLINE");
            int clock = Integer.parseInt(peerInfo[3]);
            
            if (peers.containsKey(endereco)) {
                boolean clockAtualizou = atualizarPeerClock(endereco, clock);
                if (clockAtualizou) {
                    atualizarPeerStatus(endereco, status);
                }
            } else {
                adicionarPeer(peerInfo[0], peerInfo[1], status, clock);
            }
        }
    }

    public void listarArquivos(List<Message> mensagens) {
        
        class ArquivoInfo {
            String nome;
            String tamanho;
            Set<String> peers;

            ArquivoInfo(String nome, String tamanho) {
                this.nome = nome;
                this.tamanho = tamanho;
                this.peers = new LinkedHashSet<>();
            }

            void adicionarPeer(String peer) {
                peers.add(peer);
            }
        }
        
        Map<String, ArquivoInfo> mapaArquivos = new LinkedHashMap<>();
        
        for (Message mensagem : mensagens) {
            String peer = mensagem.getOrigem();
            String[] argumentos = mensagem.getArgumentos();

            for (int i = 1; i < argumentos.length; i++) {
                String[] info = mensagem.getArgumentos()[i].split(":");
                String nomeArquivo = info[0];
                String tamanhoArquivo = info[1];
                String chave = nomeArquivo + "|" + tamanhoArquivo;

                if (!mapaArquivos.containsKey(chave)) {
                    mapaArquivos.put(chave, new ArquivoInfo(nomeArquivo, tamanhoArquivo));
                }
                
                mapaArquivos.get(chave).adicionarPeer(peer);
            }
        }

        List<ArquivoInfo> arquivos = new ArrayList<>(mapaArquivos.values());
        
        System.out.println("\nArquivos encontrados na rede:");
        System.out.println(String.format("\t%-26s | %-8s | %-20s", "Nome", "Tamanho", "Peer"));
        System.out.println(String.format("\t[ 0] %-21s | %-8s | %-20s", "<Cancelar>", "", ""));
        for (int i = 0; i < arquivos.size(); i++) {
            ArquivoInfo a = arquivos.get(i);
            String peersDoArquivo = String.join(", ", a.peers);
            System.out.println(String.format("\t[ %d] %-21s | %-8s | %-20s", i + 1, a.nome, a.tamanho, peersDoArquivo));
        }
    
        @SuppressWarnings("resource")
        Scanner scanner = new Scanner(System.in);
        System.out.print("\nDigite o numero do arquivo para fazer o download:\n> ");
        int escolha;

        try {
            escolha = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("\nEscolha inválida.");
            return;
        }

        if (escolha < 0 || escolha > arquivos.size()) {
            System.out.println("\nEscolha fora do intervalo.");
            return;
        }

        if (escolha == 0) return;

        ArquivoInfo arquivoEscolhido = arquivos.get(escolha - 1);
        System.out.println("\narquivo escolhido " + arquivoEscolhido.nome);
        download(arquivoEscolhido.nome, Integer.parseInt(arquivoEscolhido.tamanho), arquivoEscolhido.peers);
    }

    public void download(String nomeArquivo, int tamanhoArquivo, Set<String> peers) {

        int totalChunks = (int) Math.ceil((double) tamanhoArquivo / getChunk());

        List<String> listaPeers = new ArrayList<>(peers);
        int numPeers = listaPeers.size();

        for (int i = 0; i < totalChunks; i++) {
            clockLocal.incrementar();
            int clockValor = clockLocal.getValor();

            String peerEscolhido = listaPeers.get(i % numPeers);
            String[] partes = peerEscolhido.split(":");
            String endereco = partes[0];
            int porta = Integer.parseInt(partes[1]);

            String argumentos = "DL " + nomeArquivo + " " + getChunk() + " " + i;
            Message mensagem = new Message(this.endereco, clockValor, argumentos);

            Client.enviarMensagem(endereco, porta, mensagem);
        }

        long inicio = System.nanoTime();

        synchronized (MessageHandler.respostasFILE) {
            long tempoInicial = System.currentTimeMillis();
            long tempoLimite = 5000;

            while (MessageHandler.respostasFILE.size() != totalChunks) {
                long tempoPassado = System.currentTimeMillis() - tempoInicial;

                if (tempoPassado >= tempoLimite) {
                    System.out.println("Encerrando espera de chunks por timeout após " + tempoLimite + " ms");
                    System.out.println("O arquivo será salvo mas seu conteúdo está incompleto");
                    break;
                }

                try {
                    MessageHandler.respostasFILE.wait(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

        long fim = System.nanoTime();
        double tempoSegundos = (fim - inicio) / 1_000_000_000.0;

        EstatisticaManager.registrarTempo(chunk, peers.size(), tamanhoArquivo, tempoSegundos);

        armazenarArquivo(MessageHandler.respostasFILE);
        MessageHandler.respostasFILE.clear();
    }

    public void buscarArquivos() {
        int numLsEnviadas = 0;
        for (Peer peer : peers.values()) {
            if (peer.isOnline()) {
                clockLocal.incrementar();
                int clockValor = clockLocal.getValor();
                
                String endereco = peer.getEndereco();
                int porta = peer.getPorta();
        
                Message mensagem = new Message(this.endereco, clockValor, "LS");
                Client.enviarMensagem(endereco, porta, mensagem);

                numLsEnviadas++;
            }
        }

        synchronized (MessageHandler.respostasLSList) {
            long tempoInicial = System.currentTimeMillis();
            long tempoLimite = 5000;
        
            while (MessageHandler.respostasLSList.size() != numLsEnviadas) {
                long tempoPassado = System.currentTimeMillis() - tempoInicial;
        
                // sai do loop se der timeout
                if (tempoPassado >= tempoLimite) {
                    break;
                }
        
                try {
                    MessageHandler.respostasLSList.wait(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }        
        listarArquivos(MessageHandler.respostasLSList);
        MessageHandler.respostasLSList.clear();
    }

    public void enviarArquivo(Message mensagem) {
        clockLocal.incrementar();
        int clockValor = clockLocal.getValor();

        String nomeArquivo = mensagem.getArgumentos()[0];
        int tamanhoChunk = Integer.parseInt(mensagem.getArgumentos()[1]);
        int numChunk = Integer.parseInt(mensagem.getArgumentos()[2]);

        File arquivo = new File(diretorioCompartilhado + "/" + nomeArquivo);
        ChunkInfo chunkLido = FileUtils.base64(arquivo, tamanhoChunk, numChunk);
        String argumentos = nomeArquivo + " " + chunkLido.getTamanho() + " " + numChunk + " " + chunkLido.getBase64();

        String peerOrigem = mensagem.getOrigem();
        String[] partes = peerOrigem.split(":");

        Message resposta = new Message(this.endereco, clockValor, "FILE", argumentos);
        Client.enviarMensagem(partes[0], Integer.parseInt(partes[1]), resposta);
    }

    public void armazenarArquivo(List<Message> mensagens) {
        
        TreeMap<Integer, String> chunksOrdenados = new TreeMap<>();
        String nomeArquivo = null;

        for (Message mensagem : mensagens) {
            String[] argumentos = mensagem.getArgumentos();
            nomeArquivo = argumentos[0];
            int indiceChunk = Integer.parseInt(argumentos[2]);
            String conteudoBase64 = argumentos[3];
            chunksOrdenados.put(indiceChunk, conteudoBase64);
        }

        ByteArrayOutputStream base64Completo = new ByteArrayOutputStream();
        for (String chunk : chunksOrdenados.values()) {
            try {
                byte[] dadosChunk = Base64.getDecoder().decode(chunk);
                base64Completo.write(dadosChunk);
            } catch (IllegalArgumentException e) {
                System.err.println("Erro ao decodificar chunk base64: " + e.getMessage());
                return;
            } catch (IOException e) {
                System.err.println("Erro ao juntar os chunks: " + e.getMessage());
                return;
            }
        }

        try {
            FileUtils.salvaArquivo(base64Completo.toByteArray(), nomeArquivo, diretorioCompartilhado);
        } catch (IOException e) {
            System.err.println("Erro ao salvar arquivo: " + e.getMessage());
        }
    }

    public void despedidaPeers(){
        System.out.println("\nSaindo...");
        
        for (Peer peer : peers.values()) {
            if (peer.isOnline()) {
                clockLocal.incrementar();
                int clockValor = clockLocal.getValor();
                
                String endereco = peer.getEndereco();
                int porta = peer.getPorta();
        
                Message mensagem = new Message(this.endereco, clockValor, "BYE");
                Client.enviarMensagem(endereco, porta, mensagem);
            }
        }
    
    }

    public void alterarChunk() {
        @SuppressWarnings("resource")
        Scanner scanner = new Scanner(System.in);
        System.out.print("\nDigite novo tamanho de chunk:\n> ");
        int novoChunk;

        try {
            novoChunk = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("\nEscolha inválida.");
            return;
        }

        if (novoChunk <= 0) {
            System.out.println("\nEscolha inválida.");
            return;
        }

        setChunk(novoChunk);
        System.out.println("\tTamanho de chunk alterado: " + novoChunk);
    }

    public Map<String, Peer> getPeers() {
        return peers;
    }

    public Clock getClock() {
        return clockLocal;
    }

    public int getChunk() {
        return chunk;
    }

    public void setChunk(int chunk) {
        this.chunk = chunk;
    }
}
