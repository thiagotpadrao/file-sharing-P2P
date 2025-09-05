package utils;

import java.util.*;

public class EstatisticaManager {

    public static class ChaveEstatistica {
        int chunkSize;
        int numPeers;
        int tamanhoArquivo;

        public ChaveEstatistica(int chunkSize, int numPeers, int tamanhoArquivo) {
            this.chunkSize = chunkSize;
            this.numPeers = numPeers;
            this.tamanhoArquivo = tamanhoArquivo;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ChaveEstatistica)) return false;
            ChaveEstatistica that = (ChaveEstatistica) o;
            return chunkSize == that.chunkSize &&
                   numPeers == that.numPeers &&
                   tamanhoArquivo == that.tamanhoArquivo;
        }

        @Override
        public int hashCode() {
            return Objects.hash(chunkSize, numPeers, tamanhoArquivo);
        }
    }

    private static final Map<ChaveEstatistica, List<Double>> estatisticas = new HashMap<>();

    public static void registrarTempo(int chunkSize, int numPeers, int tamanhoArquivo, double tempoSegundos) {
        ChaveEstatistica chave = new ChaveEstatistica(chunkSize, numPeers, tamanhoArquivo);
        estatisticas.computeIfAbsent(chave, k -> new ArrayList<>()).add(tempoSegundos);
    }

    public static void exibirEstatisticas() {
        System.out.println(String.format("\n%-12s | %-8s | %-14s | %-3s | %-10s | %-10s",
                "Tam. Chunk", "N Peers", "Tam. Arquivo", "N", "Tempo [s]", "Desvio"));

        for (Map.Entry<ChaveEstatistica, List<Double>> entrada : estatisticas.entrySet()) {
            ChaveEstatistica chave = entrada.getKey();
            List<Double> tempos = entrada.getValue();
            double media = calcularMedia(tempos);
            double desvio = calcularDesvioPadrao(tempos, media);

            System.out.println(String.format("%-12d | %-8d | %-14d | %-3d | %-10.5f | %-10.5f",
                    chave.chunkSize, chave.numPeers, chave.tamanhoArquivo,
                    tempos.size(), media, desvio));
        }
    }

    private static double calcularMedia(List<Double> tempos) {
        return tempos.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    private static double calcularDesvioPadrao(List<Double> tempos, double media) {
        double soma = 0.0;
        for (double tempo : tempos) {
            soma += Math.pow(tempo - media, 2);
        }
        return tempos.size() > 1 ? Math.sqrt(soma / (tempos.size() - 1)) : 0.0;
    }
}