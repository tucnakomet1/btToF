package com.main.rpbt.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.ToggleButton;

import com.main.rpbt.SettingsValues;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * SaveConfig - class to save the config data to a JSON file
 */
public class SaveConfig {

    private final Context context;
    private final int rotate;
    private final String numSensors, size, order, flip, numTargets;
    private String sharpener;
    private final boolean ambient, nbSpad, nbTarget, signal, range, dist, status, reflect, motion, acc, xt;
    private String fontSize, colormapMin, colormapMax;

    /**
     * Constructor - creates a SaveConfig object with the configuration data
     *
     * @param toggleButtonNbOfSensors - number of sensors
     * @param toggleButtonSize        - size of the data
     * @param editTextNumberDecimal   - sharpener
     * @param toggleButtonOrder       - order of the data
     * @param ambientPerSpad          - ambient per spad
     * @param nbSpadsEnabled          - number of spads enabled
     * @param nbTargetDetected        - number of targets detected
     * @param signalPerSpad           - signal per spad
     * @param rangeSigma              - range sigma
     * @param distance                - distance
     * @param targetStatus            - target status
     * @param reflectancePercent      - reflectance percent
     * @param motionIndicator         - motion indicator
     * @param accel                   - acceleration
     * @param xtalk                   - xtalk
     * @param editTextFontSize        - font size
     * @param editTextColormapMin     - colormap minimum
     * @param editTextColormapMax     - colormap maximum
     */
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    public SaveConfig(Context context,
                      ToggleButton toggleButtonNbOfSensors, ToggleButton toggleButtonSize, EditText editTextNumberDecimal, ToggleButton toggleButtonOrder,
                      Switch ambientPerSpad, Switch nbSpadsEnabled, Switch nbTargetDetected, Switch signalPerSpad, Switch rangeSigma, Switch distance, Switch targetStatus,
                      Switch reflectancePercent, Switch motionIndicator, Switch accel, Switch xtalk,
                      EditText editTextFontSize, EditText editTextColormapMin, EditText editTextColormapMax) {
        this.context = context;

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

        this.fontSize = String.valueOf(editTextFontSize.getText());
        this.colormapMin = String.valueOf(editTextColormapMin.getText());
        this.colormapMax = String.valueOf(editTextColormapMax.getText());

        if (this.fontSize.isEmpty())
            this.fontSize = String.valueOf(editTextFontSize.getHint());
        if (this.colormapMin.isEmpty())
            this.colormapMin = String.valueOf(editTextColormapMin.getHint());
        if (this.colormapMax.isEmpty())
            this.colormapMax = String.valueOf(editTextColormapMax.getHint());

        toJson("config.json");
    }

    /**
     * Get the configuration data of the object as a dictionary
     *
     * @return configuration data
     */
    private Map<String, String> getConfig() {
        Map<String, String> configData = new HashMap<>();
        //configData.put("com_port", comPort);
        configData.put("num_sensors", this.numSensors);
        configData.put("size", this.size);
        configData.put("num_targets", this.numTargets);
        configData.put("order", this.order);
        configData.put("sharpener", this.sharpener);
        configData.put("ambient_per_spad", this.ambient ? "on" : "off");
        configData.put("nb_spads_enabled", this.nbSpad ? "on" : "off");
        configData.put("nb_target_detected", this.nbTarget ? "on" : "off");
        configData.put("signal_per_spad", this.signal ? "on" : "off");
        configData.put("range_sigma", this.range ? "on" : "off");
        configData.put("distance", this.dist ? "on" : "off");
        configData.put("target_status", this.status ? "on" : "off");
        configData.put("reflectance_percent", this.reflect ? "on" : "off");
        configData.put("motion_indicator", this.motion ? "on" : "off");
        configData.put("accel", this.acc ? "on" : "off");
        configData.put("xtalk", this.xt ? "on" : "off");
        configData.put("rotation", String.valueOf(this.rotate));
        configData.put("flip", this.flip);
        configData.put("fontsize", this.fontSize);
        configData.put("colormap_min", this.colormapMin);
        configData.put("colormap_max", this.colormapMax);

        return configData;
    }

    /**
     * Save the configuration data to a JSON file
     *
     * @param filename name of the file
     */
    public void toJson(String filename) {
        JSONObject jsonObject = new JSONObject(getConfig());

        try {
            saveFile(jsonObject, filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Save the JSON object to a file (config.json)
     * @param jsonObject JSON object to save
     * @param filename   name of the file
     * @throws IOException if the file cannot be saved
     */
    private void saveFile(JSONObject jsonObject, String filename) throws IOException {
        File file = FileHelper.getJsonFileDir(context, filename);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(jsonObject.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}