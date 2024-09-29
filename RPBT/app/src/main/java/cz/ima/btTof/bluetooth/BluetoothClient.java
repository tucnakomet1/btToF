package cz.ima.btTof.bluetooth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

import cz.ima.btTof.util.DeviceListAdapter;

/**
 * Bluetooth client class - connects to a device and creates a socket
 */
public class BluetoothClient {
    @SuppressLint("StaticFieldLeak")
    private static BluetoothClient instance;
    private final Context context;

    private BluetoothSocket bluetoothSocket;
    private final BluetoothAdapter bluetoothAdapter;

    private PrintWriter writer;
    private BufferedReader reader;

    /**
     * Constructor - connects to the device and creates a socket
     *
     * @param context - context of the application
     * @param deviceWrapper - the device to connect to
     * @param bluetoothAdapter - the Bluetooth adapter
     */
    public BluetoothClient(Context context, DeviceListAdapter.BluetoothDeviceWrapper deviceWrapper, BluetoothAdapter bluetoothAdapter) {
        this.context = context;
        this.bluetoothAdapter = bluetoothAdapter;

        connectToDevice(deviceWrapper);
    }

    /**
     * Get the instance of the client
     *
     * @param context - context of the application
     * @param deviceWrapper - the device to connect to
     * @param bluetoothAdapter - the Bluetooth adapter
     * @return the instance of the client
     */
    public static BluetoothClient getInstance(Context context, DeviceListAdapter.BluetoothDeviceWrapper deviceWrapper, BluetoothAdapter bluetoothAdapter) {
        if (instance == null) {
            instance = new BluetoothClient(context, deviceWrapper, bluetoothAdapter);
        }
        return instance;
    }

    /**
     * Getter for the Bluetooth socket
     *
     * @return the Bluetooth socket
     */
    public BluetoothSocket getBluetoothSocket() {
        return bluetoothSocket;
    }

    /**
     * Getter for the Output Stream
     *
     * @return the Output Stream
     */
    public PrintWriter getWriter() {
        return writer;
    }

    /**
     * Getter for the Input Stream
     *
     * @return the Input Stream
     */
    public BufferedReader getReader() {
        return reader;
    }

    /**
     * Connect to the device and create a socket
     *
     * @param deviceWrapper - the device to connect to
     */
    private void connectToDevice(DeviceListAdapter.BluetoothDeviceWrapper deviceWrapper) {
        if (bluetoothAdapter == null) {
            Log.e("BluetoothConnection", "Bluetooth adapter is null");
            return;
        }
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceWrapper.getAddress());

        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }

            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
            bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid);

            bluetoothSocket = (BluetoothSocket) device.getClass().getMethod("createInsecureRfcommSocket", int.class).invoke(device, 1);
            if (bluetoothSocket == null) {
                Toast.makeText(context, "Bluetooth server is probably not running.", Toast.LENGTH_SHORT).show();
                Log.e("BluetoothConnection", "Error creating socket. Bluetooth server is probably not running.");
                return;
            }
            bluetoothSocket.connect();

            Toast.makeText(context, "Connected to " + deviceWrapper.getName(), Toast.LENGTH_SHORT).show();
            System.out.println("Connected to " + deviceWrapper.getName());

            InputStream inStream = bluetoothSocket.getInputStream();
            OutputStream outStream = bluetoothSocket.getOutputStream();

            reader = new BufferedReader(new InputStreamReader(inStream));
            writer = new PrintWriter(outStream, true);
        } catch (IOException e) {
            Log.e("BluetoothConnection", "Error connecting to device: " + deviceWrapper.getName() + ". Bluetooth server is probably not running.", e);
            Toast.makeText(context, "Bluetooth server is probably not running.", Toast.LENGTH_SHORT).show();
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            Log.e("BluetoothConnection", "Error creating socket", e);
        }
    }
}
