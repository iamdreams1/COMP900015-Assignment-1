package org.dictionary;

// --- ClientGUI.java ---
import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientGUI extends JFrame {
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;

    private JTextField wordField;
    private JTextArea meaningArea;
    private JTextArea responseArea;

    public ClientGUI(String address, int port) {
        super("Dictionary Client");

        // Initialize GUI components
        wordField = new JTextField(20);
        meaningArea = new JTextArea(5, 20);
        responseArea = new JTextArea(10, 30);
        responseArea.setEditable(false);

        JButton queryButton = new JButton("Query");
        JButton addButton = new JButton("Add");
        JButton removeButton = new JButton("Remove");

        // Layout
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Word:"), gbc);
        gbc.gridx = 1; panel.add(wordField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Meaning(s):"), gbc);
        gbc.gridx = 1; panel.add(new JScrollPane(meaningArea), gbc);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(queryButton);
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        gbc.gridx = 1; gbc.gridy = 2; panel.add(buttonPanel, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        panel.add(new JScrollPane(responseArea), gbc);

        add(panel);

        // Add Action Listeners
        queryButton.addActionListener(e -> sendRequest("query"));
        // TODO: Add listeners for 'add' and 'remove' buttons

        // Setup window
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);

        // Connect to server
        try {
            socket = new Socket(address, port);
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Could not connect to server: " + e.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void sendRequest(String command) {
        // TODO: Construct a JSON request string based on the command and text fields
        String jsonRequest = "{\"command\":\"" + command + "\", \"word\":\"" + wordField.getText() + "\"}"; // Example

        writer.println(jsonRequest);

        try {
            String jsonResponse = reader.readLine();
            responseArea.setText("Server Response:\n" + jsonResponse);
        } catch (IOException e) {
            responseArea.setText("Error reading from server: " + e.getMessage());
        }
    }
}
