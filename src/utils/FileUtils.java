package utils;

import controller.PeerController;

import java.io.*;
import java.util.Arrays;
import java.util.Base64;

public class FileUtils {

    public static boolean carregarPeers(String caminhoArquivo, PeerController peerController) {
        File arquivo = new File(caminhoArquivo);
        if (!arquivo.exists() || !arquivo.isFile()) {
            System.out.println("\nArquivo de peers não encontrado: " + caminhoArquivo);
            return false;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(arquivo))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                String[] partes = linha.split(":");
                if (partes.length == 2) {
                    peerController.adicionarPeer(partes[0], partes[1], false, 0);
                } else {
                    System.out.println("\nFormato inválido no arquivo de peers: " + linha);
                }
            }
            return true;
        } catch (IOException e) {
            System.out.println("\nErro ao ler arquivo de peers: " + e.getMessage());
            return false;
        }
    }

    public static boolean verificarDiretorio(String nomeDiretorio) {
        File diretorio = new File(nomeDiretorio);
        if (diretorio.exists() && diretorio.isDirectory() && diretorio.canRead()) {
            return true;
        }
        
        System.out.println("\nDiretório inválido ou inacessível: " + nomeDiretorio);
        return false;
    }

    public static void listarArquivos(String nomeDiretorio) {
        File diretorio = new File(nomeDiretorio);
        if (!diretorio.exists() || !diretorio.isDirectory()) {
            System.out.println("\nDiretório inválido ou inacessível: " + nomeDiretorio);
            return;
        }

        File[] arquivos = diretorio.listFiles();
        if (arquivos == null || arquivos.length == 0) {
            System.out.println("\nNenhum arquivo encontrado no diretório.");
            return;
        }

        System.out.println("\nArquivos disponíveis:");
        for (File arquivo : arquivos) {
            if (arquivo.isFile()) {
                System.out.println("\n- " + arquivo.getName());
            }
        }
    }

    public static ChunkInfo base64(File arquivo, int tamanhoChunk, int numChunk) {
        byte[] buffer = new byte[tamanhoChunk];
        int bytesLidos = 0;

        try (RandomAccessFile raf = new RandomAccessFile(arquivo, "r")) {
            long posicaoInicial = (long) numChunk * tamanhoChunk;

            if (posicaoInicial >= raf.length()) {
                return new ChunkInfo("", 0);
            }

            raf.seek(posicaoInicial);
            bytesLidos = raf.read(buffer);

            if (bytesLidos == -1) {
                return new ChunkInfo("", 0);
            }

        } catch (IOException e) {
            e.printStackTrace();
            return new ChunkInfo("", 0);
        }

        String base64 = Base64.getEncoder().encodeToString(
            bytesLidos == tamanhoChunk ? buffer : Arrays.copyOf(buffer, bytesLidos)
        );

        return new ChunkInfo(base64, bytesLidos);
    }

    public static void salvaArquivo(byte[] dados, String nomeArquivo, String nomeDiretorio) throws IOException {
        FileOutputStream fos = new FileOutputStream(nomeDiretorio + "/" + nomeArquivo);
        fos.write(dados);
        fos.close();
        System.out.println("\nDownload do arquivo " + nomeArquivo + " finalizado.");
    }

    public static String montaLsList(String nomeDiretorio) {
        File diretorio = new File(nomeDiretorio);
        if (!diretorio.exists() || !diretorio.isDirectory()) {
            System.out.println("\nDiretório inválido ou inacessível: " + nomeDiretorio);
            return "";
        }

        File[] arquivos = diretorio.listFiles();
        if (arquivos == null || arquivos.length == 0) {
            System.out.println("\nNenhum arquivo encontrado no diretório.");
            return "";
        }

        StringBuilder argumentos = new StringBuilder();
        StringBuilder mensagem = new StringBuilder("LS_LIST ");
        StringBuilder mensagemZerada = new StringBuilder("LS_LIST ");
        int numArquivos = 0;
        
        for (File arquivo : arquivos) {
            if (arquivo.isFile()) {
                argumentos.append(" ").append(arquivo.getName()).append(":").append(arquivo.length());
                numArquivos++;
            }
        }

        mensagem.append(String.valueOf(numArquivos));
        mensagem.append(argumentos);
        mensagemZerada.append(String.valueOf(0));

        if (numArquivos > 0) {
            return mensagem.toString();
        } else {
            return mensagemZerada.toString();
        }
    }

    public static class ChunkInfo {
        private final String base64;
        private final int tamanho;

        public ChunkInfo(String base64, int tamanho) {
            this.base64 = base64;
            this.tamanho = tamanho;
        }

        public String getBase64() {
            return base64;
        }

        public int getTamanho() {
            return tamanho;
        }
    }

}
