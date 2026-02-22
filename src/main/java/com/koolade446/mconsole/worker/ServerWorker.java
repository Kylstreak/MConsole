package com.koolade446.mconsole.worker;

import com.koolade446.mconsole.Application;
import com.koolade446.mconsole.console.Sender;
import com.koolade446.mconsole.profiles.Profile;
import javafx.application.Platform;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ServerWorker {
    public static final ExecutorService IO = Executors.newFixedThreadPool(1, r -> {
        Thread t = new Thread(r, "ServerWorker");
        t.setDaemon(true);
        return t;
    });

    private static volatile Process process;

    public static void startServer(Profile profile) {
        if (isServerRunning()) {
            Application.rootWindow.getConsole().err("Server is already running");
            return;
        }
        IO.submit(()-> startServerAsync(profile));
    }

    public static void stopServerSafe() {
        if (isServerRunning()) return;
        ConsoleWorker.sendCommand("stop");

        CompletableFuture.runAsync(()-> {
            try {
                process.waitFor(60, TimeUnit.SECONDS);
                process.destroyForcibly();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static void killServer() {
        if (isServerRunning()) process.destroyForcibly();
    }

    private static void startServerAsync(Profile profile, String... args) {
        try {
            List<String> argsList = new ArrayList<>();
            // Forge has to be special
            if (profile.type.equals("forge")) {
                Platform.runLater(() -> Application.rootWindow.getConsole().log(Sender.DOWNLOAD, "Generating forge scripts..."));
                Path jvmArgs = Paths.get(profile.location.toString(), "user_jvm_args.txt");

                if (!Files.exists(jvmArgs)) Platform.runLater(()-> Application.rootWindow.getConsole().err("Missing required forge scripts, please reinstall or update server software"));

                String fileData = Files.readString(jvmArgs);
                fileData = fileData.replaceAll("(?m)^# -Xmx.*$", "-Xmx%s%s".formatted(profile.config.get("ram-amount"), profile.config.get("ram-type")));

                Files.writeString(jvmArgs, fileData);

                Platform.runLater(() -> Application.rootWindow.getConsole().log(Sender.DOWNLOAD, "Forge scripts generated"));

                argsList.add("cmd");
                argsList.add("/c");
                argsList.add(profile.executable.toString());
            }
            else {
                argsList.add("java");
                argsList.add("-jar");
                argsList.addAll(Arrays.asList(args));
                argsList.add(profile.executable.toString());
                argsList.add("nogui");
            }

            ProcessBuilder builder = new ProcessBuilder(argsList);
            builder.directory(profile.location.toFile());
            process = builder.start();

            ConsoleWorker.setDaemons(process.getInputStream(), process.getErrorStream(), process.getOutputStream());

            process.waitFor();

        }
        catch (IOException | InterruptedException e) {
            if (e.getClass().equals(InterruptedException.class)) {
                Platform.runLater(() -> Application.rootWindow.getConsole().err("Server was forcibly stopped"));
                Thread.currentThread().interrupt();
            }
            else throw new RuntimeException(e);
        }
    }

    public static boolean isServerRunning() {
        return process != null && process.isAlive();
    }

    public static void shutdown() {
        IO.shutdown();
    }
}
