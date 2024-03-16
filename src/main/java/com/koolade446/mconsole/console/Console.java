package com.koolade446.mconsole.console;

import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class Console {
    private final TextFlow flow;
    private final StackPane pane;

    public Console(StackPane pane) {
        this.flow = new TextFlow();
        this.pane = pane;
        pane.setVisible(true);
        flow.setVisible(true);

        pane.getChildren().add(flow);
    }

    public void log(Sender sender, String message) {
        if (flow.getChildren().size() > 100) flow.getChildren().remove(0, 50);
        Text text = new Text(String.format("[%s] %s\n", sender.toString(), message));
        text.setFill(sender.getColor());
        flow.getChildren().add(text);
    }


    public void err(String errorMessage) {
        log(Sender.ERROR, errorMessage);
    }

    public void clearConsole() {
        flow.getChildren().clear();
    }
}
