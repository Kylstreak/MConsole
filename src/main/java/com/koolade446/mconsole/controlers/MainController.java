package com.koolade446.mconsole.controlers;

import com.koolade446.mconsole.Application;
import com.koolade446.mconsole.api.APIAsync;
import com.koolade446.mconsole.configs.GlobalConfig;
import com.koolade446.mconsole.console.Console;
import com.koolade446.mconsole.profiles.Profile;
import com.koolade446.mconsole.profiles.Profiles;
import com.koolade446.mconsole.worker.ConsoleWorker;
import com.koolade446.mconsole.worker.ServerWorker;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.util.Objects;

public class MainController {

    //FXML GUI variables
    @FXML
    public Button startStopButton;
    @FXML
    public Button killButton;
    @FXML
    public MenuButton profileSelector;
    @FXML
    public MenuItem createProfileButton;
    @FXML
    public StackPane consolePane;
    public ScrollPane consoleScrollPane;
    @FXML
    TextField commandBox;
    @FXML
    Button ePropButton;
    @FXML
    Pane mainPane;
    @FXML
    public TextField ramAmount;
    @FXML
    public ComboBox<String> ramTypeBox;

    public GlobalConfig globalConfig;

    private Console console;
    public Profiles profiles;
    public Profile activeProfile;

    //Initialize our GUI
    public void initialize() {
        consoleScrollPane.vvalueProperty().bind(consolePane.heightProperty());
        console = new Console(consolePane);
        globalConfig = new GlobalConfig("mconsole\\config.dat");

        profiles = new Profiles().load();
        if (globalConfig.get("profile") != null) {
            activeProfile = profiles.get(globalConfig.get("profile"));
            activeProfile.load();
        }
        profileSelector.getItems().addAll(profiles.values());

        startStopButton.setStyle("-fx-background-image: url('power.png')");
        killButton.setStyle("-fx-background-image: url('kill.png')");

        ramTypeBox.getItems().addAll("G", "M");
        APIAsync.collectEndpoints();
        if (!globalConfig.get("eula-agreement").equalsIgnoreCase("true")) showEula();

    }

    public void postInit(WindowEvent ignoredEvent) {
        if (globalConfig.get("active-profile") != null) activeProfile = profiles.get(globalConfig.get("active-profile")).load();

    }

    //Called when the "Edit Properties" button is pressed
    public void editProperties(ActionEvent ignoredActionEvent) {
        try {
            Parent root;
            root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("properties-window.fxml")));
            Stage stage = new Stage();
            stage.setTitle("Edit Server Properties");
            stage.getIcons().add(new Image(Objects.requireNonNull(Application.class.getClassLoader().getResourceAsStream("app-icon.jpg"))));
            Scene scene = new Scene(root, 604, 326);
            stage.setScene(scene);
            stage.show();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //Called when the Start/Stop button is pressed
    public void togglePowerState(ActionEvent ignoredActionEvent) {
        if (!activeProfile.isRunning()) {
            activeProfile.startServer();
        }
        else {
            activeProfile.stopServer();
        }
    }

    //For the last time, killing the server isn't a crime, it's not a living thing
    public void killServer(ActionEvent ignoredActionEvent) {
        activeProfile.killServer();
    }

    public void sendCommand(ActionEvent ignoredEvent) {
        ConsoleWorker.sendCommand(commandBox.getText());
        commandBox.setText("");
    }

    public void onKeyPress(KeyEvent keyEvent) {
        if (keyEvent.getCode().equals(KeyCode.ENTER)) {
            ConsoleWorker.sendCommand(commandBox.getText());
            commandBox.setText("");
        }
    }

    private void showEula() {
        try {
            Parent root;
            root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("eula.fxml")));
            Stage stage = new Stage();
            stage.setTitle("EULA Agreement");
            stage.getIcons().add(new Image(Objects.requireNonNull(Application.class.getClassLoader().getResourceAsStream("app-icon.jpg"))));
            Scene scene = new Scene(root, 584, 549);
            stage.setScene(scene);

            stage.setAlwaysOnTop(true);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setResizable(false);
            stage.setOnCloseRequest(Event::consume);
            mainPane.setDisable(true);
            stage.setOnHidden(event -> mainPane.setDisable(false));

            stage.show();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void createNewProfile(ActionEvent ignoredActionEvent) {
        try {
            Parent root;
            root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("create-profile.fxml")));
            Stage stage = new Stage();
            stage.setTitle("Create new server profile");
            stage.getIcons().add(new Image(Objects.requireNonNull(Application.class.getClassLoader().getResourceAsStream("app-icon.jpg"))));
            Scene scene = new Scene(root, 475, 249);
            stage.setScene(scene);
            stage.show();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void loadNewProfile(Profile profile) {
        if (activeProfile != null) activeProfile.unload();
        activeProfile = profile.load();
    }

    //Make sure everything shuts down nice and cleanly leaving no threads running (IM LOOKING AT YOU MINECRAFT)
    public void runExitTasks() {
        if(activeProfile != null) {
            globalConfig.put("active-profile", activeProfile.name);
            activeProfile.unload();
        }
        globalConfig.save();
        profiles.save();
        APIAsync.shutdown();
        ConsoleWorker.shutdown();
        ServerWorker.shutdown();

        Platform.exit();
    }

    public Console getConsole() {
        return console;
    }

    public GlobalConfig getGlobalConfig() {
        return globalConfig;
    }

    public Profiles getProfiles() {
        return profiles;
    }

    public Profile getActiveProfile() {
        return activeProfile;
    }

    public void setActiveProfile(Profile activeProfile) {
        this.activeProfile = activeProfile;
    }

    public void editProfile(ActionEvent ignoredActionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("create-profile.fxml"));
            Parent root = loader.load();
            ProfileController controller = loader.getController();
            controller.updateProfile(activeProfile);
            Stage stage = new Stage();
            stage.setTitle("Edit server profile");
            stage.getIcons().add(new Image(Objects.requireNonNull(Application.class.getClassLoader().getResourceAsStream("app-icon.jpg"))));
            Scene scene = new Scene(root, 475, 249);
            stage.setScene(scene);
            stage.show();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}