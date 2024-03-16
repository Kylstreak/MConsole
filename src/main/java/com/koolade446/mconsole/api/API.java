package com.koolade446.mconsole.api;

import com.koolade446.mconsole.Application;
import com.koolade446.mconsole.console.Sender;
import javafx.application.Platform;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class API {
    private final Map<SoftwareType, List<String>> versions;
    private final ExecutorService apiThread;

    public API() {
        apiThread = Executors.newSingleThreadExecutor();
        Map<SoftwareType, List<String>> options = new HashMap<>();
        Runnable downloadTask = () -> {
            try {
                for (SoftwareType st : SoftwareType.values()) {
                    URL url = new URL("https://serverjars.com/api/fetchAll/" + st.getEndpoint());
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    con.connect();

                    JSONObject response = new JSONObject(new BufferedReader(new InputStreamReader(con.getInputStream())).lines().collect(Collectors.joining()));

                    con.disconnect();

                    List<String> versions = new ArrayList<>();
                    for (Object obj : response.getJSONArray("response")) {
                        JSONObject jsonObj = (JSONObject) obj;
                        versions.add(jsonObj.getString("version"));
                    }

                    options.put(st, versions);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        apiThread.execute(downloadTask);
        this.versions = options;
    }

    public List<String> getVersions(SoftwareType type) {
        return versions.get(type);
    }

    public void downloadAndInstallSoftware(Path path, SoftwareType type, String version) {
        Runnable downloadServerProc = () -> {
            try {
                URL url = new URL("https://serverjars.com/api/fetchJar/" + type.getEndpoint() + "/" + version);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                Platform.runLater(() -> Application.rootWindow.getConsole().log(Sender.DOWNLOAD, "Connecting to servers..."));
                con.connect();

                Platform.runLater(() -> Application.rootWindow.getConsole().log(Sender.DOWNLOAD, "Downloading server software..."));
                InputStream is = con.getInputStream();
                byte[] jarFileData = is.readAllBytes();

                //Forge requires specialized build scripts
                if (type.equals(SoftwareType.FORGE)) {
                    FileOutputStream fos = new FileOutputStream(Paths.get(path.toString(), "installer.jar").toFile());
                    fos.write(jarFileData, 0, jarFileData.length);
                    fos.flush();
                    fos.close();

                    Platform.runLater(() -> Application.rootWindow.getConsole().log(Sender.DOWNLOAD, "Creating forge build scripts..."));

                    ProcessBuilder procBuilder = new ProcessBuilder("java", "-jar", "installer.jar", "--installServer");
                    procBuilder.directory(path.toFile());
                    Process forgeInstallerProc = procBuilder.start();
                    BufferedReader br = new BufferedReader(new InputStreamReader(forgeInstallerProc.getInputStream()));
                    String line;
                    while (forgeInstallerProc.isAlive()) {
                        if ((line = br.readLine()) != null) {
                            String finalLine = line;
                            Platform.runLater(() -> Application.rootWindow.getConsole().log(Sender.DOWNLOAD, finalLine));
                        }
                    }
                    br.close();

                    Path runBat = Paths.get(path.toString(), "run.bat");
                    br = new BufferedReader(new InputStreamReader(new FileInputStream(runBat.toFile())));
                    StringBuilder sb = new StringBuilder();
                    while ((line = br.readLine()) != null) {
                        if (line.contains("java")) line = line.replace("%*", "nogui %*");
                        if (line.contains("pause")) line = line.replace("pause", "");
                        sb.append(line).append("\n");
                    }
                    br.close();

                    fos = new FileOutputStream(runBat.toFile());
                    fos.write(sb.toString().getBytes(StandardCharsets.UTF_8), 0, sb.toString().getBytes().length);

                    Platform.runLater(() -> Application.rootWindow.getConsole().log(Sender.INFO, "Successfully installed Forge build scripts"));
                    fos.flush();
                    fos.close();
                } else {
                    FileOutputStream fileWriter = new FileOutputStream(Paths.get(path.toString(), "server.jar").toFile());
                    fileWriter.write(jarFileData, 0, jarFileData.length);
                    fileWriter.flush();
                    fileWriter.close();
                }
                Platform.runLater(() -> Application.rootWindow.getConsole().log(Sender.INFO, "Updated server to %s %s".formatted(type.toString(), version)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
        apiThread.execute(downloadServerProc);
    }

    public void shutdown() {
        apiThread.shutdown();
    }
}
