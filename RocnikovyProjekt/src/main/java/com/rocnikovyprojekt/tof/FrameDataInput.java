package com.rocnikovyprojekt.tof;

import java.util.HashMap;
import java.util.Map;

/** Class for handling the ToF sensor frame data input */
public class FrameDataInput {
    public double[][][] ambientPerSpad;
    public int[][][] nbSpadsEnabled;
    public int[][][] nbTargetDetected;
    public double[][][] signalPerSpad;
    public double[][][] rangeSigma;
    public double[][][] distance;
    public int[][][] targetStatus;
    public double[][][] reflectancePercent;
    public double[][][] motionIndicator;
    public double[][][] accel;

    /** Constructor
     * @param data Map with the data */
    public FrameDataInput(Map<String, Object> data) {
        this.ambientPerSpad = (double[][][]) data.get("ambientPerSpad");
        this.nbSpadsEnabled = (int[][][]) data.get("nbSpadsEnabled");
        this.nbTargetDetected = (int[][][]) data.get("nbTargetDetected");
        this.signalPerSpad = (double[][][]) data.get("signalPerSpad");
        this.rangeSigma = (double[][][]) data.get("rangeSigma");
        this.distance = (double[][][]) data.get("distance");
        this.targetStatus = (int[][][]) data.get("targetStatus");
        this.reflectancePercent = (double[][][]) data.get("reflectancePercent");
        this.motionIndicator = (double[][][]) data.get("motionIndicator");
        this.accel = (double[][][]) data.get("accel");
    }

    /** Method for converting to FrameData
     * @param numTargets number of targets
     * @return FrameData converted data */
    public FrameData toFrameData(int numTargets) {
        return FrameData.deserialize(toMap(), numTargets);
    }

    /** Method for converting to Map
     * @return Map<String, Object> converted data */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("ambientPerSpad", this.ambientPerSpad);
        map.put("nbSpadsEnabled", this.nbSpadsEnabled);
        map.put("nbTargetDetected", this.nbTargetDetected);
        map.put("signalPerSpad", this.signalPerSpad);
        map.put("rangeSigma", this.rangeSigma);
        map.put("distance", this.distance);
        map.put("targetStatus", this.targetStatus);
        map.put("reflectancePercent", this.reflectancePercent);
        map.put("motionIndicator", this.motionIndicator);
        map.put("accel", this.accel);

        return map;
    }
}

