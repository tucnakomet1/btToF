package cz.ima.btTof.lan;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicBoolean;

import cz.ima.btTof.utils.WriteJson;
import cz.ima.btTof.tof.TofFunc;

/**
 * Class for handling the LAN server
 */
public class LanServer {
    private static final String SAVE_FOLDER = "save";

    private static final AtomicBoolean isStreaming = new AtomicBoolean(false), isRecording = new AtomicBoolean(false);
    private static TofFunc func;

    /**
     * Constructor
     * @param func function to call when starting the stream
     */
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
                        case "config" -> handleConfig(reader);          // handle the config command
                        case "stream" -> handleStream(writer);          // handle the stream command
                        case "record" -> handleRecord(writer, output);  // handle the record command
                        default -> System.out.println("Unknown command");
                    }
                }
            } catch (IOException ex) {
                System.out.println("Server exception: " + ex.getMessage());
                ex.printStackTrace();
            }
        }

        /**
         * Handle the config command - read the config info from the client
         *
         * @param reader reader to read the config info from
         * @throws IOException - if an I/O error occurs
         */
        private void handleConfig(BufferedReader reader) throws IOException {
            int length = Integer.parseInt(reader.readLine());   // Read length of config info

            char[] configBuffer = new char[length];
            reader.read(configBuffer, 0, length);           // Read the config info

            String configInfo = new String(configBuffer);
            WriteJson.handleConfig(configInfo);

            System.out.println("Received config: " + configInfo);
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
         * @param output output stream to send the recorded file to
         */
        private void handleRecord(PrintWriter writer, OutputStream output) {
            if (isRecording.get())
                stopRecording(writer, output);
            else
                startRecording();
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
         * @param output output stream to send the recorded file to
         */
        private void stopRecording(PrintWriter writer, OutputStream output) {
            System.out.println("Recording ended.");

            isRecording.set(false);
            String fileName = func.recording_end();

            //handleStream(writer);
            isStreaming.set(false);
            stopThread = true;
            System.out.println("Stream stopped.");
            startStream(writer);

            sendRecordedFile(writer, fileName);

            //handleStream(writer);
        }

        /**
         * Send the recorded file to the client
         *
         * @param writer writer to send the data to
         * @param fileName name of the recorded file
         */
        private void sendRecordedFile(PrintWriter writer, String fileName) {
            String filePath = Paths.get(SAVE_FOLDER, fileName).toString();

            File file = new File(filePath);
            if (!file.exists()) {
                System.out.println("File not found");
                return;
            }

            try (FileInputStream fileInput = new FileInputStream(file);
                BufferedInputStream bufferedInput = new BufferedInputStream(fileInput)) {

                // send file name and file size
                writer.println("content");
                writer.println(fileName);
                writer.println(file.length());
                writer.flush();

                System.out.println("Sending file: " + fileName);
                System.out.println("File size: " + file.length());

                // send file content in chunks -> 3*1024 bytes because of the Base64 encoding
                byte[] buffer = new byte[3 * 1024];
                int bytesRead;
                while ((bytesRead = bufferedInput.read(buffer)) != -1) {
                    String base64Chunk = Base64.getEncoder().encodeToString(Arrays.copyOf(buffer, bytesRead));
                    writer.println(base64Chunk);
                    writer.flush();
                }

                writer.println("EOF");
                writer.flush();
                System.out.println("File sent successfully.");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
