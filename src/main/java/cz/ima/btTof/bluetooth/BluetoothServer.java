package cz.ima.btTof.bluetooth;

import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BluetoothServer {
    //private static final String SERVER_UUID = "1101"; // Random UUID for the server
    private static UUID SERVER_UUID;
    private static final String SERVER_NAME = "BluetoothServer"; // Name of the server
    private static final String SERVER_URL = "btspp://localhost:" + SERVER_UUID + ";name=" + SERVER_NAME;

    private StreamConnectionNotifier notifier;

    public BluetoothServer() {
        SERVER_UUID = new UUID("1101", true); //new UUID("d0c722b07e1511e1b0c40800200c9a66", false);//UUID.randomUUID();
        System.out.println("Generated UUID: " + SERVER_UUID);

        // start the server
        try {
            LocalDevice localDevice = LocalDevice.getLocalDevice();
            localDevice.setDiscoverable(DiscoveryAgent.GIAC);

            //notifier = (StreamConnectionNotifier) Connector.open(SERVER_URL);
            String url = "btspp://localhost:" + SERVER_UUID.toString() + ";name=BluetoothServer";
            notifier = (StreamConnectionNotifier) Connector.open(url);
            System.out.println("Waiting for client connection...");

            StreamConnection connection = notifier.acceptAndOpen();
            System.out.println("Client connected.");

            // handle the connection
            handleConnection(connection);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleConnection(StreamConnection connection) {
        try (InputStream inStream = connection.openInputStream(); OutputStream outStream = connection.openOutputStream()) {
            // open the input and output streams


            // Odeslání náhodně generovaného UUID klientovi
            outStream.write(SERVER_UUID.toString().getBytes());
            outStream.flush();
            System.out.println("Sent UUID to client.");

            /*
            // read the data from the client
            byte[] buffer = new byte[1024];
            int bytesRead = inStream.read(buffer);

            // print the data
            System.out.println("Received: " + new String(buffer, 0, bytesRead));

            // send a response to the client
            outStream.write("Hello from server!".getBytes());*/

            // Send ping1 to the client
            String ping1 = "ping1";
            outStream.write(ping1.getBytes());
            outStream.flush();
            System.out.println("Sent: " + ping1);

            // Receive ping2 from the client
            byte[] buffer = new byte[1024];
            int bytesRead = inStream.read(buffer);
            String received = new String(buffer, 0, bytesRead);
            System.out.println("Received: " + received);

            connection.close();
            notifier.close();
            System.out.println("Connection closed.");

            // close the streams and the connection
        } catch (RuntimeException | IOException e) {
            e.printStackTrace();
        }
    }
}
