package com.main.rpbt;

/**
 * Class for storing settings values
 */
public class SettingsValues {
    private static int FPS = 10, rotate = 0;

    private static final boolean[] detected = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false};
    // 0 ambientPerSpad, 1 nbSpadsEnabled, 2 nbTargetDetected, 3 signalPerSpad, 4 rangeSigma, 5 distance, 6 targetStatus, 7 reflectancePercent,
    // 8 motionIndicator, 9 xtalk, 10 accel, 11 toggleButtonNbOfSensors, 12 size, 13 order
    private static String flip = "None", nbTargets = "4";
    private static String sharpener, font, colMin, colMax;

    public static int getFPS() {
        return FPS;
    }
    public static int getRotate() {
        return rotate;
    }
    public static boolean isDetected(int pos) {
        return detected[pos];
    }

    /*
     *  setters *
     */

    public static void setFPS(int FPS) {
        SettingsValues.FPS = FPS;
    }
    public static void setRotate(int rotate) {
        SettingsValues.rotate = rotate;
    }
    public static void setDetected(boolean detected, int pos) {
        SettingsValues.detected[pos] = detected;
    }

    public static void setFlip(String flip) {
        SettingsValues.flip = flip;
    }

    public static void setNbTargets(String nbTargets) {
        SettingsValues.nbTargets = nbTargets;
    }

    public static void setSharpener(String sharpener) {
        SettingsValues.sharpener = sharpener;
    }

    public static void setFont(String font) {
        SettingsValues.font = font;
    }

    public static void setColMin(String colMin) {
        SettingsValues.colMin = colMin;
    }

    public static void setColMax(String colMax) {
        SettingsValues.colMax = colMax;
    }

    /*
     *  getters *
     */

    public static String getFlip() {
        return SettingsValues.flip;
    }
    public static int getFlipInt() {
        switch (SettingsValues.flip) {
            case "None": return 0;
            case "x": return 1;
            case "y": return 2;
            case "xy": return 3;
        }
        return 0;
    }

    public static String getNbTargets() {
        return SettingsValues.nbTargets;
    }


    public static String getSharpener() {
        return SettingsValues.sharpener;
    }

    public static String getFont() {
        return SettingsValues.font;
    }

    public static String getColMin() {
        return SettingsValues.colMin;
    }

    public static String getColMax() {
        return SettingsValues.colMax;
    }
}
