package com.koolade446.mconsole.profiles;

import java.nio.file.Path;

public class Profile {

    private final String name;
    private final Path location;
    private String version;

    public Profile(String name, Path location, String version) {
        this.name = name;
        this.location = location;

    }
}
