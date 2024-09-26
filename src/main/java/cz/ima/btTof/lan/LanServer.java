package cz.ima.btTof.lan;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

import cz.ima.btTof.utils.SendReceive;
import cz.ima.btTof.tof.TofFunc;
import cz.ima.btTof.utils.Util;

/**
 * Class for handling the LAN server
 */
public class LanServer {
    private static final AtomicBoolean isStreaming = new AtomicBoolean(false), isRecording = new AtomicBoolean(false);
    private static TofFunc func;

    /**
     * Constructor
     * @param func function to call when starting the stream
     */
    public LanServer(TofFunc func) {
        LanServer.func = func;

        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            System.out.println("Server is running on address: " + Util.getLocalIPAddress() + ":8080");

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

    /**
     * Class for handling the client
     */
    private record ClientHandler(Socket socket) implements Runnable {
        private static boolean stopThread = false;

        /**
         * Run method of the client handler
         */
        @Override
        public void run() {
            try (OutputStream output = socket.getOutputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter writer = new PrintWriter(output, true)) {

                String command;
                while ((command = reader.readLine()) != null) {
                    System.out.println("Received command: " + command);

                    switch (command.toLowerCase()) {
                        case "config" -> SendReceive.handleConfig(reader);  // handle the config command
                        case "stream" -> handleStream(writer);              // handle the stream command
                        case "record" -> handleRecord(writer);              // handle the record command
                    }
                }
            } catch (IOException ex) {
                System.out.println("Server exception: " + ex.getMessage());
                ex.printStackTrace();
            }
        }

        /**
         * Handle the stream command - start or stop the stream
         *
         * @param writer writer to send the data to
         */
        private void handleStream(PrintWriter writer) {
            if (isStreaming.get()) {
                isStreaming.set(false);
                stopThread = true;
                System.out.println("Stream stopped.");
                startStream(writer);
            } else {
                isStreaming.set(true);
                stopThread = false;
                System.out.println("Stream started.");

                new Thread(() -> startStream(writer)).start();
            }
        }

        /**
         * Start the stream
         *
         * @param writer writer to send the data to
         */
        private void startStream(PrintWriter writer) {
            while (isStreaming.get() && !stopThread)
                func.start_stream(writer);

            if (!isStreaming.get())
                func.stop_stream();
        }

        /**
         * Handle the record command - start or stop the recording
         *
         * @param writer writer to send the data to
         */
        private void handleRecord(PrintWriter writer) {
            if (isRecording.get()) stopRecording(writer);
            else startRecording();
        }

        /**
         * Start the recording
         */
        private void startRecording() {
            isRecording.set(true);
            func.recording_start();
            System.out.println("Recording started.");
        }

        /**
         * Stop the recording
         *
         * @param writer writer to send the data to
         */
        private void stopRecording(PrintWriter writer) {
            System.out.println("Recording ended.");

            isRecording.set(false);
            String fileName = func.recording_end();

            isStreaming.set(false);
            stopThread = true;
            startStream(writer);

            SendReceive.sendRecordedFile(writer, fileName);
        }
    }
}
