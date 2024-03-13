package com.koolade446.mconsole.console;

import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class Console {

    private final TextArea visualConsole;
    private final TextFlow flow;
    public Console(TextArea visualConsole) {
        this.visualConsole = visualConsole;
        this.flow = new TextFlow();

        visualConsole.setEditable(false);
    }

    public void log(Sender sender, String message) {
        Text text = new Text(String.format("[%s] %s\n", sender.toString(), message));
        text.setFill(sender.getColor());
        flow.getChildren().add(text);
    }


    public void err(String errorMessage) {
        log(Sender.ERROR, errorMessage);
    }

    public void clearConsole() {
        visualConsole.clear();
    }
}
