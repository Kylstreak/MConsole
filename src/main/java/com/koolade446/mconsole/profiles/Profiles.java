package com.koolade446.mconsole.profiles;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

public class Profiles extends HashMap<String, Profile> implements Serializable {
    private final Path saveLocation = Paths.get(System.getProperty("user.dir"), "profiles.bin");

    public void save() {
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(saveLocation.toFile()));
            outputStream.writeObject(this);
            outputStream.flush();
            outputStream.close();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Profiles load() {
        File saveFile = saveLocation.toFile();
        if (!saveFile.exists()) return this;
        try {
            ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(saveFile));
            this.putAll((Profiles) inputStream.readObject());
            inputStream.close();
            return this;
        }
        catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
