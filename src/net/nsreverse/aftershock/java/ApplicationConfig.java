package net.nsreverse.aftershock.java;

import net.nsreverse.aftershock.java.utils.Logger;

/**
 * ApplicationConfig -
 *
 * General all-purpose class for Application-wide functions/defaults.
 *
 * @author Robert
 * Created on 8/26/2017
 */
public class ApplicationConfig {
    private static final String TAG = ApplicationConfig.class.getName();
    public static final boolean loggingEnabled = true;

    public static String currentProjectFolder = null;

    public static void applicationWillLaunch() {
        if (loggingEnabled) Logger.i(TAG, "applicationWillLaunch");
    }

    public static void applicationWillClose() {
        if (loggingEnabled) Logger.i(TAG, "applicationWillClose");
    }
}
