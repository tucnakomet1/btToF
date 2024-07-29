package com.rocnikovyprojekt.utils;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Class for writing to JSON files
 * usage: WriteJson.updateConfig("config.json", "colormap_min", "100");
 */
public class WriteJson {

    /**
     * Updates a value in a JSON file
     * @param filePath path to the JSON file
     * @param key key of the value to update
     * @param newValue new value
     */
    public static void updateConfig(String filePath, String key, String newValue) {
        JSONObject jsonObject;

        try (FileReader reader = new FileReader(filePath)) {
            JSONTokener tokener = new JSONTokener(reader);
            jsonObject = new JSONObject(tokener);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        jsonObject.put(key, newValue);

        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(jsonObject.toString(4));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
