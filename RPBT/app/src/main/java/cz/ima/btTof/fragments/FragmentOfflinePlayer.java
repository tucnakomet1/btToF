package cz.ima.btTof.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

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
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import cz.ima.btTof.util.JsonParser;
import cz.ima.btTof.R;
import cz.ima.btTof.SettingsValues;
import cz.ima.btTof.databinding.FragmentOfflinePlayerBinding;
import cz.ima.btTof.util.ColorConfig;
import cz.ima.btTof.util.FileHelper;
import cz.ima.btTof.util.RecyclerViewAdapter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Fragment for offline player - used for playing offline records
 */
public class FragmentOfflinePlayer extends Fragment {
    private ActivityResultLauncher<String> filePicker;
    private FragmentOfflinePlayerBinding binding;
    private ScheduledExecutorService scheduler;
    private Handler handler;
    private ImageView cameraView;
    private TextView counterText;
    private StreamRunnable streamRunnable;
    private ColorConfig colorConfig;
    private List<double[][]> distanceFrames, leftFrames, rightFrames;
    private Map<String, List<double[][]>> frames;

    private boolean isRunning = false;
    private int minValue = 20, maxValue = 2000, currentFrameIndex = 0;
    private int GRID_SIZE = 8, CELL_SIZE = 40, TEXT_SIZE = 18; // 8x8 grid, size of each square in pixels
    private String leftTab = "none", rightTab = "none", fileName;

    private final String[] itemList = {
            "ambient_per_spad", "nb_spads_enabled", "nb_target_detected", "signal_per_spad",
            "range_sigma", "distance", "target_status", "reflectance_percent", "motion_indicator", "none"};

    /** Constructor */
    public FragmentOfflinePlayer() { }

    /**
     * Create the view - set up the view
     * @return view
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentOfflinePlayerBinding.inflate(inflater, container, false);

        binding.fpsBar.setProgress(SettingsValues.getFPS());
        binding.progressFPS.setText(String.valueOf(SettingsValues.getFPS()));

        registerFilePicker();       // for picking a file

        String min = SettingsValues.getColMin();
        if (!min.isEmpty()) minValue = Integer.parseInt(min);

        String max = SettingsValues.getColMax();
        if (!max.isEmpty()) maxValue = Integer.parseInt(max);

        if (SettingsValues.isDetected(12)) GRID_SIZE = 4;   // if 4x4

        // left and right tabs
        setAutoCompleteTextViews(binding.autoCompleteTextLeft, true);
        setAutoCompleteTextViews(binding.autoCompleteTextRight, false);

        return binding.getRoot();
    }

    /**
     * On view created - set up the view
     * @param view - view
     * @param savedInstanceState - saved instance state
     */
    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // get display info
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;
        CELL_SIZE = screenWidth / GRID_SIZE;

        binding.autoCompleteTextLeft.setWidth(screenWidth);
        binding.autoCompleteTextRight.setWidth(screenWidth);

        listFiles();        // list all files in the assets directory

        counterText = binding.counterTxt;
        TEXT_SIZE += Integer.parseInt(SettingsValues.getFont());

        int rotation = 90 * SettingsValues.getRotate();
        cameraView = binding.cameraView;
        cameraView.setRotation(rotation);

        // fps bar
        binding.fpsBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                SettingsValues.setFPS(i);
                binding.progressFPS.setText(String.valueOf(i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });


        // buttons
        binding.ButtonNext.setOnClickListener(v -> NavHostFragment.findNavController(FragmentOfflinePlayer.this)
                .navigate(R.id.action_PlayerOfflineFragment_to_CameraOnlineFragment));
        binding.ButtonPrev.setOnClickListener(v -> NavHostFragment.findNavController(FragmentOfflinePlayer.this)
                .navigate(R.id.action_PlayerOfflineFragment_to_SettingsFragment));
        binding.ButtonLoad.setOnClickListener(v -> filePicker.launch("application/json"));

