package com.rocnikovyprojekt.utils;

import com.rocnikovyprojekt.tof.TofRecording;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/** Class for handling the configuration data */
public class ConfigData {
    private static final byte[] SYNC_MESSAGE = {(byte) 0xA5, (byte) 0x68, (byte) 0x47, (byte) 0x0F};

    private static String[] metadata;

    private static String comPort = "", numSensors = "", size = "", numTargets = "", order = "", sharpener = "";
    private static String ambientPerSpad = "", nbSpadsEnabled = "", nbTargetDetected = "", signalPerSpad = "", rangeSigma = "", distance = "", targetStatus = "", reflectancePercent = "", motionIndicator = "", accel = "", xtalk = "";
    private static String rotation = "", flip = "", fontsize = "", colormapMin = "", colormapMax = "";


    /** Constructor - fills the metadata array */
    public ConfigData() {
        metadata = new String[]{
                comPort, numSensors, size, numTargets, order, sharpener,
                ambientPerSpad, nbSpadsEnabled, nbTargetDetected, signalPerSpad, rangeSigma, distance, targetStatus, reflectancePercent, motionIndicator, accel, xtalk,
                rotation, flip, fontsize, colormapMin, colormapMax
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
        configData.put("comPort", comPort);
        configData.put("numSensors", numSensors);
        configData.put("size", size);
        configData.put("numTargets", numTargets);
        configData.put("order", order);
        configData.put("sharpener", sharpener);
        configData.put("ambientPerSpad", ambientPerSpad);
        configData.put("nbSpadsEnabled", nbSpadsEnabled);
        configData.put("nbTargetDetected", nbTargetDetected);
        configData.put("signalPerSpad", signalPerSpad);
        configData.put("rangeSigma", rangeSigma);
        configData.put("distance", distance);
        configData.put("targetStatus", targetStatus);
        configData.put("reflectancePercent", reflectancePercent);
        configData.put("motionIndicator", motionIndicator);
        configData.put("accel", accel);
        configData.put("xtalk", xtalk);
        configData.put("rotation", rotation);
        configData.put("flip", flip);
        configData.put("fontsize", fontsize);
        configData.put("colormapMin", colormapMin);
        configData.put("colormapMax", colormapMax);

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

            System.out.println("ConfigData: " + jsonObject);

            // Use the keys to set values for ConfigData fields
            comPort = jsonObject.optString("comPort", "");
            numSensors = jsonObject.optString("numSensors", "1");
            size = jsonObject.optString("size", "8x8");
            numTargets = jsonObject.optString("numTargets", "4");
            order = jsonObject.optString("order", "closest");
            sharpener = jsonObject.optString("sharpener", "5");
            ambientPerSpad = jsonObject.optString("ambientPerSpad", "off");
            nbSpadsEnabled = jsonObject.optString("nbSpadsEnabled", "off");
            nbTargetDetected = jsonObject.optString("nbTargetDetected", "on");
            signalPerSpad = jsonObject.optString("signalPerSpad", "off");
            rangeSigma = jsonObject.optString("rangeSigma", "off");
            distance = jsonObject.optString("distance", "on");
            targetStatus = jsonObject.optString("targetStatus", "on");
            reflectancePercent = jsonObject.optString("reflectancePercent", "off");
            motionIndicator = jsonObject.optString("motionIndicator", "off");
            accel = jsonObject.optString("accel", "off");
            xtalk = jsonObject.optString("xtalk", "off");
            rotation = jsonObject.optString("rotation", "0");
            flip = jsonObject.optString("flip", "y");
            fontsize = jsonObject.optString("fontsize", "0");
            colormapMin = jsonObject.optString("colormapMin", "20");
            colormapMax = jsonObject.optString("colormapMax", "2000");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Set the default values for the configuration */
    public void getDefault() {
        comPort = "";
        numSensors = "1";
        size = "8x8";
        numTargets = "4";
        order = "closest";
        sharpener = "5";
        ambientPerSpad = "off";
        nbSpadsEnabled = "off";
        nbTargetDetected = "on";
        signalPerSpad = "off";
        rangeSigma = "off";
        distance = "on";
        targetStatus = "on";
        reflectancePercent = "off";
        motionIndicator = "off";
        accel = "off";
        xtalk = "off";

        System.out.println("Default ConfigData: " + Arrays.toString(metadata));
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
                comPort = configData.getOrDefault("comPort", "");
                numSensors = configData.getOrDefault("numSensors", "1");
                size = configData.getOrDefault("size", "8x8");
                numTargets = configData.getOrDefault("numTargets", "4");
                order = configData.getOrDefault("order", "closest");
                sharpener = configData.getOrDefault("sharpener", "5");
                ambientPerSpad = configData.getOrDefault("ambientPerSpad", "off");
                nbSpadsEnabled = configData.getOrDefault("nbSpadsEnabled", "off");
                nbTargetDetected = configData.getOrDefault("nbTargetDetected", "on");
                signalPerSpad = configData.getOrDefault("signalPerSpad", "off");
                rangeSigma = configData.getOrDefault("rangeSigma", "off");
                distance = configData.getOrDefault("distance", "on");
                targetStatus = configData.getOrDefault("targetStatus", "on");
                reflectancePercent = configData.getOrDefault("reflectancePercent", "off");
                motionIndicator = configData.getOrDefault("motionIndicator", "off");
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

        int index = SYNC_MESSAGE.length;

        for (int i = 0; i < protocolItems.length; i++) {
            String value = ConfigData.metadata[i];

            if (translate.containsKey(value)) {
                initMsg[index++] = translate.get(value);
            } else {
                try {
                    if (Objects.equals(value, "")) {
                        initMsg[index++] = 0;
                    } else {
                        initMsg[index++] = Byte.parseByte(value);
                    }
                } catch (NumberFormatException e) {
                    System.out.println("ERROR - getInitMsg: " + e);
                }
            }
        }

        return initMsg;
    }

    /** Get the protocol items - list of class variables that are part of the UART communication
     * @return protocol items */
    private static String[] getProtocolTxItems() {
        return new String[]{
                "size",
                "numSensors",
                "numTargets",
                "order",
                "sharpener",
                "ambientPerSpad",
                "nbSpadsEnabled",
                "nbTargetDetected",
                "signalPerSpad",
                "rangeSigma",
                "distance",
                "targetStatus",
                "reflectancePercent",
                "motionIndicator",
                "accel",
                "xtalk"
        };
    }

    /** Get the current frame resolution
     * @return frame resolution: (4,4) or (4,8) or (8,8) or (8,16) */
    public int[] getFrameResolution() {
        String[] splitted = size.split("x");
        int width = Integer.parseInt(splitted[0]);
        int height = Integer.parseInt(splitted[1]) * Integer.parseInt(numSensors);

        return new int[]{width, height};
    }


    /** Get number of sensors
     * @return number of sensors */
    public int getNumTargets() {
        return Integer.parseInt(numTargets);
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
