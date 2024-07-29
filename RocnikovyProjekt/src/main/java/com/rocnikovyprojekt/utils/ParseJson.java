package com.rocnikovyprojekt.utils;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Class for parsing JSON files */
public class ParseJson {
    /** Parses the JSON file and returns a list of strings
     * @param filePath path to the JSON file
     * @return list of strings */
    public static List<String> parseConfig(String filePath) {
        List<String> configList = new ArrayList<>();

        try (FileReader reader = new FileReader(filePath)) {
            JSONTokener tokener = new JSONTokener(reader);
            JSONObject jsonObject = new JSONObject(tokener);

            for (String key : jsonObject.keySet()) {
                String value = jsonObject.getString(key);
                configList.add(key + ": " + value);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return configList;
    }
}
