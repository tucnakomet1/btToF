package com.main.rpbt.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Class that handles the color configuration of the heatmap.
 */
public class ColorConfig {
    private final int maxValue, minValue, TEXT_SIZE;

    /**
     * Constructor
     *
     * @param text_size size of the text
     * @param maxValue  maximum value
     * @param minValue  minimum value
     */
    public ColorConfig(int text_size, int maxValue, int minValue) {
        this.TEXT_SIZE = text_size;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    /**
     * Returns the color of the pixel based on the value. -> converts number into color
     *
     * @param value value of the pixel
     * @return color
     */
    public int getColorFromValue(double value) {
        if (value >= maxValue || value <= minValue)
            return Color.HSVToColor(new float[]{0f, 0.0f, 1.0f});

        float normalizedValue = (float) ((value - minValue) / (maxValue - minValue));

        float hue = (float) (normalizedValue * 240.0);
        return Color.HSVToColor(new float[]{hue, 0.9f, 1.0f});
    }

    /**
     * Creates a bitmap of the heatmap.
     *
     * @param gridSize          size of the grid
     * @param cellSize          size of the cell
     * @param data              data to be displayed
     * @param currentLeftFrame  current left frame
     * @param currentRightFrame current right frame
     * @param leftTab           left tab
     * @param rightTab          right tab
     * @return bitmap
     */
    public Bitmap createHeatmapBitmap(int gridSize, int cellSize, double[][] data, double[][] currentLeftFrame, double[][] currentRightFrame, String leftTab, String rightTab) {
        Bitmap bitmap = Bitmap.createBitmap(gridSize * cellSize, gridSize * cellSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();

        paint.setColor(Color.WHITE);
        canvas.drawRect(0, 0, gridSize * cellSize, gridSize * cellSize, paint);

        // text style settings
        Paint textPaintLeft = new Paint();
        textPaintLeft.setColor(Color.BLACK);
        //
        textPaintLeft.setTextSize(TEXT_SIZE);
        textPaintLeft.setTextAlign(Paint.Align.LEFT);

        Paint textPaintRight = new Paint();
        textPaintRight.setColor(Color.BLACK);
        textPaintRight.setTextSize(TEXT_SIZE);
        textPaintRight.setTextAlign(Paint.Align.RIGHT);

        for (int x = 0; x < gridSize; x++) {
            for (int y = 0; y < gridSize; y++) {
                double value = data[x][y];
                int color = getColorFromValue(value);
                paint.setColor(color);

                int left = x * cellSize;
                int top = y * cellSize;
                int right = left + cellSize;
                int bottom = top + cellSize;

                // draw pixel
                canvas.drawRect(left, top, right, bottom, paint);

                // draw text - left
                if (!leftTab.equals("none")) {
                    int val = (int) currentLeftFrame[x][y];
                    if (val != 0) {
                        String textLeft = String.valueOf(val);
                        canvas.drawText(textLeft, left + TEXT_SIZE, top + TEXT_SIZE, textPaintLeft);
                    }
                }

                // draw text - right
                if (!rightTab.equals("none")) {
                    int val = (int) currentRightFrame[x][y];
                    if (val != 0) {
                        String textRight = String.valueOf(val);
                        canvas.drawText(textRight, right - TEXT_SIZE, top + TEXT_SIZE, textPaintRight);
                    }
                }
            }
        }

        return bitmap;
    }

    /**
     * Creates an empty screen with a message.
     *
     * @param gridSize    size of the grid
     * @param cellSize    size of the cell
     * @param offlineMode true if the stream is not running
     * @return bitmap
     */
    public static Bitmap createEmptyScreen(int gridSize, int cellSize, boolean offlineMode) {
        Bitmap bitmap = Bitmap.createBitmap(gridSize * cellSize, gridSize * cellSize, Bitmap.Config.ARGB_8888);

        Paint paint = new Paint();
        paint.setColor(Color.WHITE);

        Canvas canvas = new Canvas(bitmap);
        canvas.drawRect(0, 0, gridSize * cellSize, gridSize * cellSize, paint);

        Paint textPaintLeft = new Paint();
        textPaintLeft.setColor(Color.BLACK);
        textPaintLeft.setTextSize(65);
        textPaintLeft.setTextAlign(Paint.Align.CENTER);

        String text = "The stream is not running.";
        if (offlineMode) text = "No record has been selected yet.";

        canvas.drawText(text, (gridSize * cellSize) >> 1, (gridSize * cellSize) >> 2, textPaintLeft);
        canvas.drawText(text, (gridSize * cellSize) >> 1, (gridSize * cellSize) >> 1, textPaintLeft);
        canvas.drawText(text, (gridSize * cellSize) >> 1, (3*gridSize * cellSize) >> 2, textPaintLeft);

        return bitmap;
    }
}
