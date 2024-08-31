package com.main.rpbt.util;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class JsonHelper {

    private final Context context;

    public JsonHelper(Context context) {
        this.context = context;
    }

    private File getJsonFile() {
        File jsonDir = new File(context.getExternalFilesDir(null), "jsonFiles");
        if (!jsonDir.exists()) {
            jsonDir.mkdirs();
        }
        return new File(jsonDir, "dateConfig.json");
    }

    public void addToJson(String fileName, String date) {
        File file = getJsonFile();
        JSONObject jsonObject = new JSONObject();
        try {
            if (file.exists()) {
                FileInputStream fis = new FileInputStream(file);
                byte[] data = new byte[(int) file.length()];
                fis.read(data);
                fis.close();
                jsonObject = new JSONObject(new String(data));
            }
            jsonObject.put(fileName, date);

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(jsonObject.toString().getBytes());
            fos.close();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    public void removeFromJson(String fileName) {
        File file = getJsonFile();
        if (!file.exists()) return;

        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            fis.close();

            JSONObject jsonObject = new JSONObject(new String(data));
            if (jsonObject.has(fileName)) {
                jsonObject.remove(fileName);
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(jsonObject.toString().getBytes());
                fos.close();
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    public String getFromJson(String fileName) {
        File file = getJsonFile();
        if (!file.exists()) return "";

        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            fis.close();

            JSONObject jsonObject = new JSONObject(new String(data));
            return jsonObject.optString(fileName, "");
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return "";
    }
}
