package com.main.rpbt;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.net.Uri;
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
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.main.rpbt.databinding.FragmentSecondBinding;
import com.main.rpbt.util.ColorConfig;
import com.main.rpbt.util.FileHelper;
import com.main.rpbt.util.RecyclerViewAdapter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SecondFragment extends Fragment {

    private ActivityResultLauncher<String> filePicker;
    private FragmentSecondBinding binding;
    private ScheduledExecutorService scheduler;
    private Handler handler;
    private ImageView CameraView;
    private TextView counterText;
    private ColorConfig colorConfig;
    private List<double[][]> distanceFrames, leftFrames, rightFrames;
    private Map<String, List<double[][]>> frames;

    private boolean isRunning = false, isRecording = false;
    private int minValue = 20, maxValue = 2000, currentFrameIndex = 0;
    private int GRID_SIZE = 8, CELL_SIZE = 40, TEXT_SIZE = 18; // 8x8 grid, size of each square in pixels
    private String leftTab = "none", rightTab = "none", fileName;

    private final String[] itemList = {
            "ambient_per_spad", "nb_spads_enabled", "nb_target_detected", "signal_per_spad",
            "range_sigma", "distance", "target_status", "reflectance_percent", "motion_indicator", "none"};

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentSecondBinding.inflate(inflater, container, false);

        binding.fpsBar.setProgress(SettingsValues.getFPS());
        binding.progressFPS.setText(String.valueOf(SettingsValues.getFPS()));

        registerFilePicker();

        String min = SettingsValues.getColMin();
        if (!min.isEmpty()) minValue = Integer.parseInt(min);

        String max = SettingsValues.getColMax();
        if (!max.isEmpty()) maxValue = Integer.parseInt(max);

        if (SettingsValues.isDetected(12)) GRID_SIZE = 4;   // 4x4

        AutoCompleteTextView autoCompleteTextViewLeft = binding.autoCompleteTextLeft;
        ArrayAdapter<String> adapterLeft = new ArrayAdapter<>(SecondFragment.this.getContext(), R.layout.list_item, itemList);
        autoCompleteTextViewLeft.setAdapter(adapterLeft);
        autoCompleteTextViewLeft.setOnItemClickListener((adapterView, view, i, l) -> {
            leftTab = adapterView.getItemAtPosition(i).toString();
            leftFrames = frames.get(leftTab);
            System.out.println(leftTab);
        });

        AutoCompleteTextView autoCompleteTextViewRight = binding.autoCompleteTextRight;
        ArrayAdapter<String> adapterRight = new ArrayAdapter<>(SecondFragment.this.getContext(), R.layout.list_item, itemList);
        autoCompleteTextViewRight.setAdapter(adapterRight);
        autoCompleteTextViewRight.setOnItemClickListener((adapterView, view, i, l) -> {
            rightTab = adapterView.getItemAtPosition(i).toString();
            rightFrames = frames.get(rightTab);
            System.out.println(rightTab);
        });


        return binding.getRoot();
    }

    private void stopRunning() {
        scheduler.shutdown();
        scheduler = null;
        isRunning = false;
    }

    @SuppressLint("SetTextI18n")
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // gets display info
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;
        CELL_SIZE = screenWidth / GRID_SIZE;

        binding.autoCompleteTextLeft.setWidth(screenWidth);
        binding.autoCompleteTextRight.setWidth(screenWidth);

        listFiles();

        // other
        counterText = binding.counterTxt;
        TEXT_SIZE += Integer.parseInt(SettingsValues.getFont());

        int rotation = 90 * SettingsValues.getRotate();
        CameraView = binding.cameraView;
        CameraView.setRotation(rotation);

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


        Button playPause = binding.ButtonPlayPause;
        playPause.setOnClickListener(v -> {
            if (isRunning) {
                stopRunning();
                playPause.setText("Play");
            } else {
                try {
                    handler.post(frameRunnable);
                    isRunning = true;
                    playPause.setText("Pause");
                } catch (Exception ex) {
                    Toast toast = Toast.makeText(this.requireContext(), "Create or upload an record.!", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

        binding.ButtonNext.setOnClickListener(v -> NavHostFragment.findNavController(SecondFragment.this)
                .navigate(R.id.action_SecondFragment_to_BluetoothFragment));
        binding.ButtonPrev.setOnClickListener(v -> NavHostFragment.findNavController(SecondFragment.this)
                .navigate(R.id.action_SecondFragment_to_FirstFragment));

        Button recordStop = binding.ButtonRecordStop;
        recordStop.setOnClickListener(v -> {
            if (isRecording) {
                isRecording = false;
                recordStop.setText("Record");
            } else {
                isRecording = true;
                recordStop.setText("Stop");
            }
        });

        binding.ButtonLoad.setOnClickListener(v -> filePicker.launch("application/json"));

        colorConfig = new ColorConfig(TEXT_SIZE, maxValue, minValue);
        Bitmap defaultImage = colorConfig.createEmptyScreen(GRID_SIZE, CELL_SIZE);
        CameraView.setImageBitmap(defaultImage);
    }

    private void registerFilePicker() {
        filePicker = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                this::onPickFile
        );
    }

    //this gets executed when the user picks a file
    private void onPickFile(Uri uri) {
        try (InputStream ignored = requireContext().getContentResolver().openInputStream(uri)) {
            if (uri != null) {
                FileHelper.copyFileToInternalStorage(uri, requireContext());
                listFiles();    // reload
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }


    // used for reloading items in recyclerview
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

        if (arr_names_sizes.size() == 0) {
            binding.textNoRecords.setText("No records!");
            binding.textNoRecords.setTextSize(20);
            binding.videoHistoryRecyclerView.setVisibility(View.INVISIBLE);
        } else {
            binding.textNoRecords.setText("Hold for 1 second to remove the item.");
            binding.textNoRecords.setTextSize(13);
            binding.videoHistoryRecyclerView.setVisibility(View.VISIBLE);
            //binding.textNoRecords.setHeight(0);

            fileName = arr_names_sizes.get(0)[0];

            // normal click - choosing a file
            RecyclerViewAdapter.OnItemClickListener onItemClickListener = position -> {
                fileName = arr_names_sizes.get(position)[0];
                frames = JsonParser.loadDataFromJson(requireContext(), GRID_SIZE, fileName);
                distanceFrames = frames.get("distance");
                currentFrameIndex = 0;
            };

            RecyclerViewAdapter listAdapter = new RecyclerViewAdapter(
                    SecondFragment.this.requireContext(),
                    arr_names_sizes,
                    onItemClickListener,
                    null
            );

            // longer click (1 second) - remove an item, file
            RecyclerViewAdapter finalListAdapter = listAdapter;
            RecyclerViewAdapter.OnItemLongClickListener onItemLongClickListener = position -> {
                String fileName = arr_names_sizes.get(position)[0];

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
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
            };

            listAdapter = new RecyclerViewAdapter(
                    SecondFragment.this.requireContext(),
                    arr_names_sizes,
                    onItemClickListener,
                    onItemLongClickListener
            );

            binding.videoHistoryRecyclerView.setAdapter(listAdapter);
            binding.videoHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            binding.videoHistoryRecyclerView.setClickable(true);



            handler = new Handler(Looper.getMainLooper());
            frames = JsonParser.loadDataFromJson(requireContext(), GRID_SIZE, fileName);
            distanceFrames = frames.get("distance");
        }

    }

    private void updateFragment() {
        Fragment currentFragment = SecondFragment.this;
        @SuppressLint("DetachAndAttachSameFragment") FragmentTransaction fragTransaction = getParentFragmentManager().beginTransaction();
        fragTransaction.detach(currentFragment);
        fragTransaction.attach(currentFragment);
        fragTransaction.commit();
    }


    // sets bitmap from JSON and iterate
    private void SetColors() {
        double[][] currentDistanceFrame = distanceFrames.get(currentFrameIndex);

        double[][] currentLeftFrame = null;
        if (!leftTab.equals("none"))
            currentLeftFrame = leftFrames.get(currentFrameIndex);

        double[][] currentRightFrame = null;
        if (!rightTab.equals("none"))
            currentRightFrame = rightFrames.get(currentFrameIndex);

        Bitmap heatmapBitmap = colorConfig.createHeatmapBitmap(GRID_SIZE, CELL_SIZE, currentDistanceFrame, currentLeftFrame, currentRightFrame, leftTab, rightTab);
        CameraView.setImageBitmap(heatmapBitmap);

        currentFrameIndex = (currentFrameIndex + 1) % distanceFrames.size();
        counterText.setText(String.valueOf(currentFrameIndex));
    }

    // runnable for running frames depending on FPS
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


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}