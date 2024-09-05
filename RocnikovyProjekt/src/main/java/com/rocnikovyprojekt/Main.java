package com.rocnikovyprojekt;


import com.rocnikovyprojekt.bluetooth.BluetoothServer;
import com.rocnikovyprojekt.lan.LanServer;
import com.rocnikovyprojekt.tof.TofFrame;
import com.rocnikovyprojekt.tof.TofFunc;
import com.rocnikovyprojekt.utils.GetCOM;

import java.util.Arrays;

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

        System.out.println("Using port: " + port);

        // start the ToF sensor stream
        TofFunc func = new TofFunc(port);
        //func.start_stream(null);
        //func.record_stream();

        new LanServer(func);

        // bluetooth connection
        //new BluetoothServer();
    }

}