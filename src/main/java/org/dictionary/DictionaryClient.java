package org.dictionary;

// --- DictionaryClient.java ---
import javax.swing.SwingUtilities;

public class DictionaryClient {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java -jar DictionaryClient.jar <server-address> <port>");
            return;
        }

        String address = args[0];
        int port = Integer.parseInt(args[1]);

        // Launch the GUI
        SwingUtilities.invokeLater(() -> new ClientGUI(address, port).setVisible(true));
    }
}
