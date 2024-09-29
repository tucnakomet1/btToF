package cz.ima.btTof.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import cz.ima.btTof.R;
import cz.ima.btTof.SettingsValues;
import cz.ima.btTof.bluetooth.BluetoothClient;
import cz.ima.btTof.databinding.FragmentCameraBinding;
import cz.ima.btTof.lan.LanClient;
import cz.ima.btTof.util.ColorConfig;
import cz.ima.btTof.util.JsonParser;
import cz.ima.btTof.util.SendReceive;

/**
 * Fragment for online player - used for streaming and recording the frames from the server.
 */
public class FragmentOnlineCamera extends Fragment {
    private PrintWriter btWriter;
    private BufferedReader btReader;


    private PrintWriter writer;
    private BufferedReader reader;
    private ExecutorService executorService;

    // rest

    private FragmentCameraBinding binding;
    private StreamRunnable streamRunnable;
    private List<double[][]> leftFrames, rightFrames;
    private Map<String, List<double[][]>> frames;

    private boolean isRunning = false, isRecording = false, isStreaming = false, isBluetooth = false, isConnected = false;;
    private int minValue = 20, maxValue = 2000;
    private int GRID_SIZE = 8, TEXT_SIZE = 18;  // 8x8 grid, size of each square in pixels

    /** Constructor */
    public FragmentOnlineCamera() {}

    /**
     * Create the view - set up the view
     * @return view
     */
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentCameraBinding.inflate(inflater, container, false);

        // connection to the server
        executorService = Executors.newFixedThreadPool(2);
        try {
            executorService.execute(this::connectToBluetoothServer);
            connectToBluetoothServer();

            if (!isBluetooth) {
                executorService.execute(this::connectToLanServer);
                connectToLanServer();
            }
        } catch (Exception ignored) {

        }

        String min = SettingsValues.getColMin();
        if (!min.isEmpty()) minValue = Integer.parseInt(min);

        String max = SettingsValues.getColMax();
        if (!max.isEmpty()) maxValue = Integer.parseInt(max);

        if (SettingsValues.isDetected(12)) GRID_SIZE = 4;   // 4x4 grid

