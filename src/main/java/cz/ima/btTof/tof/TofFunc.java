package cz.ima.btTof.tof;

import com.fazecast.jSerialComm.SerialPort;
import cz.ima.btTof.utils.ConfigData;
import cz.ima.btTof.utils.Event;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.logging.Logger;

/** Class for handling the ToF sensor */
public class TofFunc {
    private final Logger logger = Logger.getLogger(TofFunc.class.getName());

    private final SerialPort port;
    private final Event e_recording;
    private ConfigData configData;
    private String[] config;
    private Threads th;


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

    /** Start the ToF sensor stream - open the COM port and start the thread */
    public void start_stream(PrintWriter writer) {
        try {
            checkOpenPortAndSendInitBytes();

            TofRecording tof_recording = new TofRecording();
            tof_recording.set_timestamp_now();
            e_recording.set();

            th = new Threads(port, configData, e_recording, tof_recording, writer);
            th.startThread();
            th.run();

            logger.info("stream started");
        } catch (Exception e) {
            logger.warning("No configuration exists, please use Save before starting the stream.");

            //init_stream();

            e.printStackTrace();
        }
    }

    /** Initialize the stream */
    private void checkOpenPortAndSendInitBytes() {
        if (port.openPort()) {
            configData = getConfigData();
            byte[] initMsg = configData.getInitMsg();

            logger.info("initMsg: " + Arrays.toString(initMsg));

            // send the initialization message to the sensor
            port.writeBytes(initMsg, initMsg.length);
        } else {
            logger.warning("Port not opened");
        }
    }


    /** Start the recording */
    public void recording_start() {
        checkOpenPortAndSendInitBytes();

        System.out.println("\nrecording...");
        th.startRecording();
    }


    /** Stop the recording
     *  if recording is on -> stop it and save the result */
    public String recording_end() {
        th.stopRecording();
        return th.recording_end();
    }

    /** Stop the stream */
    public void stop_stream() {
        th.stopThread();
        port.closePort();
    }
}
