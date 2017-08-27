package net.nsreverse.aftershock.java.utils;

import javafx.geometry.Insets;

/**
 * SimpleInsets [-> Insets] -
 *
 * Class for creating Insets with 2 parameters.
 *
 * @author Robert
 * Created on 8/26/2017
 */
public class SimpleInsets extends Insets {
    public SimpleInsets(int topBottom, int leftRight) {
        super(topBottom, leftRight, topBottom, leftRight);
    }
}
