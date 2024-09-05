package com.rocnikovyprojekt.lan;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

import com.rocnikovyprojekt.tof.TofFrame;
import com.rocnikovyprojekt.tof.TofFunc;
import org.json.JSONObject;

public class LanServer {
    private static AtomicBoolean isStreaming = new AtomicBoolean(false);
    private static AtomicBoolean isRecording = new AtomicBoolean(false);
    private static String configInfo = "";

    private static TofFunc func;


    public LanServer(TofFunc func) {
        LanServer.func = func;

        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            System.out.println("Server is listening on port 8080");

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");

                new Thread(new ClientHandler(socket)).start();
            }
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private final Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (InputStream input = socket.getInputStream();
                 OutputStream output = socket.getOutputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                 PrintWriter writer = new PrintWriter(output, true)) {

                String command;
                while ((command = reader.readLine()) != null) {
                    System.out.println("Received command: " + command);
                    switch (command.toLowerCase()) {
                        case "config" -> {
                            handleConfig(reader);
                            writer.println("Config updated: " + configInfo);
                        }
                        case "stream" -> handleStream(writer);
                        case "record" -> handleRecord(writer);
                        default -> writer.println("Unknown command");
                    }
                }
            } catch (IOException ex) {
                System.out.println("Server exception: " + ex.getMessage());
                ex.printStackTrace();
            }
        }

        private void handleConfig(BufferedReader reader) throws IOException {
            int length = Integer.parseInt(reader.readLine()); // Read length of config info
            char[] configBuffer = new char[length];
            reader.read(configBuffer, 0, length); // Read the config info
            configInfo = new String(configBuffer);

            System.out.println("Received config: " + configInfo);
        }

        private void handleStream(PrintWriter writer) {
            if (isStreaming.get()) {
                isStreaming.set(false);
                System.out.println("Stream stopped.");

                writer.println("Stream stopped.");
            } else {
                isStreaming.set(true);
                writer.println("Stream started.");
                System.out.println("Stream started.");

                new Thread(() -> startStream(writer)).start();
            }
        }

        private void startStream(PrintWriter writer) {
            while (isStreaming.get()) {
                func.start_stream(writer);
            }
        }

        private void handleRecord(PrintWriter writer) {
            if (isRecording.get()) {
                stopRecording();
                writer.println("Recording stopped.");
            } else {
                handleStream(writer);
                startRecording(writer);
                writer.println("Recording started.");
            }
        }

        private void startRecording(PrintWriter writer) {
            isRecording.set(true);
            func.recording_start(true, writer);
            System.out.println("Recording started.");
        }

        private void stopRecording() {
            isRecording.set(false);
            func.recording_end();
            func.recording_save();
            System.out.println("Recording ended.");
        }
    }
}
