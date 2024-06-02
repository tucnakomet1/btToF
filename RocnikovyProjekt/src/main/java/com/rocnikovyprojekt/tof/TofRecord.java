package com.rocnikovyprojekt.tof;

public class TofRecord {

    private static Object[] metadata;

    public TofRecord() {
        metadata = new Object[]{0, "No description", "No environment", "No sensor type"};
    }

    public static Object[] get_metadata() {
        return metadata;
    }

    public static void set_metadata(float timestamp,
                                    String description,
                                    String environment,
                                    String sensor_type) {

        metadata = new Object[]{timestamp, description, environment, sensor_type};

    }

    public static void set_timestamp() {
        metadata[0] = System.currentTimeMillis();
    }

}
