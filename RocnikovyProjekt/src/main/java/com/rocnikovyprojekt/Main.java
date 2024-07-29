package com.rocnikovyprojekt;


import com.rocnikovyprojekt.tof.TofFunc;
import com.rocnikovyprojekt.utils.GetCOM;

/** Main class - entry point of the application */
public class Main {

    /** Main method - entry point of the application
     * @param args command line arguments */
    public static void main(String[] args) {
        // get all available COM ports and check if there are any
        GetCOM gc = new GetCOM();
        gc.CheckForPorts();

        // start the ToF sensor stream
        TofFunc func = new TofFunc(gc.getPorts()[0][0]);
        func.start_stream();
    }

}