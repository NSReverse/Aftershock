package net.nsreverse.aftershock.java.ui;

import com.sun.javafx.robot.FXRobot;
import com.sun.javafx.robot.impl.BaseFXRobot;
import com.terminalfx.TerminalBuilder;
import com.terminalfx.TerminalTab;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import net.nsreverse.aftershock.java.ApplicationConfig;
import net.nsreverse.aftershock.java.utils.*;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.io.*;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MainController -
 *
 * This IDE is a pain and needs some serious work. There also seems to be a memory leak somewhere so it'd be great
 * if someone could find it. Will go though and document this project at a later date.
 *
 * @author Robert
 * Created on 8/26/2017
 */
public class MainController implements ConfirmDialog.Delegate,
                                       EntryDialog.Delegate {
    private static final String TAG = MainController.class.getName();
    private static final int CLOSE_DIALOG_TAG = 1001;
    private static final int DELETE_DIALOG_TAG = 1002;
    private static final int CREATE_CLASS_DIALOG_TAG = 2001;
    private static final int CREATE_INTERFACE_DIALOG_TAG = 2002;

    @FXML private TreeView<String> projectTreeView;
    @FXML private CodeArea mainCodeArea;
    @FXML private ImageView cleanImageView;
    @FXML private ImageView playImageView;
    @FXML private ImageView buildImageView;
    @FXML private MenuItem newClassMenuItem;
    @FXML private MenuItem newInterfaceMenuItem;
    @FXML private MenuItem deleteMenuItem;
    @FXML private Label statusLabel;
    @FXML private TabPane terminalTabPane;
    @FXML private TitledPane terminalTitledPane;
    @FXML private MenuItem saveMenuItem;

    private File projectBaseDirectory;
    @SuppressWarnings("unused") private File selectedDirectory;
    private File currentEditingFile;
    private File currentAmbiguousSelection;

    private TerminalTab terminal;

    public void initialize() {
        mainCodeArea.setParagraphGraphicFactory(LineNumberFactory.get(mainCodeArea));
        mainCodeArea.richChanges().filter(ch -> !ch.getInserted().equals(ch.getRemoved()))
                .subscribe(change -> {
                    if (mainCodeArea.lengthProperty().getValue() > 0) {
                        mainCodeArea.setStyleSpans(0, computeHighlighting(mainCodeArea.getText()));
                    }
                });
        mainCodeArea.getStylesheets().add(ResourceResolver.getStylesheet("code"));
        mainCodeArea.setDisable(true);
        cleanImageView.setImage(new Image(ResourceResolver.getImage("clean")));
        playImageView.setImage(new Image(ResourceResolver.getImage("play")));
        buildImageView.setImage(new Image(ResourceResolver.getImage("build")));

        if (projectBaseDirectory == null) {
            newClassMenuItem.setDisable(true);
            newInterfaceMenuItem.setDisable(true);
            deleteMenuItem.setDisable(true);
            saveMenuItem.setDisable(true);
        }

        statusLabel.setText("Please open a folder.");
    }

    @FXML private void newClassMenuItemSelected() {
        EntryDialog.with(this).title("New Java Class").message("Please enter a class name.")
                .tag(CREATE_CLASS_DIALOG_TAG).show();
    }

    @FXML private void newInterfaceMenuItemSelected() {
        EntryDialog.with(this).title("New Java Interface").message("Please enter an interface name.")
                .tag(CREATE_INTERFACE_DIALOG_TAG).show();
    }

    @FXML private void openFolderMenuItemSelected() {
        Stage currentStage = ((Stage)playImageView.getScene().getWindow());

        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Open Project Folder");
        chooser.setInitialDirectory(new File(System.getProperty("user.home")));
        projectBaseDirectory = chooser.showDialog(currentStage);

        if (projectBaseDirectory != null) {
            refreshDirectoryListing();
            statusLabel.setText("Opened workspace: " + projectBaseDirectory.getPath());
            currentStage.setTitle("[" + projectBaseDirectory.getAbsolutePath() + "] - Aftershock Editor");

            terminalTabPane.getTabs().clear();

            TerminalBuilder terminalBuilder = new TerminalBuilder();
            terminal = terminalBuilder.newTerminal();
            terminal.setText(projectBaseDirectory.getName());
            terminal.setTerminalPath(projectBaseDirectory.toPath());
            terminal.setContextMenu(null);
            terminalTabPane.getTabs().add(terminal);
        }
    }

    @FXML private void terminalMenuItemSelected() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(ResourceResolver.getLayout("independent_terminal")));

            Stage terminalStage = new Stage();
            terminalStage.setTitle("Independent Terminal");
            terminalStage.setScene(new Scene(root, 600, 400));
            terminalStage.show();
        }
        catch (IOException ex) {
            if (ApplicationConfig.loggingEnabled) Logger.e(TAG, ex.getMessage());
            AlertDialog.withTitle("Fatal Error").message("Please warn the developer about this message.\n\n" +
                    ex.getMessage()).show();
        }
    }

    @FXML private void deleteMenuItemSelected() {
        ConfirmDialog.with(this)
                .title("Confirm Delete")
                .message("Do you wish to delete " + currentAmbiguousSelection.getName() + "?")
                .tag(DELETE_DIALOG_TAG)
                .show();
    }

    @FXML private void closeMenuItemSelected() {
        ConfirmDialog.with(this).title("Confirm Close").message("Are you sure you wish to exit?").tag(CLOSE_DIALOG_TAG)
                .show();
    }

    @FXML private void aboutMenuItemSelected() {
        AlertDialog.withTitle("About Editor").message("This is a simple Java Editor for compiling and writing quick" +
                " Java code on the fly.\nWritten by Robert (@NSReverse)").show();
    }

    @FXML private void cleanImageViewClicked() {
        if (projectBaseDirectory != null) {
            int count = 0;

            File[] directoryListing = projectBaseDirectory.listFiles();

            for (File currentFile : directoryListing) {
                if (currentFile.getName().contains(".class")) {
                    boolean deleted = currentFile.delete();

                    if (deleted) count++;
                }
            }

            statusLabel.setText(String.format("Clean result: %d item(s).", count));

            refreshDirectoryListing();
        }
    }

    @FXML private void buildImageViewClicked() {
        cleanImageViewClicked();

        Scene currentScene = playImageView.getScene();

        if (projectBaseDirectory != null || currentEditingFile != null) {
            try {
                terminal.command("javac " + currentEditingFile.getName());
                terminal.focusCursor();

                simulateEnter(currentScene);
            }
            catch (Exception ex) {
                if (ApplicationConfig.loggingEnabled) Logger.e(TAG, ex.getMessage());
            }
        }

        statusLabel.setText("Rebuild complete.");
    }

    @FXML private void playImageViewClicked() {
        Scene currentScene = playImageView.getScene();

        if (terminal != null) {
            try {
                if (currentEditingFile != null) {
                    statusLabel.setText("Compiling " + currentEditingFile.getName());

                    Process p = Runtime.getRuntime().exec("javac " + currentEditingFile.getName(), null,
                            projectBaseDirectory);

                    if (ApplicationConfig.loggingEnabled) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                        BufferedReader eReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                        String line;

                        while ((line = reader.readLine()) != null) {
                            Logger.d(TAG, "Input Stream > " + line);
                        }

                        while ((line = eReader.readLine()) != null) {
                            Logger.d(TAG, "Error Stream > " + line);
                        }
                    }

                    String className = currentEditingFile.getName().split("\\.")[0];

                    if (!terminalTitledPane.isExpanded()) terminalTitledPane.setExpanded(true);

                    terminal.command("java " + className);
                    terminal.focusCursor();

                    simulateEnter(currentScene);

                    statusLabel.setText("Executed " + currentEditingFile.getName());

                    refreshDirectoryListing();
                }
            }
            catch (Exception ex) {
                if (ApplicationConfig.loggingEnabled) Logger.e(TAG, ex.getMessage());
            }
        }
    }

    @FXML private void saveMenuItemSelected() {
        if (currentEditingFile != null) {
            statusLabel.setText("Saving " + currentEditingFile.getName());

            String currentCode = mainCodeArea.getText();

            try {
                PrintWriter writer = new PrintWriter(currentEditingFile);
                writer.print(currentCode);
                writer.close();
            }
            catch (FileNotFoundException ex) {
                if (ApplicationConfig.loggingEnabled) Logger.e(TAG, ex.getMessage());
            }

            statusLabel.setText("Saved " + currentEditingFile.getName());
        }
    }

    @Override
    public void dialogConfirmedWithTag(int tag) {
        switch (tag) {
            case CLOSE_DIALOG_TAG: {
                ApplicationConfig.applicationWillClose();
                ((Stage)playImageView.getScene().getWindow()).close();
            } break;

            case DELETE_DIALOG_TAG: {
                if (currentAmbiguousSelection != null) {
                    boolean deleted = currentAmbiguousSelection.delete();

                    if (ApplicationConfig.loggingEnabled && deleted) Logger.i(TAG, "Deleted: " +
                            currentAmbiguousSelection.getName());

                    refreshDirectoryListing();
                }
            } break;
        }
    }

    @Override
    public void dialogCanceledWithTag(int tag) {
        if (tag == CLOSE_DIALOG_TAG) {
            if (ApplicationConfig.loggingEnabled) Logger.d(TAG, "shouldClose = false");
        }
    }

    @Override
    public void dialogConfirmedWithEntry(int tag, String entry) {

        File newJavaClass = new File(Paths.get(projectBaseDirectory.getAbsolutePath(),
                entry + ".java").toString());
        String boiler;

        switch (tag) {
            case CREATE_CLASS_DIALOG_TAG: {
                try {
                    boiler = "public class " + entry + " {\n" +
                            "    \n" +
                            "}";

                    boolean result = createFileWithBoiler(newJavaClass, boiler);

                    if (result) statusLabel.setText("Created " + newJavaClass.getName());
                    else statusLabel.setText("Failed to create " + newJavaClass.getName());
                }
                catch (Exception ex) {
                    if (ApplicationConfig.loggingEnabled) Logger.e(TAG, ex.getMessage());
                }
            } break;

            case CREATE_INTERFACE_DIALOG_TAG: {
                try {
                    boiler = "public interface " + entry + " {\n" +
                            "    \n" +
                            "}";

                    boolean result = createFileWithBoiler(newJavaClass, boiler);

                    if (result) statusLabel.setText("Created " + newJavaClass.getName());
                    else statusLabel.setText("Failed to create " + newJavaClass.getName());
                }
                catch (Exception ex) {
                    if (ApplicationConfig.loggingEnabled) Logger.e(TAG, ex.getMessage());
                }
            } break;
        }

        refreshDirectoryListing();
    }

    private boolean createFileWithBoiler(File file, String boiler) throws IOException {
        boolean create = file.createNewFile();

        PrintWriter writer = new PrintWriter(file);
        writer.print(boiler);
        writer.close();

        return create;
    }

    private void refreshDirectoryListing() {
        File[] directoryContents = projectBaseDirectory.listFiles();

        TreeItem<String> root = new TreeItem<>(projectBaseDirectory.getName() + " (Project Workspace)");
        root.setExpanded(true);

        if (directoryContents != null) {
            for (File currentFile : directoryContents) {
                String fileName = currentFile.getName();

                if (currentFile.isDirectory()) {
                    fileName += "/";
                }

                TreeItem<String> currentItem = new TreeItem<>(fileName);

                root.getChildren().add(currentItem);
            }
        }

        projectTreeView.setRoot(root);
        projectTreeView.getSelectionModel().selectedItemProperty()
                .addListener(((observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        File newFile =
                                Paths.get(projectBaseDirectory.getAbsolutePath(), newValue.getValue()).toFile();

                        if (newFile.isFile()) {
                            currentEditingFile = newFile;

                            try {
                                Scanner scn = new Scanner(currentEditingFile);
                                scn.useDelimiter("\\A");

                                String content = "";

                                while (scn.hasNext()) {
                                    content = scn.next();
                                }

                                mainCodeArea.replaceText(content);
                                scn.close();
                            }
                            catch (FileNotFoundException ex) {
                                if (ApplicationConfig.loggingEnabled) Logger.e(TAG, ex.getMessage());
                            }

                            statusLabel.setText("Opened " + currentEditingFile.getName());

                            if (mainCodeArea.isDisable()) {
                                mainCodeArea.setDisable(false);
                            }
                        }

                        currentAmbiguousSelection = new File(Paths.get(projectBaseDirectory.getAbsolutePath(),
                                newValue.getValue()).toString());
                    }
                }));

        newClassMenuItem.setDisable(false);
        newInterfaceMenuItem.setDisable(false);
        saveMenuItem.setDisable(false);
        deleteMenuItem.setDisable(false);
    }

    private void simulateEnter(Scene scene) {
        FXRobot robot = new BaseFXRobot(scene);
        robot.keyPress(KeyCode.ENTER);
        robot.keyRelease(KeyCode.ENTER);
    }

    //region: --CodeArea Setup--
    // https://github.com/rahmanusta/TerminalFX
    private static final String[] KEYWORDS = new String[] {
            "abstract", "assert", "boolean", "break", "byte",
            "case", "catch", "char", "class", "const",
            "continue", "default", "do", "double", "else",
            "enum", "extends", "final", "finally", "float",
            "for", "goto", "if", "implements", "import",
            "instanceof", "int", "interface", "long", "native",
            "new", "package", "private", "protected", "public",
            "return", "short", "static", "strictfp", "super",
            "switch", "synchronized", "this", "throw", "throws",
            "transient", "try", "void", "volatile", "while"
    };

    private static final String[] VALUES = new String[] {
            "true", "false"
    };

    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String VALUE_PATTERN = "\\b(" + String.join("|", VALUES) + ")\\b";
    private static final String INT_PATTERN = "\\b-?\\d+\\b";
    private static final String PAREN_PATTERN = "\\(|\\)";
    private static final String BRACE_PATTERN = "\\{|\\}";
    private static final String BRACKET_PATTERN = "\\[|\\]";
    private static final String SEMICOLON_PATTERN = "\\;";
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    private static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";

    private static final Pattern PATTERN = Pattern.compile(
            "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
                    + "|(?<VALUE>" + VALUE_PATTERN + ")"
                    + "|(?<INT>" + INT_PATTERN + ")"
                    + "|(?<PAREN>" + PAREN_PATTERN + ")"
                    + "|(?<BRACE>" + BRACE_PATTERN + ")"
                    + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
                    + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
                    + "|(?<STRING>" + STRING_PATTERN + ")"
                    + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
    );

    private static StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder
                = new StyleSpansBuilder<>();
        while(matcher.find()) {
            String styleClass =
                    matcher.group("KEYWORD") != null ? "keyword" :
                            matcher.group("VALUE") != null ? "value" :
                                    matcher.group("INT") != null ? "value" :
                                            matcher.group("PAREN") != null ? "paren" :
                                                    matcher.group("BRACE") != null ? "brace" :
                                                            matcher.group("BRACKET") != null ? "bracket" :
                                                                    matcher.group("SEMICOLON") != null ? "semicolon" :
                                                                            matcher.group("STRING") != null ? "string" :
                                                                                    matcher.group("COMMENT") != null ? "comment" :
                                                                                            null; /* never happens */ assert styleClass != null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }
    //endregion
}
