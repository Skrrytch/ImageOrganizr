package eu.spex.iorg.component.pane;

import java.util.LinkedHashMap;
import java.util.Map;

import javafx.scene.paint.Color;

/**
 * Assigns a distinct colour to each category, in the order the categories are created. Ten colours
 * is about the limit of what stays reliably distinguishable, so beyond that the palette repeats.
 * The colours are the Tableau 10 set, which is designed for exactly this purpose.
 */
public class CategoryColors {

    private static final Color[] PALETTE = {
            Color.web("#4e79a7"), // blue
            Color.web("#f28e2b"), // orange
            Color.web("#e15759"), // red
            Color.web("#76b7b2"), // teal
            Color.web("#59a14f"), // green
            Color.web("#edc948"), // yellow
            Color.web("#b07aa1"), // purple
            Color.web("#ff9da7"), // pink
            Color.web("#9c755f"), // brown
            Color.web("#bab0ac"), // grey
    };

    private final Map<String, Color> assigned = new LinkedHashMap<>();

    public Color colorFor(String category) {
        Color color = assigned.get(category);
        if (color == null) {
            color = PALETTE[assigned.size() % PALETTE.length];
            assigned.put(category, color);
        }
        return color;
    }

    public static String toCss(Color color) {
        return String.format("#%02x%02x%02x",
                Math.round(color.getRed() * 255),
                Math.round(color.getGreen() * 255),
                Math.round(color.getBlue() * 255));
    }

    /**
     * Black or white, whichever stays readable on the given background.
     */
    public static String textColorOn(Color background) {
        double luminance = 0.2126 * background.getRed()
                + 0.7152 * background.getGreen()
                + 0.0722 * background.getBlue();
        return luminance > 0.6 ? "#1a1a1a" : "#ffffff";
    }

    public static Color hoverVariant(Color color) {
        return color.deriveColor(0, 1, 0.87, 1);
    }
}
