package cz.ima.btTof.bluetooth;

import java.util.ArrayList;

import cz.ima.btTof.util.DeviceListAdapter;

public class BluetoothUtils {

    /**
     * Find the index of the device with the given name in the list of paired devices
     * @param pairedDevicesList - list of paired devices
     * @param btDeviceName - name of the device
     * @return index of the device in the list
     */
    public static int findWrapperByName(ArrayList<DeviceListAdapter.BluetoothDeviceWrapper> pairedDevicesList, String btDeviceName) {
        for (int i = 0; i < pairedDevicesList.size(); i++) {
            if (pairedDevicesList.get(i).getName().equals(btDeviceName)) {
                return i;
            }
        }
        return -1;
    }
}
