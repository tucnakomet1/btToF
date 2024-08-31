package com.rocnikovyprojekt.tof;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Class for handling the ToF sensor frame */
public class TofFrame {
    private int[] size;
    private Object sensorID; // int or List<Integer>
    private FrameData data;

    /** Constructor
     * @param data data of the frame
     * @param size size of the frame
     * @param ID sensor ID */
    public TofFrame(Object data, int[] size, Object ID) {
        this.size = size;
        this.sensorID = ID;

        // if data is FrameData, FrameDataInput, or Map<String, Object>
        if (data instanceof FrameData)
            this.data = (FrameData) data;
        else if (data instanceof FrameDataInput)
            this.data = ((FrameDataInput) data).toFrameData();
        else if (data instanceof Map) {
            this.data = FrameData.deserialize((Map<?, ?>) data);
        } else
            throw new IllegalArgumentException("Data must be of type FrameData, FrameDataInput, or Map<String, Object>");
    }


    /** Getter for size
     * @return size of the frame */
    public int[] getSize() {
        return size;
    }

    /** Setter for size
     * @param size size of the frame */
    public void setSize(int[] size) {
        this.size = size;
    }

    /** Getter for sensor ID
     * @return sensor ID */
    public Object getSensorID() {
        return sensorID;
    }

    /** Setter for sensor ID
     * @param sensorID sensor ID */
    public void setSensorID(Object sensorID) {
        this.sensorID = sensorID;
    }

    /** Getter for data
     * @return data of the frame */
    public FrameData getData() {
        return data;
    }

    /** Setter for data
     * @param data data of the frame */
    public void setData(FrameData data) {
        this.data = data;
    }


    /** Method for converting the frame to string (override of the toString method)
     * @return string representation of the frame */
    @Override
    public String toString() {
        return Arrays.deepToString(data.getDistance()) + Arrays.deepToString(data.getTargetStatus());
    }


    /** Method for serializing the frame to a map
     * @return serialized frame */
    public Map<String, Object> serialize() {
        return data.serialize();
    }

    /** Method for deserializing the frame from a map
     * @param input map to deserialize
     * @return deserialized frame */
    public static TofFrame deserialize(String input) {
        // Convert from JSON string to map using JSONObject
        JSONObject jsonObject = new JSONObject(input);
        Map<String, Object> map = new HashMap<>();
        for (String key : jsonObject.keySet()) {
            map.put(key, jsonObject.get(key));
        }

        FrameData data = FrameData.deserialize(map);
        int[] size = {data.getDistance().length, data.getDistance()[0].length};
        return new TofFrame(data, size, null);
    }

    /** Method for getting the distance of the first target in each zone
     * @return distance of the first target in each zone */
    public double[][] getDistance() {
        int[][][] distances = data.getDistance();
        double[][] firstTargets = new double[distances.length][distances[0].length];
        for (int i = 0; i < distances.length; i++) {
            for (int j = 0; j < distances[i].length; j++) {
                firstTargets[i][j] = distances[i][j][0];
            }
        }
        return firstTargets;
    }

    /** Method for getting all targets
     * @return all targets */
    public int[][][] getAllTargets() {
        return data.getDistance();
    }
}
