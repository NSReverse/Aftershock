package net.nsreverse.aftershock.java.utils;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * EntryDialog -
 *
 * This class provides a simple way of creating a modal alert asking the user for an answer.
 *
 * @author Robert
 * Created on 8/26/2017
 */
public class EntryDialog {

    private String title;
    private String message;
    private int tag;
    private Delegate delegate;

    public interface Delegate {
        void dialogConfirmedWithEntry(int tag, String entry);
    }

    private EntryDialog(Delegate delegate) {
        this.delegate = delegate;
    }

    public static EntryDialog with(Delegate delegate) {
        return new EntryDialog(delegate);
    }

    public EntryDialog title(String title) {
        this.title = title;
        return this;
    }

    public EntryDialog message(String message) {
        this.message = message;
        return this;
    }

    public EntryDialog tag(int tag) {
        this.tag = tag;
        return this;
    }

    public void show() {
        Stage alertStage = new Stage();
        alertStage.setTitle(title);

        TextField textField = new TextField();

        Label messageLabel = new Label(message);

        Button confirmButton = new Button("Confirm");
        confirmButton.setDefaultButton(true);
        confirmButton.setOnAction(event -> {
            if (delegate != null) {
                delegate.dialogConfirmedWithEntry(tag, textField.getText());
            }

            alertStage.close();
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(event -> alertStage.close());

        HBox buttonLayout = new HBox(10);
        buttonLayout.getChildren().addAll(confirmButton, cancelButton);
        buttonLayout.setAlignment(Pos.CENTER);

        VBox rootLayout = new VBox(10);
        rootLayout.setPadding(new SimpleInsets(15, 30));
        rootLayout.setAlignment(Pos.CENTER);
        rootLayout.getChildren().addAll(messageLabel, textField, buttonLayout);

        alertStage.setScene(new Scene(rootLayout));
        alertStage.show();
    }
}
