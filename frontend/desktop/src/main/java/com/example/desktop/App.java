package com.example.desktop;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        Navigator.init(stage);
        Navigator.showList();

        stage.setTitle("Заявления на материальную помощь");
        stage.setMinWidth(1100);
        stage.setMinHeight(650);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}