package com.rocnikovyprojekt.tof;

import com.fazecast.jSerialComm.SerialPort;
import com.rocnikovyprojekt.utils.ConfigData;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;


/** Class for handling the threads of the ToF sensor */
public class Threads {
    private final Logger logger = Logger.getLogger(Threads.class.getName());

    private static final byte[] syncPacket4 = {(byte) 0xA5, (byte) 0x68, (byte) 0xB8, (byte) 0xFE};
    private static final byte[] syncPacket8 = {(byte) 0xA5, (byte) 0x68, (byte) 0xB8, (byte) 0x01};
    private static final byte[] syncPacket2x4 = {(byte) 0xA5, (byte) 0x68, (byte) 0xC3, (byte) 0xFE};
    private static final byte[] syncPacket2x8 = {(byte) 0xA5, (byte) 0x68, (byte) 0xC3, (byte) 0x01};


    private static SerialPort port;
    private static ConfigData config;
    private static Map<String, Object[]> parser;


    /** Constructor
     * @param port COM port
     * @param config configuration data */
    public Threads(SerialPort port, ConfigData config) {
        Threads.port = port;
        Threads.config = config;

        parser = get_parser_dict(config);       // Map<String, Object[]>
        printParser(parser);
    }

    /** Run the thread
     * TODO: make run as a thread method*/
    public void run() {
        // stops after 5 seconds
        long end = System.currentTimeMillis() + 3000;

        while (end - System.currentTimeMillis() > 0) {
            System.out.println(end - System.currentTimeMillis());
            TofFrame frame = readFrame();
            logger.info("frame" + frame);
        }
        logger.info("\t\tStop thread!\t\t");
    }

    /** Read a frame from the sensor - get the data
     * @return TofFrame */
    public TofFrame readFrame() {
        int[] res;
        LinkedList<Byte> syncFifo = new LinkedList<>();
        byte[] buffer = new byte[1];

        // Synchronize - wait for the sync packet
        while (true) {
            port.readBytes(buffer, 1);
            byte readByte = buffer[0];

            if (syncFifo.size() >= 4)
                syncFifo.removeFirst(); // Remove oldest byte

            syncFifo.addLast(readByte);
            if (matches(syncFifo, syncPacket4)) {
                res = new int[]{4, 4};
                break;
            } else if (matches(syncFifo, syncPacket8)) {
                res = new int[]{8, 8};
                break;
            } else if (matches(syncFifo, syncPacket2x4)) {
                res = new int[]{4, 8};
                break;
            } else if (matches(syncFifo, syncPacket2x8)) {
                res = new int[]{8, 16};
                break;
            }
        }

        System.out.println("Synced! The resolution is: " + res[0] + "x" + res[1]);

        // Read the frame data
        Map<String, Object> data = new HashMap<>();

        if (res[0] - res[1] == 0) {         // if res is [4,4] or [8,8]
            data = grabFrame(parser);

            if ("on".equals(config.getAccel()))
                data.put("accel", grabAccel());

            int[][][] sensorIDList = (int[][][]) data.remove("sensor_ID");
            return new TofFrame(data, res, sensorIDList[0][0][0]);

        } else {
            Map<String, Object> data1 = grabFrame(parser);
            Map<String, Object> data2 = grabFrame(parser);

            if ("on".equals(config.getAccel()))
                data.put("accel", grabAccel());

            int[][][][] sensorIDs = { (int[][][]) data1.remove("sensor_ID"), (int[][][]) data2.remove("sensor_ID")};

            for (Map.Entry<String, Object> entry : data1.entrySet()) {
                data.put(entry.getKey(), concatenateData(entry.getValue(), data2.get(entry.getKey())));
            }
            return new TofFrame(data, res, sensorIDs);
        }
    }

    /** Check if the sync FIFO matches the sync packet - Helper method
     * @param syncFifo FIFO
     * @param syncPacket sync packet
     * @return boolean */
    private boolean matches(LinkedList<Byte> syncFifo, byte[] syncPacket) {
        if (syncFifo.size() < syncPacket.length)
            return false;

        for (int i = 0; i < syncPacket.length; i++) {
            if (!syncFifo.get(syncFifo.size() - syncPacket.length + i).equals(syncPacket[i]))
                return false;
        }
        return true;
    }

    /** Concatenate two data arrays - Helper method
     * @param data1 first data array
     * @param data2 second data array
     * @return Object */
    private Object concatenateData(Object data1, Object data2) {
        if (data1 instanceof int[] arr1 && data2 instanceof int[] arr2) {
            int[] result = new int[arr1.length + arr2.length];

            System.arraycopy(arr1, 0, result, 0, arr1.length);
            System.arraycopy(arr2, 0, result, arr1.length, arr2.length);

            return result;
        }
        return null;
    }

    /** Grab the accelerometer data - Helper method
     * @return int[] */
    private int[] grabAccel() {
        int[] accelData = new int[3];

        System.out.println("Grab!");
        for (int i = 0; i < 3; i++) {
            byte[] buffer = new byte[2];
            port.readBytes(buffer, 2);  // Read 2 bytes per axis
            accelData[i] = bytesToSignedInt(buffer);
        }

        return accelData;
    }

