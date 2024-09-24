package cz.ima.btTof.utils;

import java.io.File;
import java.util.ArrayList;
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
}
