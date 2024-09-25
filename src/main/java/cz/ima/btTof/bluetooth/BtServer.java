package cz.ima.btTof.bluetooth;



import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

public class BtServer {

    public BtServer() {
        printAllDevices();
        BluetoothServer();
    }

    private void BluetoothServer() {
        try {
            // Vytvoření UUID pro server
            UUID uuid = new UUID("1101", true); // "1101" je standardní UUID pro SPP (Serial Port Profile)
            String connectionString = "btspp://localhost:" + uuid + ";name=BluetoothServer";

            System.out.println(connectionString);

            StreamConnectionNotifier notifier = (StreamConnectionNotifier) Connector.open(connectionString);
            System.out.println("Bluetooth server is je spuštěn a čeká na příchozí připojení...");

            // Čekání na připojení
            StreamConnection connection = notifier.acceptAndOpen();
            System.out.println("Zařízení se připojilo!");

            // Spuštění vlákna pro přijímání dat
            BluetoothReceiver receiver = new BluetoothReceiver(connection);
            Thread receiverThread = new Thread(receiver);
            receiverThread.start();

            // Spuštění vlákna pro odesílání dat
            BluetoothSender sender = new BluetoothSender(connection);
            Thread senderThread = new Thread(sender);
            senderThread.start();

            // Ukončení hlavního vlákna serveru, když jsou obě vlákna ukončena
            receiverThread.join();
            senderThread.join();

            // Zavření spojení
            connection.close();
            notifier.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void printAllDevices() {
        try {
            // Získání lokálního Bluetooth zařízení
            LocalDevice localDevice = LocalDevice.getLocalDevice();

            // Výpis jména a adresy lokálního zařízení
            System.out.println("Bluetooth local Name: '" + localDevice.getFriendlyName() + "' and Address: '" + localDevice.getBluetoothAddress() + "'");

            // Získání seznamu spárovaných zařízení
            DiscoveryAgent agent = localDevice.getDiscoveryAgent();
            RemoteDevice[] pairedDevices = agent.retrieveDevices(DiscoveryAgent.PREKNOWN);

            if (pairedDevices != null) {
                for (RemoteDevice device : pairedDevices) {
                    System.out.println("Paired Device: " + device.getFriendlyName(false) + " [" + device.getBluetoothAddress() + "]");
                }
            } else {
                System.out.println("No paired devices found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
