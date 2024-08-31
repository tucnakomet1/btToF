package com.main.rpbt.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.main.rpbt.R;

import java.util.ArrayList;

public class DeviceListAdapter extends BaseAdapter {

    private final Context context;
    private final ArrayList<BluetoothDeviceWrapper> devices;

    public DeviceListAdapter(Context context, ArrayList<BluetoothDeviceWrapper> devices) {
        this.context = context;
        this.devices = devices;
    }

    @Override
    public int getCount() {
        return devices.size();
    }

    @Override
    public Object getItem(int position) {
        return devices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.list_item_bt_device, parent, false);
        }

        TextView deviceName = convertView.findViewById(R.id.device_name_text);
        TextView deviceAddress = convertView.findViewById(R.id.device_mac_address_text);

        BluetoothDeviceWrapper device = devices.get(position);

        deviceName.setText(device.getName());
        deviceAddress.setText(device.getAddress());

        return convertView;
    }

    public static class BluetoothDeviceWrapper {
        private final String name;
        private final String address;

        public BluetoothDeviceWrapper(String name, String address) {
            this.name = name;
            this.address = address;
        }

        public String getName() {
            return name;
        }

        public String getAddress() {
            return address;
        }
    }
}