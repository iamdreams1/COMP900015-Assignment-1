package org.dictionary;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ConcurrentWriteTester {

    private static final String ADDRESS = "localhost";
    private static final int PORT = 4444; // IMPORTANT: Must match your server's port
    private static final Gson gson = new Gson();

    public static void main(String[] args) throws InterruptedException {
        String wordToModify = "apple";
        List<String> newMeaningsToAdd = List.of(
                "A classic dessert ingredient",
                "major technology company",
                "A type of fruit"
        );

        System.out.println("--- Starting Concurrent Write Test on word: '" + wordToModify + "' ---");

        List<Thread> writerThreads = new ArrayList<>();

        String meaning0 = newMeaningsToAdd.get(0);

            // String initialMeaning = "cnm";
        String meaning1 = newMeaningsToAdd.get(1);

        String meaning2 = newMeaningsToAdd.get(2);

        // Create a task for each new meaning
        Runnable writerTask1 = () -> {
            try (Socket socket = new Socket(ADDRESS, PORT);
                 PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                JsonObject request = new JsonObject();
                request.addProperty("command", "updateMeaning");
                request.addProperty("word", wordToModify);
                request.addProperty("oldMeaning", meaning0);
                request.addProperty("newMeaning", meaning1);

                System.out.println(Thread.currentThread().getName() + ": Sending request to update '" + meaning0 +  " to " + meaning1 + "'");
                writer.println(gson.toJson(request));

                String response = reader.readLine();
                System.out.println(Thread.currentThread().getName() + ": Received response: " + response);

            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        writerThreads.add(new Thread(writerTask1));

        Runnable writerTask2 = () -> {
            try (Socket socket = new Socket(ADDRESS, PORT);
                 PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                JsonObject request = new JsonObject();
                request.addProperty("command", "updateMeaning");
                request.addProperty("word", wordToModify);
                request.addProperty("oldMeaning", meaning1);
                request.addProperty("newMeaning", meaning2);

                System.out.println(Thread.currentThread().getName() + ": Sending request to update '" + meaning1 +  " to " + meaning2 + "'");
                writer.println(gson.toJson(request));

                String response = reader.readLine();
                System.out.println(Thread.currentThread().getName() + ": Received response: " + response);

            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        writerThreads.add(new Thread(writerTask2));

        // Start all threads at roughly the same time
        for (Thread t : writerThreads) {
            t.start();
        }

        // Wait for all writer threads to complete
        for (Thread t : writerThreads) {
            t.join();
        }

        System.out.println("\n--- All writer threads have completed. Verifying final state... ---");

        // Final verification step: Query the word to see the result
        try (Socket socket = new Socket(ADDRESS, PORT);
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            JsonObject request = new JsonObject();
            request.addProperty("command", "query");
            request.addProperty("word", wordToModify);

            writer.println(gson.toJson(request));
            System.out.println("Final state response: " + reader.readLine());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
