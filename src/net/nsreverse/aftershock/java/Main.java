package net.nsreverse.aftershock.java;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import net.nsreverse.aftershock.java.utils.ConfirmDialog;
import net.nsreverse.aftershock.java.utils.ResourceResolver;

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

    private static final int CLOSE_DIALOG_TAG = 1001;

    private Stage currentStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        currentStage = primaryStage;

        ApplicationConfig.applicationWillLaunch();
        Parent root = FXMLLoader.load(getClass().getResource(ResourceResolver.getLayout("main")));

        primaryStage.setTitle("[No folder selected] - Aftershock Editor");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
        primaryStage.setOnCloseRequest(event -> {
            event.consume();

            ConfirmDialog.with(Main.this).title("Confirm Close").message("Are you sure you wish to exit?")
                    .tag(CLOSE_DIALOG_TAG).show();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void dialogConfirmedWithTag(int tag) {
        if (tag == CLOSE_DIALOG_TAG) {
            ApplicationConfig.applicationWillClose();
            currentStage.close();
        }
    }

    @Override
    public void dialogCanceledWithTag(int tag) { }
}
