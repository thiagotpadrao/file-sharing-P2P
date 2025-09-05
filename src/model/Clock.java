package model;

public class Clock {
    private int valor;

    public Clock() {
        this.valor = 0;
    }

    public synchronized void incrementar() {
        valor++;
        System.out.println("\n=> Atualizando relógio para " + valor);
    }

    public synchronized boolean atualizar(int clockRecebido) {
        int valorInicial = valor;
        valor = Math.max(valor, clockRecebido);
        if (valorInicial == valor) {
            return false;
        } else {
            return true;
        }
    }

    public synchronized int getValor() {
        return valor;
    }
    public synchronized void setValor(int valor) {
        this.valor = valor;
    }
}