    /** Convert two bytes in little-endian array to signed 16-bit integer - Helper method
     * @param bytes byte array
     * @return int */
    private int bytesToSignedInt(byte[] bytes) {
        return (bytes[1] << 8) | (bytes[0] & 0xFF);
    }


    /** Grab the frame data
     * @param parser parser dictionary
     * @return Map<String, Object> */
    private Map<String, Object> grabFrame(Map<String, Object[]> parser) {
        Map<String, Object> data = new HashMap<>();

        // Iterate over the parser dictionary
        for (Map.Entry<String, Object[]> entry : parser.entrySet()) {
            String key = entry.getKey();
            Object[] value = entry.getValue();

            // if the type of data was set to send from sensor
            if ((Boolean) value[0]) {
                int byteLength = 1;

                for (int subItem : (int[]) value[1])
                    byteLength *= subItem;


                List<Integer> values = new ArrayList<>(byteLength);
                Integer val = (Integer) value[2];

                byte[] byteArray = new byte[byteLength * val];
                port.readBytes(byteArray, byteArray.length);

                // Convert byte array to List<Integer>
                ByteBuffer byteBuffer = ByteBuffer.wrap(byteArray);
                byteBuffer.order(ByteOrder.BIG_ENDIAN);

                for (int i = 0; i < byteLength; i++) {
                    // Extract bytes and convert to integer
                    if (val == 1)
                        values.add((int) byteBuffer.get(i));
                    else if (val == 2)
                        values.add((int) byteBuffer.getShort(i * val));
                }
                int[] intArray = values.stream().mapToInt(Integer::intValue).toArray();

                 System.out.println("Int array: " + Arrays.toString(intArray));

                // Reshape the data into a multi-dimensional array
                int[][][] reshapedArray = reshape(intArray, (int[]) value[1]);
                data.put(key, reshapedArray);
            }
        }

        return data;
    }

    /** Reshape the data array
     * @param data data array
     * @param shape shape array
     * @return Object */
    private int[][][] reshape(int[] data, int[] shape) {
        System.out.println("Reshape: " + Arrays.toString(shape));
        int rows = shape[0];
        int cols = shape[1];
        int depth = shape[2];
        int[][][] reshaped = new int[rows][cols][depth];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                System.arraycopy(data, i * cols * depth + j * depth, reshaped[i][j], 0, depth);
            }
        }
        return reshaped;
    }

    /** Get the parser dictionary
     * @param config configuration data
     * @return parser dictionary */
    private static Map<String, Object[]> get_parser_dict(ConfigData config) {
        Map<String, Object[]> items = new HashMap<>();
        int[] res = config.getFrameResolution();

        items.put("sensor_ID", new Object[]{true, new int[]{1, 1, 1}, 1});
        items.put("ambient_per_spad", new Object[]{false, new int[]{res[0], res[1], 1}, 4});
        items.put("nb_spads_enabled", new Object[]{false, new int[]{res[0], res[1], 1}, 4});
        items.put("nb_target_detected", new Object[]{false, new int[]{res[0], res[1], 1}, 1});
        items.put("signal_per_spad", new Object[]{false, new int[]{res[0], res[1], config.getNumTargets()}, 4});
        items.put("range_sigma", new Object[]{false, new int[]{res[0], res[1], config.getNumTargets()}, 2});
        items.put("distance", new Object[]{false, new int[]{res[0], res[1], config.getNumTargets()}, 2});
        items.put("target_status", new Object[]{false, new int[]{res[0], res[1], config.getNumTargets()}, 1});
        items.put("reflectance_percent", new Object[]{false, new int[]{res[0], res[1], config.getNumTargets()}, 1});
        items.put("motion_indicator", new Object[]{false, new int[]{1, 1, 1}, 140});


        // change first value to true if the key is in the config
        Map<String, String> conf = config.getConfig();
        for (Map.Entry<String, Object[]> entry : items.entrySet()) {
            String key = entry.getKey();
            Object[] value = entry.getValue();
            boolean isEnabled = (Boolean) value[0];


            if (!isEnabled && "on".equals(conf.get(key))) {
                value[0] = true;
            }
        }
        return items;
    }

    // TODO: delete - this is for debugging
    public void printParser(Map<String, Object[]>  items) {
        System.out.println("Parser: {");
        for (Map.Entry<String, Object[]> entry : items.entrySet()) {
            String key = entry.getKey();
            Object[] value = entry.getValue();

            System.out.print("  '" + key + "': [");
            for (int i = 0; i < value.length; i++) {
                if (i == 0)
                    System.out.print(value[i] + ", ");
                else if (i == 1){
                    int[] nums = (int[]) value[i];
                    System.out.print(Arrays.toString(nums) + ", ");
                } else
                    System.out.print(value[i] + "]");
            }
            System.out.println();
        }
        System.out.println("}");
    }
}
