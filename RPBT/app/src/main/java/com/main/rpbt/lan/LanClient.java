package com.main.rpbt.lan;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.StrictMode;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.main.rpbt.util.FileHelper;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Base64;

/**
 * Class representing the client side of the LAN connection
 */
public class LanClient {
    @SuppressLint("StaticFieldLeak")
    private static LanClient instance;
    private static Socket socket;
    private static PrintWriter writer;
    private static BufferedReader reader;
    private final Context context;

    /**
     * Constructor - connects to the server and sends a command to start streaming data
     * @param context - context of the application
     * @param ip - IP address of the server
     * @param port - port of the server
     */
    public LanClient(Context context, String ip, int port) {
        this.context = context;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            System.out.println(ip + "\t" + port);
            socket = new Socket(ip, port);
            Toast.makeText(context, "Connected to server!", Toast.LENGTH_SHORT).show();


            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException ex) {
            System.out.println("Client error: " + ex.getMessage());
        }
    }

    /**
     * Get the instance of the client
     * @param context - context of the application
     * @param ip - IP address of the server
     * @param port - port of the server
     * @return - instance of the client
     */
    public static LanClient getInstance(Context context, String ip, int port) {
        if (instance == null) {
            instance = new LanClient(context, ip, port);
        }
        return instance;
    }

    /**
     * Send the config file to the server
     * @param context - context of the application
     * @throws IOException - if an I/O error occurs
     */
    public void sendConfig(Context context) throws IOException {
        File file = new File(context.getFilesDir(), "jsonFiles/config.json");
        StringBuilder fileContent = new StringBuilder();

        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    fileContent.append(line).append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(context, "Please save the config!", Toast.LENGTH_SHORT).show();
            return;
        }
        String config = fileContent.toString();
        System.out.println(config);

        writer.println(file.length());
        writer.println(config);
        writer.flush();
    }

    /**
     * Getter for the socket
     * @return socket socket of the client
     */
    public Socket getSocket() {
        return socket;
    }

    /**
     * Getter for the writer
     * @return writer writer of the client
     */
    public PrintWriter getWriter() {
        return writer;
    }

    /**
     * Getter for the reader
     * @return reader reader of the client
     */
    public BufferedReader getReader() {
        return reader;
    }


    /**
     * Handle the config command - read the config info from the client
     *
     * @param reader reader to read the config info from
     * @throws IOException - if an I/O error occurs
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void handleContent(BufferedReader reader) throws IOException {
        String fileName = reader.readLine();                        // read the file name
        int fileSize = Integer.parseInt(reader.readLine());         // read the file size

        System.out.println("fileName: " + fileName);
        System.out.println("lengthContent: " + fileSize);

        File file = FileHelper.getJsonFileDir(context, fileName);   // save the file to the directory

        try (FileOutputStream fileOutput = new FileOutputStream(file);
             BufferedOutputStream bufferedOutput = new BufferedOutputStream(fileOutput)) {

            String line;
            while (!(line = reader.readLine()).equals("EOF")) {     // read until the end of the file
                byte[] decodedChunk = Base64.getDecoder().decode(line);
                bufferedOutput.write(decodedChunk);                 // write the decoded chunk to the file
            }

            bufferedOutput.flush();

            new Runnable() {
                public void run() {
                    Toast.makeText(context, "File received and saved: " + fileName, Toast.LENGTH_SHORT).show();
                }
            };
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}