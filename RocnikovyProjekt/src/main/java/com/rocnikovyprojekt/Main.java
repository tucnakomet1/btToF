package com.rocnikovyprojekt;

import com.rocnikovyprojekt.tof.TofRecord;
import com.rocnikovyprojekt.utils.GetCOM;

public class Main {
    public static void main(String[] args) {
        GetCOM.CheckForPorts();
        GetCOM.getCOM();
        GetCOM.CheckForPorts();

        TofRecord tofRecord = new TofRecord();
        readList(TofRecord.get_metadata());
        TofRecord.set_timestamp();
        readList(TofRecord.get_metadata());

    }

    private static void readList(Object[] input) {
        for (Object o : input) {
            System.out.println(o);
        }
    }
}