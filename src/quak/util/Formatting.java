package quak.util;

/**
 * Provides some basic helper methods for printing info to console.
 */
public abstract class Formatting {
    public static String padRight(String s, int n) {
        return String.format("%1$-" + n + "s", s);
    }

    public static String padLeft(String s, int n) {
        return String.format("%1$" + n + "s", s);
    }
}
