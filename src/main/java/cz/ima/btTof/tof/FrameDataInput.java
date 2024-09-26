package cz.ima.btTof.tof;

import java.util.HashMap;
import java.util.Map;

/** Class for handling the ToF sensor frame data input */
public class FrameDataInput {
    private final double[][][] ambientPerSpad;
    private final int[][][] nbSpadsEnabled;
    private final int[][][] nbTargetDetected;
    private final double[][][] signalPerSpad;
    private final double[][][] rangeSigma;
    private final double[][][] distance;
    private final int[][][] targetStatus;
    private final double[][][] reflectancePercent;
    private final double[][][] motionIndicator;
    private final double[][][] accel;

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
     * @return FrameData converted data */
    public FrameData toFrameData() {
        return FrameData.deserialize(toMap());
    }

    /** Method for converting to Map
     * @return Map&lt;String, Object&gt; converted data */
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

