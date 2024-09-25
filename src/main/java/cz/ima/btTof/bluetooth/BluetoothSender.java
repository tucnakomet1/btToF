package cz.ima.btTof.bluetooth;

import javax.microedition.io.StreamConnection;
import java.io.OutputStream;
import java.util.Scanner;

public class BluetoothSender implements Runnable {
    private final StreamConnection connection;

    public BluetoothSender(StreamConnection connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        try {
            OutputStream outStream = connection.openOutputStream();
            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.print("Zadej zprávu pro odeslání: ");
                String message = scanner.nextLine();

                if (message.equalsIgnoreCase("exit")) break; // Ukončení senderu

                outStream.write(message.getBytes());
                outStream.flush();
                System.out.println("Zpráva odeslána: " + message);
            }

            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}