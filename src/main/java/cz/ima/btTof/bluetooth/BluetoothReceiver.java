package cz.ima.btTof.bluetooth;

import cz.ima.btTof.tof.TofFunc;
import cz.ima.btTof.utils.SendReceive;

import javax.microedition.io.StreamConnection;
import java.io.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class BluetoothReceiver implements Runnable {
    private final StreamConnection connection;
    private final TofFunc func;
    private static boolean stopThread = false;
    private static final AtomicBoolean isStreaming = new AtomicBoolean(false), isRecording = new AtomicBoolean(false);


    /**
     * Constructor
     *
     * @param connection connection to the client
     * @param func       function to call when starting the stream
     */
    public BluetoothReceiver(StreamConnection connection, TofFunc func) {
        this.connection = connection;
        this.func = func;
    }

    /**
     * Run method of the client handler
     */
    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.openInputStream()));
             OutputStream output = connection.openOutputStream();
             PrintWriter writer = new PrintWriter(output, true)) {

            String command;
            while ((command = reader.readLine()) != null) {
                System.out.println("Received command: " + command);

                switch (command.toLowerCase()) {
                    case "config" -> SendReceive.handleConfig(reader);          // handle the config command
                    case "stream" -> handleStream(writer);                      // handle the stream command
                    case "record" -> handleRecord(writer);                      // handle the record command
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
    public void handleStream(PrintWriter writer) {
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
