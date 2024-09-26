package cz.ima.btTof.lan;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.StrictMode;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Class representing the client side of the LAN connection
 */
public class LanClient {
    @SuppressLint("StaticFieldLeak")
    private static LanClient instance;
    private static Socket socket;
    private static PrintWriter writer;
    private static BufferedReader reader;

    /**
     * Constructor - connects to the server and sends a command to start streaming data
     * @param context - context of the application
     * @param ip - IP address of the server
     * @param port - port of the server
     */
    public LanClient(Context context, String ip, int port) {

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
}