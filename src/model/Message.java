package model;

public class Message {
    private String origem;
    private int clock;
    private String tipo;
    private String[] argumentos;

    public Message(String origem, int clock, String tipo, String... argumentos) {
        this.origem = origem;
        this.clock = clock;
        this.tipo = tipo;
        this.argumentos = argumentos;
    }

    public String getOrigem() {
        return origem;
    }

    public int getClock() {
        return clock;
    }

    public String getTipo() {
        return tipo;
    }

    public String[] getArgumentos() {
        return argumentos;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(origem + " " + clock + " " + tipo);
        for (String arg : argumentos) {
            sb.append(" ").append(arg);
        }
        return sb.toString();
    }

    public static Message fromString(String mensagem) throws IllegalArgumentException {
        String[] partes = mensagem.trim().split(" ");
        if (partes.length < 3) {
            throw new IllegalArgumentException("Formato de mensagem inválido");
        }
        String origem = partes[0];
        int clock = Integer.parseInt(partes[1]);
        String tipo = partes[2];
        String[] argumentos = new String[partes.length - 3];
        System.arraycopy(partes, 3, argumentos, 0, argumentos.length);

        return new Message(origem, clock, tipo, argumentos);
    }
} 
