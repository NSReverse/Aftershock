package net.nsreverse.aftershock.java.utils;

import com.sun.istack.internal.NotNull;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * ConfirmDialog -
 *
 * This class provides a simple way of creating a modal alert asking the user for a confirmation.
 *
 * @author Robert
 * Created on 8/26/2017
 */
public class ConfirmDialog {

    private String title;
    private String message;
    private int tag;
    private Delegate delegate;

    public interface Delegate {
        void dialogConfirmedWithTag(int tag);
        void dialogCanceledWithTag(int tag);
    }

    private ConfirmDialog(Delegate delegate) {
        this.title = "";
        this.message = "";
        this.tag = -1;
        this.delegate = delegate;
    }

    public static ConfirmDialog with(@NotNull Delegate delegate) {
        return new ConfirmDialog(delegate);
    }

    public ConfirmDialog title(@NotNull String title) {
        this.title = title;
        return this;
    }

    public ConfirmDialog message(@NotNull String message) {
        this.message = message;
        return this;
    }

    public ConfirmDialog tag(@NotNull int tag) {
        this.tag = tag;
        return this;
    }

    public void show() {
        Stage alertStage = new Stage();

        Label messageLabel = new Label(message);

        Button confirmButton = new Button("Confirm");
        confirmButton.setDefaultButton(true);
        confirmButton.setOnAction(event -> {
            if (delegate != null) {
                delegate.dialogConfirmedWithTag(tag);
            }

            alertStage.close();
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(event -> {
            if (delegate != null) {
                delegate.dialogCanceledWithTag(tag);
            }

            alertStage.close();
        });

        HBox buttonLayout = new HBox(10);
        buttonLayout.getChildren().addAll(confirmButton, cancelButton);
        buttonLayout.setAlignment(Pos.CENTER);

        VBox rootLayout = new VBox(10);
        rootLayout.setPadding(new SimpleInsets(15, 30));
        rootLayout.getChildren().addAll(messageLabel, buttonLayout);
        rootLayout.setAlignment(Pos.CENTER);

        alertStage.setTitle(title);
        alertStage.setScene(new Scene(rootLayout));
        alertStage.show();
    }

}
