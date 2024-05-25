package com.main.rpbt;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.main.rpbt.databinding.BluetoothBinding;

import java.util.Set;

@RequiresApi(api = Build.VERSION_CODES.S)
public class BluetoothFragment extends Fragment {
    private BluetoothBinding binding;

    String[] permissions = {Manifest.permission.BLUETOOTH_CONNECT};

    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = BluetoothBinding.inflate(inflater, container, false);

        registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Log.d(TAG, "Bluetooth is enabled now");
                    }
                });

        return binding.getRoot();

    }


    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.ButtonNext.setOnClickListener(view12 -> NavHostFragment.findNavController(BluetoothFragment.this)
                .navigate(R.id.action_BluetoothFragment_to_FirstFragment));
        binding.ButtonPrev.setOnClickListener(view1 -> NavHostFragment.findNavController(BluetoothFragment.this)
                .navigate(R.id.action_BluetoothFragment_to_SecondFragment));

        binding.SearchButton.setOnClickListener(view13 -> {
            TextView name = binding.devsText;
            TextView mac = binding.devsText2;

            if (bluetoothAdapter == null) {
                Log.d(TAG, "Device doesn't support Bluetooth");
            } else {
                Log.d(TAG, "Device support Bluetooth");

                if (!bluetoothAdapter.isEnabled()) {
                    Log.d(TAG, "Bluetooth is disabled");
                    Toast.makeText(requireActivity().getApplicationContext(), "Please activate Bluetooth", Toast.LENGTH_LONG).show();
                } else {
                    Log.d(TAG, "Bluetooth is enabled");
                }


                /* pokud nemame opravneni se androidu ptat na bluetooth */
                if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(requireActivity(), permissions, 0);
                    return;
                }
                Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
                Log.d(TAG, "Paired devices: " + pairedDevices.toString() + "\t" + pairedDevices.size());


                if (pairedDevices.size() > 0) {
                    // There are paired devices. Get the name and address of each paired device.
                    for (BluetoothDevice device : pairedDevices) {
                        String deviceName = device.getName();
                        String deviceHardwareAddress = device.getAddress(); // MAC address

                        //If we find the HC 05 device (the Arduino BT module)
                        //We assign the device value to the Global variable BluetoothDevice
                        //We enable the button "Connect to HC 05 device"
                        name.setText(deviceName);
                        mac.setText(deviceHardwareAddress);
                    }
                }


            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
