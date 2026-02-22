package com.koolade446.mconsole.api.centrojar;

import com.koolade446.mconsole.Application;
import com.koolade446.mconsole.api.APIAsync;
import com.koolade446.mconsole.api.Endpoint;
import com.koolade446.mconsole.console.Sender;
import javafx.application.Platform;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

public class FetchJarRequest {
    private final String endpoint;

    public FetchJarRequest(Endpoint endpoint, String version) {
        this.endpoint = Endpoint.parseEndpoint("fetchJar", endpoint) + "/" + version;
    }

    public CompletableFuture<Response> send() {
        return APIAsync.sendFileRequest(endpoint)
                .whenComplete((resp, error)-> Platform.runLater(()-> Application.rootWindow.getConsole().log(Sender.DOWNLOAD, "Downloaded complete")))
                .thenApply(Response::new);
    }

    public static class Response {
        private final byte[] fileBytes;

        public Response(byte[] fileBytes) {
            this.fileBytes = fileBytes;
        }

        public void generateForgeScripts(String location) {
            try {
                Platform.runLater(() -> Application.rootWindow.getConsole().log(Sender.INFO, "Generating forge scripts..."));
                Path path = Paths.get(location);
                Path installer = path.resolve("installer.jar");
                Files.write(installer, fileBytes);

                ProcessBuilder builder = new ProcessBuilder("java", "-jar", installer.toString(), "--installServer");
                builder.directory(path.toFile());
                BufferedReader br = new BufferedReader(new InputStreamReader(builder.start().getInputStream()));
                String line;
                while ((line = br.readLine()) != null) {
                    String finalLine = line;
                    Platform.runLater(()-> Application.rootWindow.getConsole().log(Sender.DOWNLOAD, finalLine));
                }


                Path runBat = path.resolve("run.bat");

                br = new BufferedReader(new InputStreamReader(new FileInputStream(runBat.toFile())));
                StringBuilder sb = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    if (line.contains("java")) line = line.replace("%*", "nogui %*");
                    if (line.contains("pause")) line = line.replace("pause", "");
                    sb.append(line).append("\n");
                }
                br.close();

                Files.write(runBat, sb.toString().getBytes());

                Files.deleteIfExists(installer);

                Platform.runLater(()-> Application.rootWindow.getConsole().log(Sender.INFO, "Forge run scripts generated"));

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public void writeToFile(String location) {
            try {
                Files.write(Paths.get(location), fileBytes);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
