package com.koolade446.mconsole.worker;

import com.koolade446.mconsole.Application;
import com.koolade446.mconsole.console.Sender;
import javafx.application.Platform;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

public class ConsoleWorker {
    public static final ExecutorService IO = Executors.newFixedThreadPool(3, r -> {
        Thread t = new Thread(r, "ConsoleWorker");
        t.setDaemon(true);
        return t;
    });

    private static final LinkedBlockingQueue<String> commandQueue = new LinkedBlockingQueue<>();

    public static void setDaemons(InputStream in, InputStream err, OutputStream commandSender) {
        IO.submit(()-> startLoggerAsync(in));
        IO.submit(()-> startErrorLoggerAsync(err));
        IO.submit(()-> startCommandSenderAsync(commandSender));
    }

    private static void startLoggerAsync(InputStream in) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        try {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                String finalLine = line;
                Platform.runLater(()-> Application.rootWindow.getConsole().log(Sender.MINECRAFT, finalLine));
            }
        } catch (IOException e) {
            Platform.runLater(()-> Application.rootWindow.getConsole().err("Exception occurred in Minecraft logger"));
        }

    }

    private static void startErrorLoggerAsync(InputStream err) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(err));

        try {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                String finalLine = line;
                Platform.runLater(()-> Application.rootWindow.getConsole().err(finalLine));
            }
        } catch (IOException e) {
            Platform.runLater(()-> Application.rootWindow.getConsole().err("Exception occurred in ConsoleWorker"));
        }
    }

    private static void startCommandSenderAsync(OutputStream commandSender) {
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(commandSender, StandardCharsets.UTF_8), true);

        try {
            while (!writer.checkError()) {
                String nextCommand = commandQueue.take();
                writer.println(nextCommand);
            }
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void sendCommand(String command) {
        commandQueue.offer(command);
    }

    public static void shutdown() {
        IO.shutdown();
    }
}
