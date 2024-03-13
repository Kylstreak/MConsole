package com.koolade446.mconsole.controlers;

import com.koolade446.mconsole.Application;
import com.koolade446.mconsole.api.API;
import com.koolade446.mconsole.configs.GlobalConfig;
import com.koolade446.mconsole.console.Console;
import com.koolade446.mconsole.profiles.Profile;
import com.koolade446.mconsole.profiles.Profiles;
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
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.nio.file.Paths;

public class MainController {

    //FXML GUI variables
    @FXML
    public TextArea visualConsole;
    @FXML
    public Button startStopButton;
    @FXML
    public Button killButton;
    public MenuButton profileSelector;
    public MenuItem createProfileButton;
    @FXML
    TextField commandBox;
    @FXML
    Button ePropButton;
    @FXML
    Pane mainPane;
    @FXML
    TextField ramAmount;
    @FXML
    ComboBox<String> ramTypeBox;

    public final GlobalConfig globalConfig;

    private final Console console;
    public final API API;
    public final Profiles profiles;
    public Profile activeProfile;


    //Initializes our variables
    public MainController() {
        console = new Console(visualConsole);
        API = new API();
        globalConfig = new GlobalConfig(Paths.get("mconsole", "config.dat"));
        profiles = new Profiles().load();
        if (globalConfig.get("profile") != null) {
            activeProfile = profiles.get(globalConfig.get("profile"));
            activeProfile.load();
        }
    }

    //Initialize our GUI
    public void initialize() {
        startStopButton.setStyle("-fx-background-image: url('power.png')");
        killButton.setStyle("-fx-background-image: url('kill.png')");

        ramTypeBox.getItems().addAll("G", "M");
        if (!globalConfig.get("eula-agreement").equalsIgnoreCase("true")) showEula();
    }

    //Called when the "Edit Properties" button is pressed
    public void editProperties(ActionEvent actionEvent) {
        try {
            Parent root;
            root = FXMLLoader.load(getClass().getResource("properties-window.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Edit Server Properties");
            stage.getIcons().add(new Image(Application.class.getClassLoader().getResourceAsStream("app-icon.jpg")));
            Scene scene = new Scene(root, 604, 326);
            stage.setScene(scene);
            stage.show();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //Called when the Start/Stop button is pressed
    public void togglePowerState(ActionEvent actionEvent) {
        if (!activeProfile.running) {
            activeProfile.startServer(Integer.parseInt(ramAmount.getText()), ramTypeBox.getValue());
        }
        else {
            activeProfile.stopServer();
        }
    }

    //For the last time, killing the server isn't a crime, it's not a living thing
    public void killServer(ActionEvent actionEvent) {
        activeProfile.killServer();
    }

    public void sendCommand(ActionEvent event) {
        activeProfile.sendCommand(commandBox.getText());
        commandBox.setText("");
    }

    public void onKeyPress(KeyEvent keyEvent) {
        if (keyEvent.getCode().equals(KeyCode.ENTER)) {
            activeProfile.sendCommand(commandBox.getText());
            commandBox.setText("");
        }
    }

    private void showEula() {
        try {
            Parent root;
            root = FXMLLoader.load(getClass().getResource("eula.fxml"));
            Stage stage = new Stage();
            stage.setTitle("EULA Agreement");
            stage.getIcons().add(new Image(Application.class.getClassLoader().getResourceAsStream("app-icon.jpg")));
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

    public void createNewProfile(ActionEvent actionEvent) {
        try {
            Parent root;
            root = FXMLLoader.load(getClass().getResource("create-profile.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Create new server profile");
            stage.getIcons().add(new Image(Application.class.getClassLoader().getResourceAsStream("app-icon.jpg")));
            Scene scene = new Scene(root, 475, 249);
            stage.setScene(scene);
            stage.show();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void loadNewProfile(Profile profile) {
        activeProfile.unload();
        activeProfile = profile.load();
    }

    //Make sure everything shuts down nice and cleanly leaving no threads running (IM LOOKING AT YOU MINECRAFT)
    public void runExitTasks() {
        activeProfile.unload();
        globalConfig.save();
    }

    public Console getConsole() {
        return console;
    }

    public com.koolade446.mconsole.api.API getAPI() {
        return API;
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
}