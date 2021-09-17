package com.github.nikitakuchur.automata;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Objects;

public class Main extends Application {

    public static final String TITLE = "Game of Life";

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        URL resource = getClass().getResource("/window.fxml");
        Objects.requireNonNull(resource);
        Parent root = FXMLLoader.load(resource);
        primaryStage = stage;
        primaryStage.setTitle(TITLE);
        primaryStage.setScene(new Scene(root, 660, 600));
        primaryStage.show();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
