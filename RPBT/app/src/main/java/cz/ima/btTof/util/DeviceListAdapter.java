package cz.ima.btTof.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import cz.ima.btTof.R;

import java.util.ArrayList;

/**
 * Adapter for the list of Bluetooth devices
 */
public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.DeviceViewHolder> {

    private final Context context;
    private final ArrayList<BluetoothDeviceWrapper> devices;

    /**
     * Constructor
     * @param context Context
     * @param devices List of devices
     */
    public DeviceListAdapter(Context context, ArrayList<BluetoothDeviceWrapper> devices) {
        this.context = context;
        this.devices = devices;
    }

    /**
     * Create a new view holder
     */
    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_bt_device, parent, false);
        return new DeviceViewHolder(view);
    }

    /**
     * Bind the view holder
     */
    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        BluetoothDeviceWrapper device = devices.get(position);
        holder.deviceName.setText(device.getName());
        holder.deviceAddress.setText(device.getAddress());
    }

    /**
     * View holder for the device
     */
    static class DeviceViewHolder extends RecyclerView.ViewHolder {
        TextView deviceName;
        TextView deviceAddress;

        // itemView is the view of the list item
        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceName = itemView.findViewById(R.id.device_name_text);
            deviceAddress = itemView.findViewById(R.id.device_mac_address_text);
        }
    }

    /**
     * Get the number of devices
     */
    @Override
    public int getItemCount() {
        return devices.size();
    }

    /**
     * Wrapper class for BluetoothDevice
     */
    public static class BluetoothDeviceWrapper {
        private final String name;
        private final String address;

        /**
         * Constructor of the wrapper (inner) class
         * @param name Name of the device
         * @param address Address of the device
         */
        public BluetoothDeviceWrapper(String name, String address) {
            this.name = name;
            this.address = address;
        }

        /**
         * Get the name of the device
         * @return Name of the device
         */
        public String getName() {
            return name;
        }

        /**
         * Get the address of the device
         * @return Address of the device
         */
        public String getAddress() {
            return address;
        }
    }
}