package com.koolade446.mconsole.profiles;

import com.koolade446.mconsole.Application;
import com.koolade446.mconsole.api.SoftwareType;
import com.koolade446.mconsole.configs.LocalConfig;
import com.koolade446.mconsole.console.Sender;
import javafx.application.Platform;
import javafx.scene.control.MenuItem;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class Profile extends MenuItem implements Serializable {

    public final String name;
    public final Path location;
    public Path executable;
    public SoftwareType type;
    public String version;
    public ExecutorService executor;
    public Process serverProcess;
    public PrintWriter outStream;
    public InputStreamReader inStream;
    public LocalConfig config;
    public boolean running;

    public Profile(String name, String location, SoftwareType type, String version) {
        this.name = name;
        this.location = Path.of(location);
        this.type = type;
        this.version = version;
        this.config = new LocalConfig(Paths.get(this.location.toString(), "mconsole-data.dat").toString());
        this.running = false;
        Application.rootWindow.profiles.put(this.name, this);
    }

    public Profile load() {
        this.executor = Executors.newFixedThreadPool(1);
        return this;

    }

    public boolean unload() {
        try {
            this.executor.shutdown();
            if (running) {
                stopServer();
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) serverProcess.destroyForcibly();
            }
            if (inStream != null) inStream.close();
            if (outStream != null) outStream.close();
            config.save();
            return true;
        }
        catch (IOException | InterruptedException e) {
            return false;
        }
    }

    public Profile create() {
        updateSoftware(type, version);
        //TODO: agree to eula
        return this;
    }

    public void updateSoftware(SoftwareType type, String version) {
        config.put("type", type.toString());
        this.type = type;
        this.version = version;
        Application.rootWindow.API.downloadAndInstallSoftware(this.location, type, version);
        this.executable = Objects.equals(config.get("type"), "forge") ? Paths.get(this.location.toString(), "run.bat") : Paths.get(this.location.toString(), "server.jar");
    }

    public void startServer(int ramAmount, String ramType) {
        Application.rootWindow.getConsole().clearConsole();

        Runnable serverProc = ()-> {
            //Future proofing
            List<String> args = new ArrayList<>();
            args.add("-Xmx%s%s".formatted(ramAmount, ramType));
            args.add("-Xms1G");

            try {
                if (this.type.equals(SoftwareType.FORGE)) {
                    Platform.runLater(() -> Application.rootWindow.getConsole().log(Sender.INFO, "Updating forge run scripts"));
                    Path jvmArgs = Paths.get(this.location.toString(), "user_jvm_args.txt");
                    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(jvmArgs.toFile())));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.contains("-Xmx")) line = args.get(0);
                        sb.append(line);
                    }
                    reader.close();
                    byte[] updatedFileBytes = sb.toString().getBytes();

                    FileOutputStream fileWriter = new FileOutputStream(jvmArgs.toFile());
                    fileWriter.write(updatedFileBytes, 0, updatedFileBytes.length);
                    fileWriter.flush();
                    fileWriter.close();
                    Platform.runLater(()-> Application.rootWindow.getConsole().log(Sender.INFO, "Forge run scripts updated"));
                }
                Platform.runLater(()-> Application.rootWindow.getConsole().log(Sender.MINECRAFT, "Starting server"));
                ProcessBuilder builder = this.type.equals(SoftwareType.FORGE) ? new ProcessBuilder("cmd", "/c", executable.toString()) : new ProcessBuilder("java", "-jar", args.get(0), args.get(1), executable.toString(), "nogui");
                builder.directory(this.location.toFile());

                serverProcess = builder.start();
                this.outStream = new PrintWriter(new OutputStreamWriter(serverProcess.getOutputStream()));
                this.inStream = new InputStreamReader(serverProcess.getInputStream());
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
        this.executor.execute(serverProc);
        this.running = true;
    }

    public void stopServer() {
        try {
            outStream.write("stop");
            outStream.flush();
            outStream.close();
            outStream = null;
            inStream.close();
            inStream = null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void killServer() {
        if (serverProcess.isAlive()) {
            serverProcess.destroyForcibly();
            Application.rootWindow.getConsole().log(Sender.WARN, "Server closed forcibly");
        }
    }

    public void sendCommand(String command) {
        this.outStream.println(command);
    }
}