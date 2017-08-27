package net.nsreverse.aftershock.java;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import net.nsreverse.aftershock.java.ui.MainController;
import net.nsreverse.aftershock.java.utils.AlertDialog;
import net.nsreverse.aftershock.java.utils.ConfirmDialog;
import net.nsreverse.aftershock.java.utils.Logger;
import net.nsreverse.aftershock.java.utils.ResourceResolver;

import java.io.IOException;

/**
 * Main -
 *
 * The starting point for this application.
 *
 * @author Robert
 * Created on 8/26/2017
 */
public class Main extends Application
                  implements ConfirmDialog.Delegate {

    private static final String TAG = Main.class.getName();
    private static final int CLOSE_DIALOG_TAG = 1001;

    private Stage currentStage;
    private MainController controller;

    @Override
    public void start(Stage primaryStage) {
        currentStage = primaryStage;

        ApplicationConfig.applicationWillLaunch();

        try {
            FXMLLoader loader = new FXMLLoader();
            Parent root = loader.load(getClass().getResource(ResourceResolver.getLayout("main")).openStream());

            controller = loader.getController();

            primaryStage.setTitle("[No folder selected] - Aftershock Editor");
            primaryStage.setScene(new Scene(root));
            primaryStage.show();
            primaryStage.setOnCloseRequest(event -> {
                event.consume();

                ConfirmDialog.with(Main.this).title("Confirm Close").message("Are you sure you wish to exit?")
                        .tag(CLOSE_DIALOG_TAG).show();
            });
        }
        catch (Exception ex) {
            if (ApplicationConfig.loggingEnabled) Logger.e(TAG, ex.getMessage());
            AlertDialog.withTitle("Fatal Error").message("Please warn the developer about this message.\n\n" +
                    ex.getClass().getName() + ": " + ex.getMessage()).show();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void dialogConfirmedWithTag(int tag) {
        if (tag == CLOSE_DIALOG_TAG) {
            ApplicationConfig.applicationWillClose();

            if (controller != null) {
                controller.killTerminal();
                Logger.d(TAG, "Attempting to kill terminal.");
            }
            else Logger.d(TAG, "Controller is null");

            currentStage.close();
        }
    }

    @Override
    public void dialogCanceledWithTag(int tag) { }
}
