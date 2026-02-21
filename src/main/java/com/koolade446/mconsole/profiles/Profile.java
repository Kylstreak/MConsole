package com.koolade446.mconsole.profiles;

import com.koolade446.mconsole.Application;
import com.koolade446.mconsole.api.APIAsync;
import com.koolade446.mconsole.api.centrojar.FetchJarRequest;
import com.koolade446.mconsole.configs.LocalConfig;
import com.koolade446.mconsole.console.Sender;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.MenuItem;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class Profile extends MenuItem implements Serializable {

    @Serial
    private static final long serialVersionUID = 6814877710283817953L;
    public String name;
    public Path location;
    public Path executable;
    public String type;
    public String version;
    public transient ExecutorService executor;
    public transient Process serverProcess;
    public transient PrintWriter outStream;
    public transient BufferedReader inStream;
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

        try {
            File eula = Paths.get(location.toString(), "eula.txt").toFile();
            if (!eula.exists()) {
                eula.createNewFile();
                FileOutputStream fileOutputStream = new FileOutputStream(eula);
                byte[] agreement = "eula=true".getBytes(StandardCharsets.UTF_8);
                fileOutputStream.write(agreement, 0, agreement.length);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public Profile load() {
        this.executor = Executors.newFixedThreadPool(1);
        Application.rootWindow.ramAmount.setText(config.get("ram-amount"));
        Application.rootWindow.ramTypeBox.setValue(config.get("ram-type"));
        Application.rootWindow.profileSelector.setText(name);
        Application.rootWindow.getConsole().clearConsole();
        return this;

    }

    public void unload() {
        try {
            this.executor.shutdown();
            if (isRunning()) {
                stopServer();
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) serverProcess.destroyForcibly();
            }
            if (inStream != null) inStream.close();
            if (outStream != null) outStream.close();

            config.put("ram-amount", Application.rootWindow.ramAmount.getText());
            config.put("ram-type", Application.rootWindow.ramTypeBox.getValue());
            config.save();
        }
        catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void updateSoftware(String type, String version) {
        config.put("type", type);
        this.type = type;
        this.version = version;
        FetchJarRequest request = new FetchJarRequest(APIAsync.ENDPOINTS.getByType(type), version);
        request.send().thenAccept(resp -> resp.writeToFile(this.location.toString() + "/server.jar"));
        this.executable = type.equals("forge") ? Paths.get(this.location.toString(), "run.bat") : Paths.get(this.location.toString(), "server.jar");
    }

    public void startServer(int ramAmount, String ramType) {
        Runnable serverProc = ()-> {
            //Future proofing
            List<String> args = new ArrayList<>();
            args.add("-Xmx%s%s".formatted(ramAmount, ramType));
            args.add("-Xms1G");

            try {
                if (this.type.equals("forge")) {
                    Platform.runLater(() -> Application.rootWindow.getConsole().log(Sender.INFO, "Updating forge run scripts"));
                    Path jvmArgs = Paths.get(this.location.toString(), "user_jvm_args.txt");
                    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(jvmArgs.toFile())));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.contains("-Xmx")) line = args.get(0);
                        sb.append(line).append("\n");
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
                ProcessBuilder builder = this.type.equals("forge") ? new ProcessBuilder("cmd", "/c", executable.toString()) : new ProcessBuilder("java", "-jar", args.get(0), args.get(1), executable.toString(), "nogui");
                builder.directory(this.location.toFile());

                serverProcess = builder.start();

                this.outStream = new PrintWriter(new OutputStreamWriter(serverProcess.getOutputStream()));
                this.inStream = new BufferedReader(new InputStreamReader(serverProcess.getInputStream()));

                String line;
                while (serverProcess.isAlive()) {
                    if ((line = inStream.readLine()) != null) {
                        String finalLine = line;
                        Platform.runLater(()-> Application.rootWindow.getConsole().log(Sender.MINECRAFT, finalLine));
                    }
                }

            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
        this.executor.execute(serverProc);
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
        if (isRunning()) {
            this.outStream.println(command);
        }
        else {
            Application.rootWindow.getConsole().err("No server is running");
        }
    }

    public void changeToProfile(ActionEvent event) {
        Application.rootWindow.loadNewProfile(this);
    }

    @Override
    public String toString() {
        String string = """
                name: %s
                location: %s
                executable: %S
                type: %s
                version: %s
                executor: %s
                serverProcess: %s
                outStream: %s
                inStream: %s
                config: %s
                running: %s
                """.formatted(
                        name,
                        location,
                        executable,
                        type,
                        version,
                        executor,
                        serverProcess,
                        outStream,
                        inStream,
                        config,
                        isRunning()
                    );
        return string;
    }

    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeUTF(name);
        out.writeUTF(location.toString());
        out.writeUTF(executable.toString());
        out.writeUTF(type.toString());
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
        return this.serverProcess != null && this.serverProcess.isAlive();
    }
}