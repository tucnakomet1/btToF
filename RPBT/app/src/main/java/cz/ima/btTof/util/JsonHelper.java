package cz.ima.btTof.util;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Helper class to read and write JSON data to a file
 */
public class JsonHelper {
    private final Context context;

    /**
     * Constructor
     *
     * @param context Context
     */
    public JsonHelper(Context context) {
        this.context = context;
    }

    /**
     * Get the JSON file
     *
     * @return JSON file
     */
    private File getJsonFile() {
        File jsonDir = new File(context.getExternalFilesDir(null), "jsonFiles");
        if (!jsonDir.exists()) {
            jsonDir.mkdirs();
        }
        return new File(jsonDir, "dateConfig.json");
    }

    /**
     * Add a new entry to the JSON file
     *
     * @param fileName File name
     * @param date     Date
     */
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

    /**
     * Remove an entry from the JSON file
     *
     * @param fileName File name
     */
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

    /** Get the value from the JSON file
     *
     * @param fileName File name
     * @return Value
     */
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
