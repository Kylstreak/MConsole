package com.koolade446.mconsole.controlers;

import com.koolade446.mconsole.Application;
import com.koolade446.mconsole.api.SoftwareType;
import com.koolade446.mconsole.profiles.Profile;
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
        for (SoftwareType type : SoftwareType.values()) {
            softwareTypeSelector.getItems().add(type.toString());
        }
        locationBox.setEditable(false);
    }

    public void updateVersions(ActionEvent actionEvent) {
        softwareVersionSelector.getItems().clear();
        softwareVersionSelector.setValue(null);
        softwareVersionSelector.getItems().addAll(Application.rootWindow.getAPI().getVersions(SoftwareType.valueOf((String) softwareTypeSelector.getValue())));
    }

    public void create(ActionEvent actionEvent) {
        Profile profile = new Profile().create(nameBox.getText(), directory.toString(), SoftwareType.valueOf((String) softwareTypeSelector.getValue()), (String) softwareVersionSelector.getValue());
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
