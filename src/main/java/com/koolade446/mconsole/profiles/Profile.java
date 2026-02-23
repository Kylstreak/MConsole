package com.koolade446.mconsole.profiles;

import com.koolade446.mconsole.Application;
import com.koolade446.mconsole.api.APIAsync;
import com.koolade446.mconsole.api.centrojar.FetchJarRequest;
import com.koolade446.mconsole.configs.LocalConfig;
import com.koolade446.mconsole.console.Sender;
import com.koolade446.mconsole.worker.ServerWorker;
import javafx.event.ActionEvent;
import javafx.scene.control.MenuItem;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


@SuppressWarnings("ResultOfMethodCallIgnored")
public class Profile extends MenuItem implements Serializable {

    @Serial
    private static final long serialVersionUID = 6814877710283817953L;
    public String name;
    public Path location;
    public Path executable;
    public String type;
    public String version;
    public transient LocalConfig config;

    public Profile create(String name, String location, String type, String version) {
        this.name = name;
        this.type = type;
        this.version = version;
        this.location = Path.of(location);
        this.config = new LocalConfig(Paths.get(this.location.toString(), "mconsole-data.dat").toString());
        Application.rootWindow.profiles.put(this.name, this);

        this.setOnAction(this::changeToProfile);
        this.setText(name);


        updateSoftware(type, version);

        return this;
    }

    public Profile load() {

        // Fallback to default values if a save error happens
        String ramAmount = !Objects.equals(config.get("ram-amount"), "null") ? config.get("ram-amount") : "2048";
        String ramType = !Objects.equals(config.get("ram-type"), "null") ? config.get("ram-type") : "M";

        Application.rootWindow.ramAmount.setText(ramAmount);
        Application.rootWindow.ramTypeBox.setValue(ramType);

        Application.rootWindow.profileSelector.setText(name);
        Application.rootWindow.getConsole().clearConsole();
        return this;

    }



    public void updateSoftware(String type, String version) {
        config.put("type", type);
        this.type = type;
        this.version = version;
        this.executable = type.equals("forge") ? Paths.get(this.location.toString(), "run.bat") : Paths.get(this.location.toString(), "server.jar");
        Application.rootWindow.getConsole().log(Sender.DOWNLOAD, "Downloading %s %s...".formatted(type, version));
        FetchJarRequest request = new FetchJarRequest(APIAsync.ENDPOINTS.getByType(type), version);
        request.send().thenAccept(resp -> {
            if (type.equals("forge")) resp.generateForgeScripts(this.location.toString());
            else resp.writeToFile(this.location.toString() + "/server.jar");
        });
    }

    public void startServer() {
        Path eula = location.resolve("eula.txt");
        String eulaText = "eula=true";
        try {
            Files.writeString(eula, eulaText);
        }
        catch (IOException e) {
            Application.rootWindow.getConsole().log(Sender.ERROR, "Failed to write eula.txt");
        }
        ServerWorker.startServer(this);
    }

    public void stopServer() {
        ServerWorker.stopServerSafe();
    }

    public void unload() {
        config.put("ram-amount", Application.rootWindow.ramAmount.getText());
        config.put("ram-type", Application.rootWindow.ramTypeBox.getValue());
        config.save();
    }

    public void killServer() {
        ServerWorker.killServer();
    }

    public void changeToProfile(ActionEvent event) {
        Application.rootWindow.loadNewProfile(this);
    }

    @Override
    public String toString() {
        return """
                name: %s
                location: %s
                executable: %S
                type: %s
                version: %s
                config: %s
                running: %s
                """.formatted(
                        name,
                        location,
                        executable,
                        type,
                        version,
                        config,
                        isRunning()
                    );
    }

    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeUTF(name);
        out.writeUTF(location.toString());
        out.writeUTF(executable.toString());
        out.writeUTF(type);
        out.writeUTF(version);

    }

    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.name = in.readUTF();
        this.location = Paths.get(in.readUTF());
        this.executable = Paths.get(in.readUTF());
        this.type = in.readUTF();
        this.version = in.readUTF();

        this.config = new LocalConfig(Paths.get(this.location.toString(), "mconsole-data.dat").toString());
        this.executable = type.equals("forge") ? Paths.get(location.toString(), "run.bat") : Paths.get(location.toString(), "server.jar");

        this.setOnAction(this::changeToProfile);
        this.setText(name);
    }

    public boolean isRunning() {
        return Application.rootWindow.activeProfile == this && ServerWorker.isServerRunning();
    }
}