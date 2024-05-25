package com.main.rpbt;

public class SettingsValues {
    private static int FPS = 10;
    private static int rotate = 0;
    private static boolean detected = false;
    private static boolean distance = false;
    private static boolean stats = false;

    public static int getFPS() {
        return FPS;
    }
    public static int getRotate() {
        return rotate;
    }
    public static boolean isDetected() {
        return detected;
    }
    public static boolean isDistance() {
        return distance;
    }
    public static boolean isStats() {
        return stats;
    }

    public static void setFPS(int FPS) {
        SettingsValues.FPS = FPS;
    }
    public static void setRotate(int rotate) {
        SettingsValues.rotate = rotate;
    }
    public static void setDetected(boolean detected) {
        SettingsValues.detected = detected;
    }
    public static void setDistance(boolean distance) {
        SettingsValues.distance = distance;
    }
    public static void setStats(boolean stats) {
        SettingsValues.stats = stats;
    }
}
