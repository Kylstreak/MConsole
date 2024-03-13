package com.koolade446.mconsole.configs;

import java.nio.file.Path;

public enum RegexConstant {
    CURRENT(Path.of(System.getProperty("user.dir"))),
    HOME(Path.of(System.getProperty(("user.home")))),
    MINECRAFT(Path.of(System.getenv("APPDATA"), ".minecraft"));

    final Path path;
    RegexConstant(Path path) {
        this.path = path;
    }
    public Path getPath() {
        return path;
    }
}
