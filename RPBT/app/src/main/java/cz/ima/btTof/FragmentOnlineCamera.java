package cz.ima.btTof;


import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import cz.ima.btTof.databinding.FragmentCameraBinding;
import cz.ima.btTof.lan.LanClient;
import cz.ima.btTof.util.ColorConfig;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class FragmentOnlineCamera extends Fragment {

    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private ExecutorService executorService;
    private LanClient client;

    // rest

    private FragmentCameraBinding binding;
    StreamRunnable streamRunnable;
    private List<double[][]> leftFrames, rightFrames;
    private Map<String, List<double[][]>> frames;

    private boolean isRunning = false, isRecording = false, isConnected = false, isStreaming = false;
    private int minValue = 20, maxValue = 2000;
    private int GRID_SIZE = 8, TEXT_SIZE = 18;  // 8x8 grid, size of each square in pixels
    private String leftTab = "none", rightTab = "none";

    private final String[] itemList = {
            "ambient_per_spad", "nb_spads_enabled", "nb_target_detected", "signal_per_spad",
            "range_sigma", "distance", "target_status", "reflectance_percent", "motion_indicator", "none"};

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
        executorService.execute(this::connectToServer);
        connectToServer();

        String min = SettingsValues.getColMin();
        if (!min.isEmpty()) minValue = Integer.parseInt(min);

        String max = SettingsValues.getColMax();
        if (!max.isEmpty()) maxValue = Integer.parseInt(max);

        if (SettingsValues.isDetected(12)) GRID_SIZE = 4;   // 4x4

        // left and right tabs
        AutoCompleteTextView autoCompleteTextViewLeft = binding.autoCompleteTextLeft;
        ArrayAdapter<String> adapterLeft = new ArrayAdapter<>(FragmentOnlineCamera.this.getContext(), R.layout.list_item, itemList);
        autoCompleteTextViewLeft.setAdapter(adapterLeft);
        autoCompleteTextViewLeft.setOnItemClickListener((adapterView, view, i, l) -> {
            leftTab = adapterView.getItemAtPosition(i).toString();
            leftFrames = frames.get(leftTab);
            System.out.println(leftTab);
        });

        AutoCompleteTextView autoCompleteTextViewRight = binding.autoCompleteTextRight;
        ArrayAdapter<String> adapterRight = new ArrayAdapter<>(FragmentOnlineCamera.this.getContext(), R.layout.list_item, itemList);
        autoCompleteTextViewRight.setAdapter(adapterRight);
        autoCompleteTextViewRight.setOnItemClickListener((adapterView, view, i, l) -> {
            rightTab = adapterView.getItemAtPosition(i).toString();
            rightFrames = frames.get(rightTab);
            System.out.println(rightTab);
        });

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

        binding.autoCompleteTextLeft.setWidth(screenWidth);
        binding.autoCompleteTextRight.setWidth(screenWidth);

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
        if (writer != null) {
            sendMessage("config");
            try {
                client.sendConfig(requireContext());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
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

        // Constructor for passing necessary dependencies
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
        public void setFrameData(double[][] frameData) {
            this.frameData = frameData;
        }

        public void setCameraView(ImageView cameraView) {
            this.cameraView = cameraView;
        }

        public void setColorConfig(ColorConfig colorConfig) {
            this.colorConfig = colorConfig;
        }
    }

    /**
     * Destroy the view
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;

        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        executorService.shutdown();
    }


    /**
     * Connect to the server
     */
    @SuppressLint("SetTextI18n")
    private void connectToServer() {
        client = LanClient.getInstance(requireContext(), null, 0);

        if (client.getSocket() != null) {
            socket = client.getSocket();
            writer = client.getWriter();
            reader = client.getReader();

            binding.pairedView.setImageResource(R.drawable.circle_green);
            isConnected = true;
            binding.ButtonPlayPause.setText("Stream");
        } else {
            new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(requireContext(), "No connection to the server", Toast.LENGTH_SHORT).show());
        }
    }

    /**
     * Start streaming
     */
    private void startStreaming() {
        isStreaming = !isStreaming;
        executorService.execute(() -> sendMessage("stream"));
        listenForServerResponse();
    }

    /**
     * Send message to the server
     * @param message - message to send
     */
    private void sendMessage(String message) {
        writer.println(message);
    }

    /**
     * Listen for server response - get the frames from the server and set the colors
     */
    private void listenForServerResponse() {
        try {
            String message;
            while ((message = reader.readLine()) != null) {
                System.out.println("         message: " + message);

                if (message.contains("content")) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        client.handleContent(reader);
                    }
                    sendMessage("stream");

                } else if (message.startsWith("[")) {
                    System.out.println("Received message: " + message);

                    double[][] frameData = makeDistantFramesFromString(message);

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
     * Make distant frames from string
     * @param message - String in format "[[double], [double], ...]"
     * @return frame - double[][] array
     */
    private double[][] makeDistantFramesFromString(String message) {
        JSONArray jsonArray;
        double[][] frame;
        try {
            jsonArray = new JSONArray(message);
            frame = new double[jsonArray.length()][jsonArray.getJSONArray(0).length()];

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONArray innerArray = jsonArray.getJSONArray(i);
                for (int j = 0; j < innerArray.length(); j++) {
                    frame[i][j] = innerArray.getDouble(j);
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        //System.out.println("Frame: " + Arrays.deepToString(frame));

        return frame;
    }
}