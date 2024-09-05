package com.main.rpbt.lan;

import android.os.StrictMode;

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

    public LanClient() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);

        try {
            socket = new Socket("192.168.0.47", 8080); // Připojení k serveru
            System.out.println("Connected to the server");

            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            sendCommand("stream");
            listenForServerResponse();

            /*Scanner scanner = new Scanner(System.in);

            while (true) {
               /* System.out.println("Enter command (config, stream, record, exit): ");
                String command = scanner.nextLine();

                if (command.equalsIgnoreCase("exit")) {
                    break;
                }



                switch (command.toLowerCase()) {
                    case "config":
                        sendConfig(scanner);
                        break;
                    case "stream":
                        sendCommand("stream");
                        break;
                    case "record":
                        sendCommand("record");
                        break;
                    default:
                        System.out.println("Unknown command");
                }

                listenForServerResponse();
            }*/
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
