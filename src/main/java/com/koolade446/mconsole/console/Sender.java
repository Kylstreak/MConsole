package com.koolade446.mconsole.console;


import javafx.scene.paint.Color;

public enum Sender {
    MINECRAFT(Color.GREEN),
    INFO(Color.WHITE),
    DOWNLOAD(Color.CYAN),
    WARN(Color.YELLOW),
    ERROR(Color.RED);

    final Color color;
    Sender(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }
}
