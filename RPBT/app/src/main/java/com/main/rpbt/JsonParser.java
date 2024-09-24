package com.main.rpbt;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class JsonParser extends AppCompatActivity {
    private static final String[] itemList = {
            "ambient_per_spad", "nb_spads_enabled", "nb_target_detected", "signal_per_spad",
            "range_sigma", "distance", "target_status", "reflectance_percent", "motion_indicator"};

    public static Map<String, List<double[][]>> loadDataFromJson(Context context, int grid, String fileName) {
        Map<String, List<double[][]>> framesMap = new HashMap<>();

        // Initialize map with empty lists for each item in itemList
        for (String item : itemList) {
            framesMap.put(item, new ArrayList<>());
        }

        try {
            File jsonDir = new File(context.getFilesDir(), "jsonFiles");
            File file = new File(jsonDir, fileName); // fileName je název souboru, který chcete načíst
            InputStream inputStream = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                inputStream = Files.newInputStream(file.toPath());
            }

            //InputStream inputStream = context.getAssets().open(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder jsonBuilder = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
            String jsonString = jsonBuilder.toString();

            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray recordingArray = jsonObject.getJSONArray("recording");

            for (int i = 0; i < recordingArray.length(); i++) {
                JSONObject record = recordingArray.getJSONObject(i);

                for (String item : itemList) {
                    if (record.has(item) && !record.isNull(item)) {
                        JSONArray itemArray = record.getJSONArray(item);

                        double[][] frame = new double[grid][grid];

                        for (int x = 0; x < grid; x++) {
                            JSONArray rowArray = itemArray.getJSONArray(x);

                            for (int y = 0; y < grid; y++) {
                                JSONArray cellArray = rowArray.getJSONArray(y);

                                if (cellArray.length() > 0) {
                                    frame[y][x] = cellArray.getInt(0);  // Use the first value if the array is not empty
                                } else {
                                    frame[y][x] = 0.0;                        // If the field is empty, assign the value zero
                                }
                            }
                        }
                        Objects.requireNonNull(framesMap.get(item)).add(frame);
                    } else {
                        // If the item is null or does not exist, add a grid filled with zeros
                        double[][] emptyFrame = new double[grid][grid];
                        Objects.requireNonNull(framesMap.get(item)).add(emptyFrame);
                    }
                }
            }
        } catch (IOException | org.json.JSONException e) {
            e.printStackTrace();
        }
        return framesMap;
    }
}
