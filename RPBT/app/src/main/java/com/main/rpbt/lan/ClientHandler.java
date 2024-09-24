package com.main.rpbt.lan;

import android.annotation.SuppressLint;
import android.content.Context;

import com.main.rpbt.util.FileHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * ClientHandler class - handles the client connection
 */
public class ClientHandler implements Runnable {
    private static Socket socket;
    @SuppressLint("StaticFieldLeak")
    private static Context context;

    /**
     * Constructor
     *
     * @param socket - the socket of the client
     * @param context - the context of the application
     */
    public ClientHandler(Socket socket, Context context) {
        ClientHandler.socket = socket;
        ClientHandler.context = context;
    }

    /**
     * Run method of the client handler
     */
    @Override
    public void run () {
        try (InputStream input = socket.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {

            String command;
            while ((command = reader.readLine()) != null) {
                System.out.println("Received command: " + command);
                if (command.equals("content"))
                    handleContent(reader);
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
    public void handleContent(BufferedReader reader) throws IOException {
        String fileName = reader.readLine();

        int lengthContent = Integer.parseInt(reader.readLine()); // Read length of the content
        char[] contentBuffer = new char[lengthContent];
        reader.read(contentBuffer, 0, lengthContent);      // Read the content
        String content = new String(contentBuffer);

        saveFile(fileName, content);
    }

    /**
     * Save the file to the internal storage of the application
     *
     * @param fileName - the name of the file
     * @param content - the content of the file
     * @throws IOException - if an I/O error occurs
     */
    private void saveFile(String fileName, String content) throws IOException {
        File file = FileHelper.getJsonFileDir(context, fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(content.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
