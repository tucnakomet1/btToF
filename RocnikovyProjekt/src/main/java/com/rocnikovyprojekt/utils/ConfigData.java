package com.rocnikovyprojekt.utils;

import com.rocnikovyprojekt.tof.TofRecording;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/** Class for handling the configuration data */
public class ConfigData {

    private final Logger logger = Logger.getLogger(ConfigData.class.getName());
    private static final byte[] SYNC_MESSAGE = {(byte) 0xA5, (byte) 0x68, (byte) 0x47, (byte) 0x0F};

    private static String[] metadata;

    private static String com_port = "", num_sensors = "", size = "", num_targets = "", order = "", sharpener = "";
    private static String ambient_per_spad = "", nb_spads_enabled = "", nb_target_detected = "", signal_per_spad = "", range_sigma = "", distance = "", target_status = "", reflectance_percent = "", motion_indicator = "", accel = "", xtalk = "";
    private static String rotation = "", flip = "", fontsize = "", colormap_min = "", colormap_max = "";


    /** Constructor - fills the metadata array */
    public ConfigData() {
        metadata = new String[]{
                com_port, num_sensors, size, num_targets, order, sharpener,
                ambient_per_spad, nb_spads_enabled, nb_target_detected, signal_per_spad, range_sigma, distance, target_status, reflectance_percent, motion_indicator, accel, xtalk,
                rotation, flip, fontsize, colormap_min, colormap_max
        };
    }

    /** Update the metadata array
     * @return new ConfigData object */
    public ConfigData update () {
        return new ConfigData();
    }

    /** Get the configuration data of the object as a dictionary
     * @return configuration data */
    public Map<String, String> getConfig() {
        Map<String, String> configData = new HashMap<>();
        configData.put("com_port", com_port);
        configData.put("num_sensors", num_sensors);
        configData.put("size", size);
        configData.put("num_targets", num_targets);
        configData.put("order", order);
        configData.put("sharpener", sharpener);
        configData.put("ambient_per_spad", ambient_per_spad);
        configData.put("nb_spads_enabled", nb_spads_enabled);
        configData.put("nb_target_detected", nb_target_detected);
        configData.put("signal_per_spad", signal_per_spad);
        configData.put("range_sigma", range_sigma);
        configData.put("distance", distance);
        configData.put("target_status", target_status);
        configData.put("reflectance_percent", reflectance_percent);
        configData.put("motion_indicator", motion_indicator);
        configData.put("accel", accel);
        configData.put("xtalk", xtalk);
        configData.put("rotation", rotation);
        configData.put("flip", flip);
        configData.put("fontsize", fontsize);
        configData.put("colormap_min", colormap_min);
        configData.put("colormap_max", colormap_max);

        return configData;
    }