        Button playPause = binding.ButtonPlayPause;
        playPause.setOnClickListener(v -> {
            if (isRunning) {
                isRunning = false;

                stopRunning();
                playPause.setText("Play");
            } else {
                isRunning = true;

                try {
                    handler.post(frameRunnable);
                    playPause.setText("Pause");
                } catch (Exception ex) {
                    Toast toast = Toast.makeText(this.requireContext(), "Create or upload an record.!", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

        // set up the default image
        colorConfig = new ColorConfig(TEXT_SIZE, maxValue, minValue);
        Bitmap defaultImage = ColorConfig.createEmptyScreen(GRID_SIZE, CELL_SIZE, true);
        cameraView.setImageBitmap(defaultImage);

        // set up the stream runnable
        streamRunnable.setCameraView(cameraView);
        streamRunnable.setColorConfig(colorConfig);
    }

    /**
     * Set up the auto complete text views
     * @param autoCompleteTextView - auto complete text view
     * @param left - left or right
     */
    private void setAutoCompleteTextViews(AutoCompleteTextView autoCompleteTextView, boolean left) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.list_item, itemList);
        autoCompleteTextView.setAdapter(adapter);
        autoCompleteTextView.setOnItemClickListener((adapterView, view, i, l) -> {
            if (left) {
                leftTab = adapterView.getItemAtPosition(i).toString();
                leftFrames = frames.get(leftTab);
                System.out.println(leftTab);
            } else {
                rightTab = adapterView.getItemAtPosition(i).toString();
                rightFrames = frames.get(rightTab);
                System.out.println(rightTab);
            }
        });
    }


    /**
     * Register file picker - used for picking a file
     */
    private void registerFilePicker() {
        filePicker = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                this::onPickFile
        );
    }

    /**
     * On pick file - this gets executed when the user picks a file
     * @param uri - URI of the file
     */
    private void onPickFile(Uri uri) {
        if (uri != null) {
            try (InputStream ignored = requireContext().getContentResolver().openInputStream(uri)) {
                FileHelper.copyFileToInternalStorage(uri, requireContext());
                listFiles();    // reload
            } catch (IOException exception) {
                System.out.println("Failed to open file: " + exception.getMessage());
            }
        } else {
            System.out.println("No file was selected.");
        }
    }


    /**
     * List files - list all files in the assets directory. Also used for reloading items in recyclerview
     */
    @SuppressLint("SetTextI18n")
    private void listFiles() {
        // list assets directory - all JSON files (history)
        Map<String, String> listAssets = FileHelper.listAssetFiles(this.requireContext());
        ArrayList<String[]> arr_names_sizes = new ArrayList<>();
        for (Map.Entry<String, String> entry : listAssets.entrySet()) {
            String[] pair = new String[2];
            pair[0] = entry.getKey();
            pair[1] = entry.getValue();
            arr_names_sizes.add(pair);
        }

        // fill recyclerview with items
        if (arr_names_sizes.size() == 0) {
            binding.textNoRecords.setText("No records!");
            binding.textNoRecords.setTextSize(20);
            binding.videoHistoryRecyclerView.setVisibility(View.INVISIBLE);

            handler = new Handler(Looper.getMainLooper());
            streamRunnable = new StreamRunnable(handler, cameraView, colorConfig, GRID_SIZE, CELL_SIZE);
        } else {
            binding.textNoRecords.setText("Hold for 1 second to remove the item.");
            binding.textNoRecords.setTextSize(13);
            binding.videoHistoryRecyclerView.setVisibility(View.VISIBLE);

            fileName = arr_names_sizes.get(0)[0];

            // normal click - choosing a file
            RecyclerViewAdapter.OnItemClickListener onItemClickListener = position -> {
                fileName = arr_names_sizes.get(position)[0];
                frames = JsonParser.loadDataFromJson(requireContext(), GRID_SIZE, fileName);
                distanceFrames = frames.get("distance");
                currentFrameIndex = 0;
            };

            RecyclerViewAdapter listAdapter = new RecyclerViewAdapter(FragmentOfflinePlayer.this.requireContext(), arr_names_sizes, onItemClickListener, null);

            // longer click (1 second) - remove an item, file
            RecyclerViewAdapter finalListAdapter = listAdapter;
            RecyclerViewAdapter.OnItemLongClickListener onItemLongClickListener = position -> {
                String fileName = arr_names_sizes.get(position)[0];

                // alert dialog - are you sure you want to delete the file?
                new AlertDialog.Builder(requireContext())
                        .setTitle("Delete File")
                        .setMessage("Are you sure you want to delete this file?")
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> {

                            // removing file from the folder jsonFiles
                            File jsonDir = new File(requireContext().getFilesDir(), "jsonFiles");
                            File file = new File(jsonDir, fileName);

                            if (file.exists() && file.delete()) {
                                // removing item from RecyclerView and update the fragment
                                finalListAdapter.removeItem(position, fileName);
                                Toast.makeText(requireContext(), "File deleted", Toast.LENGTH_SHORT).show();

                                listFiles();
                                updateFragment();

                            } else {
                                Toast.makeText(requireContext(), "Failed to delete file", Toast.LENGTH_SHORT).show();
                            }
                        }).setNegativeButton(android.R.string.no, null).show();
            };

            listAdapter = new RecyclerViewAdapter(FragmentOfflinePlayer.this.requireContext(), arr_names_sizes, onItemClickListener, onItemLongClickListener);

            binding.videoHistoryRecyclerView.setAdapter(listAdapter);
            binding.videoHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            binding.videoHistoryRecyclerView.setClickable(true);

            handler = new Handler(Looper.getMainLooper());
            streamRunnable = new StreamRunnable(handler, cameraView, colorConfig, GRID_SIZE, CELL_SIZE);
            frames = JsonParser.loadDataFromJson(requireContext(), GRID_SIZE, fileName);
            distanceFrames = frames.get("distance");
        }
    }

