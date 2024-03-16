package com.koolade446.mconsole;

import com.koolade446.mconsole.controlers.MainController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;

public class Application extends javafx.application.Application {

    public static MainController rootWindow;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("console-window.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1030, 570);
        stage.setTitle("Server control panel");
        stage.getIcons().add(new Image(Application.class.getClassLoader().getResourceAsStream("app-icon.jpg")));
        stage.setScene(scene);
        stage.setResizable(false);

        rootWindow = fxmlLoader.getController();

        stage.getScene().getWindow().addEventFilter(WindowEvent.WINDOW_SHOWN, rootWindow::postInit);

        stage.show();

        stage.getScene().getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, this::onWindowClose);}

    public static void start() {
        launch();
    }

    public void onWindowClose(WindowEvent event) {
        rootWindow.runExitTasks();
        event.consume();
    }
}