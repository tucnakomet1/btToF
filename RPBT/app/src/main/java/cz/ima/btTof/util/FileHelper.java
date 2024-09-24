package cz.ima.btTof.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Helper class for file operations
 */
public class FileHelper {
    /**
     * Copies a file to the internal storage of the application
     * @param uri - the URI of the file
     * @param context - the context of the application
     */
    public static void copyFileToInternalStorage(Uri uri, Context context) {
        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null)
                throw new IOException("Unable to open input stream from URI");

            // Create or access the jsonFiles directory in the internal repository
            File jsonDir = new File(context.getFilesDir(), "jsonFiles");
            if (!jsonDir.exists()) {
                if (!jsonDir.mkdir()) {
                    throw new IOException("Unable to create directory");
                }
            }

            String nameOfJsonFile = getFileName(context, uri);
            assert nameOfJsonFile != null;
            if (nameOfJsonFile.isEmpty())
                nameOfJsonFile = System.currentTimeMillis() + ".json";

            // Save the file to the created directory
            File file = new File(jsonDir, nameOfJsonFile);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                outputStream = Files.newOutputStream(file.toPath());
            }

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                assert outputStream != null;
                outputStream.write(buffer, 0, bytesRead);
            }

            JsonHelper jsonHelper = new JsonHelper(context);
            jsonHelper.addToJson(nameOfJsonFile, getTime());

            System.out.println("\nFile copied to json files folder\n");
        } catch (Exception e) {
            System.out.println("Failed to copy file.\n" + e);
            Toast.makeText(context, "Failed to copy file", Toast.LENGTH_LONG).show();

        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                Log.e("FileHelper", "Error closing streams", e);
            }
        }
    }

    /**
     * Returns the current time in the format "EEEE HH:mm\ndd. MM. yyyy"
     * @return the current time as a string
     */
    private static String getTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("EEEE HH:mm\ndd. MM. yyyy", Locale.ENGLISH);
        Date date = new Date();
        return formatter.format(date);
    }

    /**
     * Lists all the files in the jsonFiles directory
     * @param context - the context of the application
     * @return a map of file names and their sizes
     */
    public static Map<String, String> listAssetFiles(Context context) {
        Map<String, String> fileInfoMap = new HashMap<>();

        // gets jsonFiles directory
        File jsonDir = new File(context.getFilesDir(), "jsonFiles");

        // gets the list of files in jsonFiles dir
        File[] files = jsonDir.listFiles();

        if (files != null && files.length > 0) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".json") && !file.getName().equals("config.json")) {
                    long fileSize = file.length();

                    @SuppressLint("DefaultLocale") String fileSizeFormatted = String.format("%.1f", (double) fileSize / 1024.0);

                    String fileInfo = fileSizeFormatted + " KiB";
                    fileInfoMap.put(file.getName(), fileInfo);
                }
            }
        }

        return fileInfoMap;
    }

    /**
     * Gets the file name from the URI
     * @param context - the context of the application
     * @param uri - the URI of the file
     * @return the file name as a string
     */
    private static String getFileName(Context context, Uri uri) {
        String[] projection = {MediaStore.MediaColumns.DISPLAY_NAME};
        try (Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME);
                return cursor.getString(index);
            }
        }
        return null;
    }

    /**
     * Gets the file from the jsonFiles directory
     * @param context - the context of the application
     * @param fileName - the name of the file
     * @return the file
     * @throws IOException - if an I/O error occurs
     */
    public static File getJsonFileDir(Context context, String fileName) throws IOException {
        // Create or access the jsonFiles directory in the internal repository
        File jsonDir = new File(context.getFilesDir(), "jsonFiles");
        if (!jsonDir.exists()) {
            if (!jsonDir.mkdir()) {
                throw new IOException("Unable to create directory");
            }
        }

        return new File(jsonDir, fileName);
    }
}
