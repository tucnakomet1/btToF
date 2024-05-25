package com.main.rpbt;


import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.main.rpbt.databinding.FragmentSecondBinding;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SecondFragment extends Fragment {

    private FragmentSecondBinding binding;
    private ScheduledExecutorService scheduler;
    private Handler handler;

    private boolean isRunning = false;
    private double MinValue = 20;
    private double MaxValue = 2000;

    private static int GRID_SIZE = 8; // 8x8 mřížka
    private static int CELL_SIZE = 40; // Velikost každého čtverce v pixelech
    private ImageView CameraView;
    private TextView counterText;
    private List<double[][]> frames;
    private int currentFrameIndex = 0;


    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentSecondBinding.inflate(inflater, container, false);
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

        // ziska informace o displeji
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;
        CELL_SIZE = screenWidth / GRID_SIZE;

        CameraView = binding.cameraView;
        counterText = binding.counterTxt;

        handler = new Handler(Looper.getMainLooper());
        frames = JsonParser.loadDataFromJson(requireContext());
        //MaxValue = JsonParser.getMaxNum();

        int rotation = 90 * SettingsValues.getRotate();
        CameraView.setRotation(rotation);

        binding.ButtonNext.setOnClickListener(view12 -> NavHostFragment.findNavController(SecondFragment.this)
                .navigate(R.id.action_SecondFragment_to_BluetoothFragment));
        binding.ButtonPrev.setOnClickListener(view1 -> NavHostFragment.findNavController(SecondFragment.this)
                .navigate(R.id.action_SecondFragment_to_FirstFragment));


        Button playPause = binding.ButtonPlayPause;
        playPause.setOnClickListener(view13 -> {
            if (isRunning) {
                stopRunning();
                playPause.setText("Přehrát");
            } else {
                handler.post(frameRunnable);
                isRunning = true;
                playPause.setText("Pozastavit");
            }

        });
    }

    // vytvori barevnou bitmapu
    private Bitmap createHeatmapBitmap(int gridSize, int cellSize, double[][] data) {
        Bitmap bitmap = Bitmap.createBitmap(gridSize * cellSize, gridSize * cellSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();

        paint.setColor(Color.WHITE);
        canvas.drawRect(0, 0, gridSize * cellSize, gridSize * cellSize, paint);

        for (int x = 0; x < gridSize; x++) {
            for (int y = 0; y < gridSize; y++) {
                double value = data[x][y];
                int color = getColorFromValue(value);
                paint.setColor(color);

                int left = x * cellSize;
                int top = y * cellSize;
                int right = left + cellSize;
                int bottom = top + cellSize;

                canvas.drawRect(left, top, right, bottom, paint);
            }
        }

        return bitmap;
    }

    // prekonvertuje cislo na barvu
    private int getColorFromValue(double value) {
        if (value == 0.0) return Color.HSVToColor(new float[]{0f, 0.0f, 1.0f});

        double normalizedValue = (value - MinValue) / (MaxValue - MinValue);
        float hue = (float) (normalizedValue * 240.0);
        return Color.HSVToColor(new float[]{hue, 1.0f, 1.0f});
    }

    // nastavi bitmapu z JSON dat a posune iterator
    private void SetColors() {
        double[][] currentFrame = frames.get(currentFrameIndex);
        Bitmap heatmapBitmap = createHeatmapBitmap(GRID_SIZE, CELL_SIZE, currentFrame);
        CameraView.setImageBitmap(heatmapBitmap);

        currentFrameIndex = (currentFrameIndex + 1) % frames.size();
        counterText.setText(String.valueOf(currentFrameIndex));
    }

    // runnable pro spousteni snimku po sobe v zavislosti na FPS
    private final Runnable frameRunnable = new Runnable() {
        @Override
        public void run() {
            if (!frames.isEmpty()) {
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