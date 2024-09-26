package cz.ima.btTof.tof;

import cz.ima.btTof.utils.Util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class for handling the ToF sensor recording
 */
public class TofRecording {
    private static final String SAVE_FOLDER = "save";

    private static Map<String, Object> metadata;
    private static int rec_limit;
    private static List<TofFrame> recording;

    /** Constructor
     * sets current_frame to 0 and rec_limit to 20000 */
    public TofRecording() {
        recording = new ArrayList<>();
        metadata = metadata_template();
        rec_limit = 20000;
    }

    /** Save recording (current list of TofFrames + metadata) to file in JSON format:
     * @param filename name of the file */
    public void to_json(String filename) {
        // create save folder if it doesn't exist
        if (!Util.listDir().contains(SAVE_FOLDER))
            Util.createDir(SAVE_FOLDER);

        System.out.println(recording.size());

        String filePath = Paths.get(SAVE_FOLDER, filename).toString();
        try (FileWriter outfile = new FileWriter(filePath)) {
            List<JSONObject> toSave = new ArrayList<>();

            for (TofFrame item : recording) {
                Map<String, Object> serializedItem = item.serialize();
                JSONObject jsonObject = new JSONObject(serializedItem);

                // if the item is null, add it to the JSONObject (library skips null values)
                for (Map.Entry<String, Object> entry : serializedItem.entrySet()) {
                    if (entry.getValue() == null)
                        jsonObject.put(entry.getKey(), JSONObject.NULL);
                }

                toSave.add(jsonObject);
            }

            JSONObject out = new JSONObject();
            out.put("metadata", new JSONObject(getMetadata()));
            out.put("recording", new JSONArray(toSave));

            outfile.write(out.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Create a metadata template
     * @return metadata template - empty dict of metadata structure */
    private Map<String, Object> metadata_template() {
        Map<String, Object> out = new HashMap<>();
        out.put("timestamp", null);
        out.put("description", null);
        out.put("environment", null);
        out.put("sensor_type", null);
        out.put("number_of_sensors", null);
        out.put("position_of_sensors", null);
        out.put("config", null);
        out.put("gestures", null);
        return out;
    }

    /** Set the metadata from the map
     * @param map map with metadata */
    public void set_metadata(Map<String, Object> map) {
        set_metadata(
                (long) map.get("timestamp"),
                (String) map.get("description"),
                (String) map.get("environment"),
                (String) map.get("sensor_type"),
                Integer.parseInt((String) map.get("number_of_sensors")),
                List.of(((String) map.get("position_of_sensors")).split(",")),
                (Map<String, String>) map.get("config"),
                map.get("gestures")
        );
    }


    /** Set the metadata
     * @param timestamp timestamp
     * @param description description
     * @param environment environment
     * @param sensor_type sensor type
     * @param number_of_sensors number of sensors
     * @param position_of_sensors position of sensors
     * @param config config
     * @param gestures gestures */
    private void set_metadata(long timestamp, String description, String environment, String sensor_type, int number_of_sensors, List<?> position_of_sensors, Map<String, String> config, Object gestures) {
        if (number_of_sensors == 0) number_of_sensors = 1;

        metadata.put("timestamp", timestamp);                       // timestamp: unixtimestamp
        metadata.put("description", description);                   // description: str
        metadata.put("environment", environment);                   // environment: str
        metadata.put("sensor_type", sensor_type);                   // sensor_type: str
        metadata.put("number_of_sensors", number_of_sensors);       // number_of_sensors: int
        metadata.put("position_of_sensors", position_of_sensors);   // position_of_sensors: List[Tuple[x,y,z,heading/azimut,pitch/elevation]]
        metadata.put("config", config);                             //
        metadata.put("gestures", gestures);                         // gestures: List[gesture:int,start_frame:int,num_frames:int]
    }


    /** Set the metadata["timestamp"] to the current time */
    public void set_timestamp_now() {
        metadata.put("timestamp", System.currentTimeMillis());
    }


    /** Adds provided frame to the recording list while not exceeded rec_limit
     * @param frame frame to be added
     * @return true if the frame was added, false if the recording is full */
    public boolean add(TofFrame frame) {
        if (recording.size() < rec_limit) {
            recording.add(frame);
            return true;
        }
        return false;
    }

    /** Get the metadata
     * @return metadata */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    /** Get the number of frames in the recording
     * @return number of frames */
    public int getRecordingNumber() {
        return recording.size();
    }
}
