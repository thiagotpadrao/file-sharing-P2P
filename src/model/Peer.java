package model;

public class Peer {
    private String endereco;
    private int porta;
    private boolean status;
    private Clock clock;

    public Peer(String endereco, int porta, boolean status, int clock) {
        this.endereco = endereco;
        this.porta = porta;
        this.status = status;
        this.clock = new Clock();
        this.clock.setValor(clock);
    }

    public String getEndereco() {
        return endereco;
    }

    public int getPorta() {
        return porta;
    }

    public Clock getClock() {
        return clock;
    }

    public boolean isOnline() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
} 