        return binding.getRoot();
    }

    /**
     * Set up the view
     * @param view - view
     * @param savedInstanceState - saved instance state
     */
    @SuppressLint("SetTextI18n")
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // gets display info
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;
        int CELL_SIZE = screenWidth / GRID_SIZE;


        // other
        TEXT_SIZE += Integer.parseInt(SettingsValues.getFont());

        int rotation = 90 * SettingsValues.getRotate();
        ImageView cameraView = binding.cameraView;
        cameraView.setRotation(rotation);

        // buttons
        binding.ButtonNext.setOnClickListener(v -> NavHostFragment.findNavController(FragmentOnlineCamera.this)
                .navigate(R.id.action_CameraOnlineFragment_to_BluetoothFragment));
        binding.ButtonPrev.setOnClickListener(v -> NavHostFragment.findNavController(FragmentOnlineCamera.this)
                .navigate(R.id.action_CameraOnlineFragment_to_PlayerOfflineFragment));

        Button playPause = binding.ButtonPlayPause;
        playPause.setOnClickListener(v -> {
            if (isRunning) {
                isRunning = false;
                isStreaming = !isStreaming;

                playPause.setText("Stream");
                executorService.execute(() -> sendMessage("stream"));
            } else {
                isRunning = true;

                if (isConnected) {
                    Thread thread = new Thread(this::startStreaming);
                    thread.start();
                    playPause.setText("Pause");
                } else {
                    Toast toast = Toast.makeText(this.requireContext(), "You have to connect first!", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

        Button recordStop = binding.ButtonRecordStop;
        recordStop.setOnClickListener(v -> {
            if (isStreaming) {
                if (isRecording) {
                    isRecording = false;
                    System.out.println("Stop recording!!!");

                    executorService.execute(() -> sendMessage("record"));

                    recordStop.setText("Record");
                } else {
                    isRecording = true;
                    System.out.println("Start recording!!!");
                    executorService.execute(() -> sendMessage("record"));
                    recordStop.setText("Stop");
                }
            } else {
                Toast.makeText(requireContext(), "Start streaming first!", Toast.LENGTH_SHORT).show();
            }
        });

        // set up the default image
        ColorConfig colorConfig = new ColorConfig(TEXT_SIZE, maxValue, minValue);
        Bitmap defaultImage = ColorConfig.createEmptyScreen(GRID_SIZE, CELL_SIZE, false);
        cameraView.setImageBitmap(defaultImage);

        Handler handler = new Handler(Looper.getMainLooper());
        streamRunnable = new StreamRunnable(handler, cameraView, colorConfig, GRID_SIZE, CELL_SIZE);

        // set up the stream runnable
        streamRunnable.setCameraView(cameraView);
        streamRunnable.setColorConfig(colorConfig);

        // send config to the server
        PrintWriter commonWriter = isBluetooth ? btWriter : writer;
        if (commonWriter != null) {
            sendMessage("config");
            try {
                SendReceive.sendConfig(requireContext(), commonWriter);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else noConnection();
    }

    /**
     * Runnable for streaming frames from the server
     */
    public static class StreamRunnable implements Runnable {
        private double[][] frameData;
        private final Handler handler;
        private ImageView cameraView;
        private ColorConfig colorConfig;
        private final int GRID_SIZE, CELL_SIZE;

        /**
         * Constructor for passing necessary dependencies
         * @param handler - handler
         * @param cameraView - camera view
         * @param colorConfig - color config
         * @param GRID_SIZE - grid size
         * @param CELL_SIZE - cell size
         */
        public StreamRunnable(Handler handler, ImageView cameraView, ColorConfig colorConfig, int GRID_SIZE, int CELL_SIZE) {
            this.handler = handler;
            this.cameraView = cameraView;
            this.colorConfig = colorConfig;
            this.GRID_SIZE = GRID_SIZE;
            this.CELL_SIZE = CELL_SIZE;
        }

        @Override
        public void run() {
            if (frameData != null) {
                // Post UI update to the main thread using the handler
                handler.post(() -> {
                    assert colorConfig != null;
                    Bitmap heatmapBitmap = colorConfig.createHeatmapBitmap(GRID_SIZE, CELL_SIZE, frameData, null, null, "none", "none");
                    cameraView.setImageBitmap(heatmapBitmap);
                });
            }
        }

        /**
         * Set the frame data
         * @param frameData - frame data
         */
        public void setFrameData(double[][] frameData) {
            this.frameData = frameData;
        }

        /**
         * Set the camera view
         * @param cameraView - camera view
         */
        public void setCameraView(ImageView cameraView) {
            this.cameraView = cameraView;
        }

        /**
         * Set the color config
         * @param colorConfig - color config
         */
        public void setColorConfig(ColorConfig colorConfig) {
            this.colorConfig = colorConfig;
        }
    }

    /**
     * Connect to the LAN server
     */
    @SuppressLint("SetTextI18n")
    private void connectToLanServer() {
        LanClient client = LanClient.getInstance(requireContext(), null, 0);

        if (client.getSocket() != null) {
            writer = client.getWriter();
            reader = client.getReader();

            if (writer != null) {
                binding.pairedView.setImageResource(R.drawable.circle_green);
                isConnected = true;
                binding.ButtonPlayPause.setText("Stream");
            } else noConnection();
        } else noConnection();
    }

    /**
     * Connect to the Bluetooth server
     */
    @SuppressLint("SetTextI18n")
    private void connectToBluetoothServer() {
        BluetoothClient btClient = BluetoothClient.getInstance(requireContext(), null, null);

        if (btClient.getBluetoothSocket() != null) {
            btWriter = btClient.getWriter();
            btReader = btClient.getReader();

            if (btWriter != null) {
                isConnected = true;
                isBluetooth = true;
                binding.pairedView.setImageResource(R.drawable.circle_green);
            } else noConnection();
        } else noConnection();
    }

    /**
     * Start streaming
     */
    private void startStreaming() {
        isStreaming = !isStreaming;
        executorService.execute(() -> sendMessage("stream"));

        BufferedReader listenReader = isBluetooth ? btReader : this.reader;
        listenForServerResponse(listenReader);
    }

    /**
     * Send message to the server
     * @param message - message to send
     */
    private void sendMessage(String message) {
        PrintWriter commonWriter = isBluetooth ? btWriter : writer;
        if (commonWriter != null) commonWriter.println(message);
        else noConnection();
    }

    /**
     * Listen for server response - get the frames from the server and set the colors
     */
    private void listenForServerResponse(BufferedReader listenReader) {
        try {
            String message;
            if (listenReader == null) return;
            while ((message = listenReader.readLine()) != null) {
                if (message.contains("content")) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        showAlertSendingFile();
                        SendReceive.handleContent(requireContext(), listenReader);
                    }
                    sendMessage("stream");

                } else if (message.startsWith("[")) {
                    double[][] frameData = JsonParser.makeDistantFramesFromString(message);

                    // Start streaming task with received data
                    streamRunnable.setFrameData(frameData);
                    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
                    scheduler.execute(streamRunnable);
                }
            }
        } catch (IOException ex) {
            System.out.println("Error reading server response: " + ex.getMessage());
        }
    }

    /**
     * Show alert that the file is being sent from the server.
     */
    private void showAlertSendingFile() {
        requireActivity().runOnUiThread(() -> {
            new AlertDialog.Builder(requireContext())
                .setTitle("Wait")
                .setMessage("The file is being sent from the server to your device. Please wait. It may take a while.\n\n" +
                        "Sending is done when streaming is started again.")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {}).show();
        });
    }

    /**
     * No connection to the server
     */
    private void noConnection() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() ->Toast.makeText(requireContext(), "No connection to the server", Toast.LENGTH_SHORT).show());

        binding.pairedView.setImageResource(R.drawable.circle_red);
    }

    /**
     * Destroy the view
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;

        executorService.shutdown();
    }
}