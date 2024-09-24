package com.main.rpbt.fragments;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.main.rpbt.R;
import com.main.rpbt.databinding.FragmentBluetoothBinding;
import com.main.rpbt.lan.LanClient;
import com.main.rpbt.util.DeviceListAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

@RequiresApi(api = Build.VERSION_CODES.S)
public class FragmentBluetooth extends Fragment {

    private ListView listView;

    private FragmentBluetoothBinding binding;
    private BluetoothAdapter bluetoothAdapter;
    private ArrayList<DeviceListAdapter.BluetoothDeviceWrapper> pairedDevicesList;
    private BluetoothSocket bluetoothSocket;
    private InputStream inStream;
    private OutputStream outStream;

    //private static final String SERVER_UUID = "1101"; // Same UUID as the server
    private UUID serverUUID;

    String[] permissions = {Manifest.permission.BLUETOOTH_CONNECT};

    public FragmentBluetooth() {}


    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentBluetoothBinding.inflate(inflater, container, false);

        listView = binding.listViewDevices;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            Log.d(TAG, "This device does not support Bluetooth! :(");
        } else {
            Log.d(TAG, "Device support Bluetooth");

            // Check if Bluetooth is enabled
            if (!bluetoothAdapter.isEnabled()) {
                Log.d(TAG, "Bluetooth is disabled");

                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE); // Request user to enable Bluetooth
                startActivity(intent);
            }

            // if permission is not granted, request it
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), permissions, 0);
            }
        }

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

        binding.ButtonNext.setOnClickListener(v -> NavHostFragment.findNavController(FragmentBluetooth.this)
                .navigate(R.id.action_BluetoothFragment_to_SettingsFragment));
        binding.ButtonPrev.setOnClickListener(v -> NavHostFragment.findNavController(FragmentBluetooth.this)
                .navigate(R.id.action_BluetoothFragment_to_CameraOnlineFragment));
        binding.SearchButton.setOnClickListener(v -> {
            fillConnectedDevice();
            scanDevices();
        });
        binding.ConnectButton.setOnClickListener(v -> connectLAN());

        listView.setOnItemClickListener((AdapterView<?> parent, View v, int position, long id) -> {
            DeviceListAdapter.BluetoothDeviceWrapper deviceWrapper = pairedDevicesList.get(position);
            System.out.println(deviceWrapper);
            connectToDevice(deviceWrapper);
        });
    }

    /**
     * Connect to the server via LAN
     */
    private void connectLAN() {
        String serverIP = "192.168.0.47";
        //String serverIP = binding.ipAddress.getText().toString();
        if (serverIP.isEmpty()) {
            Toast.makeText(requireContext(), "Enter the IP address of the server!", Toast.LENGTH_SHORT).show();
            return;
        }

        String serverPort = binding.portNumber.getText().toString();
        if (serverPort.isEmpty())
            serverPort = String.valueOf(binding.portNumber.getHint());

        LanClient client = LanClient.getInstance(requireContext(), serverIP, Integer.parseInt(serverPort));
    }


    private void sendMessage() {
    }

    private void receiveMessage() {
    }

    private void closeConnection() {
        try {
            inStream.close();
            outStream.close();
            bluetoothSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void connectToDevice(DeviceListAdapter.BluetoothDeviceWrapper deviceWrapper) {
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceWrapper.getAddress());

        try {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }

            // UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
            // bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid);

            bluetoothSocket = (BluetoothSocket) device.getClass().getMethod("createInsecureRfcommSocket", int.class).invoke(device, 1);
            assert bluetoothSocket != null;
            bluetoothSocket.connect();

            System.out.println("\n\nConnected to " + deviceWrapper.getName());
        } catch (IOException e) {
            Log.e("BluetoothConnection", "Error connecting to device: " + deviceWrapper.getName(), e);
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            Log.e("BluetoothConnection", "Error creating socket", e);
        }
    }

    private void scanDevices() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        pairedDevicesList = new ArrayList<>();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                pairedDevicesList.add(new DeviceListAdapter.BluetoothDeviceWrapper(device.getName(), device.getAddress()));

                String deviceInfo = device.getName() + "   " + device.getAddress() + "   Type: " + device.getType();
                System.out.println(deviceInfo);
            }
        } else {
            System.out.println("No paired devices found.");
        }


        DeviceListAdapter adapter = new DeviceListAdapter(requireContext(), pairedDevicesList);
        listView.setAdapter(adapter);

    }

    // Show currently connected device
    private void fillConnectedDevice() {
        TextView name = binding.devsText;
        TextView mac = binding.devsText2;

        bluetoothAdapter.getProfileProxy(requireContext(), new BluetoothProfile.ServiceListener() {
            @Override
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                if (profile == BluetoothProfile.HEADSET) {
                    BluetoothDevice connectedDevice = proxy.getConnectedDevices().isEmpty() ? null : proxy.getConnectedDevices().get(0);

                    if (connectedDevice != null) {
                        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                        name.setText(connectedDevice.getName());
                        mac.setText(connectedDevice.getAddress());

                        setBluetoothSocket(connectedDevice.getAddress());
                    } else {
                        name.setText("");
                        mac.setText("");
                    }
                }
                bluetoothAdapter.closeProfileProxy(profile, proxy);
            }

            @Override
            public void onServiceDisconnected(int profile) {
                name.setText("");
                mac.setText("");
            }
        }, BluetoothProfile.HEADSET);
    }

    private void setBluetoothSocket(String address) {
        System.out.println("\n\n\n\t\t\tHere!\n\n\n");
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        try {
            bluetoothSocket = (BluetoothSocket) device.getClass().getMethod("createInsecureRfcommSocket", int.class).invoke(device, 1);

            assert bluetoothSocket != null;
            inStream = bluetoothSocket.getInputStream();
            outStream = bluetoothSocket.getOutputStream();
        } catch (IOException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
