package cz.ima.btTof.util;

import android.content.Context;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Base64;

public class SendReceive {

    /**
     * Send the config file to the server
     * @param context - context of the application
     * @param writer - writer to the server
     * @throws IOException - if an I/O error occurs
     */
    public static void sendConfig(Context context, PrintWriter writer) throws IOException {
        File file = new File(context.getFilesDir(), "jsonFiles/config.json");
        StringBuilder fileContent = new StringBuilder();

        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    fileContent.append(line).append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(context, "Please save the config!", Toast.LENGTH_SHORT).show();
            return;
        }
        String config = fileContent.toString();
        System.out.println("Sending config: " + config);

        writer.println(file.length());
        writer.println(config);
        writer.flush();
    }

    /**
     * Handle the config command - read the config info from the client
     *
     * @param reader reader to read the config info from
     * @throws IOException - if an I/O error occurs
     */

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void handleContent(Context context, BufferedReader reader) throws IOException {
        String fileName = reader.readLine();                        // read the file name
        int fileSize = Integer.parseInt(reader.readLine());         // read the file size

        System.out.println("fileName: " + fileName);
        System.out.println("lengthContent: " + fileSize);

        File file = FileHelper.getJsonFileDir(context, fileName);   // save the file to the directory

        try (FileOutputStream fileOutput = new FileOutputStream(file);
             BufferedOutputStream bufferedOutput = new BufferedOutputStream(fileOutput)) {

            String line;
            while (!(line = reader.readLine()).equals("EOF")) {     // read until the end of the file
                byte[] decodedChunk = Base64.getDecoder().decode(line);
                bufferedOutput.write(decodedChunk);                 // write the decoded chunk to the file
            }

            bufferedOutput.flush();

            new Runnable() {
                public void run() {
                    Toast.makeText(context, "File received and saved: " + fileName, Toast.LENGTH_SHORT).show();
                }
            };
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