    /**
     * Update the fragment
     */
    private void updateFragment() {
        Fragment currentFragment = FragmentOfflinePlayer.this;
        @SuppressLint("DetachAndAttachSameFragment") FragmentTransaction fragTransaction = getParentFragmentManager().beginTransaction();
        fragTransaction.detach(currentFragment);
        fragTransaction.attach(currentFragment);
        fragTransaction.commit();
    }

    /**
     * Runnable for running frames depending on FPS
     */
    private final Runnable frameRunnable = new Runnable() {
        @Override
        public void run() {
            if (!distanceFrames.isEmpty()) {
                scheduler = Executors.newScheduledThreadPool(4);
                Runnable setPixelsTask = () -> handler.post(() -> SetColors());
                scheduler.scheduleAtFixedRate(setPixelsTask, 0, 1000 / SettingsValues.getFPS(), TimeUnit.MILLISECONDS);
            }
        }
    };

    /**
     * Set colors on the screen - from JSON or from the server
     */
    private void SetColors() {
        double[][] currentDistanceFrame = distanceFrames.get(currentFrameIndex);
        double[][] currentLeftFrame = !leftTab.equals("none") ? leftFrames.get(currentFrameIndex) : null;
        double[][] currentRightFrame = !rightTab.equals("none") ? rightFrames.get(currentFrameIndex) : null;

        Bitmap heatmapBitmap = colorConfig.createHeatmapBitmap(GRID_SIZE, CELL_SIZE, currentDistanceFrame, currentLeftFrame, currentRightFrame, leftTab, rightTab);
        cameraView.setImageBitmap(heatmapBitmap);
        cameraView.invalidate();

        currentFrameIndex = (currentFrameIndex + 1) % distanceFrames.size();
        counterText.setText(String.valueOf(currentFrameIndex));
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
         * Set the camera view
         * @param cameraView - camera view (ImageView)
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
      * Stop running
      */
    private void stopRunning() {
        scheduler.shutdown();
        scheduler = null;
        isRunning = false;
    }

    /**
     * Destroy the view
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}