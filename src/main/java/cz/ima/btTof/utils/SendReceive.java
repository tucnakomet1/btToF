package cz.ima.btTof.utils;

import java.io.*;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;

/**
 * Helping class for sending and receiving data
 */
public class SendReceive {
    private static final String SAVE_FOLDER = "save";

    /**
     * Handle the config command - read the config info from the client
     *
     * @param reader reader to read the config info from
     * @throws IOException - if an I/O error occurs
     */
    public static void handleConfig(BufferedReader reader) throws IOException {
        int length = Integer.parseInt(reader.readLine());   // Read length of config info

        char[] configBuffer = new char[length];
        reader.read(configBuffer, 0, length);           // Read the config info

        String configInfo = new String(configBuffer);
        System.out.println("config: " + configInfo);

        WriteJson.handleConfig(configInfo);
    }


    /**
     * Send the recorded file to the client
     *
     * @param writer writer to send the data to
     * @param fileName name of the recorded file
     */
    public static void sendRecordedFile(PrintWriter writer, String fileName) {
        String filePath = Paths.get(SAVE_FOLDER, fileName).toString();

        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("File not found");
            return;
        }

        try (FileInputStream fileInput = new FileInputStream(file);
             BufferedInputStream bufferedInput = new BufferedInputStream(fileInput)) {

            // send file name and file size
            writer.println("content");
            writer.println(fileName);
            writer.println(file.length());
            writer.flush();

            System.out.println("Sending file: " + fileName);
            System.out.println("File size: " + file.length());

            // send file content in chunks -> 3*1024 bytes because of the Base64 encoding
            byte[] buffer = new byte[3 * 1024];
            int bytesRead;
            while ((bytesRead = bufferedInput.read(buffer)) != -1) {
                String base64Chunk = Base64.getEncoder().encodeToString(Arrays.copyOf(buffer, bytesRead));
                writer.println(base64Chunk);
                writer.flush();
            }

            writer.println("EOF");
            writer.flush();
            System.out.println("File sent successfully.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
