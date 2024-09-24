package cz.ima.btTof.tof;

import org.json.JSONArray;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Class for handling the ToF sensor frame data */
public class FrameData {
    private double[][][] ambient_per_spad;
    private int[][][] nb_spads_enabled;
    private int[][][] nb_target_detected;
    private double[][][] signal_per_spad;
    private int[][][] range_sigma;
    private int[][][] distance;
    private int[][][] target_status;
    private double[][][] reflectance_percent;
    private double[][][] motion_indicator;
    private int[][][] accel;

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
        serMap.put("ambient_per_spad", convertToJSONArray(cropData(ambient_per_spad)));
        serMap.put("nb_spads_enabled", convertToJSONArray(cropData(nb_spads_enabled)));
        serMap.put("nb_target_detected", convertToJSONArray(cropData(nb_target_detected)));
        serMap.put("signal_per_spad", convertToJSONArray(cropData(signal_per_spad)));
        serMap.put("range_sigma", convertToJSONArray(cropData(range_sigma)));
        serMap.put("distance", convertToJSONArray(cropData(distance)));
        serMap.put("target_status", convertToJSONArray(cropData(target_status)));
        serMap.put("reflectance_percent", convertToJSONArray(cropData(reflectance_percent)));
        serMap.put("motion_indicator", convertToJSONArray(cropData(motion_indicator)));
        serMap.put("accel", convertToJSONArray(accel));
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
        frameData.range_sigma = (int[][][]) loadedMap.get("range_sigma");
        frameData.distance = (int[][][]) loadedMap.get("distance");
        frameData.target_status = (int[][][]) loadedMap.get("target_status");
        frameData.reflectance_percent = (double[][][]) loadedMap.get("reflectance_percent");
        frameData.motion_indicator = (double[][][]) loadedMap.get("motion_indicator");
        frameData.accel = (int[][][]) loadedMap.get("accel");

        return frameData;
    }

    /**
     * Method for cropping the data - Helper method
     *
     * @param data data to be cropped
     * @param <T>  type of the data
     * @return cropped data
     */
    private <T> int[][][] cropData(T data) {
        if (data == null)
            return null;

        return (int[][][]) data;
    }

    /**
     * Method for converting the data to JSONArray
     *
     * @param array data to be converted
     * @return JSONArray converted data
     */
    public static JSONArray convertToJSONArray(int[][][] array) {
        if (array == null)
            return null;

        JSONArray jsonArray = new JSONArray();
        for (int[][] subArray : array) {
            JSONArray subJsonArray = new JSONArray();
            for (int[] innerArray : subArray) {
                JSONArray innerJsonArray = new JSONArray();
                for (int value : innerArray) {
                    innerJsonArray.put(value);
                }
                subJsonArray.put(innerJsonArray);
            }
            jsonArray.put(subJsonArray);
        }
        return jsonArray;
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


    public boolean isEmpty() {
        // check if all the data of FrameData is 0
        for (int[][] subArray : distance) {
            for (int[] innerArray : subArray) {
                for (int value : innerArray) {
                    if (value != 0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
