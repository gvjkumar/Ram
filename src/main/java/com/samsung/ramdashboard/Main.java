package com.samsung.ramdashboard;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {

        // Loading FXML File
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("device-selection-view.fxml"));
        // Creating Scene
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("RAM Dashboard"); // For setting Windows Title
        stage.setScene(scene); // Setting the above fxml file as a scene
        stage.show(); // show it on the application
    }

    public static void main(String[] args) {
        launch(); //invoke the start() method
    }
}