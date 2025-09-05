package network;

import model.Message;

import java.io.*;
import java.net.*;

public class Client {

    public static boolean enviarMensagem(String endereco, int porta, Message mensagem) {
        try (Socket socket = new Socket(endereco, porta);
             PrintWriter saida = new PrintWriter(socket.getOutputStream(), true);) {

            System.out.println("\nEncaminhando mensagem: \"" + mensagem + "\" para " + endereco + ":" + porta);
            Thread.sleep(100);
            saida.println(mensagem.toString());

            return true;
        } catch (IOException e) {
            System.out.println("\nFalha ao enviar mensagem para " + endereco + ":" + porta + " - " + e.getMessage());
            return false;
        } catch (InterruptedException e) {
            System.out.println("Thread foi interrompida");
            return false;
        }
    }
} 
