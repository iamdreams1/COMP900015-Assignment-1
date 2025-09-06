package org.dictionary;

// --- ClientHandler.java ---
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final DictionaryManager dictionaryManager;
    private final Gson gson = new Gson();

    public ClientHandler(Socket socket, DictionaryManager manager) {
        this.clientSocket = socket;
        this.dictionaryManager = manager;
    }

    @Override
    public void run() {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Parse the JSON request from the client
                JsonObject request = gson.fromJson(line, JsonObject.class);
                String command = request.get("command").getAsString();

                JsonObject response = new JsonObject();

                // Process the command
                switch (command.toLowerCase()) {
                    case "query":
                        // TODO: Handle query logic and build response
                        break;
                    case "add":
                        // TODO: Handle add logic and build response
                        break;
                    case "remove":
                        // TODO: Handle remove logic and build response
                        break;
                    default:
                        response.addProperty("status", "error");
                        response.addProperty("message", "Unknown command");
                }

                writer.println(gson.toJson(response));
            }
        } catch (IOException e) {
            System.out.println("Client disconnected: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
