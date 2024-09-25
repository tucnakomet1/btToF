package cz.ima.btTof.bluetooth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Base64;
import java.util.UUID;

import cz.ima.btTof.util.DeviceListAdapter;
import cz.ima.btTof.util.FileHelper;

public class BluetoothClient {
    @SuppressLint("StaticFieldLeak")
    private static BluetoothClient instance;
    private final Context context;

    private InputStream inStream;
    private BluetoothSocket bluetoothSocket;
    private final BluetoothAdapter bluetoothAdapter;

    private OutputStream outStream;

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
    public OutputStream getOutStream() {
        return outStream;
    }

    /**
     * Getter for the Input Stream
     *
     * @return the Input Stream
     */
    public InputStream getInStream() {
        return inStream;
    }

    /**
     * Connect to the device and create a socket
     *
     * @param deviceWrapper - the device to connect to
     */
    private void connectToDevice(DeviceListAdapter.BluetoothDeviceWrapper deviceWrapper) {
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
            assert bluetoothSocket != null;
            bluetoothSocket.connect();

            Toast.makeText(context, "Connected to " + deviceWrapper.getName(), Toast.LENGTH_SHORT).show();
            System.out.println("\n\nConnected to " + deviceWrapper.getName());

            inStream = bluetoothSocket.getInputStream();
            outStream = bluetoothSocket.getOutputStream();
        } catch (IOException e) {
            Log.e("BluetoothConnection", "Error connecting to device: " + deviceWrapper.getName(), e);
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            Log.e("BluetoothConnection", "Error creating socket", e);
        }
    }


    /**
     * Send the config file to the server
     * @param context - context of the application
     * @throws IOException - if an I/O error occurs
     */
    public void sendConfig(Context context) throws IOException {
        File file = new File(context.getFilesDir(), "jsonFiles/config.json");
        StringBuilder fileContent = new StringBuilder();

        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    fileContent.append(line).append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(context, "Please save the config!", Toast.LENGTH_SHORT).show();
            return;
        }
        String config = fileContent.toString();
        System.out.println(config);

        outStream.write((int) file.length());
        outStream.write(config.getBytes());
        outStream.flush();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void handleContent(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        String fileName = reader.readLine();                        // read the file name
        int fileSize = Integer.parseInt(reader.readLine());         // read the file size

        System.out.println("fileName: " + fileName);
        System.out.println("lengthContent: " + fileSize);

        File file = FileHelper.getJsonFileDir(context, fileName);   // save the file to the directory

        try (FileOutputStream fileOutput = new FileOutputStream(file);
             BufferedOutputStream bufferedOutput = new BufferedOutputStream(fileOutput)) {

            String line;
            while (!(line = reader.readLine()).equals("EOF")) {     // read until the end of the file
                byte[] decodedChunk = Base64.getDecoder().decode(line);
                bufferedOutput.write(decodedChunk);                 // write the decoded chunk to the file
            }

            bufferedOutput.flush();

            new Runnable() {
                public void run() {
                    Toast.makeText(context, "File received and saved: " + fileName, Toast.LENGTH_SHORT).show();
                }
            };
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
