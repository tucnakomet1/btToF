package cz.ima.btTof.utils;

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/** Utility class for various functions */
public class Util {

    /** List all directories in the current directory
     * @return list of directories */
    public static List<String> listDir() {
        File f = new File(".");         // current directory

        File[] files = f.listFiles();
        List<String> dirs = new ArrayList<>();
        for (File file : files) {
            if (file.isDirectory()) {
                dirs.add(file.getName());
            }
        }
        return dirs;
    }

    /** Create a directory
     * @param dirName name of the directory */
    public static void createDir(String dirName) {
        File file = new File(dirName);
        if (file.mkdir()) {
            System.out.println("Directory " + dirName + " is created!");
        }
    }


    /**
     * Get the local IP address
     * @return local IP address
     * */
    public static String getLocalIPAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            System.out.print("All IP addresses: [");
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof java.net.Inet4Address) {
                        String IP = inetAddress.getHostAddress();
                        System.out.print(inetAddress.getHostAddress() + ", ");
                        ip = IP;
                    }
                }
            }
            System.out.println("]");
        } catch (SocketException e) {
            System.out.println("Nepodařilo se získat IP adresu: " + e.getMessage());
        }
        return ip;
    }
}