    /** Save the configuration data to a JSON file
     * @param filename name of the file */
    public void toJson(String filename) {
        JSONObject jsonObject = new JSONObject(getConfig());

        try (FileWriter file = new FileWriter(filename)) {
            file.write(jsonObject.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /** Load the configuration data from a JSON file and set the fields
     * @param filename name of the file */
    public void fromJson(String filename) {
        try (FileReader reader = new FileReader(filename)) {
            JSONTokener tokener = new JSONTokener(reader);
            JSONObject jsonObject = new JSONObject(tokener);

            // Use the keys to set values for ConfigData fields
            com_port = jsonObject.optString("com_port", "none");
            num_sensors = jsonObject.optString("num_sensors", "1");
            size = jsonObject.optString("size", "8x8");
            num_targets = jsonObject.optString("num_targets", "4");
            order = jsonObject.optString("order", "closest");
            sharpener = jsonObject.optString("sharpener", "5");
            ambient_per_spad = jsonObject.optString("ambient_per_spad", "off");
            nb_spads_enabled = jsonObject.optString("nb_spads_enabled", "off");
            nb_target_detected = jsonObject.optString("nb_target_detected", "on");
            signal_per_spad = jsonObject.optString("signal_per_spad", "off");
            range_sigma = jsonObject.optString("range_sigma", "off");
            distance = jsonObject.optString("distance", "on");
            target_status = jsonObject.optString("target_status", "on");
            reflectance_percent = jsonObject.optString("reflectance_percent", "off");
            motion_indicator = jsonObject.optString("motion_indicator", "off");
            accel = jsonObject.optString("accel", "off");
            xtalk = jsonObject.optString("xtalk", "off");
            rotation = jsonObject.optString("rotation", "0");
            flip = jsonObject.optString("flip", "y");
            fontsize = jsonObject.optString("fontsize", "0");
            colormap_min = jsonObject.optString("colormap_min", "20");
            colormap_max = jsonObject.optString("colormap_max", "2000");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Set the default values for the configuration */
    public void getDefault() {
        com_port = "none";
        num_sensors = "1";
        size = "8x8";
        num_targets = "4";
        order = "closest";
        sharpener = "5";
        ambient_per_spad = "off";
        nb_spads_enabled = "off";
        nb_target_detected = "on";
        signal_per_spad = "off";
        range_sigma = "off";
        distance = "on";
        target_status = "on";
        reflectance_percent = "off";
        motion_indicator = "off";
        accel = "off";
        xtalk = "off";

        logger.info("Default ConfigData: " + Arrays.toString(metadata));
    }


    /** Creates the configuration object from the recording metadata
     * @param recording recording to get the configuration from
     * @return configuration data */
    public static ConfigData fromRec(TofRecording recording) {
        Map<String, Object> metadata = recording.getMetadata();

        if (metadata != null && metadata.containsKey("config")) {
            Map<String, String> configData = (Map<String, String>) metadata.get("config");

            if (configData != null) {
                ConfigData config = new ConfigData();
                com_port = configData.getOrDefault("com_port", "none");
                num_sensors = configData.getOrDefault("num_sensors", "1");
                size = configData.getOrDefault("size", "8x8");
                num_targets = configData.getOrDefault("num_targets", "4");
                order = configData.getOrDefault("order", "closest");
                sharpener = configData.getOrDefault("sharpener", "5");
                ambient_per_spad = configData.getOrDefault("ambient_per_spad", "off");
                nb_spads_enabled = configData.getOrDefault("nb_spads_enabled", "off");
                nb_target_detected = configData.getOrDefault("nb_target_detected", "on");
                signal_per_spad = configData.getOrDefault("signal_per_spad", "off");
                range_sigma = configData.getOrDefault("range_sigma", "off");
                distance = configData.getOrDefault("distance", "on");
                target_status = configData.getOrDefault("target_status", "on");
                reflectance_percent = configData.getOrDefault("reflectance_percent", "off");
                motion_indicator = configData.getOrDefault("motion_indicator", "off");
                accel = configData.getOrDefault("accel", "off");
                xtalk = configData.getOrDefault("xtalk", "off");

                return config;
            }
        }
        return new ConfigData();
    }

    /** Translate the configuration data to the initialization message as a byte array */
    private static final Map<String, Byte> translate = new HashMap<>();
    static {
        translate.put("4x4", (byte) 0xFE);
        translate.put("8x8", (byte) 0x01);
        translate.put("off", (byte) 0x0F);          // we want it off -> set disabled to active -> 0F
        translate.put("on", (byte) 0xF0);
        translate.put("closest", (byte) 0x0F);
        translate.put("strongest", (byte) 0xF0);
    }

    /** Get the initialization message as a byte array:
     * @return initialization message according the UART protocol v2 specfication */
    public byte[] getInitMsg() {
        String[] protocolItems = getProtocolTxItems();

        byte[] initMsg = new byte[SYNC_MESSAGE.length + protocolItems.length];
        System.arraycopy(SYNC_MESSAGE, 0, initMsg, 0, SYNC_MESSAGE.length); // copy SYNC_MESSAGE to initMsg

        Map<String, String> meta = getConfig();

        int i = 4;
        for (String item : protocolItems) {
            String value = meta.get(item);

            if (translate.containsKey(value)) {
                initMsg[i] = translate.get(value);
            } else {
                try {
                    if (Objects.equals(value, ""))
                        initMsg[i] = 0;
                    else
                        initMsg[i] = Byte.parseByte(value);
                } catch (NumberFormatException e) {
                    logger.warning("ERROR - getInitMsg: " + e);
                }
            }
            i++;
        }

        return initMsg;
    }

    /** Get the protocol items - list of class variables that are part of the UART communication
     * @return protocol items */
    private static String[] getProtocolTxItems() {
        return new String[]{
                "size",
                "num_sensors",
                "num_targets",
                "order",
                "sharpener",
                "ambient_per_spad",
                "nb_spads_enabled",
                "nb_target_detected",
                "signal_per_spad",
                "range_sigma",
                "distance",
                "target_status",
                "reflectance_percent",
                "motion_indicator",
                "accel",
                "xtalk"
        };
    }

    /** Get the current frame resolution
     * @return frame resolution: (4,4) or (4,8) or (8,8) or (8,16) */
    public int[] getFrameResolution() {
        String[] splitted = size.split("x");
        int width = Integer.parseInt(splitted[0]);
        int height = Integer.parseInt(splitted[1]) * Integer.parseInt(num_sensors);

        return new int[]{width, height};
    }


    /** Get number of sensors
     * @return number of sensors */
    public int getNumTargets() {
        return Integer.parseInt(num_targets);
    }

    /** Get the acceleration
     * @return acceleration */
    public String getAccel() {
        return accel;
    }

    /** Get the metadata array
     * @return metadata array */
    public static String[] getMetadata() {
        return metadata;
    }

}
