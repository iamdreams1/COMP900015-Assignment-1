package org.dictionary;

// --- DictionaryClient.java ---
import javax.swing.SwingUtilities;

public class DictionaryClient {
    public static void main(String[] args) {
        if (args.length < 2 || args.length > 3) { // Allow 2 or 3 arguments
            System.out.println("Usage: java -jar DictionaryClient.jar <server-address> <port> [delay-ms]");
            return;
        }

        String address = args[0];
        int port = Integer.parseInt(args[1]);

        // Default delay is 0 if not provided
        int delay = (args.length == 3) ? Integer.parseInt(args[2]) : 0;

        // Launch the GUI, passing the delay to it
        SwingUtilities.invokeLater(() -> new ClientGUI(address, port, delay).setVisible(true));
    }
}
