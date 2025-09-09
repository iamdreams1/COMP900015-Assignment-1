package org.dictionary;

// --- ServerGUI.java ---
import javax.swing.*;
import java.awt.*;

public class ServerGUI extends JFrame {
    private final JButton startButton;
    private final JButton stopButton;
    private final JTextArea logArea;
    private final JLabel clientCountLabel;
    private final DictionaryServer dictionaryServer;

    public ServerGUI(int port, String dictPath) {
        super("Dictionary Server Dashboard");

        // --- Initialize the server logic ---
        dictionaryServer = new DictionaryServer(port, dictPath);

        // --- Create GUI Components ---
        startButton = new JButton("Start Server");
        stopButton = new JButton("Stop Server");
        stopButton.setEnabled(false); // Can't stop a server that hasn't started

        logArea = new JTextArea(20, 50);
        logArea.setEditable(false);
        clientCountLabel = new JLabel("Active Clients: 0");

        // --- Layout ---
        JPanel controlPanel = new JPanel();
        controlPanel.add(startButton);
        controlPanel.add(stopButton);
        controlPanel.add(clientCountLabel);

        add(controlPanel, BorderLayout.NORTH);
        add(new JScrollPane(logArea), BorderLayout.CENTER);

        // --- Add Action Listeners ---
        startButton.addActionListener(e -> startServer());
        stopButton.addActionListener(e -> stopServer());

        // --- Window Setup ---
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        // --- Link server logic to GUI components for updates ---
        setupServerCallbacks();
    }

    private void setupServerCallbacks() {
        // This is how the server logic updates the GUI safely
        dictionaryServer.setLogUpdater(message ->
                SwingUtilities.invokeLater(() -> logArea.append(message + "\n"))
        );
        dictionaryServer.setClientCountUpdater(count ->
                SwingUtilities.invokeLater(() -> clientCountLabel.setText("Active Clients: " + count))
        );
    }

    private void startServer() {
        // Run the server on a new thread to avoid freezing the GUI
        new Thread(dictionaryServer).start();
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
    }

    private void stopServer() {
        dictionaryServer.stopServer();
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java ServerGUI <port> <dictionary-file-path>");
            return;
        }
        int port = Integer.parseInt(args[0]);
        String dictPath = args[1];

        // Start the GUI
        SwingUtilities.invokeLater(() -> new ServerGUI(port, dictPath));
    }
}
