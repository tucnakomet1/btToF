package com.main.rpbt.util;

import android.widget.EditText;
import android.widget.Switch;
import android.widget.ToggleButton;

import com.main.rpbt.SettingsValues;

import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SaveConfig {

    private final int rotate;
    private final String numSensors, size, order, flip, numTargets;
    private String sharpener;
    private final boolean ambient, nbSpad, nbTarget, signal, range, dist, status, reflect, motion, acc, xt;
    private String fontsize, colormapMin, colormapMax;

    public SaveConfig(
            ToggleButton toggleButtonNbOfSensors, ToggleButton toggleButtonSize, EditText editTextNumberDecimal, ToggleButton toggleButtonOrder,
            Switch ambientPerSpad, Switch nbSpadsEnabled, Switch nbTargetDetected, Switch signalPerSpad, Switch rangeSigma, Switch distance, Switch targetStatus,
            Switch reflectancePercent, Switch motionIndicator, Switch accel, Switch xtalk,
            EditText editTextFontSize, EditText editTextColormapMin, EditText editTextColormapMax) {

        this.rotate = SettingsValues.getRotate();
        this.flip = SettingsValues.getFlip();
        this.numTargets = SettingsValues.getNbTargets();

        this.numSensors = String.valueOf(toggleButtonNbOfSensors.getText());
        this.size = String.valueOf(toggleButtonSize.getText());
        this.sharpener = String.valueOf(editTextNumberDecimal.getText());
        this.order = String.valueOf(toggleButtonOrder.getText());

        if (this.sharpener.isEmpty())
            this.sharpener = String.valueOf(editTextNumberDecimal.getHint());

        this.ambient = ambientPerSpad.isChecked();
        this.nbSpad = nbSpadsEnabled.isChecked();
        this.nbTarget = nbTargetDetected.isChecked();
        this.signal = signalPerSpad.isChecked();
        this.range = rangeSigma.isChecked();
        this.dist = distance.isChecked();
        this.status = targetStatus.isChecked();
        this.reflect = reflectancePercent.isChecked();
        this.motion = motionIndicator.isChecked();
        this.acc = accel.isChecked();
        this.xt = xtalk.isChecked();

        this.fontsize = String.valueOf(editTextFontSize.getText());
        this.colormapMin = String.valueOf(editTextColormapMin.getText());
        this.colormapMax = String.valueOf(editTextColormapMax.getText());

        if (this.fontsize.isEmpty())
            this.fontsize = String.valueOf(editTextFontSize.getHint());
        if (this.colormapMin.isEmpty())
            this.colormapMin = String.valueOf(editTextColormapMin.getHint());
        if (this.colormapMax.isEmpty())
            this.colormapMax = String.valueOf(editTextColormapMax.getHint());

        toJson("config.json");
    }

    /** Get the configuration data of the object as a dictionary
     * @return configuration data */
    private Map<String, String> getConfig() {
        Map<String, String> configData = new HashMap<>();
        //configData.put("com_port", comPort);
        configData.put("num_sensors", this.numSensors);
        configData.put("size", this.size);
        configData.put("num_targets", this.numTargets);
        configData.put("order", this.order);
        configData.put("sharpener", this.sharpener);
        configData.put("ambient_per_spad", String.valueOf(this.ambient));
        configData.put("nb_spads_enabled", String.valueOf(this.nbSpad));
        configData.put("nb_target_detected", String.valueOf(this.nbTarget));
        configData.put("signal_per_spad", String.valueOf(this.signal));
        configData.put("range_sigma", String.valueOf(this.range));
        configData.put("distance", String.valueOf(this.dist));
        configData.put("target_status", String.valueOf(this.status));
        configData.put("reflectance_percent", String.valueOf(this.reflect));
        configData.put("motion_indicator", String.valueOf(this.motion));
        configData.put("accel", String.valueOf(this.acc));
        configData.put("xtalk", String.valueOf(this.xt));
        configData.put("rotation", String.valueOf(this.rotate));
        configData.put("flip", this.flip);
        configData.put("fontsize", this.fontsize);
        configData.put("colormap_min", this.colormapMin);
        configData.put("colormap_max", this.colormapMax);

        System.out.println(configData);

        return configData;
    }

    /** Save the configuration data to a JSON file
     * @param filename name of the file */
    public void toJson(String filename) {
        JSONObject jsonObject = new JSONObject(getConfig());
        System.out.println(jsonObject);

        // TODO: save file - different method
        try (FileWriter file = new FileWriter(filename)) {
            file.write(jsonObject.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
