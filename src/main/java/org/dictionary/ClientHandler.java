package org.dictionary;

// --- ClientHandler.java ---

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.List;




public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final DictionaryManager dictionaryManager;
    private final Gson gson = new Gson();
    private final Runnable onDisconnect; // <-- ADD THIS

    // Modify the constructor
    public ClientHandler(Socket socket, DictionaryManager manager, Runnable onDisconnect) {
        this.clientSocket = socket;
        this.dictionaryManager = manager;
        this.onDisconnect = onDisconnect; // <-- ADD THIS
    }

    @Override
    public void run() {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    JsonObject request = gson.fromJson(line, JsonObject.class);
                    if (request == null) continue; // Ignore empty lines

                    String command = request.get("command").getAsString();
                    JsonObject response = new JsonObject();

                    // --- Your existing switch statement goes here ---
                    switch (command) {
                        case "query": {
                            String wordToSearch = request.get("word").getAsString();
                            List<String> meanings = dictionaryManager.query(wordToSearch);

                            if (meanings != null) {
                                // Word was found, so build a success response.
                                response.addProperty("status", "success");

                                // 4. Convert the List<String> to a JsonElement (which will be a JsonArray).
                                JsonElement meaningsElement = gson.toJsonTree(meanings);

                                // 5. Add the JSON array to the response object under the "meanings" key.
                                response.add("meanings", meaningsElement);

                            } else {
                                // Word was not found, so build an error response.
                                response.addProperty("status", "error");
                                response.addProperty("message", "Word '" + wordToSearch + "' not found.");
                            }
                            break;
                        }
                        case "add": {
                            String wordToAdd = request.get("word").getAsString();
                            JsonArray meaningsArray = request.getAsJsonArray("meanings");
                            Type listType = new TypeToken<List<String>>() {}.getType();
                            List<String> meanings = gson.fromJson(meaningsArray, listType);

                            long delay = request.has("delay") ? request.get("delay").getAsLong() : 0;

                            // NEW: Check if any of the meanings in the list are empty or just whitespace.
                            boolean hasEmptyMeaning = meanings.stream().anyMatch(m -> m == null || m.trim().isEmpty());

                            if (wordToAdd == null || wordToAdd.trim().isEmpty() || meanings.isEmpty() || hasEmptyMeaning) {
                                response.addProperty("status", "error");
                                response.addProperty("message", "Word or meanings cannot be empty.");
                            } else {
                                String status = dictionaryManager.add(wordToAdd, meanings, delay);

                                response.addProperty("status", status.toLowerCase());
                                if ("SUCCESS".equals(status)) {
                                    response.addProperty("message", "Word '" + wordToAdd + "' added successfully.");
                                } else {
                                    response.addProperty("message", "Word '" + wordToAdd + "' already exists.");
                                }
                            }
                            break;
                        }

                        case "remove": {
                            long delay = request.has("delay") ? request.get("delay").getAsLong() : 0;

                            String wordToRemove = request.get("word").getAsString();

                            String status = dictionaryManager.remove(wordToRemove, delay);

                            response.addProperty("status", status.toLowerCase());
                            if ("SUCCESS".equals(status)) {
                                response.addProperty("message", "Word '" + wordToRemove + "' removed successfully.");
                            } else { // Assumes DUPLICATE status
                                response.addProperty("message", "Word '" + wordToRemove + "' doesn't exist.");
                            }
                            break;
                        }
                        case "updateMeaning": {
                            String word = request.get("word").getAsString();
                            String oldMeaning = request.get("oldMeaning").getAsString();
                            String newMeaning = request.get("newMeaning").getAsString();
                            long delay = request.has("delay") ? request.get("delay").getAsLong() : 0;


                            // NEW: Add validation for the new meaning.
                            if (newMeaning == null || newMeaning.trim().isEmpty()) {
                                response.addProperty("status", "error");
                                response.addProperty("message", "New meaning cannot be empty.");
                            } else {
                                String status = dictionaryManager.updateMeaning(word, oldMeaning, newMeaning, delay);

                                response.addProperty("status", status.toLowerCase());
                                switch (status) {
                                    case "SUCCESS":
                                        response.addProperty("message", "Meaning updated successfully for '" + word + "'.");
                                        break;
                                    case "WORD_NOT_FOUND":
                                        response.addProperty("message", "Word '" + word + "' not found.");
                                        break;
                                    case "MEANING_EXISTS":
                                        response.addProperty("message", "This meaning already exists for the word '" + word + "'.");
                                        break;
                                    case "MEANING_NOT_FOUND":
                                        response.addProperty("message", "The specified meaning to update was not found.");
                                        break;
                                }
                            }
                            break;
                        }
                        case "addMeaning": {
                            String word = request.get("word").getAsString();
                            String newMeaning = request.get("newMeaning").getAsString();
                            long delay = request.has("delay") ? request.get("delay").getAsLong() : 0;


                            // NEW: Add validation for the new meaning.
                            if (newMeaning == null || newMeaning.trim().isEmpty()) {
                                response.addProperty("status", "error");
                                response.addProperty("message", "New meaning cannot be empty.");
                            } else {
                                String status = dictionaryManager.addNewMeaning(word, newMeaning, delay);
                                response.addProperty("status", status.toLowerCase());
                                switch (status) {
                                    case "SUCCESS":
                                        response.addProperty("message", "New meaning added successfully to '" + word + "'.");
                                        break;
                                    case "WORD_NOT_FOUND":
                                        response.addProperty("message", "Word '" + word + "' not found.");
                                        break;
                                    case "MEANING_EXISTS":
                                        response.addProperty("message", "This meaning already exists for the word '" + word + "'.");
                                        break;
                                }
                            }
                            break;
                        }
                        default:
                            response.addProperty("status", "error");
                            response.addProperty("message", "Unknown command");
                    }

                    writer.println(gson.toJson(response));

                } catch (Exception e) { // Catch JsonSyntaxException or a general Exception
                    // This block now catches the bad JSON error
                    System.err.println("Error parsing JSON from client: " + line);

                    // Build and send a proper error response to the client
                    JsonObject errorResponse = new JsonObject();
                    errorResponse.addProperty("status", "error");
                    errorResponse.addProperty("message", "Malformed JSON request received.");
                    writer.println(gson.toJson(errorResponse));
                }
            }
        } catch (IOException e) {
            System.out.println("Client disconnected: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            onDisconnect.run();
        }
    }
}
