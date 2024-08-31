package com.rocnikovyprojekt.tof;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Class for handling the ToF sensor frame data */
public class FrameData {
    private double[][][] ambient_per_spad;
    private int[][][] nb_spads_enabled;
    private int[][][] nb_target_detected;
    private double[][][] signal_per_spad;
    private double[][][] range_sigma;
    private int[][][] distance;
    private int[][][] target_status;
    private double[][][] reflectance_percent;
    private double[][][] motion_indicator;
    private double[][][] accel;

    /** Constructor */
    public FrameData() {
        this.ambient_per_spad = null;
        this.nb_spads_enabled = null;
        this.nb_target_detected = null;
        this.signal_per_spad = null;
        this.range_sigma = null;
        this.distance = null;
        this.target_status = null;
        this.reflectance_percent = null;
        this.motion_indicator = null;
        this.accel = null;
    }

    /** Method for serializing the data to Map
     * @return Map<String, Object>  serialized data */
    public Map<String, Object> serialize() {
        Map<String, Object> serMap = new HashMap<>();
        serMap.put("ambient_per_spad", cropData(ambient_per_spad));
        serMap.put("nb_spads_enabled", cropData(nb_spads_enabled));
        serMap.put("nb_target_detected", cropData(nb_target_detected));
        serMap.put("signal_per_spad", cropData(signal_per_spad));
        serMap.put("range_sigma", cropData(range_sigma));
        serMap.put("distance", cropData(distance));
        serMap.put("target_status", cropData(target_status));
        serMap.put("reflectance_percent", cropData(reflectance_percent));
        serMap.put("motion_indicator", cropData(motion_indicator));
        serMap.put("accel", accel);
        return serMap;
    }

    /** Method for deserializing the data from Map
     * @param loadedMap Map with the data
     * @return FrameData deserialized data */
    public static FrameData deserialize(Map<?, ?> loadedMap) {
        FrameData frameData = new FrameData();
        frameData.ambient_per_spad = (double[][][]) loadedMap.get("ambient_per_spad");
        frameData.nb_spads_enabled = (int[][][]) loadedMap.get("nb_spads_enabled");
        frameData.nb_target_detected = (int[][][]) loadedMap.get("nb_target_detected");
        frameData.signal_per_spad = (double[][][]) loadedMap.get("signal_per_spad");
        frameData.range_sigma = (double[][][]) loadedMap.get("range_sigma");
        frameData.distance = (int[][][]) loadedMap.get("distance");
        frameData.target_status = (int[][][]) loadedMap.get("target_status");
        frameData.reflectance_percent = (double[][][]) loadedMap.get("reflectance_percent");
        frameData.motion_indicator = (double[][][]) loadedMap.get("motion_indicator");
        frameData.accel = (double[][][]) loadedMap.get("accel");

        // TODO: implement logic for converting data to the correct format if it is null or of the wrong type
        // TODO: use the num_targets parameter to determine the size of the data

        return frameData;
    }

    /** Method for cropping the data - Helper method
     * @param data data to be cropped
     * @param <T> type of the data
     * @return cropped data */
    private <T> T cropData(T data) {
        return data;
    }

    /** Getter for the distance
     * @return distance */
    public int[][][] getDistance() {
        return distance;
    }

    /** Getter for the target status
     * @return target status */
    public int[][][] getTargetStatus() {
        return target_status;
    }


    /** Method for converting the data to string (overridden method)
     * @return String representation of the data */
    @Override
    public String toString() {
        return "FrameData{" +
                "distance=" + Arrays.deepToString(distance) +
                ", target_status=" + Arrays.deepToString(target_status) +
                '}';
    }


}
