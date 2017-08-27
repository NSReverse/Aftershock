package net.nsreverse.aftershock.java.utils;

/**
 * ResourceResolver -
 *
 * Simple class for retrieving resource locations.
 */
public class ResourceResolver {
    public static String getImage(String imageName) {
        return String.format("/net/nsreverse/aftershock/res/image/%s.png", imageName);
    }

    public static String getStylesheet(String sheetName) {
        return String.format("/net/nsreverse/aftershock/res/stylesheets/%s.css", sheetName);
    }

    public static String getLayout(String layoutName) {
        return String.format("/net/nsreverse/aftershock/res/layout/%s.fxml", layoutName);
    }
}
