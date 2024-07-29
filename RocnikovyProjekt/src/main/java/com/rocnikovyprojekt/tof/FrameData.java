package com.rocnikovyprojekt.tof;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Class for handling the ToF sensor frame data */
public class FrameData {
    private double[][][] ambientPerSpad;
    private int[][][] nbSpadsEnabled;
    private int[][][] nbTargetDetected;
    private double[][][] signalPerSpad;
    private double[][][] rangeSigma;
    private double[][][] distance;
    private int[][][] targetStatus;
    private double[][][] reflectancePercent;
    private double[][][] motionIndicator;
    private double[][][] accel;

    /** Constructor */
    public FrameData() {
        this.ambientPerSpad = null;
        this.nbSpadsEnabled = null;
        this.nbTargetDetected = null;
        this.signalPerSpad = null;
        this.rangeSigma = null;
        this.distance = null;
        this.targetStatus = null;
        this.reflectancePercent = null;
        this.motionIndicator = null;
        this.accel = null;
    }

    /** Method for serializing the data to Map
     * @return Map<String, Object>  serialized data */
    public Map<String, Object> serialize() {
        Map<String, Object> serMap = new HashMap<>();
        serMap.put("ambientPerSpad", cropData(ambientPerSpad));
        serMap.put("nbSpadsEnabled", cropData(nbSpadsEnabled));
        serMap.put("nbTargetDetected", cropData(nbTargetDetected));
        serMap.put("signalPerSpad", cropData(signalPerSpad));
        serMap.put("rangeSigma", cropData(rangeSigma));
        serMap.put("distance", cropData(distance));
        serMap.put("targetStatus", cropData(targetStatus));
        serMap.put("reflectancePercent", cropData(reflectancePercent));
        serMap.put("motionIndicator", cropData(motionIndicator));
        serMap.put("accel", accel);
        return serMap;
    }

    /** Method for deserializing the data from Map
     * @param loadedMap Map with the data
     * @param num_targets number of targets
     * @return FrameData deserialized data */
    public static FrameData deserialize(Map<?, ?> loadedMap, int num_targets) {
        FrameData frameData = new FrameData();
        frameData.ambientPerSpad = (double[][][]) loadedMap.get("ambientPerSpad");
        frameData.nbSpadsEnabled = (int[][][]) loadedMap.get("nbSpadsEnabled");
        frameData.nbTargetDetected = (int[][][]) loadedMap.get("nbTargetDetected");
        frameData.signalPerSpad = (double[][][]) loadedMap.get("signalPerSpad");
        frameData.rangeSigma = (double[][][]) loadedMap.get("rangeSigma");
        frameData.distance = (double[][][]) loadedMap.get("distance");
        frameData.targetStatus = (int[][][]) loadedMap.get("targetStatus");
        frameData.reflectancePercent = (double[][][]) loadedMap.get("reflectancePercent");
        frameData.motionIndicator = (double[][][]) loadedMap.get("motionIndicator");
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
    public double[][][] getDistance() {
        return distance;
    }

    /** Getter for the target status
     * @return target status */
    public int[][][] getTargetStatus() {
        return targetStatus;
    }


    /** Method for converting the data to string (overridden method)
     * @return String representation of the data */
    @Override
    public String toString() {
        return "FrameData{" +
                "distance=" + Arrays.deepToString(distance) +
                ", targetStatus=" + Arrays.deepToString(targetStatus) +
                '}';
    }


}
