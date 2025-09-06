package org.dictionary;

// --- DictionaryServer.java ---
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class DictionaryServer {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java -jar DictionaryServer.jar <port> <dictionary-file-path>");
            return;
        }

        int port = Integer.parseInt(args[0]);
        String dictPath = args[1];

        DictionaryManager manager = new DictionaryManager(dictPath);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on port " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress().getHostAddress());

                // Create a new thread for each client
                ClientHandler clientHandler = new ClientHandler(clientSocket, manager);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
