package net.nsreverse.aftershock.java.utils;

/**
 * Logger -
 *
 * Simple logging output class.
 *
 * @author Robert
 * Created on 8/26/2017
 */
public class Logger {
    public static void d(String tag, String message) {
        print(String.format("%s/D > %s", tag, message));
    }

    public static void e(String tag, String message) {
        print(String.format("%s/E > %s", tag, message));
    }

    public static void i(String tag, String message) {
        print(String.format("%s/I > %s", tag, message));
    }

    public static void v(String tag, String message) {
        print(String.format("%s/V > %s", tag, message));
    }

    private static void print(String message) {
        System.out.println(message);
    }
}
