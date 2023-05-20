/*
 * Copyright (c) 2023.
 * @author Sourabh P. Bhat ( https://spbhat.in/ )
 */

package in.spbhat.icons;

import javafx.scene.image.*;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.Arrays;

public class Icon {
    public static ImageView graphic(String iconName, int px) {
        URL resource = Icon.class.getResource(iconName);
        if (resource != null) {
            return new ImageView(new Image(resource.toString(), px, -1, true, true));
        } else {
            return new ImageView(generatePlainImage(px, px));
        }
    }

    private static Image generatePlainImage(int width, int height) {
        WritableImage img = new WritableImage(width, height);
        PixelWriter pw = img.getPixelWriter();

        Color color = Color.GRAY;
        int alpha = (int) (color.getOpacity() * 255);
        int r = (int) (color.getRed() * 255);
        int g = (int) (color.getGreen() * 255);
        int b = (int) (color.getBlue() * 255);

        int pixel = (alpha << 24) | (r << 16) | (g << 8) | b;
        int[] pixels = new int[width * height];
        Arrays.fill(pixels, pixel);

        pw.setPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), pixels, 0, width);
        return img;
    }
}
