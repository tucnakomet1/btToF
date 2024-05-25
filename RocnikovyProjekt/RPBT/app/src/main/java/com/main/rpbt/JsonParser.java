package com.main.rpbt;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;
import android.content.res.AssetManager;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class JsonParser extends AppCompatActivity {
    private static int maxNum = 0;

    public static void print(String str) {
        Log.d(TAG, str);
    }

    public static int getMaxNum() {
        return maxNum;
    }

    public static List<double[][]> loadDataFromJson(Context context) {
        List<double[][]> frames = new ArrayList<>();
        try {
            InputStream inputStream = context.getAssets().open("play.json");
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
                JSONArray distanceArray = recordingArray.getJSONObject(i).getJSONArray("distance");
                double[][] frame = new double[8][8];
                for (int x = 0; x < 8; x++) {
                    JSONArray rowArray = distanceArray.getJSONArray(x);
                    for (int y = 0; y < 8; y++) {
                        JSONArray cellArray = rowArray.getJSONArray(y);

                        if (cellArray.length() > 0) {
                            int sum = 0;
                            for (int z = 0; z < cellArray.length(); z++) {
                                sum += cellArray.getInt(z);
                            }
                            if (sum > maxNum) maxNum = sum;
                            frame[y][x] = sum;       // Pokud pole neni prazdne, vezmeme prvni hodnotu jako cislo
                        } else {
                            frame[y][x] = 0.0;                                  // Pokud je pole prazdne, priradime hodnotu nula
                        }
                    }
                }
                frames.add(frame);
            }

        } catch (IOException | org.json.JSONException e) {
            e.printStackTrace();
        }
        return frames;
    }
}
