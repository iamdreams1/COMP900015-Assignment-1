package org.dictionary;

// --- DictionaryServer.java ---
// --- DictionaryServer.java (Modified) ---
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.net.Socket;

public class DictionaryServer implements Runnable {
    private final List<Socket> activeClientSockets = Collections.synchronizedList(new ArrayList<>());
    private final int port;
    private final DictionaryManager manager;
    private volatile boolean isRunning = false;
    private ServerSocket serverSocket;
    private final AtomicInteger activeClientCount = new AtomicInteger(0);

    // Callbacks to update the GUI
    private Consumer<String> logUpdater;
    private Consumer<Integer> clientCountUpdater;

    public DictionaryServer(int port, String dictPath) {
        this.port = port;
        this.manager = new DictionaryManager(dictPath);
    }

    public void setLogUpdater(Consumer<String> logUpdater) {
        this.logUpdater = logUpdater;
    }

    public void setClientCountUpdater(Consumer<Integer> clientCountUpdater) {
        this.clientCountUpdater = clientCountUpdater;
    }

    @Override
    public void run() {
        isRunning = true;
        try {
            serverSocket = new ServerSocket(port);
            logUpdater.accept("Server started on port " + port);

            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                activeClientSockets.add(clientSocket); // <-- ADD THIS LINE
                logUpdater.accept("New client connected: " + clientSocket.getInetAddress().getHostAddress());

                clientCountUpdater.accept(activeClientCount.incrementAndGet());

                Runnable onClientDisconnect = () -> {
                    clientCountUpdater.accept(activeClientCount.decrementAndGet());
                    activeClientSockets.remove(clientSocket); // <-- ADD THIS LINE
                    logUpdater.accept("Client disconnected: " + clientSocket.getInetAddress().getHostAddress());
                };

                ClientHandler clientHandler = new ClientHandler(clientSocket, manager, onClientDisconnect);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            if (isRunning) {
                logUpdater.accept("Server error: " + e.getMessage());
            }
        } finally {
            // When the server stops, ensure all remaining client connections are closed.
            stopServer();
        }
    }

    public void stopServer() {
        isRunning = false;
        try {
            // Close all active client connections first
            // We iterate over a copy to avoid modification issues while iterating
            for (Socket clientSocket : new ArrayList<>(activeClientSockets)) {
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
            }

            // Then, close the main server socket
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            logUpdater.accept("Error while stopping server: " + e.getMessage());
        }
    }
}
