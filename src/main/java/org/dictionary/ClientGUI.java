package org.dictionary;

// --- ClientGUI.java ---
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

public class ClientGUI extends JFrame {
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private final Gson gson = new Gson();

    private JTextArea responseArea;
    private final int delay; // <-- Add this field


    public ClientGUI(String address, int port, int delay) {

        super("Dictionary Client");

        this.delay = delay; // <-- Store the delay

        // --- Main UI Components ---
        JTabbedPane tabbedPane = new JTabbedPane();
        responseArea = new JTextArea(10, 40);
        responseArea.setEditable(false);

        // --- Create and Add Tabs ---
        tabbedPane.addTab("Search / Remove", createSearchRemovePanel());
        tabbedPane.addTab("Add Word", createAddWordPanel());
        tabbedPane.addTab("Modify Word", createModifyWordPanel());

        // --- Main Layout ---
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(tabbedPane, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(responseArea), BorderLayout.CENTER);
        add(mainPanel);

        // --- Window Setup ---
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);

        // --- Connect to Server ---
        try {
            socket = new Socket(address, port);
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Could not connect to server: " + e.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private JPanel createSearchRemovePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JTextField wordField = new JTextField(25);
        JButton queryButton = new JButton("Query Word");
        JButton removeButton = new JButton("Remove Word");

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Word:"), gbc);
        gbc.gridx = 1; panel.add(wordField, gbc);
        gbc.gridx = 1; gbc.gridy = 1;

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(queryButton);
        buttonPanel.add(removeButton);
        panel.add(buttonPanel, gbc);

        queryButton.addActionListener(e -> {
            JsonObject request = new JsonObject();
            request.addProperty("command", "query");
            request.addProperty("word", wordField.getText().trim());
            sendRequest(request);

        });

        removeButton.addActionListener(e -> {
            JsonObject request = new JsonObject();
            request.addProperty("command", "remove");
            request.addProperty("word", wordField.getText().trim());
            sendRequest(request);

            if (this.delay > 0) {
                request.addProperty("delay", this.delay);
            }
        });

        return panel;
    }

    private JPanel createAddWordPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JTextField wordField = new JTextField(25);
        JTextArea meaningArea = new JTextArea(5, 25);
        meaningArea.setToolTipText("Enter one meaning per line.");
        JButton addButton = new JButton("Add New Word");

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Word:"), gbc);
        gbc.gridx = 1; panel.add(wordField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Meanings:"), gbc);
        gbc.gridx = 1; panel.add(new JScrollPane(meaningArea), gbc);
        gbc.gridx = 1; gbc.gridy = 2; panel.add(addButton, gbc);

        addButton.addActionListener(e -> {
            String[] meanings = meaningArea.getText().split("\\n");
            JsonObject request = new JsonObject();
            request.addProperty("command", "add");
            request.addProperty("word", wordField.getText().trim());
            request.add("meanings", gson.toJsonTree(Arrays.asList(meanings)));
            sendRequest(request);

            if (this.delay > 0) {
                request.addProperty("delay", this.delay);
            }
        });

        return panel;
    }

    private JPanel createModifyWordPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JTextField wordField = new JTextField(25);
        JTextField oldMeaningField = new JTextField(25);
        JTextField newMeaningField = new JTextField(25);
        JButton addMeaningButton = new JButton("Add New Meaning");
        JButton updateMeaningButton = new JButton("Update Existing Meaning");

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Word:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; panel.add(wordField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1; panel.add(new JLabel("Old Meaning:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; panel.add(oldMeaningField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1; panel.add(new JLabel("New Meaning:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; panel.add(newMeaningField, gbc);

        gbc.gridx = 1; gbc.gridy = 3; gbc.gridwidth = 1; panel.add(addMeaningButton, gbc);
        gbc.gridx = 2; gbc.gridy = 3; panel.add(updateMeaningButton, gbc);

        addMeaningButton.addActionListener(e -> {
            JsonObject request = new JsonObject();
            request.addProperty("command", "addMeaning");
            request.addProperty("word", wordField.getText().trim());
            request.addProperty("newMeaning", newMeaningField.getText().trim());
            sendRequest(request);

            if (this.delay > 0) {
                request.addProperty("delay", this.delay);
            }
        });

        updateMeaningButton.addActionListener(e -> {
            JsonObject request = new JsonObject();
            request.addProperty("command", "updateMeaning");
            request.addProperty("word", wordField.getText().trim());
            request.addProperty("oldMeaning", oldMeaningField.getText().trim());
            request.addProperty("newMeaning", newMeaningField.getText().trim());
            sendRequest(request);

            if (this.delay > 0) {
                request.addProperty("delay", this.delay);
            }
        });

        return panel;
    }

    private void sendRequest(JsonObject request) {
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                writer.println(gson.toJson(request));
                try {
                    return reader.readLine();
                } catch (IOException e) {
                    return "{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}";
                }
            }

            @Override
            protected void done() {
                try {
                    String jsonResponse = get();
                    if (jsonResponse == null) {
                        responseArea.setText("Server disconnected.");
                        return;
                    }

                    JsonObject responseObj = gson.fromJson(jsonResponse, JsonObject.class);
                    String status = responseObj.get("status").getAsString();

                    if ("success".equalsIgnoreCase(status)) {
                        if (responseObj.has("meanings")) {
                            JsonArray meaningsArray = responseObj.getAsJsonArray("meanings");
                            StringBuilder meaningsText = new StringBuilder("Success! Meanings:\n");
                            for (JsonElement meaning : meaningsArray) {
                                meaningsText.append("- ").append(meaning.getAsString()).append("\n");
                            }
                            responseArea.setText(meaningsText.toString());
                        } else {
                            responseArea.setText("Success: " + responseObj.get("message").getAsString());
                        }
                    } else { // Error status
                        responseArea.setText("Error: " + responseObj.get("message").getAsString());
                    }
                } catch (Exception e) {
                    responseArea.setText("An application error occurred: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }
}