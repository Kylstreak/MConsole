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

import java.nio.file.Path;

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

    public void updateVersions(ActionEvent actionEvent) {
        softwareVersionSelector.getItems().clear();
        softwareVersionSelector.setValue(null);

        Endpoint endpoint = APIAsync.ENDPOINTS.getByType(softwareTypeSelector.getValue().toString());
        FetchTypesRequest request = new FetchTypesRequest(endpoint.category(), endpoint.type());
        request.send().thenAccept(jarInfo -> Platform.runLater(() -> {
            jarInfo.getTypes().forEach(type -> softwareVersionSelector.getItems().add(type.version));
        })).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });
    }

    public void create(ActionEvent actionEvent) {
        Profile profile = new Profile().create(nameBox.getText(), directory.toString(), softwareTypeSelector.getValue().toString(), (String) softwareVersionSelector.getValue());
        Application.rootWindow.loadNewProfile(profile);
        Application.rootWindow.getProfiles().put(profile.name, profile);
        Application.rootWindow.profileSelector.getItems().add(profile);
        Application.rootWindow.profileSelector.setText(profile.name);
        panel.getScene().getWindow().hide();
    }

    public void cancel(ActionEvent actionEvent) {
        panel.getScene().getWindow().hide();
    }

    public void chooseFile(ActionEvent actionEvent) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choose new directory");
        directory = directoryChooser.showDialog(panel.getScene().getWindow()).toPath();
        locationBox.setText(directory.toString());
    }

}
