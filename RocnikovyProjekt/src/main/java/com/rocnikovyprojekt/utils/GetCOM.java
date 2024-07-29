package com.rocnikovyprojekt.utils;

import com.fazecast.jSerialComm.SerialPort;

import java.util.logging.Logger;

/** Get all available COM ports. */
public class GetCOM {
    private static final Logger logger = Logger.getLogger(GetCOM.class.getName());
    private static String[][] allPorts;

    /**
     * Constructor. Load all available COM ports into allPorts.
     */
    public GetCOM() {
        allPorts = getUnixCOM();
    }

    /**
     * Check if there are any available COM ports.
     * @return true if there are any available COM ports, false otherwise
     */
    public boolean CheckForPorts() {
        if (allPorts == null) {
            logger.warning("No ports found. \nPlease refresh the list of ports.");
            return false;
        } return true;
    }

    /**
     * Get all available, already loaded, COM ports.
     * @return all available COM ports
     */
    public String[][] getPorts() {
        return allPorts;
    }

    /**
     * Get all available COM ports on Unix systems.
     * @return all available COM ports
     */
    private static String[][] getUnixCOM() {
        SerialPort[] ports = SerialPort.getCommPorts();
        String[][] allPorts = new String[][]{new String[ports.length]};

        int i = 0;
        for (SerialPort port : ports) {
            String[] portInfo = {port.getSystemPortPath(), port.getDescriptivePortName(), port.getPortDescription()};
            allPorts[i++] = portInfo;
        }
        for (String[] p : allPorts) {
            logger.info("Port: " + p[0] + " (" + p[1] + ", " + p[2] + ")");
        }

        return allPorts;
    }
}
