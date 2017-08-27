package net.nsreverse.aftershock.java.utils;

import com.sun.istack.internal.NotNull;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

/**
 * AlertDialog -
 *
 * This class provides a simple way of creating a modal alert.
 *
 * @author Robert
 * Created on 8/26/2017
 */
public class AlertDialog {

    private String title;
    private String message;

    private AlertDialog(@NotNull String title) {
        this.title = title;
        message = "";
    }

    public static AlertDialog withTitle(@NotNull String title) {
        return new AlertDialog(title);
    }

    public AlertDialog message(@NotNull String message) {
        this.message = message;
        return this;
    }

    public void show() {
        Stage alertStage = new Stage();

        Label messageLabel = new Label(message);
        messageLabel.setTextAlignment(TextAlignment.CENTER);

        Button dismissButton = new Button("Dismiss");
        dismissButton.setDefaultButton(true);
        dismissButton.setOnAction(event -> {
            alertStage.close();
        });

        VBox rootLayout = new VBox(10);
        rootLayout.setPadding(new SimpleInsets(15, 30));
        rootLayout.getChildren().addAll(messageLabel, dismissButton);
        rootLayout.setAlignment(Pos.CENTER);

        alertStage.setTitle(title);
        alertStage.setScene(new Scene(rootLayout));
        alertStage.show();
    }
}
