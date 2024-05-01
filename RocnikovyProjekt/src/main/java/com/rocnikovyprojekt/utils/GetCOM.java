package com.rocnikovyprojekt.utils;

import com.fazecast.jSerialComm.SerialPort;

public class GetCOM {
    private static String[][] allPorts;

    /**
     * Get all available COM ports.
     */
    public static void getCOM() {
        allPorts = getUnixCOM();
    }

    public static boolean CheckForPorts() {
        if (allPorts == null) {
            System.out.println("No ports found.");
            System.out.println("Please refresh the list of ports.");
            return false;
        } return true;
    }

    public static String[][] getPorts() {
        return allPorts;
    }

    private static String[][] getUnixCOM() {
        SerialPort[] ports = SerialPort.getCommPorts();
        String[][] allPorts = new String[][]{new String[ports.length]};

        int i = 0;
        for (SerialPort port : ports) {
            String[] portInfo = {port.getSystemPortPath(), port.getDescriptivePortName(), port.getPortDescription()};
            allPorts[i++] = portInfo;
        }
        for (String[] p : allPorts) {
            System.out.println("Port info: " + p[0] + ", " + p[1] + ", " + p[2]);
        }

        return allPorts;
    }
}
