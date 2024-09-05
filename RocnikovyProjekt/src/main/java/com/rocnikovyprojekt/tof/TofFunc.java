package com.rocnikovyprojekt.tof;

import com.fazecast.jSerialComm.SerialPort;
import com.rocnikovyprojekt.utils.ConfigData;
import com.rocnikovyprojekt.utils.Event;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Logger;

/** Class for handling the ToF sensor */
public class TofFunc {
    private final Logger logger = Logger.getLogger(TofFunc.class.getName());

    private final SerialPort port;
    private final Event e_recording;
    private ConfigData configData;
    private String[] config;
    private TofRecording tof_recording;


    /** Constructor
     * @param portDescriptor COM port descriptor */
    public TofFunc(String portDescriptor) {
        port = SerialPort.getCommPort(portDescriptor);
        port.setBaudRate(1000000);
        port.setNumDataBits(8);
        port.setNumStopBits(SerialPort.ONE_STOP_BIT);
        port.setParity(SerialPort.NO_PARITY);

        configData = new ConfigData();
        configData = getConfigData();

        e_recording = new Event();

        logger.info("Config: " + Arrays.toString(config));
    }

    /** Get the configuration data:
     *   fills the ConfigData object self.config from "config.json",
     *   if file is missing, uses get_default() method of ConfigData to fill it
     * @return ConfigData object
     */
    private ConfigData getConfigData() {
        try {
            String CONFIG_FILENAME = "config.json";

            configData.fromJson(CONFIG_FILENAME);
            configData = configData.update();
            config = configData.getMetadata();
        } catch (Exception e) {
            logger.warning("ERROR: getConfigData - " + e);

            configData.getDefault();
            configData = configData.update();
            config = configData.getMetadata();
        }
        return configData;
    }

    /** Start or Stop the ToF sensor stream */
    public void record_stream() {
        if (e_recording.isSet())
            recording_end();
        else
            recording_start(true, null);
    }

    /** Start the ToF sensor stream - open the COM port and start the thread */
    public void start_stream(PrintWriter writer) {
        try {
            if (port.openPort()) {
                byte[] initMsg = configData.getInitMsg();

                logger.info("initMsg: " + Arrays.toString(initMsg));

                // send the initialization message to the sensor
                port.writeBytes(initMsg, initMsg.length);
                recording_start(false, writer);
            } else {
                logger.warning("Port not opened");
            }

            // TODO: start the thread
            Threads th = new Threads(port, configData, e_recording, tof_recording, writer);
            System.out.println("here");
            th.run(false);

            logger.info("stream started");
        } catch (Exception e) {
            logger.warning("No configuration exists, please use Save before starting the stream.");

            //init_stream();

            e.printStackTrace();
        }
    }

    /** Start the recording */
    public void recording_start(boolean recording, PrintWriter writer) {
        if (port.openPort()) {
            byte[] initMsg = configData.getInitMsg();

            logger.info("initMsg: " + Arrays.toString(initMsg));

            // send the initialization message to the sensor
            port.writeBytes(initMsg, initMsg.length);
        } else {
            logger.warning("Port not opened");
        }

        tof_recording = new TofRecording();
        tof_recording.set_timestamp_now();

        // set the event
        e_recording.set();
        System.out.println("\nrecording...");

        if (recording) {
            Threads th = new Threads(port, configData, e_recording, tof_recording, writer);
            TofFrame frame = th.readFrame();
            //logger.info("frame1 " + frame);
            recording(frame);

            recording_end();
            //System.out.println("Ended");
        }
    }


    /** Record the frame
     * gets the frame, saves it to the buffer
     * @param frame frame to be recorded */
    public void recording(TofFrame frame) {
        if (!tof_recording.add(frame)) {
            logger.info("ERROR\nrecording\nrecording buffer is full => saving");
            recording_end();
        }
    }

    /** Stop the recording
     *  if recording is on -> stop it and save the result */
    public void recording_end() {
        e_recording.clear();
        logger.info("Recording stopped!");
        recording_save();
    }


    /** Save the recording from the buffer to the file */
    public void recording_save() {
        // TODO: get these info from the user
        String description = "description";
        String environment = "environment";
        String sensor_type = "sensor type";
        String number_of_sensors = "1";
        String position_of_sensors = "position of sensors";

        Map<String, String> ssMap = new java.util.HashMap<>();
        ssMap.put("description", description);
        ssMap.put("environment", environment);
        ssMap.put("sensor_type", sensor_type);
        ssMap.put("number_of_sensors", number_of_sensors);
        ssMap.put("position_of_sensors", position_of_sensors);
        Map<String, Object> metadata_new = new java.util.HashMap<>(ssMap);

        metadata_new.put("config", ConfigData.getConfig());
        metadata_new.put("timestamp", System.currentTimeMillis());

        logger.info("metadata_new: " + metadata_new);

        String filename = "data" + System.currentTimeMillis() + ".json";

        tof_recording.set_metadata(metadata_new);       // nastavi metadata
        try {
            tof_recording.to_json(filename);             // ulozi zaznam do souboru
            int numFrames = tof_recording.getRecordingNumber();    // zjisti pocet snimku
            logger.info("recording saved \t" + numFrames);
        } catch (Exception e) {
            e.printStackTrace();
            logger.warning("ERROR - couldn't save recording");
        }
    }
}
