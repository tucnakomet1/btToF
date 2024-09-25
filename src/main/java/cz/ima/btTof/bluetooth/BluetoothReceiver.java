package cz.ima.btTof.bluetooth;

import javax.microedition.io.StreamConnection;
import java.io.InputStream;

public class BluetoothReceiver implements Runnable {
    private StreamConnection connection;

    public BluetoothReceiver(StreamConnection connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        try {
            InputStream inStream = connection.openInputStream();
            byte[] buffer = new byte[1024];
            while (true) {
                // Čekání na přijetí dat
                int bytesRead = inStream.read(buffer);
                if (bytesRead == -1) break; // Konec streamu

                String receivedMessage = new String(buffer, 0, bytesRead);
                System.out.println("Přijatá zpráva: " + receivedMessage);
            }
            inStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
