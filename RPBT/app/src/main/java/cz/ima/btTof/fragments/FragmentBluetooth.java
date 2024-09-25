package cz.ima.btTof.fragments;

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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import cz.ima.btTof.R;
import cz.ima.btTof.bluetooth.BluetoothClient;
import cz.ima.btTof.bluetooth.BluetoothUtils;
import cz.ima.btTof.databinding.FragmentBluetoothBinding;
import cz.ima.btTof.lan.LanClient;
import cz.ima.btTof.util.DeviceListAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

/**
 * Fragment for the Bluetooth connection
 */
@RequiresApi(api = Build.VERSION_CODES.S)
public class FragmentBluetooth extends Fragment {

    private RecyclerView recyclerView;

    private FragmentBluetoothBinding binding;
    private BluetoothAdapter bluetoothAdapter;
    private ArrayList<DeviceListAdapter.BluetoothDeviceWrapper> pairedDevicesList;
    private BluetoothSocket bluetoothSocket;
    private InputStream inStream;
    private OutputStream outStream;

    private String btDeviceName, btDeviceAddress;

    String[] permissions = {Manifest.permission.BLUETOOTH_CONNECT};

    public FragmentBluetooth() {}


    /**
     * Create the view of the Bluetooth fragment
     *
     * @param inflater - the layout inflater
     * @param container - the view group container
     * @param savedInstanceState - the saved instance state
     * @return the view of the Bluetooth fragment
     */
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentBluetoothBinding.inflate(inflater, container, false);

        recyclerView = binding.recyclerViewDevices;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            Log.d(TAG, "This device does not support Bluetooth! :(");
        } else {
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


    /**
     * Set up the listeners for the buttons
     *
     * @param view - the view of the fragment
     * @param savedInstanceState - the saved instance state
     */
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // buttons
        binding.ButtonNext.setOnClickListener(v -> NavHostFragment.findNavController(FragmentBluetooth.this).navigate(R.id.action_BluetoothFragment_to_SettingsFragment));
        binding.ButtonPrev.setOnClickListener(v -> NavHostFragment.findNavController(FragmentBluetooth.this).navigate(R.id.action_BluetoothFragment_to_CameraOnlineFragment));
        binding.SearchButton.setOnClickListener(v -> {
            fillPairedDevice();
            scanDevices();
        });
        binding.ConnectBtButton.setOnClickListener(v -> {
            DeviceListAdapter.BluetoothDeviceWrapper deviceWrapper = pairedDevicesList.get(BluetoothUtils.findWrapperByName(pairedDevicesList, btDeviceName));

            BluetoothClient.getInstance(requireContext(), deviceWrapper, bluetoothAdapter);

            fillConnectedDevice();
        });
        binding.ConnectButton.setOnClickListener(v -> connectLAN());
    }


    /**
     * Connect to the server via LAN
     * Read the IP address and port number from the text fields
     */
    private void connectLAN() {
        String serverIP = binding.ipAddress.getText().toString();
        if (serverIP.isEmpty()) {
            Toast.makeText(requireContext(), "Enter the IP address of the server!", Toast.LENGTH_SHORT).show();
            return;
        }

        String serverPort = binding.portNumber.getText().toString();
        if (serverPort.isEmpty())
            serverPort = String.valueOf(binding.portNumber.getHint());

        LanClient.getInstance(requireContext(), serverIP, Integer.parseInt(serverPort));
    }

    /**
     * Fill the paired device text fields - Show paired devices
     */
    private void fillPairedDevice() {
        TextView name = binding.devsText;
        TextView address = binding.devsText2;

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
                        btDeviceName = connectedDevice.getName();
                        btDeviceAddress = connectedDevice.getAddress();

                        name.setText(btDeviceName);
                        address.setText(btDeviceAddress);

                        setBluetoothSocket(btDeviceAddress);
                    } else {
                        btDeviceName = "None";
                        btDeviceAddress = "";

                        name.setText(btDeviceName);
                        address.setText(btDeviceAddress);
                    }
                }
                bluetoothAdapter.closeProfileProxy(profile, proxy);
            }

            @Override
            public void onServiceDisconnected(int profile) {
                btDeviceName = "None";
                btDeviceAddress = "";

                name.setText(btDeviceName);
                address.setText(btDeviceAddress);
            }
        }, BluetoothProfile.HEADSET);
    }

    /**
     * Scan for devices and show them in the list view
     */
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
            for (BluetoothDevice device : pairedDevices)
                pairedDevicesList.add(new DeviceListAdapter.BluetoothDeviceWrapper(device.getName(), device.getAddress()));
        } else
            System.out.println("No paired devices found.");

        DeviceListAdapter adapter = new DeviceListAdapter(requireContext(), pairedDevicesList);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }


    /**
     * Fill the connected device text fields - Show currently connected devices
     */
    private void fillConnectedDevice() {
        binding.connText.setText(btDeviceName);
        binding.connText2.setText(btDeviceAddress);
    }


    /**
     * Set the Bluetooth socket
     *
     * @param address - the address of the device
     */
    private void setBluetoothSocket(String address) {
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

    /**
     * Destroy the view of the fragment
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
