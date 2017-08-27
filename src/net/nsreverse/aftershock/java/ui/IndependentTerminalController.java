package net.nsreverse.aftershock.java.ui;

import com.terminalfx.TerminalBuilder;
import com.terminalfx.TerminalTab;
import javafx.fxml.FXML;
import javafx.scene.control.TabPane;

/**
 * IndependentTerminalController -
 *
 * Controller for a detached Terminal window.
 *
 * @author Robert
 * Created on 8/26/2017
 */
public class IndependentTerminalController {
    @FXML private TabPane terminalTabPane;

    public void initialize() {
        TerminalBuilder terminalBuilder = new TerminalBuilder();
        TerminalTab terminal = terminalBuilder.newTerminal();
        terminalTabPane.getTabs().add(terminal);
    }
}
