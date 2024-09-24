package com.main.rpbt.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.tabs.TabLayout;
import com.main.rpbt.R;
import com.main.rpbt.SettingsValues;
import com.main.rpbt.databinding.FragmentSettingsBinding;
import com.main.rpbt.util.SaveConfig;

import java.util.Objects;

/**
 * Fragment for the settings page
 */
public class FragmentSettings extends Fragment {

    private FragmentSettingsBinding binding;

    /**
     * Create the view of the settings fragment
     *
     * @param inflater - the layout inflater
     * @param container - the view group container
     * @param savedInstanceState - the saved instance state
     * @return the view of the settings fragment
     */
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        // bar
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        binding.rotateBar.setProgress(SettingsValues.getRotate());

        // switch
        binding.ambientPerSpad.setChecked(SettingsValues.isDetected(0));
        binding.nbSpadsEnabled.setChecked(SettingsValues.isDetected(1));
        binding.nbTargetDetected.setChecked(SettingsValues.isDetected(2));
        binding.signalPerSpad.setChecked(SettingsValues.isDetected(3));
        binding.rangeSigma.setChecked(SettingsValues.isDetected(4));
        binding.distance.setChecked(SettingsValues.isDetected(5));
        binding.targetStatus.setChecked(SettingsValues.isDetected(6));
        binding.reflectancePercent.setChecked(SettingsValues.isDetected(7));
        binding.motionIndicator.setChecked(SettingsValues.isDetected(8));
        binding.accel.setChecked(SettingsValues.isDetected(9));
        binding.xtalk.setChecked(SettingsValues.isDetected(10));

        // toggle button
        binding.toggleButtonNbOfSensors.setChecked(SettingsValues.isDetected(11));
        binding.toggleButtonSize.setChecked(SettingsValues.isDetected(12));
        binding.toggleButtonOrder.setChecked(SettingsValues.isDetected(13));

        // edit text - int
        binding.editTextNumberDecimal.setText(SettingsValues.getSharpener());
        binding.editTextFontSize.setText(SettingsValues.getFont());
        binding.editTextColormapMin.setText(SettingsValues.getColMin());
        binding.editTextColormapMax.setText(SettingsValues.getColMax());

        // layouts
        TabLayout.Tab nbTargetTab = binding.nbTargetLayout.getTabAt(Integer.parseInt(SettingsValues.getNbTargets()) - 1);
        if (nbTargetTab != null) nbTargetTab.select();

        TabLayout.Tab flipTab = binding.flipLayout.getTabAt(SettingsValues.getFlipInt());
        if (flipTab != null) flipTab.select();

        return binding.getRoot();

    }

    /**
     * Set the listeners for the buttons and the switches
     *
     * @param view - the view of the fragment
     * @param savedInstanceState - the saved instance state
     */
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // buttons
        binding.ButtonNext.setOnClickListener(v -> {
            editButtonsSet();
            NavHostFragment.findNavController(FragmentSettings.this).navigate(R.id.action_SettingsFragment_to_PlayerOfflineFragment);
        });
        binding.ButtonPrev.setOnClickListener(v -> {
            editButtonsSet();
            NavHostFragment.findNavController(FragmentSettings.this).navigate(R.id.action_SettingsFragment_to_BluetoothFragment);
        });
        binding.ButtonSaveConfig.setOnClickListener(v -> {
            editButtonsSet();
            new SaveConfig(requireContext(),
                    binding.toggleButtonNbOfSensors, binding.toggleButtonSize, binding.editTextNumberDecimal, binding.toggleButtonOrder,
                    binding.ambientPerSpad, binding.nbSpadsEnabled, binding.nbTargetDetected, binding.signalPerSpad, binding.rangeSigma,
                    binding.distance, binding.targetStatus, binding.reflectancePercent, binding.motionIndicator, binding.accel, binding.xtalk,
                    binding.editTextFontSize, binding.editTextColormapMin, binding.editTextColormapMax
                    );
        });

        // switch
        binding.ambientPerSpad.setOnClickListener(v -> SettingsValues.setDetected(binding.ambientPerSpad.isChecked(), 0));
        binding.nbSpadsEnabled.setOnClickListener(v -> SettingsValues.setDetected(binding.nbSpadsEnabled.isChecked(), 1));
        binding.nbTargetDetected.setOnClickListener(v -> SettingsValues.setDetected(binding.nbTargetDetected.isChecked(), 2));
        binding.signalPerSpad.setOnClickListener(v -> SettingsValues.setDetected(binding.signalPerSpad.isChecked(), 3));
        binding.rangeSigma.setOnClickListener(v -> SettingsValues.setDetected(binding.rangeSigma.isChecked(), 4));
        binding.distance.setOnClickListener(v -> SettingsValues.setDetected(binding.distance.isChecked(), 5));
        binding.targetStatus.setOnClickListener(v -> SettingsValues.setDetected(binding.targetStatus.isChecked(), 6));
        binding.reflectancePercent.setOnClickListener(v -> SettingsValues.setDetected(binding.reflectancePercent.isChecked(), 7));
        binding.motionIndicator.setOnClickListener(v -> SettingsValues.setDetected(binding.motionIndicator.isChecked(), 8));
        binding.accel.setOnClickListener(v -> SettingsValues.setDetected(binding.accel.isChecked(), 9));
        binding.xtalk.setOnClickListener(v -> SettingsValues.setDetected(binding.xtalk.isChecked(), 10));

        // toggle button
        binding.toggleButtonNbOfSensors.setOnClickListener(v -> SettingsValues.setDetected(binding.toggleButtonNbOfSensors.isChecked(), 11));
        binding.toggleButtonSize.setOnClickListener(v -> SettingsValues.setDetected(binding.toggleButtonSize.isChecked(), 12));
        binding.toggleButtonOrder.setOnClickListener(v -> SettingsValues.setDetected(binding.toggleButtonOrder.isChecked(), 13));

        // edit text - int
        binding.editTextNumberDecimal.setOnClickListener(v -> SettingsValues.setSharpener(String.valueOf(binding.editTextNumberDecimal.getText())));
        binding.editTextFontSize.setOnClickListener(v -> SettingsValues.setFont(String.valueOf(binding.editTextFontSize.getText())));
        binding.editTextColormapMin.setOnClickListener(v -> SettingsValues.setColMin(String.valueOf(binding.editTextColormapMin.getText())));
        binding.editTextColormapMax.setOnClickListener(v -> SettingsValues.setColMax(String.valueOf(binding.editTextColormapMax.getText())));

        // bar
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

        // layouts
        binding.flipLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String val = Objects.requireNonNull(tab.getText()).toString();
                SettingsValues.setFlip(val);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        binding.nbTargetLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String val = Objects.requireNonNull(tab.getText()).toString();
                SettingsValues.setNbTargets(val);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    /**
     * Set the values of the buttons
     */
    private void editButtonsSet() {
        SettingsValues.setSharpener(String.valueOf(binding.editTextNumberDecimal.getText()));
        SettingsValues.setColMin(String.valueOf(binding.editTextColormapMin.getText()));
        SettingsValues.setColMax(String.valueOf(binding.editTextColormapMax.getText()));

        String fontSize = String.valueOf(binding.editTextFontSize.getText());
        if (fontSize.equals(""))
            SettingsValues.setFont("0");
        else
            SettingsValues.setFont(fontSize);
    }

    /**
     * Destroy the view of the settings fragment
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}