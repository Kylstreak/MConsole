package com.koolade446.mconsole.controlers;

import com.koolade446.mconsole.Application;
import com.koolade446.mconsole.api.APIAsync;
import com.koolade446.mconsole.api.Endpoint;
import com.koolade446.mconsole.api.centrojar.FetchTypesRequest;
import com.koolade446.mconsole.profiles.Profile;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;


@SuppressWarnings({"rawtypes", "unchecked", "CallToPrintStackTrace", "ResultOfMethodCallIgnored"})
public class ProfileController {
    @FXML
    public TextField locationBox;
    @FXML
    public TextField nameBox;
    @FXML
    public ComboBox softwareTypeSelector;
    @FXML
    public ComboBox softwareVersionSelector;
    @FXML
    public Button createButton;
    @FXML
    public Button cancelButton;
    public Button openFileButton;
    @FXML
    public Pane panel;

    private Path directory;

    public void initialize() {
        openFileButton.setStyle("-fx-background-image: url('folder.png')");
        for (Endpoint endpoint : APIAsync.ENDPOINTS) {
            softwareTypeSelector.getItems().add(endpoint.type());
        }
        locationBox.setEditable(false);
    }

    public void updateVersions(ActionEvent ignoredActionEvent) {
        softwareVersionSelector.getItems().clear();
        softwareVersionSelector.setValue(null);

        Endpoint endpoint = APIAsync.ENDPOINTS.getByType(softwareTypeSelector.getValue().toString());
        FetchTypesRequest request = new FetchTypesRequest(endpoint.category(), endpoint.type());
        request.send().thenAccept(jarInfo -> Platform.runLater(() -> jarInfo.getTypes().forEach(type -> softwareVersionSelector.getItems().add(type.version)))).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });
    }

    public void create(ActionEvent ignoredActionEvent) {
        Profile profile = new Profile().create(nameBox.getText(), directory.toString(), softwareTypeSelector.getValue().toString(), (String) softwareVersionSelector.getValue());
        Application.rootWindow.loadNewProfile(profile);
        Application.rootWindow.getProfiles().put(profile.name, profile);
        Application.rootWindow.profileSelector.getItems().add(profile);
        Application.rootWindow.profileSelector.setText(profile.name);
        panel.getScene().getWindow().hide();

    }

    public void update(ActionEvent event) {
        Profile profile = Application.rootWindow.activeProfile;

        boolean softwareChanged = !Objects.equals(profile.type, softwareTypeSelector.getValue().toString()) || !Objects.equals(profile.version, softwareVersionSelector.getValue());

        profile.name = nameBox.getText();
        profile.location = directory;
        profile.type = softwareTypeSelector.getValue().toString();
        profile.version = (String) softwareVersionSelector.getValue();

        if (softwareChanged) {
            try (Stream<Path> files = Files.walk(profile.location)) {
                List<String> excepted = Arrays.asList("mods",
                        "mconsole-data.dat",
                        "server.properties",
                        profile.location.getFileName().toString());
                files.forEach(file -> {
                    if (!excepted.contains(file.getFileName().toString())) {
                        deleteFileRecursively(file.toFile());
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            profile.updateSoftware(profile.type, profile.version);
        }

        panel.getScene().getWindow().hide();
    }

    public void updateProfile(Profile profile) {
        nameBox.setText(profile.name);
        directory = profile.location;
        locationBox.setText(profile.location.toString());
        softwareTypeSelector.setValue(profile.type);
        softwareVersionSelector.setValue(profile.version);

        createButton.setOnAction(this::update);
    }

    public void cancel(ActionEvent ignoredActionEvent) {
        panel.getScene().getWindow().hide();
    }

    public void chooseFile(ActionEvent ignoredActionEvent) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choose new directory");
        directory = directoryChooser.showDialog(panel.getScene().getWindow()).toPath();
        locationBox.setText(directory.toString());
    }

    // the original name was private void nukeEverything
    private void deleteFileRecursively(File file) {
        if (file.isDirectory()) {
            for (File child : Objects.requireNonNull(file.listFiles())) {
                deleteFileRecursively(child);
            }
        }
        file.delete();
    }

}
