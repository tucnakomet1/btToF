package cz.ima.btTof.bluetooth;

import cz.ima.btTof.tof.TofFunc;

import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

/**
 * Class for handling the Bluetooth server
 */
public class BluetoothServer {

    /**
     * Constructor - Create a Bluetooth server, then waiting for a connection
     * @param func function to call when starting the stream
     */
    public BluetoothServer(TofFunc func) {
        printAllDevices();

        try {
            UUID uuid = new UUID("1101", true);     // "1101" is standard UUID for Serial Port Profile
            String connectionString = "btspp://localhost:" + uuid + ";name=BluetoothServer";

            StreamConnectionNotifier notifier = (StreamConnectionNotifier) Connector.open(connectionString);
            System.out.println("Bluetooth server is running. Waiting for connection...");

            // Waiting for connection
            StreamConnection connection = notifier.acceptAndOpen();
            System.out.println("Device connected!");

            // Start the receiver and sender threads
            BluetoothReceiver receiver = new BluetoothReceiver(connection, func);
            Thread receiverThread = new Thread(receiver);
            receiverThread.start();

            // Close the connection
            receiverThread.join();

            connection.close();
            notifier.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Print all paired devices
     */
    public void printAllDevices() {
        try {
            LocalDevice localDevice = LocalDevice.getLocalDevice();     // Local Bluetooth device
            System.out.println("Bluetooth local Name: '" + localDevice.getFriendlyName() + "' and Address: '" + localDevice.getBluetoothAddress() + "'");

            // Get the discovery agent - used to find devices
            DiscoveryAgent agent = localDevice.getDiscoveryAgent();
            RemoteDevice[] pairedDevices = agent.retrieveDevices(DiscoveryAgent.PREKNOWN);

            if (pairedDevices != null) {
                System.out.println("Paired devices:");
                for (RemoteDevice device : pairedDevices) {
                    System.out.println("Device: " + device.getFriendlyName(false) + " [" + device.getBluetoothAddress() + "]");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
