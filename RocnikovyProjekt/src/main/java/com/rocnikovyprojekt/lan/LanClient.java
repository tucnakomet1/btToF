package com.rocnikovyprojekt.lan;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class LanClient {

    private static Socket socket;
    private static PrintWriter writer;
    private static BufferedReader reader;

    public static void main(String[] args) {
        try {
            socket = new Socket("192.168.0.47", 8080); // Připojení k serveru
            System.out.println("Connected to the server");

            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.println("Enter command (config, stream, record, exit): ");
                String command = scanner.nextLine();

                if (command.equalsIgnoreCase("exit")) {
                    break;
                }

                boolean isDefault = false;

                switch (command.toLowerCase()) {
                    case "config" -> sendConfig(scanner);
                    case "stream" -> sendCommand("stream");
                    case "record" -> sendCommand("record");
                    default -> isDefault = true;
                }

                if (isDefault) {
                    System.out.println("Invalid command");
                } else {
                    listenForServerResponse();
                }

            }
        } catch (IOException ex) {
            System.out.println("Client error: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private static void sendConfig(Scanner scanner) {
        System.out.println("Enter config string: ");
        String configData = scanner.nextLine();
        writer.println("config"); // Odeslání příkazu "config"
        writer.println(configData.length()); // Odeslání délky config zprávy
        writer.println(configData); // Odeslání samotné zprávy
    }

    private static void sendCommand(String command) {
        writer.println(command); // Odeslání příkazu na server
    }

    private static void listenForServerResponse() {
        try {
            String serverResponse;
            while ((serverResponse = reader.readLine()) != null) {
                System.out.println("Server: " + serverResponse);
                // Break loop if we are not streaming anymore
                //if (!serverResponse.contains("Streaming data")) {
                //    break;
                //}
            }
        } catch (IOException ex) {
            System.out.println("Error reading server response: " + ex.getMessage());
        }
    }
}
