package cz.ima.btTof;

import cz.ima.btTof.bluetooth.BluetoothServer;
import cz.ima.btTof.lan.LanServer;
import cz.ima.btTof.utils.GetCOM;
import cz.ima.btTof.utils.WriteJson;
import cz.ima.btTof.tof.TofFunc;

import java.util.Scanner;

/** Main class - entry point of the application */
public class Main {

    /** Main method - entry point of the application
     * @param args command line arguments */
    public static void main(String[] args) {
        // get all available COM ports and check if there are any
        GetCOM gc = new GetCOM();
        String[][] ports = gc.getPorts();


        if (!gc.CheckForPorts()) {
            System.out.println("No COM ports found.");
            return;
        }

        String port = ports[0][0];
        if (gc.getPorts().length > 1) {
            for (String[] strings : ports) {
                if (strings[1].contains("STM")) {
                    port = strings[0];
                    break;
                }
            }
        }
        WriteJson.updateConfig("config.json", "com_port", port);
        System.out.println("Using port: " + port);

        // start the ToF sensor stream
        TofFunc func = new TofFunc(port);

        Scanner scanner = new Scanner(System.in);
        System.out.println("Do you want to use Bluetooth server or LAN server? [bt / lan]:");
        String choice = scanner.nextLine();

        if (choice.equalsIgnoreCase("bt")) {
            new BluetoothServer(func);
        } else if (choice.equalsIgnoreCase("lan")) {
            new LanServer(func);
        } else {
            System.out.println("Neplatná volba. Prosím, zadejte 'BT' nebo 'LAN'.");
        }
        scanner.close();
    }

}