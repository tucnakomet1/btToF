package com.main.rpbt;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.main.rpbt.databinding.FragmentFirstBinding;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        binding.rotateBar.setProgress(SettingsValues.getRotate());
        binding.fpsBar.setProgress(SettingsValues.getFPS());
        binding.progressFPS.setText(String.valueOf(SettingsValues.getFPS()));
        binding.detected.setChecked(SettingsValues.isDetected());
        binding.distance.setChecked(SettingsValues.isDistance());
        binding.stats.setChecked(SettingsValues.isStats());

        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.ButtonNext.setOnClickListener(view1 -> NavHostFragment.findNavController(FirstFragment.this)
                .navigate(R.id.action_FirstFragment_to_SecondFragment));
        binding.ButtonPrev.setOnClickListener(view12 -> NavHostFragment.findNavController(FirstFragment.this)
                .navigate(R.id.action_FirstFragment_to_BluetoothFragment2));

        binding.detected.setOnClickListener(view13 -> SettingsValues.setDetected(binding.detected.isChecked()));
        binding.distance.setOnClickListener(view14 -> SettingsValues.setDistance(binding.distance.isChecked()));
        binding.stats.setOnClickListener(view15 -> SettingsValues.setStats(binding.stats.isChecked()));

        // nastaveni rotate
        binding.rotateBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                SettingsValues.setRotate(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        // nastaveni FPS
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
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}