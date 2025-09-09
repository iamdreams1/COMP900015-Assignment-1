package org.dictionary;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class LoadTester {

    public static void main(String[] args) {
        String address = "localhost";
        int port = 4444; // Use the same port your server is running on

        // Create one "writer" thread that will add a word
        Runnable writerTask = () -> {
            try (Socket socket = new Socket(address, port);
                 PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                System.out.println("WRITER: Sending add request...");
                String addRequest = "{\"command\":\"add\", \"word\":\"testword\", \"meanings\":[\"a word for testing\"]}";
                writer.println(addRequest);
                System.out.println("WRITER: Received response: " + reader.readLine());

            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        // Create multiple "reader" threads that will query a word
        Runnable readerTask = () -> {
            try (Socket socket = new Socket(address, port);
                 PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                String threadName = Thread.currentThread().getName();
                System.out.println(threadName + ": Sending query request...");
                String queryRequest = "{\"command\":\"query\", \"word\":\"testword\"}";
                writer.println(queryRequest);
                System.out.println(threadName + ": Received response: " + reader.readLine());

            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        // --- Start the Test ---
        System.out.println("Starting load test...");

        new Thread(writerTask).start(); // Start the writer

        // Start multiple readers at the same time
        for (int i = 0; i < 5; i++) {
            new Thread(readerTask).start();
        }
    }
}
